package cn.huizhang43.pro.aitest.chat;

import cn.huizhang43.pro.aitest.agent.Agent;
import cn.huizhang43.pro.aitest.base.BusinessException;
import cn.huizhang43.pro.aitest.chat.dao.AiMessageRepository;
import cn.huizhang43.pro.aitest.chat.dao.dto.AiMessageInput;
import cn.huizhang43.pro.aitest.chat.dto.AiMessageWrapper;
import cn.huizhang43.pro.aitest.memory.AiMessageChatMemory;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("message")
public class ChatMessageController {
    
    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiMessageChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private AiMessageRepository messageRepository;


    @Autowired
    private StabilityAiImageModel stabilityAiImageModel;

    @Autowired
    private OpenAiImageModel openAiImageModel;

    @Value("classpath:judge.st")
    private Resource judgeTemplate;

    /**
     * 消息保存
     *
     * @param input 用户发送的消息/AI回复的消息
     */
    @PostMapping
    public void save(@RequestBody AiMessageInput input) {
        messageRepository.save(input.toEntity());
    }

    @SneakyThrows
    @PostMapping(value = "chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestPart String input, @RequestPart(required = false) MultipartFile file) {
        AiMessageWrapper aiMessageWrapper = objectMapper.readValue(input, AiMessageWrapper.class);
        String[] functionNames = new String[0];

        if (aiMessageWrapper.getParams().getEnableAgent()) {
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Agent.class);
            functionNames = new String[beansWithAnnotation.size()];
            functionNames = beansWithAnnotation.keySet().toArray(functionNames);
        }

        if (imageIntent(aiMessageWrapper.getMessage().getTextContent())) {
            ImageOptions imageOptions = ImageOptionsBuilder.builder()
                    .withN(1)
                    .withHeight(1792)
                    .withWidth(1024).build();
            ImagePrompt imagePrompt = new ImagePrompt(aiMessageWrapper.getMessage().getTextContent(), imageOptions);
            ImageResponse imageResponse = openAiImageModel.call(imagePrompt);
            AssistantMessage assistantMessage = new AssistantMessage("<img src='" + imageResponse.getResult().getOutput().getUrl() + "' alt='图片'/>");
            assistantMessage.getMetadata().put("finishReason", "STOP");
            ChatGenerationMetadata chatGenerationMetadata = ChatGenerationMetadata.from("STOP", "");
            Generation generation = new Generation(assistantMessage, chatGenerationMetadata);
            ChatResponse chatResponse = ChatResponse.builder().withMetadata("finishReason", "STOP").withGenerations(Lists.newArrayList(generation)).build();
            return Flux.just("").map(str -> {
                try {
                    return ServerSentEvent.builder(objectMapper.writeValueAsString(chatResponse))
                            .event("message")
                            .build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return ChatClient.create(openAiChatModel).prompt()
                // 代入用户文件上下文
                .system(promptSpec -> useFile(promptSpec, file))
                // 代入用户媒体内容
                .user(promptSpec -> toPrompt(promptSpec, aiMessageWrapper.getMessage()))
                // 调用智能体工具类
                .functions(functionNames)
                .advisors(advisorSpec -> {
                    // 代入会话历史
                    useChatHistory(advisorSpec, aiMessageWrapper.getMessage().getSessionId());
                    // 代入知识库
                    useVectorStore(advisorSpec, aiMessageWrapper.getParams().getEnableVectorStore());
                })
                // 流式返回
                .stream()
                // 构造SSE（ServerSendEvent）格式返回结果
                .chatResponse().map(chatResponse -> {
                    try {
                        return ServerSentEvent.builder(objectMapper.writeValueAsString(chatResponse))
                                .event("message")
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
    }

    /**
     * 生成图片意图识别
     *
     * @return
     */
    private boolean imageIntent(String query) {
        Prompt prompt = new PromptTemplate(judgeTemplate).create(Map.of("prompt", query));
        String content = dashScopeChatModel.call(prompt).getResult().getOutput().getContent();
        switch (content.toLowerCase()) {
            case "yes", "yes.":
                return true;
            default:
                return false;
        }
    }

    private void useVectorStore(ChatClient.AdvisorSpec advisorSpec, Boolean enableVectorStore) {
        if (!enableVectorStore) return;
        String promptWithContext = """
                下面是上下文信息
                ---------------------
                {question_answer_context}
                ---------------------
                给定的上下文和提供的历史信息，而不是事先的知识，回复用户的意见。如果答案不在上下文中，告诉用户你不能回答这个问题。
                """;
        advisorSpec.advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), promptWithContext));
    }

    private void useChatHistory(ChatClient.AdvisorSpec advisorSpec, String sessionId) {
        advisorSpec.advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10));
    }

    @SneakyThrows
    private void useFile(ChatClient.PromptSystemSpec systemSpec, MultipartFile file) {
        if (file == null) return;
        String content = new TikaDocumentReader(new InputStreamResource(file.getInputStream())).get().get(0).getContent();
        Message context = new PromptTemplate("""
                已下内容是额外的知识，在你回答问题时可以参考下面的内容
                ---------------------
                {context}
                ---------------------
                """).createMessage(Map.of("context", content));
        systemSpec.text(context.getContent());
    }

    private void toPrompt(ChatClient.PromptUserSpec userSpec, AiMessageInput messageInput) {
        // AiMessageInput转成Message
        Message message = AiMessageChatMemory.toSpringAiMessage(messageInput.toEntity());
        if (message instanceof UserMessage userMessage &&
                !CollectionUtils.isEmpty(userMessage.getMedia())) {
            // 用户发送的图片/语言
            Media[] medias = new Media[userMessage.getMedia().size()];
            userSpec.media(userMessage.getMedia().toArray(medias));
        }
        // 用户发送的文本
        userSpec.text(message.getContent());
    }
}

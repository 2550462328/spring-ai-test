package cn.huizhang43.pro.aitest;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.moonshot.MoonshotChatModel;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

/**
 * 大模型chat验证
 */
public class ChatMessageTests extends AitestApplicationTests {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private ZhiPuAiChatModel zhiPuAiChatModel;

    @Autowired
    private QianFanChatModel qianFanChatModel;

    @Autowired
    private MoonshotChatModel moonshotChatModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ObjectMapper objectMapper;

    private final ChatMemory chatMemory = new InMemoryChatMemory();

    /**
     * 同步聊天
     */
    @Test
    void testChatMessageWithCall() {
        ChatClient chatClient = ChatClient.create(dashScopeChatModel);
        Prompt prompt = new Prompt(Lists.newArrayList(new UserMessage("你是谁")));
        ChatOptions chatOptions = new DashScopeChatOptions();
        
        System.out.println(chatClient.prompt(prompt).call().content());
    }

    /**
     * 流式聊天
     */
    @Test
    void testChatMessageWithStream() {
        ChatClient chatClient = ChatClient.create(zhiPuAiChatModel);
        Flux<ServerSentEvent<String>> flux = chatClient.prompt().messages(new UserMessage("我想写一篇文章")).stream().chatResponse().map(chatResponse ->
        {
            try {
                return ServerSentEvent.builder(objectMapper.writeValueAsString(chatResponse)).event("test-event").build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        flux.subscribe(stringServerSentEvent -> {
            System.out.println("Received event：" + stringServerSentEvent); // Confirm subscription and data reception
        }, error -> {
            System.err.println("Error occurred: " + error); // Error handling
        }, () -> {
            System.out.println("Completed"); // Completion signal
        });

        StepVerifier.create(flux)
                .expectNextCount(1)
                .thenCancel()
                .verify();
        flux.blockLast();
    }

    /**
     * 基于历史会话的聊条
     */
    @Test
    void testChatMessageInMemory() {
        String sessionId = "1234";

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory, sessionId, 10);
        ChatClient chatClient = ChatClient.create(dashScopeChatModel);
        Flux<ServerSentEvent<String>> flux = chatClient.prompt()
                .messages(new UserMessage("是屁股"))
                .advisors(messageChatMemoryAdvisor)
                .stream().chatResponse().map(chatResponse -> {
                    try {
                        return ServerSentEvent.builder(objectMapper.writeValueAsString(chatResponse)).event("test-event").build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        flux.subscribe(stringServerSentEvent -> {
            System.out.println("Received event：" + stringServerSentEvent); // Confirm subscription and data reception
        }, error -> {
            System.err.println("Error occurred: " + error); // Error handling
        }, () -> {
            System.out.println("Completed"); // Completion signal
        });

        StepVerifier.create(flux)
                .expectNextCount(1)
                .thenCancel()
                .verify();
        flux.blockLast();
    }

    @Test
    void testChatWithRag() {
        String promptWithContext = """
                下面是上下文信息
                                ---------------------
                                {question_answer_context}
                                ---------------------
                                给定的上下文和提供的历史信息，而不是事先的知识，回复用户的意见。如果答案不在上下文中，告诉用户你不能回答这个问题。
                """;

        Flux<ServerSentEvent<String>> flux = ChatClient.create(dashScopeChatModel).prompt()
                .user("我想换工作，应该怎么做")
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), promptWithContext))
                .stream()
                .content()
                .map(chatResponse ->
                        ServerSentEvent.builder(chatResponse)
                                .event("test-event").build());

        flux.subscribe(stringServerSentEvent -> {
            System.out.println("Received event：" + stringServerSentEvent); // Confirm subscription and data reception
        }, error -> {
            System.err.println("Error occurred: " + error); // Error handling
        }, () -> {
            System.out.println("Completed"); // Completion signal
        });

        flux.blockLast();

    }

    /**
     * 结构化
     */
    @Test
    void testChatMessageWithStructure() {
        BeanOutputConverter<AuthorBook> beanConverter = new BeanOutputConverter<>(AuthorBook.class);
        String format = beanConverter.getFormat();
        String content = new PromptTemplate("""
                为作家{actor}生成一份经典作品集，作品数量不超过10个。
                {format}
                """).createMessage(Map.of("actor", "余华", "format", format)).getContent();
        Flux<ServerSentEvent<String>> flux = dashScopeChatModel.stream(content).map(chatResponse ->
        {
            try {
                return ServerSentEvent.builder(objectMapper.writeValueAsString(chatResponse)).event("test-event").build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        flux.subscribe(stringServerSentEvent -> {
            System.out.println("Received event：" + stringServerSentEvent); // Confirm subscription and data reception
        }, error -> {
            System.err.println("Error occurred: " + error); // Error handling
        }, () -> {
            System.out.println("Completed"); // Completion signal
        });

        StepVerifier.create(flux)
                .expectNextCount(1)
                .thenCancel()
                .verify();
        flux.blockLast();
    }

    @Data
    private static class AuthorBook {
        private String author;
        private List<String> books;
    }

}

package cn.huizhang43.pro.aitest;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.net.URL;

/**
 * 多模态测试
 */
public class ChatModerationTests extends AitestApplicationTests {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private DashScopeImageModel dashScopeImageModel;
    
    @Autowired
    private StabilityAiImageModel stabilityAiImageModel;
    
    @Autowired
    private OpenAiImageModel openAiImageModel;
    
    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    
    @Autowired
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;
    
    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("classpath:test.mp3")
    private Resource audioResource;
    
    /**
     * 流式聊天
     */
    @Test
    @SneakyThrows
    void testChatMessageWithImage() {
        Media media = new Media(MimeTypeUtils.IMAGE_PNG, new URL("https://oss-beijing-m8.openstorage.cn/yys-oss-dev/video/fe692100dcf8e94a1a907b1e06d15dcf.jpg"));
        Flux<ServerSentEvent<String>> flux = openAiChatModel.stream(new UserMessage("请帮我解读下这张照片", media)).map(chatResponse ->
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

    @Test
    void testTextToImageInBase64() {
        ImageOptions imageOptions = ImageOptionsBuilder.builder()
                .withN(1)
                .withHeight(512)
                .withWidth(512).build();
        ImagePrompt imagePrompt = new ImagePrompt("i want to produce a picture about cat, a little cat, she is playing with above bed", imageOptions);
        ImageResponse imageResponse = stabilityAiImageModel.call(imagePrompt);
        String b64Json = imageResponse.getResult().getOutput().getB64Json();
        String mimeType = "image/png";
        String dataUrl = "data:" + mimeType + ";base64," + b64Json;
        String imgUrl =  "<img src='" + dataUrl + "' alt='图片'/>";
        System.out.println(imgUrl);
    }

    @Test
    void testTextToImageInUrl() {
        ImageOptions imageOptions = ImageOptionsBuilder.builder()
                .withN(1)
                .withModel("dall-e-3")
                .withHeight(1792)
                .withWidth(1024).build();
        ImagePrompt imagePrompt = new ImagePrompt("i want to produce a picture about cat, a little cat, she is playing with above bed", imageOptions);
        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);
        String imgUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imgUrl);
    }

    /**
     * 转换语音到文本
     */
    @Test
    void testAudioToText() {
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f).build();
        AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(audioResource, transcriptionOptions);
        
        AudioTranscriptionResponse transcriptionResponse = openAiAudioTranscriptionModel.call(audioTranscriptionPrompt);
        System.out.println(transcriptionResponse.getResult().getOutput());
    }

    /**
     * 转换文本到语音
     */
    @Test
    void testTextToSpeech() {
        OpenAiAudioSpeechOptions aiAudioSpeechOptions = OpenAiAudioSpeechOptions.builder()
                .withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .withSpeed(1.0f)
                .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .withModel(OpenAiAudioApi.TtsModel.TTS_1_HD.value).build();

        SpeechPrompt speechPrompt = new SpeechPrompt("你好啊，帅哥", aiAudioSpeechOptions);
        SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);
        FileUtil.writeBytes(speechResponse.getResult().getOutput(),new File("D:\\test-output.mp3"));
    }
}

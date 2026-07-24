package cn.huizhang43.pro.aitest;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public class ChatFunctionTests extends AitestApplicationTests{

    @Autowired
    private DashScopeChatModel dashScopeChatModel;
    
    @Test
    void testWithFunction() {
        Flux<ServerSentEvent<ChatResponse>> flux = ChatClient.create(dashScopeChatModel).prompt()
                .user("D:\\develop\\workspace\\workspace-ai\\aitest\\src\\test\\resources\\test.txt 这篇文章讲的是什么，你觉得讲的有没有道理")
                .functions("documentAnalyzerFunction")
                .stream()
                .chatResponse()
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
}

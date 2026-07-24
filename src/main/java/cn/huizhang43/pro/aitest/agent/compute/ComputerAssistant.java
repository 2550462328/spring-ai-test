package cn.huizhang43.pro.aitest.agent.compute;

import cn.huizhang43.pro.aitest.agent.AbstractAgent;
import cn.huizhang43.pro.aitest.agent.Agent;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Agent
@Description("提供关于当前主机的cpu，文件，文件夹相关问题的有用回答")
@AllArgsConstructor
public class ComputerAssistant extends AbstractAgent implements Function<ComputerAssistant.Request, String> {
    private final DashScopeChatModel dashscopeChatModel;

    @Override
    public String apply(Request request) {
        return ChatClient.create(dashscopeChatModel)
                .prompt()
                .functions(getFunctions(CpuAnalyzer.class, DirectoryReader.class))
                .user(request.query())
                .call()
                .content();
    }

    public record Request(
            @JsonProperty(required = true) @JsonPropertyDescription(value = "用户原始的提问") String query) {
    }


}
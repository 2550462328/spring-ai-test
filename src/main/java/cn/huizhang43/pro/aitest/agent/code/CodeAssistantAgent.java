package cn.huizhang43.pro.aitest.agent.code;

import cn.huizhang43.pro.aitest.agent.AbstractAgent;
import cn.huizhang43.pro.aitest.agent.Agent;
import cn.huizhang43.pro.aitest.agent.code.analyze.AnalyzeFunction;
import cn.huizhang43.pro.aitest.agent.code.arthas.ArthasFunction;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Description("提供有关于Java代码的评审分析，在线诊断异常相关的回答")
@Agent
@AllArgsConstructor
public class CodeAssistantAgent extends AbstractAgent implements Function<CodeAssistantAgent.Request, String> {
    private final DashScopeChatModel dashScopeChatModel;

    @Override
    public String apply(Request request) {
        return ChatClient.create(dashScopeChatModel)
                .prompt()
                .user(request.query())
                .functions(getFunctions(AnalyzeFunction.class, ArthasFunction.class))
                .call()
                .content();
    }

    public record Request(
            @JsonProperty(required = true) @JsonPropertyDescription(value = "用户原始的提问") String query) {
    }
}

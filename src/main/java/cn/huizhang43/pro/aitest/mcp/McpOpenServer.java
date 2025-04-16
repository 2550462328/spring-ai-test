package cn.huizhang43.pro.aitest.mcp;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class McpOpenServer {

    @Autowired
    private WebMvcSseServerTransportProvider provider;

    McpSyncServer syncServer;
    
    private String schema = """
            {
                "type" : "object",
                "id" : "urn:jsonschema:Operation",
                "properties" : {
                "operation" : {
                    "type" : "string"
                },
                "a" : {
                    "type" : "number"
                },
                "b" : {
                    "type" : "number"
                }
                }
            }
            """;

    @PostConstruct
    public void start() {
        // 创建并配置MCP同步服务器
        syncServer = McpServer.sync(provider)
                .serverInfo("my-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                .build();
    }

    public void addCalculateTool() {
        McpServerFeatures.SyncToolSpecification syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("calculator", "Basic calculator", schema),
                (exchange, arguments) -> {
                    Number a = (Number) arguments.get("a");
                    Number b = (Number) arguments.get("b");
                    String operation = (String) arguments.get("operation");
                    List<McpSchema.Content> result = new ArrayList<>();
                    Number count = BigDecimal.ZERO;
                    if (StrUtil.equals("add", operation)) {
                        count = NumberUtil.add(a, b);
                    }
                    result.add(new McpSchema.TextContent(NumberUtil.toStr(count)));
                    return new McpSchema.CallToolResult(result, false);
                }
        );

        syncServer.addTool(syncToolSpecification);
    }

}

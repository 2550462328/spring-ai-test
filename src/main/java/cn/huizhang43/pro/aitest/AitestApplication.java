package cn.huizhang43.pro.aitest;

import cn.huizhang43.pro.aitest.mcp.McpOpenServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AitestApplication implements CommandLineRunner {

    @Autowired
    private McpOpenServer mcpOpenServer;

    public static void main(String[] args) {
        SpringApplication.run(AitestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mcpOpenServer.addCalculateTool();
    }
}

# Spring AI Test

一个基于 Spring Boot 与 Spring AI 的 AI 能力实验项目，用于验证多模型接入、会话记忆、文档问答、向量检索、知识图谱及代码分析等场景。

> 本项目以功能验证和学习实验为主，部分接口依赖本地数据、外部中间件或特定目录，请在使用前按实际环境调整配置。

## 功能概览

- 接入通义千问、智谱、文心一言、百度千帆、Moonshot、OpenAI 兼容接口及 Stability AI
- AI 会话与消息持久化
- 基于 Redis 的向量存储与聊天记忆
- 文档解析、切分、Embedding 与检索增强生成（RAG）
- 基于 Neo4j 的公司、财报、文本块和投资关系知识图谱
- Java 项目代码结构解析与代码图谱构建
- 流式 AI 响应与函数调用实验
- Sa-Token 登录认证及 S3 兼容对象存储上传

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 基础框架 | Java 17、Spring Boot 3.3.10-SNAPSHOT |
| AI 框架 | Spring AI 1.0.0-M2、Spring AI Alibaba |
| 数据访问 | Jimmer、MySQL |
| 缓存与向量存储 | Redis、Jedis |
| 图数据库 | Neo4j |
| 认证 | Sa-Token |
| 文档与代码处理 | Apache Tika、JavaParser、EasyExcel |
| 对象存储 | AWS S3 SDK、S3 兼容存储 |
| 构建工具 | Maven Wrapper |

## 项目结构

```text
src
├─ main
│  ├─ dto                         # Jimmer DTO 定义
│  ├─ java/cn/huizhang43/pro/aitest
│  │  ├─ agent                    # AI Agent、函数调用及代码分析
│  │  ├─ chat                     # 会话、消息和文档问答
│  │  ├─ config                   # Web、Redis、Jimmer 等配置
│  │  ├─ graph                    # Neo4j 知识图谱
│  │  ├─ memory                   # 聊天记忆
│  │  └─ system                   # 登录与对象存储
│  └─ resources
│     └─ application.yml          # 应用配置
└─ test                           # AI 模型及功能调用示例
```

## 环境要求

- JDK 17+
- MySQL 8+
- Redis
- Neo4j（使用图谱功能时需要）
- 可用的 AI 服务 API Key

## 配置

修改 `src/main/resources/application.yml`，至少需要根据所使用的功能配置以下内容：

- `spring.datasource`：MySQL 连接信息
- `spring.data.redis`：Redis 连接信息
- `spring.neo4j`：Neo4j 连接信息
- `spring.ai.*`：对应模型服务的 API Key、地址和模型名
- `oss`：S3 兼容对象存储配置
- `code-assistant`：待分析项目路径及 Arthas 服务信息

建议通过环境变量或本地配置文件注入密码和 API Key，不要把真实凭据提交到版本库。例如：

```yaml
spring:
  datasource:
    url: ${MYSQL_URL}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

当前配置包含：

```yaml
spring:
  main:
    web-application-type: none
```

该设置会关闭 Web 应用模式，适合运行部分测试或非 Web 实验。如需访问 Controller 提供的 HTTP 接口，请删除该配置，或将其调整为：

```yaml
spring:
  main:
    web-application-type: servlet
```

## 运行项目

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS：

```bash
./mvnw spring-boot:run
```

也可以先构建再运行：

```bash
./mvnw clean package
java -jar target/aitest-0.0.1-SNAPSHOT.jar
```

## 运行测试

```bash
./mvnw test
```

`src/test/java` 中包含聊天、函数调用、内容审核和文本向量化等示例。部分测试会真实调用外部 AI 服务，并可能产生费用；运行前请确认对应配置和账户额度。

## 主要接口模块

启用 Web 应用模式后，可使用以下模块：

| 路径前缀 | 用途 |
| --- | --- |
| `/session` | AI 会话管理 |
| `/message` | 消息与流式聊天 |
| `/document` | 文档解析和向量化 |
| `/graph` | 图谱查询与 RAG |
| `/company`、`/form`、`/chunk`、`/manager` | 财报知识图谱构建 |
| `/code/graph` | Java 代码图谱构建 |
| `/analyze` | 代码分析任务 |
| `/login` | 登录认证 |
| `/oss` | 文件上传 |

Jimmer OpenAPI 默认配置路径：

- OpenAPI 文档：`/openapi`
- OpenAPI UI：`/openapi-ui`
- TypeScript 客户端：`/ts.zip`

## 注意事项

- 部分图谱导入代码使用了本机绝对路径，运行前需要替换为本地数据目录。
- Neo4j 文本块关系构建使用 APOC，请确保数据库已安装并启用相应插件。
- 不同 AI 服务的模型名和接口能力可能不同，请只启用已配置的服务。
- 当前依赖包含里程碑或快照版本，首次构建需要访问 Maven Central、Spring Milestones 和 Spring Snapshots 仓库。

## License

当前仓库暂未声明开源许可证。

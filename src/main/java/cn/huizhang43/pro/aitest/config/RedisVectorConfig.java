package cn.huizhang43.pro.aitest.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import lombok.AllArgsConstructor;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
@EnableAutoConfiguration(exclude = {RedisVectorStoreAutoConfiguration.class})
@EnableConfigurationProperties({RedisVectorStoreProperties.class})
@AllArgsConstructor
public class RedisVectorConfig {

    /**
     * 创建RedisStack向量数据库
     *
     * @param dashscopeEmbeddingModel 嵌入模型
     * @param properties     redis-stack的配置信息
     * @return vectorStore 向量数据库
     */
    @Bean
    public VectorStore vectorStore(DashScopeEmbeddingModel dashscopeEmbeddingModel,
                                   RedisVectorStoreProperties properties,
                                   RedisConnectionDetails redisConnectionDetails) {
        RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder().withIndexName(properties.getIndex()).withPrefix(properties.getPrefix()).build();
        return new RedisVectorStore(config, dashscopeEmbeddingModel,
                new JedisPooled(redisConnectionDetails.getStandalone().getHost(),
                        redisConnectionDetails.getStandalone().getPort()
                        , redisConnectionDetails.getUsername(),
                        redisConnectionDetails.getPassword()),
                properties.isInitializeSchema());
    }
}

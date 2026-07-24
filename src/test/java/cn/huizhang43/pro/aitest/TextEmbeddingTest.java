package cn.huizhang43.pro.aitest;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * 文本向量化验证
 */
public class TextEmbeddingTest extends AitestApplicationTests {

    @Autowired
    private DashScopeEmbeddingModel dashScopeEmbeddingModel;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 文本向量生成
     */
    @Test
    void testEmbedding() {
        float[] embed = dashScopeEmbeddingModel.embed(new Document("你好，我的名字是张辉"));
        System.out.println("文本生成的向量值：" + Arrays.toString(embed));
    }

    /**
     * 抽取文本向量化
     */
    @Test
    void testDocumentEmbedding() throws IOException {
        ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        URL localResource = classLoader.getResource("test.txt");
        Resource resource = new InputStreamResource(localResource.openStream());
        List<Document> originDocuments = new TikaDocumentReader(resource).read();
        List<Document> transferDocuments = new TokenTextSplitter().apply(originDocuments);
        vectorStore.add(transferDocuments);
    }


    @Test
    void testVectorQuery() {
        SearchRequest searchRequest = SearchRequest.query("我的职业是什么");
        List<Document> documents = vectorStore.similaritySearch("我的职业是什么");
        documents.forEach(document -> {
            System.out.println(document.getContent());
        });
    }

}

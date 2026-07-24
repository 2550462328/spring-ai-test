package cn.huizhang43.pro.aitest.agent.document;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Description("文档解析函数")
@Service
public class DocumentAnalyzerFunction implements Function<DocumentAnalyzerFunction.Request, DocumentAnalyzerFunction.Response> {

    @Data
    public static class Request {
        @JsonProperty(required = true, value = "path")
        @JsonPropertyDescription("需要解析的文档路径")
        String path;
    }

    public record Response(String result) {
    }

    @Override
    public DocumentAnalyzerFunction.Response apply(Request request) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(request.path));

        return new Response(tikaDocumentReader.read().get(0).getContent());
    }
}

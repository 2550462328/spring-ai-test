package cn.huizhang43.pro.aitest.system.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {

    private String accessKey;

    private String secretKey;

    private String hostName;

    private int timeout;

    private String bucketName;

}

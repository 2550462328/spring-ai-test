package cn.huizhang43.pro.aitest.system;

import cn.huizhang43.pro.aitest.base.BusinessException;
import cn.huizhang43.pro.aitest.system.bean.OssProperties;
import cn.huizhang43.pro.aitest.util.MD5Util;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("oss")
@AllArgsConstructor
@EnableConfigurationProperties({OssProperties.class})
public class OssController {

    private final OssProperties ossProperties;

    @PostMapping("upload")
    public String upload(@RequestParam MultipartFile file) {
        return upload(file, "video/");
    }


    /**
     * 上传文件
     *
     * @param path 相对路径
     * @return 对象服务器地址
     */
    public String upload(MultipartFile multipartFile, String path) {
        try {
            String file = path;
            if (!file.endsWith("/")) {
                file += "/";
            }
            String fileFullName = multipartFile.getOriginalFilename();
            file += MD5Util.getMD5(fileFullName.substring(0, fileFullName.lastIndexOf("."))) + fileFullName.substring(fileFullName.lastIndexOf(".")).toLowerCase();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType(file));
            objectMetadata.setContentLength(multipartFile.getSize());
            AmazonS3 amazonS3 = amazonS3();
            amazonS3.putObject(ossProperties.getBucketName(), file, multipartFile.getInputStream(), objectMetadata);
            amazonS3.setObjectAcl(ossProperties.getBucketName(), file, CannedAccessControlList.PublicRead);
            String url = amazonS3.generatePresignedUrl(new GeneratePresignedUrlRequest(ossProperties.getBucketName(), file)).toString();
            return url.substring(0, url.lastIndexOf("?"));
        } catch (Exception e) {
            throw new BusinessException("上传文件异常");
        }
    }

    private AmazonS3 amazonS3() {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();
        amazonS3ClientBuilder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ossProperties.getAccessKey(), ossProperties.getSecretKey())));
        amazonS3ClientBuilder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ossProperties.getHostName(), null));
        amazonS3ClientBuilder.withPathStyleAccessEnabled(Boolean.TRUE);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(ossProperties.getTimeout());
        clientConfiguration.setMaxErrorRetry(3);
        clientConfiguration.setProtocol(Protocol.HTTPS);
        clientConfiguration.setSocketTimeout(ossProperties.getTimeout());
        clientConfiguration.withUseExpectContinue(false);
        clientConfiguration.withSignerOverride("S3SignerType");
        amazonS3ClientBuilder.setClientConfiguration(clientConfiguration);
        return amazonS3ClientBuilder.build();
    }

    private String contentType(String file) {
        if (file.endsWith(".bmp")) {
            return "image/bmp";
        } else if (file.endsWith(".gif")) {
            return "image/gif";
        } else if (file.endsWith(".jpeg") || file.endsWith(".jpg") || file.endsWith(".png")) {
            return "image/jpg";
        } else if (file.endsWith(".html")) {
            return "text/html";
        } else if (file.endsWith(".txt")) {
            return "text/plain";
        } else if (file.endsWith(".pptx") || file.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (file.endsWith(".docx") || file.endsWith(".doc")) {
            return "application/msword";
        } else if (file.endsWith(".pdf")) {
            return "application/pdf";
        } else if (file.endsWith(".mp3"))
            return "audio/mpeg";
        else {
            throw new BusinessException("No Such ContentType Cause Exception.");
        }
    }
}

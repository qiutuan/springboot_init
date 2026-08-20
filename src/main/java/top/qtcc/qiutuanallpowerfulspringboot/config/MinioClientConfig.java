package top.qtcc.qiutuanallpowerfulspringboot.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 客户端配置
 *
 * @author qiutuan
 * @date 2024/11/19
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioClientConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

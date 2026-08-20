package top.qtcc.qiutuanallpowerfulspringboot.manager.file;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.qtcc.qiutuanallpowerfulspringboot.config.MinioClientConfig;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;

import jakarta.annotation.Resource;
import java.io.InputStream;

/**
 * MinIO 对象存储操作
 *
 * @author qiutuan
 * @date 2024/11/16
 */
@Slf4j
@Component("MinioManager")
public class MinioManager implements FileManager {

    @Resource
    private MinioClientConfig minioClientConfig;

    @Resource
    private MinioClient minioClient;

    /**
     * 流式上传（失败抛出业务异常，避免假成功）
     */
    @Override
    public void putObject(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(minioClientConfig.getBucket())
                    .object(key)
                    .stream(inputStream, contentLength, -1)
                    .contentType(StringUtils.defaultIfBlank(contentType, "application/octet-stream"))
                    .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传对象失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    @Override
    public InputStream getObject(String key) {
        try {
            return minioClient.getObject(io.minio.GetObjectArgs.builder()
                    .bucket(minioClientConfig.getBucket())
                    .object(key)
                    .build());
        } catch (Exception e) {
            log.error("获取 MinIO 对象失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(minioClientConfig.getBucket())
                    .object(key)
                    .build());
        } catch (Exception e) {
            log.error("删除 MinIO 对象失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }

    /**
     * 生成 MinIO 访问地址（endpoint/bucket/key）
     */
    @Override
    public String getFileUrl(String key) {
        return StringUtils.stripEnd(minioClientConfig.getEndpoint(), "/") + "/"
                + minioClientConfig.getBucket() + key;
    }
}

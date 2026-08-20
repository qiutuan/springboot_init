package top.qtcc.qiutuanallpowerfulspringboot.manager.file;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.qtcc.qiutuanallpowerfulspringboot.config.CosClientConfig;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;

import jakarta.annotation.Resource;
import java.io.InputStream;

/**
 * 腾讯云 COS 对象存储操作
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Slf4j
@Component("CosManager")
public class CosManager implements FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 流式上传
     */
    @Override
    public void putObject(String key, InputStream inputStream, long contentLength, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        if (StringUtils.isNotBlank(contentType)) {
            metadata.setContentType(contentType);
        }
        cosClient.putObject(cosClientConfig.getBucket(), key, inputStream, metadata);
    }

    @Override
    public InputStream getObject(String key) {
        try {
            com.qcloud.cos.model.COSObject cosObject = cosClient.getObject(cosClientConfig.getBucket(), key);
            return cosObject.getObjectContent();
        } catch (Exception e) {
            log.error("获取 COS 对象失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
        } catch (Exception e) {
            log.error("删除 COS 对象失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }

    /**
     * 生成 COS 访问地址
     */
    @Override
    public String getFileUrl(String key) {
        return String.format("https://%s.cos.%s.myqcloud.com%s",
                cosClientConfig.getBucket(), cosClientConfig.getRegion(), key);
    }
}

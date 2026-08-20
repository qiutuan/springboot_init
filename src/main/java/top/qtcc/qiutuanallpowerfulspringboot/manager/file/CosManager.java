package top.qtcc.qiutuanallpowerfulspringboot.manager.file;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.qtcc.qiutuanallpowerfulspringboot.config.CosClientConfig;

import jakarta.annotation.Resource;
import java.io.InputStream;

/**
 * 腾讯云 COS 对象存储操作
 *
 * @author qiutuan
 * @date 2024/11/02
 */
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

    /**
     * 生成 COS 访问地址
     */
    @Override
    public String getFileUrl(String key) {
        return String.format("https://%s.cos.%s.myqcloud.com%s",
                cosClientConfig.getBucket(), cosClientConfig.getRegion(), key);
    }
}

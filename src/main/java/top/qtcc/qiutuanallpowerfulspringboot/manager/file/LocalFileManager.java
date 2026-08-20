package top.qtcc.qiutuanallpowerfulspringboot.manager.file;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;

import java.io.File;
import java.io.InputStream;

/**
 * 本地磁盘文件存储管理器
 *
 * @author qiutuan
 */
@Slf4j
@Component("LocalFileManager")
public class LocalFileManager implements FileManager {

    @Value("${file.local.upload-folder:./data/uploads}")
    private String uploadFolder;

    @Override
    public void putObject(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            File destFile = getFile(key);
            FileUtil.writeFromStream(inputStream, destFile);
        } catch (Exception e) {
            log.error("本地磁盘写入文件失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        }
    }

    @Override
    public InputStream getObject(String key) {
        try {
            File destFile = getFile(key);
            if (!destFile.exists()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
            }
            return FileUtil.getInputStream(destFile);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("本地磁盘读取文件失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            File destFile = getFile(key);
            if (destFile.exists()) {
                FileUtil.del(destFile);
            }
        } catch (Exception e) {
            log.error("本地磁盘删除文件失败, key={}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }

    @Override
    public String getFileUrl(String key) {
        // 本地服务访问的相对 URL，由 FileController 暴露 /download 进行读取
        return "/api/file/download?key=" + key;
    }

    private File getFile(String key) {
        // 清洗 key，防止目录穿越
        String safeKey = key.replace("\\", "/").replace("..", "");
        if (safeKey.startsWith("/")) {
            safeKey = safeKey.substring(1);
        }
        return new File(uploadFolder, safeKey);
    }
}

package top.qtcc.qiutuanallpowerfulspringboot.manager.file.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.qtcc.qiutuanallpowerfulspringboot.manager.file.FileManager;

/**
 * 文件管理器代理：按配置 file.manager 选择 MinioManager / CosManager
 *
 * @author qiutuan
 * @date 2024/11/18
 */
@Slf4j
@Configuration
public class FileManagerProxy {

    @Value("${file.manager:MinioManager}")
    private String fileManagerClassName;

    @Bean
    public FileManager fileManager(@Qualifier("CosManager") FileManager cosManager,
                                   @Qualifier("MinioManager") FileManager minioManager,
                                   @Qualifier("LocalFileManager") FileManager localFileManager) {
        if ("CosManager".equals(fileManagerClassName)) {
            return cosManager;
        }
        if ("MinioManager".equals(fileManagerClassName)) {
            return minioManager;
        }
        if ("LocalFileManager".equals(fileManagerClassName)) {
            return localFileManager;
        }
        log.warn("未知的 file.manager={}，默认使用 LocalFileManager", fileManagerClassName);
        return localFileManager;
    }
}

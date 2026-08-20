package top.qtcc.qiutuanallpowerfulspringboot.manager.file;

import java.io.InputStream;

/**
 * 对象存储管理器抽象
 *
 * @author qiutuan
 */
public interface FileManager {

    /**
     * 流式上传对象
     *
     * @param key           对象键
     * @param inputStream   文件流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     */
    void putObject(String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * 读取对象（流式读取）
     *
     * @param key 对象键
     * @return 文件流
     */
    InputStream getObject(String key);

    /**
     * 删除对象
     *
     * @param key 对象键
     */
    void deleteObject(String key);

    /**
     * 生成可访问地址
     *
     * @param key 对象键
     * @return 访问 URL
     */
    default String getFileUrl(String key) {
        return key;
    }
}

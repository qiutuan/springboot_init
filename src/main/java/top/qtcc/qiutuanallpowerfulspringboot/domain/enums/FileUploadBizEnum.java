package top.qtcc.qiutuanallpowerfulspringboot.domain.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Getter
public enum FileUploadBizEnum {
// 文件上传业务类型枚举

    USER_AVATAR("用户头像", "user_avatar", 1024 * 1024L, Arrays.asList("jpeg", "jpg", "svg", "png", "webp")),
    USER_FILE("用户文件", "user_file", 10 * 1024 * 1024L, Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")),
    USER_IMAGE("用户图片", "user_image", 5 * 1024 * 1024L, Arrays.asList("jpeg", "jpg", "svg", "png", "webp"));

    private final String text;

    private final String value;

    private final long maxSize;

    private final List<String> suffixWhitelist;

    FileUploadBizEnum(String text, String value, long maxSize, List<String> suffixWhitelist) {
        this.text = text;
        this.value = value;
        this.maxSize = maxSize;
        this.suffixWhitelist = suffixWhitelist;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value value
     * @return 枚举
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}

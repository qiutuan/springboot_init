package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.file;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     */
    @NotBlank(message = "业务类型不能为空")
    private String biz;

    @Serial
    private static final long serialVersionUID = 1L;
}
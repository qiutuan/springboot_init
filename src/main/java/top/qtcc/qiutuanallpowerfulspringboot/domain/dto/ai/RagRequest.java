package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * RAG 请求
 *
 * @author qiutuan
 */
@Data
public class RagRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题
     */
    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题过长")
    private String question;
}

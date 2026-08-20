package top.qtcc.qiutuanallpowerfulspringboot.common;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "id 不能为空")
    private Long id;
}

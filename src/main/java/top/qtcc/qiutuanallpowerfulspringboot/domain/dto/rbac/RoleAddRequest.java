package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加角色请求
 *
 * @author qiutuan
 */
@Data
@Schema(description = "添加角色请求参数")
public class RoleAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度在2-50之间")
    @Schema(description = "角色编码")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度在2-50之间")
    @Schema(description = "角色名称")
    private String roleName;
}

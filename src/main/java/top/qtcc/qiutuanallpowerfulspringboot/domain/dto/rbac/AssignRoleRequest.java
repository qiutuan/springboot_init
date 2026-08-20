package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配用户角色请求
 *
 * @author qiutuan
 */
@Data
@Schema(description = "给用户分配角色请求参数")
public class AssignRoleRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;
}

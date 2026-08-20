package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分配权限请求
 *
 * @author qiutuan
 */
@Data
@Schema(description = "给角色分配权限请求参数")
public class AssignPermissionsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}

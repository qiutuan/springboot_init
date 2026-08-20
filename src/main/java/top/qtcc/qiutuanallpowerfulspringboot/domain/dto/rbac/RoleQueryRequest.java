package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.qtcc.qiutuanallpowerfulspringboot.common.PageRequest;

import java.io.Serializable;

/**
 * 角色分页查询请求
 *
 * @author qiutuan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询请求参数")
public class RoleQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;
}

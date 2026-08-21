package top.qtcc.qiutuanallpowerfulspringboot.domain.vo.rbac;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色视图
 *
 * @author qiutuan
 */
@Data
public class SysRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

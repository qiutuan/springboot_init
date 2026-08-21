package top.qtcc.qiutuanallpowerfulspringboot.domain.vo.rbac;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限视图
 *
 * @author qiutuan
 */
@Data
public class SysPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 父权限 ID
     */
    private Long parentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

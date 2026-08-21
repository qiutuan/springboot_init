package top.qtcc.qiutuanallpowerfulspringboot.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色实体（RBAC）
 *
 * @author qiutuan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（Sa-Token 角色标识，如 admin/user/ban）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;
}

package top.qtcc.qiutuanallpowerfulspringboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRole;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysPermission;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac.RoleQueryRequest;

import java.util.List;

/**
 * 角色权限管理服务
 *
 * @author qiutuan
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 添加角色
     *
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @return 角色ID
     */
    Long addRole(String roleName, String roleCode);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 分页查询角色
     *
     * @param queryRequest 查询参数
     * @return 角色分页结果
     */
    Page<SysRole> listRolesByPage(RoleQueryRequest queryRequest);

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 给用户分配角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean assignRoleToUser(Long userId, Long roleId);

    /**
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<SysPermission> listAllPermissions();
}

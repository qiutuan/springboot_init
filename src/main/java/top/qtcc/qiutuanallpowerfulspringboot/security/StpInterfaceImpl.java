package top.qtcc.qiutuanallpowerfulspringboot.security;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysPermissionMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysRoleMapper;

import java.util.List;

/**
 * Sa-Token 权限数据源（RBAC）：
 * 从 sys_user_role / sys_role / sys_role_permission / sys_permission 表加载角色与权限。
 *
 * @author qiutuan
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return sysPermissionMapper.selectPermissionCodesByUserId(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysRoleMapper.selectRoleCodesByUserId(Long.valueOf(loginId.toString()));
    }
}

package top.qtcc.qiutuanallpowerfulspringboot.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac.RoleQueryRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysPermission;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRole;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRolePermission;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysUserRole;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysPermissionMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysRoleMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysRolePermissionMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysUserRoleMapper;
import top.qtcc.qiutuanallpowerfulspringboot.service.SysRoleService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 角色权限管理服务实现类
 *
 * @author qiutuan
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(String roleName, String roleCode) {
        Long count = sysRoleMapper.selectCount(new QueryWrapper<SysRole>().eq("role_code", roleCode));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色编码已存在");
        }
        SysRole sysRole = new SysRole();
        sysRole.setRoleName(roleName);
        sysRole.setRoleCode(roleCode);
        boolean saveResult = this.save(sysRole);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加角色失败");
        }
        return sysRole.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        // 清理级联关系
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("role_id", id));
        sysRolePermissionMapper.delete(new QueryWrapper<SysRolePermission>().eq("role_id", id));
        return this.removeById(id);
    }

    @Override
    public Page<SysRole> listRolesByPage(RoleQueryRequest queryRequest) {
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getRoleName()), "role_name", queryRequest.getRoleName());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getRoleCode()), "role_code", queryRequest.getRoleCode());
        return this.page(new Page<>(current, size), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        sysRolePermissionMapper.delete(new QueryWrapper<SysRolePermission>().eq("role_id", roleId));
        for (Long pId : permissionIds) {
            SysPermission permission = sysPermissionMapper.selectById(pId);
            if (permission == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "权限ID不存在: " + pId);
            }
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pId);
            sysRolePermissionMapper.insert(rp);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, Long roleId) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        int insert = sysUserRoleMapper.insert(ur);
        
        // 强制踢掉该用户以刷新缓存中的角色与权限
        try {
            StpUtil.logout(userId);
        } catch (Exception e) {
            log.warn("用户分配角色，踢下线异常，用户可能未在线 userId={}", userId, e);
        }
        return insert > 0;
    }

    @Override
    public List<SysPermission> listAllPermissions() {
        return sysPermissionMapper.selectList(new QueryWrapper<>());
    }
}

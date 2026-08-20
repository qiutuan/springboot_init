package top.qtcc.qiutuanallpowerfulspringboot.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.constant.UserConstant;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.rbac.*;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysPermission;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRole;
import top.qtcc.qiutuanallpowerfulspringboot.service.SysRoleService;

import java.util.List;

/**
 * 权限控制接口（RBAC）
 *
 * @author qiutuan
 */
@RestController
@RequestMapping("/rbac")
@Tag(name = "RBAC权限管理")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @PostMapping("/role/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "添加角色（管理员）")
    public BaseResponse<Long> addRole(@RequestBody @Valid RoleAddRequest request) {
        Long roleId = sysRoleService.addRole(request.getRoleName(), request.getRoleCode());
        return ResultUtils.success(roleId);
    }

    @PostMapping("/role/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除角色（管理员）")
    public BaseResponse<Boolean> deleteRole(@RequestParam Long id) {
        boolean result = sysRoleService.deleteRole(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/role/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页获取角色列表（管理员）")
    public BaseResponse<Page<SysRole>> listRolesByPage(@RequestBody @Valid RoleQueryRequest queryRequest) {
        Page<SysRole> rolePage = sysRoleService.listRolesByPage(queryRequest);
        return ResultUtils.success(rolePage);
    }

    @PostMapping("/role/permission/assign")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "给角色分配权限（管理员）")
    public BaseResponse<Boolean> assignPermissions(@RequestBody @Valid AssignPermissionsRequest request) {
        boolean result = sysRoleService.assignPermissionsToRole(request.getRoleId(), request.getPermissionIds());
        return ResultUtils.success(result);
    }

    @PostMapping("/role/user/assign")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "给用户分配角色（管理员）")
    public BaseResponse<Boolean> assignRoleToUser(@RequestBody @Valid AssignRoleRequest request) {
        boolean result = sysRoleService.assignRoleToUser(request.getUserId(), request.getRoleId());
        return ResultUtils.success(result);
    }

    @GetMapping("/permission/list")
    @SaCheckLogin
    @Operation(summary = "获取所有权限列表")
    public BaseResponse<List<SysPermission>> listAllPermissions() {
        List<SysPermission> list = sysRoleService.listAllPermissions();
        return ResultUtils.success(list);
    }
}

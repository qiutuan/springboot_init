package top.qtcc.qiutuanallpowerfulspringboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysPermission;

import java.util.List;

/**
 * 权限 Mapper（RBAC）
 *
 * @author qiutuan
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询用户拥有的权限编码
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.is_delete = 0")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}

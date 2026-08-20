package top.qtcc.qiutuanallpowerfulspringboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRole;

import java.util.List;

/**
 * 角色 Mapper（RBAC）
 *
 * @author qiutuan
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色编码
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Select("SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_delete = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}

package top.qtcc.qiutuanallpowerfulspringboot.constant;

/**
 * 用户常量
 *
 * @author qiutuan
 * @date 2024/11/02
 */
public interface UserConstant {

    // region 角色编码（与 sys_role.role_code 对应，Sa-Token 角色标识）

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    // region 权限编码（与 sys_permission.permission_code 对应）

    String PERMISSION_USER_LIST = "user:list";
    String PERMISSION_USER_ADD = "user:add";
    String PERMISSION_USER_UPDATE = "user:update";
    String PERMISSION_USER_DELETE = "user:delete";
    String PERMISSION_USER_GET = "user:get";

    // endregion

    /**
     * 默认头像
     */
    String DEFAULT_AVATAR = "http://img.qtcc.top/i/2024/11/02/48h8hm.png";

    /**
     * 新用户默认密码（仅管理员创建用户时使用，BCrypt 加密后入库）
     */
    String DEFAULT_PASSWORD = "12345678";
}

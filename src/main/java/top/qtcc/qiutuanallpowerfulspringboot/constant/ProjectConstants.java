package top.qtcc.qiutuanallpowerfulspringboot.constant;

/**
 * 项目通用常量管理
 *
 * @author qiutuan
 */
public interface ProjectConstants {

    /**
     * 排序：升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 排序：降序
     */
    String SORT_ORDER_DESC = "descend";

    /**
     * 最大分页条数
     */
    long MAX_PAGE_SIZE = 100;

    /**
     * Redis 缓存 Key 前缀：会话缓存
     */
    String CACHE_SESSION_PREFIX = "satoken:session:";

    /**
     * Redis 缓存 Key 前缀：限流控制
     */
    String CACHE_RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * Redis 缓存 Key 前缀：防重复提交
     */
    String CACHE_REPEAT_SUBMIT_PREFIX = "repeat_submit:";

    /**
     * 角色：管理员
     */
    String ROLE_ADMIN = "admin";

    /**
     * 角色：普通用户
     */
    String ROLE_USER = "user";

    /**
     * 角色：被封禁
     */
    String ROLE_BAN = "ban";

    /**
     * JWT/Token 头部标识
     */
    String TOKEN_HEADER = "satoken";
}

package top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已登录用户视图（脱敏）
 *
 * @author qiutuan
 **/
@Data
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * Sa-Token 登录令牌（同时写入响应头 satoken）
     */
    private String token;
}

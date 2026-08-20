package top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图（脱敏）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private LocalDateTime createTime;
}

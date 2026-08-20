package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "id 不能为空")
    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;
}

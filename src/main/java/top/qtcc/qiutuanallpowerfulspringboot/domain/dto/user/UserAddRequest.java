package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 32, message = "账号长度需在 4-32 之间")
    private String userAccount;

    @Size(max = 64, message = "昵称过长")
    private String userName;

    private String userAvatar;

    private String userProfile;

    /**
     * 用户角色编码: user / admin / ban
     */
    private String userRole;
}

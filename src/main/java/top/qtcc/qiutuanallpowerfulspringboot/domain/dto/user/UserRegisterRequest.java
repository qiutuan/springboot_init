package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 32, message = "账号长度需在 4-32 之间")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在 8-64 之间")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;
}

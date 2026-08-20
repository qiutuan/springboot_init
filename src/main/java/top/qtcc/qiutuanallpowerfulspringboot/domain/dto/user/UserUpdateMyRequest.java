package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 64, message = "昵称过长")
    private String userName;

    private String userAvatar;

    @Size(max = 512, message = "简介过长")
    private String userProfile;
}

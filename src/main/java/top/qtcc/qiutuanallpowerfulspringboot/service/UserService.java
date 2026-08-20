package top.qtcc.qiutuanallpowerfulspringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserAddRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserQueryRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.User;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.LoginUserVO;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.UserVO;

import java.util.List;

/**
 * 用户服务
 *
 * @author qiutuan
 * @date 2024/11/02
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录（Sa-Token 签发 token）
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 用户注销
     */
    boolean userLogout();

    /**
     * 获取当前登录用户
     */
    User getLoginUser();

    /**
     * 管理员创建用户（默认密码 BCrypt 加密，绑定角色）
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 获取脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏的用户信息
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}

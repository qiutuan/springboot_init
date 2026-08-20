package top.qtcc.qiutuanallpowerfulspringboot.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.qtcc.qiutuanallpowerfulspringboot.annotation.RateLimit;
import top.qtcc.qiutuanallpowerfulspringboot.annotation.RepeatSubmit;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.DeleteRequest;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.constant.CommonConstant;
import top.qtcc.qiutuanallpowerfulspringboot.constant.UserConstant;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserAddRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserLoginRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserQueryRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserRegisterRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserUpdateMyRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserUpdateRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.User;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.LoginUserVO;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.UserVO;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.exception.ThrowUtils;
import top.qtcc.qiutuanallpowerfulspringboot.service.UserService;

import java.util.List;

/**
 * 用户接口（Sa-Token 鉴权）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        long result = userService.userRegister(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 用户登录（返回登录用户信息，token 通过响应头 satoken 返回）
     */
    @RateLimit(count = 30)
    @RepeatSubmit(interval = 3000)
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody @Valid UserLoginRequest userLoginRequest) {
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        return ResultUtils.success(userService.userLogout());
    }

    /**
     * 获取当前登录用户
     */
    @SaCheckLogin
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户（管理员）
     */
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody @Valid UserAddRequest userAddRequest) {
        long result = userService.addUser(userAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除用户（管理员）
     */
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody @Valid DeleteRequest deleteRequest) {
        if (deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户（管理员）
     */
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（管理员）
     */
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取脱敏用户（登录可见）
     */
    @SaCheckLogin
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（管理员）
     */
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > CommonConstant.MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "分页大小超出限制");
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表（登录可见，限制爬虫）
     */
    @SaCheckLogin
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "分页大小超出限制");
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息
     */
    @SaCheckLogin
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody @Valid UserUpdateMyRequest userUpdateMyRequest) {
        User loginUser = userService.getLoginUser();
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}

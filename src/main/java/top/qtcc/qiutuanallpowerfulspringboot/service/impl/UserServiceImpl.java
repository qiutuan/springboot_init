package top.qtcc.qiutuanallpowerfulspringboot.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.qtcc.qiutuanallpowerfulspringboot.constant.CommonConstant;
import top.qtcc.qiutuanallpowerfulspringboot.constant.UserConstant;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserAddRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.user.UserQueryRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysRole;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.SysUserRole;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.User;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.UserRoleEnum;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.LoginUserVO;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.user.UserVO;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysRoleMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.SysUserRoleMapper;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.UserMapper;
import top.qtcc.qiutuanallpowerfulspringboot.service.UserService;
import top.qtcc.qiutuanallpowerfulspringboot.utils.SqlUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现（Sa-Token 认证 + BCrypt 密码 + RBAC 角色绑定）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. BCrypt 加密
        String encryptPassword = PASSWORD_ENCODER.encode(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserAvatar(UserConstant.DEFAULT_AVATAR);
        user.setUserRole(UserRoleEnum.USER.getValue());
        try {
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底，避免并发注册竞态
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        // 3. 绑定默认角色 user
        bindRole(user.getId(), UserConstant.DEFAULT_ROLE);
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        if (user == null || !PASSWORD_ENCODER.matches(userPassword, user.getUserPassword())) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁");
        }
        // Sa-Token 签发 token（响应头 satoken + 返回体 token 字段）
        StpUtil.login(user.getId());
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(StpUtil.getTokenValue());
        return loginUserVO;
    }

    @Override
    public boolean userLogout() {
        StpUtil.logout();
        return true;
    }

    @Override
    public User getLoginUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addUser(UserAddRequest userAddRequest) {
        String userAccount = userAddRequest.getUserAccount();
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 BCrypt 加密
        user.setUserPassword(PASSWORD_ENCODER.encode(UserConstant.DEFAULT_PASSWORD));
        if (StringUtils.isBlank(user.getUserName())) {
            user.setUserName(userAccount);
        }
        String roleCode = StringUtils.defaultIfBlank(userAddRequest.getUserRole(), UserConstant.DEFAULT_ROLE);
        user.setUserRole(roleCode);
        try {
            boolean result = this.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        bindRole(user.getId(), roleCode);
        return user.getId();
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        // 排序字段白名单校验（防 SQL 注入），非法或为空则不排序
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        }
        return queryWrapper;
    }

    /**
     * 绑定用户角色
     */
    private void bindRole(Long userId, String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new QueryWrapper<SysRole>().eq("role_code", roleCode));
        if (role == null) {
            log.warn("角色不存在，跳过绑定: {}", roleCode);
            return;
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);
    }
}
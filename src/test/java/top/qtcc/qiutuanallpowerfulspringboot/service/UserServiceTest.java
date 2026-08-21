package top.qtcc.qiutuanallpowerfulspringboot.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.User;

import jakarta.annotation.Resource;

/**
 * 用户服务单元测试示例
 *
 * @author qiutuan
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testGetUserById() {
        // 测试查询不存在的用户
        User user = userService.getById(-1L);
        Assertions.assertNull(user);
    }
}

package top.qtcc.qiutuanallpowerfulspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 上下文启动测试：使用真实 Web 环境（验证 Tomcat/WebSocket/Redis/数据源等完整装配）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QiutuanAllPowerfulSpringbootApplicationTests {

    @Test
    void contextLoads() {
    }
}

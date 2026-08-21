package top.qtcc.qiutuanallpowerfulspringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 全局跨域配置（白名单从配置读取，生产按域名收敛）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!"dev".equals(activeProfile) && allowedOrigins.contains("*")) {
            throw new IllegalStateException("CORS security violation: '*' is not allowed in production environment.");
        }
        registry.addMapping("/**")
                // 允许携带 Cookie/凭证
                .allowCredentials(true)
                // 白名单域名（与 allowCredentials 兼容）
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}

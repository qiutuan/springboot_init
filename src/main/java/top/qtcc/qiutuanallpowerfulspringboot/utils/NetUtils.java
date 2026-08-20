package top.qtcc.qiutuanallpowerfulspringboot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 网络工具类
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Slf4j
public class NetUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端 IP 地址。
     * 注意：x-forwarded-for 仅在信任反向代理（且代理已覆写该头）时才可信，否则可能被伪造。
     *
     * @param request 请求
     * @return IP 地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader("X-Real-IP");
        if (isUnknown(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，第一个 IP 为客户端真实 IP
        if (StringUtils.hasText(ip) && ip.indexOf(',') > 0) {
            ip = ip.substring(0, ip.indexOf(','));
        }
        return isUnknown(ip) ? "" : ip.trim();
    }

    private static boolean isUnknown(String ip) {
        return !StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip);
    }
}

package top.qtcc.qiutuanallpowerfulspringboot.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 防止 XSS 攻击：对请求参数值做 HTML 转义
 *
 * @author qiutuan
 * @date 2024/12/07
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return StringUtils.isNotBlank(value) ? StringEscapeUtils.escapeHtml4(value) : value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            String[] escapeValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                escapeValues[i] = StringUtils.isNotBlank(values[i]) ?
                        StringEscapeUtils.escapeHtml4(values[i]) : values[i];
            }
            return escapeValues;
        }
        return null;
    }
}

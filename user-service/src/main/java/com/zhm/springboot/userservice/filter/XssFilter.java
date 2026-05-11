package com.zhm.springboot.userservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS防护过滤器
 */
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }

    /**
     * XSS请求包装器
     */
    private static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return cleanXss(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;

            String[] cleanValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleanValues[i] = cleanXss(values[i]);
            }
            return cleanValues;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return cleanXss(value);
        }

        /**
         * 清理XSS攻击字符
         */
        private String cleanXss(String value) {
            if (value == null || value.isEmpty()) {
                return value;
            }

            // HTML实体编码
            value = value.replace("&", "&amp;");
            value = value.replace("<", "&lt;");
            value = value.replace(">", "&gt;");
            value = value.replace("\"", "&quot;");
            value = value.replace("'", "&#39;");

            // 过滤危险的JavaScript
            value = value.replaceAll("(?i)javascript:", "");
            value = value.replaceAll("(?i)vbscript:", "");
            value = value.replaceAll("(?i)onload", "");
            value = value.replaceAll("(?i)onerror", "");
            value = value.replaceAll("(?i)onclick", "");
            value = value.replaceAll("(?i)onmouseover", "");

            return value;
        }
    }
}
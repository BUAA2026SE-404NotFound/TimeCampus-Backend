package com.notfound.timecampuscommon.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 通用的 Bearer Token 解析和管理工具类
 */
public class TokenUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从 Authorization 请求头中解析 Bearer token
     *
     * @param request HTTP 请求对象
     * @return Bearer token，如果不存在或格式无效则返回 null
     */
    public static String resolveBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            return null;
        }
        if (!auth.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = auth.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }

    /**
     * 生成一个新的不带连字符的 UUID token
     *
     * @return token 字符串
     */
    public static String generateToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}


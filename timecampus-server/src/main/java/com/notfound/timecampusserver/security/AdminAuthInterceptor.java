package com.notfound.timecampusserver.security;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuscommon.web.TokenUtil;
import com.notfound.timecampuspojo.constant.AdminRoles;
import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampusserver.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.regex.Pattern;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_TOKEN_PREFIX = "admin:token:";
    public static final Duration ADMIN_TOKEN_TTL = Duration.ofHours(2);
    private static final Pattern ADMIN_MEDIA_FILE_PATH =
        Pattern.compile("^/api/v1/admin/media/\\d+/file$");

    private final StringRedisTemplate redisTemplate;
    private final AdminMapper adminMapper;

    public AdminAuthInterceptor(StringRedisTemplate redisTemplate, AdminMapper adminMapper) {
        this.redisTemplate = redisTemplate;
        this.adminMapper = adminMapper;
    }

    public String issueToken(Long adminId) {
        String token = TokenUtil.generateToken();
        redisTemplate.opsForValue().set(ADMIN_TOKEN_PREFIX + token, String.valueOf(adminId), ADMIN_TOKEN_TTL);
        return token;
    }

    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redisTemplate.delete(ADMIN_TOKEN_PREFIX + token);
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (isSignedAdminMediaFileRequest(request)) {
            // 这里是“带签名的管理员媒体文件直链”场景：请求本身已经通过 accessToken + 路径规则完成鉴权，
            // 不应该再沿用当前线程里可能残留的管理员登录上下文。
            // 之所以主动清空 AdminContext，不是要求用户重新登录，而是为了防止 Web 容器线程复用时，
            // 上一个请求留下的 adminId / role 被错误地带到这一次请求中，导致权限串用或误判。
            // 这样该直链请求就只依赖签名校验，不依赖管理员会话，也不会影响后续请求；
            // afterCompletion 里还会再清一次，作为额外兜底，确保线程上下文始终干净。
            AdminContext.clear();
            return true;
        }
        String token = TokenUtil.resolveBearerToken(request);
        if (token == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing admin token");
        }
        String adminIdStr = redisTemplate.opsForValue().get(ADMIN_TOKEN_PREFIX + token);
        if (adminIdStr == null || adminIdStr.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid or expired admin token");
        }
        Long adminId = parseAdminId(adminIdStr);
        AdminEntity admin = adminMapper.findById(adminId);
        if (admin == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "admin not found");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "admin disabled");
        }
        AdminContext.setAdminId(adminId);
        AdminContext.setAdminRole(admin.getRole());
        enforceRolePermission(request);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        AdminContext.clear();
    }

    private boolean isSignedAdminMediaFileRequest(@NonNull HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String accessToken = request.getParameter("accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return ADMIN_MEDIA_FILE_PATH.matcher(path).matches();
    }

    private Long parseAdminId(String adminIdStr) {
        try {
            return Long.parseLong(adminIdStr.trim());
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid admin token payload");
        }
    }

    private void enforceRolePermission(HttpServletRequest request) {
        String path = normalizePath(request);
        String role = AdminContext.getAdminRole();

        if ("/api/v1/admin/logout".equals(path)) {
            return;
        }

        if (path.startsWith("/api/v1/admin/accounts")) {
            requireAtLeastSuper(role);
            return;
        }

        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            requireAtLeastRead(role);
        } else {
            requireAtLeastAdmin(role);
        }
    }

    private String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    private void requireAtLeastRead(String role) {
        if (AdminRoles.READ.equals(role) || AdminRoles.ADMIN.equals(role) || AdminRoles.SUPER.equals(role)) {
            return;
        }
        throw new BizException(ResultCode.FORBIDDEN, "read permission required");
    }

    private void requireAtLeastAdmin(String role) {
        if (AdminRoles.ADMIN.equals(role) || AdminRoles.SUPER.equals(role)) {
            return;
        }
        throw new BizException(ResultCode.FORBIDDEN, "admin permission required");
    }

    private void requireAtLeastSuper(String role) {
        if (AdminRoles.SUPER.equals(role)) {
            return;
        }
        throw new BizException(ResultCode.FORBIDDEN, "super admin required");
    }
}

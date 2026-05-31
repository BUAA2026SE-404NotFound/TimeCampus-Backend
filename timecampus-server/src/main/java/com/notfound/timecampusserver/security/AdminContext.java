package com.notfound.timecampusserver.security;

public class AdminContext {

    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ADMIN_ROLE = new ThreadLocal<>();

    public static void setAdminId(Long adminId) {
        ADMIN_ID.set(adminId);
    }

    public static Long getAdminId() {
        return ADMIN_ID.get();
    }

    public static void setAdminRole(String role) {
        ADMIN_ROLE.set(role);
    }

    public static String getAdminRole() {
        return ADMIN_ROLE.get();
    }

    public static void clear() {
        ADMIN_ID.remove();
        ADMIN_ROLE.remove();
    }
}

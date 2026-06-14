package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.security.AdminContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TimeCampusMcpAdminScope {

    private final TimeCampusMcpProperties properties;

    public TimeCampusMcpAdminScope(TimeCampusMcpProperties properties) {
        this.properties = properties;
    }

    public <T> T call(Supplier<T> supplier) {
        boolean alreadyBound = AdminContext.getAdminId() != null;
        if (!alreadyBound) {
            AdminContext.setAdminId(properties.getAdminId());
            AdminContext.setAdminRole(properties.getAdminRole());
        }
        try {
            return supplier.get();
        } finally {
            if (!alreadyBound) {
                AdminContext.clear();
            }
        }
    }

    public void run(Runnable runnable) {
        call(() -> {
            runnable.run();
            return null;
        });
    }
}

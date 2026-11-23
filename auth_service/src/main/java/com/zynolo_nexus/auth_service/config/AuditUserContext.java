package com.zynolo_nexus.auth_service.config;

import java.util.function.Supplier;

public final class AuditUserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private AuditUserContext() {
    }

    public static void set(String username) {
        CURRENT_USER.set(username);
    }

    public static String get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    public static <T> T runWith(String username, Supplier<T> supplier) {
        if (username == null || username.isBlank()) {
            return supplier.get();
        }
        set(username);
        try {
            return supplier.get();
        } finally {
            clear();
        }
    }

    public static void runWith(String username, Runnable runnable) {
        if (username == null || username.isBlank()) {
            runnable.run();
            return;
        }
        set(username);
        try {
            runnable.run();
        } finally {
            clear();
        }
    }
}

package com.hmall.item.interceptor;

/**
 * @author 12141
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        THREAD_LOCAL.set(userId);
    }

    public static Long getCurrentUserId() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}

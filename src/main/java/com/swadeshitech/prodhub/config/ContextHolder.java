package com.swadeshitech.prodhub.config;

import java.util.HashMap;
import java.util.Map;

public class ContextHolder {

    private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<>();

    public static void setContext(String key, Object value) {
        Map<String, Object> context = contextHolder.get();
        if (context == null) {
            context = new HashMap<>();
            contextHolder.set(context);
        }
        context.put(key, value);
    }

    public static Object getContext(String key) {
        Map<String, Object> context = contextHolder.get();
        return context != null ? context.get(key) : null;
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

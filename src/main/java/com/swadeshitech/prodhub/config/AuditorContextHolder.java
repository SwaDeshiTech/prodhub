package com.swadeshitech.prodhub.config;

public class AuditorContextHolder {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void set(String auditor) { CURRENT.set(auditor); }
    public static String get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}

package com.mythicemporium.logging;

public class AuditContextHolder {
    private static final ThreadLocal<AuditContext> contextHolder = ThreadLocal.withInitial(AuditContext::new);

    public static AuditContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}

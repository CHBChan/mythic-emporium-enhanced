package com.mythicemporium.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuditContext ctx = AuditContextHolder.getContext();

        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        ctx.setIpAddress(ip);
        return true;
    }
}

package com.hms.hospital_management_system.config;

import com.hms.hospital_management_system.util.RoleAccessUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        if (RoleAccessUtil.isPublicPath(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (!RoleAccessUtil.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String role = RoleAccessUtil.getRole(session);
        if (!RoleAccessUtil.canAccess(role, uri)) {
            response.sendRedirect(request.getContextPath() + "/dashboard?accessDenied=true");
            return false;
        }

        return true;
    }
}

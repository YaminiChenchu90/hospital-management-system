package com.hms.hospital_management_system.util;

import com.hms.hospital_management_system.entity.enums.Role;
import jakarta.servlet.http.HttpSession;

public final class RoleAccessUtil {

    private RoleAccessUtil() {
    }

    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }

    public static String getRole(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userRole");
    }

    public static boolean isPublicPath(String uri) {
        return uri.equals("/")
                || uri.startsWith("/login")
                || uri.startsWith("/register")
                || uri.startsWith("/forgot-password")
                || uri.startsWith("/reset-password")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/error");
    }

    public static boolean canAccess(String role, String uri) {
        if (role == null) {
            return false;
        }

        if (Role.ROLE_ADMIN.name().equals(role)) {
            return true;
        }

        if (Role.ROLE_PATIENT.name().equals(role)) {
            return canPatientAccess(uri);
        }

        if (Role.ROLE_DOCTOR.name().equals(role)) {
            return canDoctorAccess(uri);
        }

        if (Role.ROLE_LAB_SPECIALIST.name().equals(role)) {
            return canLabSpecialistAccess(uri);
        }

        if (Role.ROLE_NURSE.name().equals(role) || Role.ROLE_RECEPTIONIST.name().equals(role)) {
            return canStaffAccess(uri);
        }

        return false;
    }

    private static boolean canPatientAccess(String uri) {
        if (startsWithAny(uri, "/patients", "/doctors", "/departments")) {
            return false;
        }
        if (uri.contains("/delete") || uri.contains("/status")) {
            return false;
        }
        if (uri.contains("/labreports/new") || uri.contains("/labreports/upload") || uri.contains("/send/")) {
            return false;
        }
        return startsWithAny(uri, "/dashboard", "/appointments", "/video-consultation", "/video-consultations", "/labreports", "/logout");
    }

    private static boolean canDoctorAccess(String uri) {
        if (startsWithAny(uri, "/patients", "/doctors", "/departments")) {
            return false;
        }
        if (uri.contains("/appointments/add") || uri.contains("/delete")) {
            return false;
        }
        return startsWithAny(uri, "/dashboard", "/appointments", "/video-consultation", "/video-consultations", "/labreports", "/logout");
    }

    private static boolean canStaffAccess(String uri) {
        return startsWithAny(uri,
                "/dashboard", "/patients", "/doctors", "/appointments",
                "/video-consultation", "/video-consultations", "/departments", "/labreports", "/logout");
    }

    private static boolean canLabSpecialistAccess(String uri) {
        return startsWithAny(uri, "/dashboard", "/labreports", "/logout");
    }

    private static boolean startsWithAny(String uri, String... prefixes) {
        for (String prefix : prefixes) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(String role) {
        return Role.ROLE_ADMIN.name().equals(role);
    }

    public static boolean isDoctor(String role) {
        return Role.ROLE_DOCTOR.name().equals(role);
    }

    public static boolean isPatient(String role) {
        return Role.ROLE_PATIENT.name().equals(role);
    }

    public static boolean isStaff(String role) {
        return Role.ROLE_NURSE.name().equals(role) || Role.ROLE_RECEPTIONIST.name().equals(role);
    }

    public static boolean isLabSpecialist(String role) {
        return Role.ROLE_LAB_SPECIALIST.name().equals(role);
    }

    public static boolean canManageLabReports(String role) {
        return isAdmin(role) || isDoctor(role) || isStaff(role) || isLabSpecialist(role);
    }
}

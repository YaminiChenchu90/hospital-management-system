package com.hms.hospital_management_system.util;

import com.hms.hospital_management_system.entity.User;

import jakarta.servlet.http.HttpSession;

public final class SessionUserHelper {

    private SessionUserHelper() {
    }

    public static void populateSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("labField", user.getLabField());
        session.setAttribute("specialistTitle", user.getSpecialistTitle());
    }
}

package com.hms.hospital_management_system.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.Role;

import jakarta.servlet.http.HttpSession;

class SessionUserHelperTest {

    @Test
    void populatesSessionAttributesFromUser() {
        HttpSession session = new MockHttpSession();
        User user = new User();
        user.setFullName("Alice Example");
        user.setEmail("alice@example.com");
        user.setRole(Role.ROLE_DOCTOR);
        user.setLabField("Radiology");
        user.setSpecialistTitle("Consultant");

        SessionUserHelper.populateSession(session, user);

        assertEquals(null, session.getAttribute("userId"));
        assertEquals("Alice Example", session.getAttribute("userName"));
        assertEquals(Role.ROLE_DOCTOR.name(), session.getAttribute("userRole"));
        assertEquals("alice@example.com", session.getAttribute("userEmail"));
        assertEquals("Radiology", session.getAttribute("labField"));
        assertEquals("Consultant", session.getAttribute("specialistTitle"));
    }
}

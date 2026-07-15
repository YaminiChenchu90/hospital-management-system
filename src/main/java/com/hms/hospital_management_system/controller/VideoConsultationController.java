package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class VideoConsultationController {

    private final AppointmentService appointmentService;

    public VideoConsultationController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/video-consultations")
    public String listVideoConsultations(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");
        model.addAttribute("appointments", appointmentService.getVideoConsultations(role, email));
        addSessionAttrs(model, session);
        return "video_consultations";
    }

    @GetMapping("/video-consultation/{appointmentId}")
    public String videoConsultation(@PathVariable Long appointmentId, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Appointment appointment = appointmentService.getAppointmentById(appointmentId).orElse(null);
        if (appointment == null) {
            return "redirect:/video-consultations";
        }

        if (!canJoin(session, appointment)
                || appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "redirect:/video-consultations";
        }

        String userName = (String) session.getAttribute("userName");
        String roomName = "MedicareHMS-Appt-" + appointmentId;
        String videoLink = "https://meet.jit.si/" + roomName
                + "#userInfo.displayName=\"" + URLEncoder.encode(userName, StandardCharsets.UTF_8) + "\"";

        model.addAttribute("appointment", appointment);
        model.addAttribute("videoLink", videoLink);
        addSessionAttrs(model, session);
        return "video_consultation";
    }

    private boolean canJoin(HttpSession session, Appointment appointment) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (Role.ROLE_ADMIN.name().equals(role)
                || Role.ROLE_RECEPTIONIST.name().equals(role)
                || Role.ROLE_NURSE.name().equals(role)) {
            return true;
        }
        if (Role.ROLE_DOCTOR.name().equals(role)) {
            return appointment.getDoctor().getEmail().equalsIgnoreCase(email);
        }
        if (Role.ROLE_PATIENT.name().equals(role)) {
            return appointment.getPatient().getEmail().equalsIgnoreCase(email);
        }
        return false;
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

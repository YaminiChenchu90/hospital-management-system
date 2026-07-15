package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.util.RoleAccessUtil;
import com.hms.hospital_management_system.service.AppointmentService;
import com.hms.hospital_management_system.service.DoctorService;
import com.hms.hospital_management_system.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentController(AppointmentService appointmentService, PatientService patientService,
                                 DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @GetMapping
    public String listAppointments(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");
        List<Appointment> appointments;

        if (Role.ROLE_PATIENT.name().equals(role)) {
            appointments = appointmentService.getAppointmentsByPatientEmail(email);
        } else if (Role.ROLE_DOCTOR.name().equals(role)) {
            appointments = appointmentService.getAppointmentsByDoctorEmail(email);
        } else {
            appointments = appointmentService.getAllAppointments();
        }

        model.addAttribute("appointments", appointments);
        String roleName = (String) session.getAttribute("userRole");
        model.addAttribute("canBook", RoleAccessUtil.isAdmin(roleName)
                || RoleAccessUtil.isStaff(roleName)
                || RoleAccessUtil.isPatient(roleName));
        model.addAttribute("canManageStatus", !RoleAccessUtil.isPatient(roleName));
        addSessionAttrs(model, session);
        return "appointments";
    }

    @GetMapping("/add")
    public String addAppointmentPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (Role.ROLE_PATIENT.name().equals(role)) {
            model.addAttribute("patient", patientService.getPatientByEmail(email).orElse(null));
        } else {
            model.addAttribute("patients", patientService.getAllPatients());
        }

        model.addAttribute("isPatient", RoleAccessUtil.isPatient(role));
        model.addAttribute("doctors", doctorService.getAvailableDoctors());
        addSessionAttrs(model, session);
        return "add-appointment";
    }

    @PostMapping("/add")
    public String addAppointment(@ModelAttribute Appointment appointment,
                                 @RequestParam(required = false) Long patientId,
                                 @RequestParam Long doctorId,
                                 HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (Role.ROLE_PATIENT.name().equals(role)) {
            patientService.getPatientByEmail(email).ifPresent(appointment::setPatient);
        } else if (patientId != null) {
            patientService.getPatientById(patientId).ifPresent(appointment::setPatient);
        }

        doctorService.getDoctorById(doctorId).ifPresent(appointment::setDoctor);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentService.saveAppointment(appointment);
        return "redirect:/appointments";
    }

    @GetMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam AppointmentStatus status, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        appointmentService.updateStatus(id, status);
        return "redirect:/appointments";
    }

    @GetMapping("/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments";
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

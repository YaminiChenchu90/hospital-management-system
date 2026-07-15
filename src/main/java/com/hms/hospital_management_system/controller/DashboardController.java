package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.LabReport;
import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.LabReportRepository;
import com.hms.hospital_management_system.service.*;
import com.hms.hospital_management_system.util.RoleAccessUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final LabReportRepository labReportRepository;

    public DashboardController(PatientService patientService, DoctorService doctorService,
                               AppointmentService appointmentService, DepartmentService departmentService,
                               UserService userService, LabReportRepository labReportRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.labReportRepository = labReportRepository;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Boolean accessDenied,
                            HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (Boolean.TRUE.equals(accessDenied)) {
            model.addAttribute("accessDenied", true);
        }

        if (Role.ROLE_PATIENT.name().equals(role)) {
            List<Appointment> myAppointments = appointmentService.getAppointmentsByPatientEmail(email);
            model.addAttribute("availableDoctors", doctorService.getAvailableDoctors());
            model.addAttribute("myAppointments", myAppointments.size());
            model.addAttribute("scheduledAppointments", myAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                    .count());
            model.addAttribute("confirmedAppointments", myAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                    .count());
        } else if (Role.ROLE_DOCTOR.name().equals(role)) {
            List<Appointment> myAppointments = appointmentService.getAppointmentsByDoctorEmail(email);
            model.addAttribute("doctorAppointments", myAppointments);
            model.addAttribute("myAppointments", myAppointments.size());
            model.addAttribute("scheduledAppointments", myAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                    .count());
            model.addAttribute("confirmedAppointments", myAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                    .count());
        } else {
            model.addAttribute("totalPatients", patientService.getAllPatients().size());
            model.addAttribute("totalDoctors", doctorService.getAllDoctors().size());
            model.addAttribute("totalDepartments", departmentService.getAllDepartments().size());
            model.addAttribute("totalAppointments", appointmentService.getAllAppointments().size());
            model.addAttribute("scheduledAppointments", appointmentService.countByStatus(AppointmentStatus.SCHEDULED));
            List<LabReport> labReports = labReportRepository.findAll();
            List<User> labSpecialists = userService.getAllUsers().stream()
                    .filter(user -> user.getRole() == Role.ROLE_LAB_SPECIALIST)
                    .toList();
            model.addAttribute("totalLabReports", labReports.size());
            model.addAttribute("labReportFieldStats", labReports.stream()
                    .collect(Collectors.groupingBy(this::getLabFieldLabel, Collectors.counting())));
            model.addAttribute("totalLabSpecialists", labSpecialists.size());
            model.addAttribute("labSpecialists", labSpecialists);
            model.addAttribute("labSpecialistFieldStats", labSpecialists.stream()
                    .collect(Collectors.groupingBy(this::getUserLabFieldLabel, Collectors.counting())));
        }

        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", role);
        model.addAttribute("isAdmin", RoleAccessUtil.isAdmin(role));
        model.addAttribute("isDoctor", RoleAccessUtil.isDoctor(role));
        model.addAttribute("isPatient", RoleAccessUtil.isPatient(role));
        model.addAttribute("isStaff", RoleAccessUtil.isStaff(role));
        model.addAttribute("isLabSpecialist", RoleAccessUtil.isLabSpecialist(role));
        return "dashboard";
    }

    private String getLabFieldLabel(LabReport report) {
        String labField = report.getLabField();
        return labField == null || labField.isBlank() ? "Unassigned" : labField;
    }

    private String getUserLabFieldLabel(User user) {
        String labField = user.getLabField();
        return labField == null || labField.isBlank() ? "Unassigned" : labField;
    }
}

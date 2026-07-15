package com.hms.hospital_management_system.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hms.hospital_management_system.entity.LabReport;
import com.hms.hospital_management_system.entity.Patient;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.LabReportRepository;
import com.hms.hospital_management_system.service.EmailService;
import com.hms.hospital_management_system.service.LabReportService;
import com.hms.hospital_management_system.service.PatientService;
import com.hms.hospital_management_system.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class LabReportController {

    private final LabReportRepository labReportRepository;
    private final EmailService emailService;
    private final LabReportService labReportService;
    private final PatientService patientService;

    public LabReportController(LabReportRepository labReportRepository, EmailService emailService,
                               LabReportService labReportService, PatientService patientService) {
        this.labReportRepository = labReportRepository;
        this.emailService = emailService;
        this.labReportService = labReportService;
        this.patientService = patientService;
    }

    @GetMapping("/labreports")
    public String showLabReports(@RequestParam(required = false) String field, Model model, HttpSession session) {
        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        String role = RoleAccessUtil.getRole(session);
        String email = (String) session.getAttribute("userEmail");
        List<LabReport> reports;

        if (Role.ROLE_PATIENT.name().equals(role)) {
            reports = labReportRepository.findByPatientEmailIgnoreCase(email);
        } else {
            reports = labReportRepository.findAll();
            if (field != null && !field.isBlank()) {
                reports = reports.stream()
                        .filter(report -> matchesFieldFilter(report, field))
                        .toList();
            }
        }

        addSessionAttrs(model, session);
        model.addAttribute("labReports", reports);
        model.addAttribute("canUpload", RoleAccessUtil.canManageLabReports(role));
        model.addAttribute("canSendEmail", RoleAccessUtil.canManageLabReports(role));
        model.addAttribute("canDeleteUnassigned", RoleAccessUtil.canManageLabReports(role));
        model.addAttribute("selectedField", field);
        return "labreports";
    }

    @GetMapping("/labreports/new")
    public String showUploadForm(HttpSession session, Model model) {
        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!RoleAccessUtil.canManageLabReports(RoleAccessUtil.getRole(session))) {
            return "redirect:/dashboard?accessDenied=true";
        }
        addSessionAttrs(model, session);
        String role = RoleAccessUtil.getRole(session);
        List<Patient> patients = patientService.getAllPatients();
        if (RoleAccessUtil.isLabSpecialist(role)) {
            String accountLabField = (String) session.getAttribute("labField");
            if (accountLabField != null && !accountLabField.isBlank()) {
                patients = patients.stream()
                        .filter(patient -> patient.getMedicalHistory() != null && patient.getMedicalHistory().toLowerCase().contains(accountLabField.toLowerCase()))
                        .toList();
            }
        }
        model.addAttribute("patients", patients);
        model.addAttribute("accountLabField", session.getAttribute("labField"));
        return "upload_labreport";
    }

    @PostMapping("/labreports/upload")
    public String uploadLabReport(
            @RequestParam String patientEmail,
            @RequestParam String labField,
            @RequestParam String testType,
            @RequestParam String result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!RoleAccessUtil.canManageLabReports(RoleAccessUtil.getRole(session))) {
            return "redirect:/dashboard?accessDenied=true";
        }

        Patient patient = patientService.getPatientByEmail(patientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Choose a valid patient"));
        String role = RoleAccessUtil.getRole(session);
        String accountLabField = (String) session.getAttribute("labField");
        String accountSpecialistTitle = (String) session.getAttribute("specialistTitle");

        LabReport report = new LabReport();
        report.setPatientName(patient.getFullName());
        report.setPatientEmail(patient.getEmail());
        report.setLabField(RoleAccessUtil.isLabSpecialist(role) && accountLabField != null && !accountLabField.isBlank()
                ? accountLabField
                : labField);
        report.setSpecialistName((String) session.getAttribute("userName"));
        report.setSpecialistEmail((String) session.getAttribute("userEmail"));
        if (RoleAccessUtil.isLabSpecialist(role) && accountSpecialistTitle != null && !accountSpecialistTitle.isBlank()) {
            report.setSpecialistName(accountSpecialistTitle + " - " + session.getAttribute("userName"));
        }
        report.setTestType(testType);
        report.setResult(result);

        try {
            labReportService.uploadLabReport(report);
            redirectAttributes.addFlashAttribute("message",
                    "Report uploaded and sent to " + patient.getFullName());
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalStateException | MailException e) {
            labReportService.saveLabReport(report);
            redirectAttributes.addFlashAttribute("message",
                    "Report uploaded, but email was not sent. Configure Gmail username and App Password to send reports directly.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/labreports";
    }

    @GetMapping("/labreports/view/{id}")
    public String viewLabReport(@PathVariable Long id, Model model, HttpSession session) {
        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        LabReport report = labReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report Id:" + id));

        if (!canViewReport(session, report)) {
            return "redirect:/labreports";
        }

        addSessionAttrs(model, session);
        model.addAttribute("report", report);
        return "view_labreport";
    }

    @GetMapping("/labreports/download/{id}")
    public ResponseEntity<Resource> downloadLabReport(@PathVariable Long id, HttpSession session) {
        LabReport report = labReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report Id:" + id));

        if (!canViewReport(session, report)) {
            return ResponseEntity.status(403).build();
        }

        String content = "Patient: " + report.getPatientName()
                + "\nField: " + nullToDefault(report.getLabField(), "Unassigned")
                + "\nSpecialist: " + nullToDefault(report.getSpecialistName(), "Hospital Lab")
                + "\nTest: " + report.getTestType()
                + "\nResult: " + report.getResult();

        ByteArrayResource resource = new ByteArrayResource(content.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=labreport_" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    @PostMapping("/labreports/send/{id}")
    public String sendLabReport(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        LabReport report = labReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report Id:" + id));

        if (!RoleAccessUtil.canManageLabReports(RoleAccessUtil.getRole(session))) {
            return "redirect:/dashboard?accessDenied=true";
        }

        try {
            emailService.sendReport(report);
            redirectAttributes.addFlashAttribute("message",
                    "Report sent successfully to " + report.getPatientEmail());
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalStateException | MailException e) {
            redirectAttributes.addFlashAttribute("message",
                    "Could not send email. Set spring.mail.username to your Gmail address ending in @gmail.com and use an App Password.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/labreports";
    }

    @PostMapping("/labreports/delete/{id}")
    public String deleteUnassignedLabReport(@PathVariable Long id, RedirectAttributes redirectAttributes,
                                            HttpSession session) {
        if (!RoleAccessUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!RoleAccessUtil.canManageLabReports(RoleAccessUtil.getRole(session))) {
            return "redirect:/dashboard?accessDenied=true";
        }

        LabReport report = labReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report Id:" + id));
        if (!isUnassignedField(report)) {
            redirectAttributes.addFlashAttribute("message",
                    "Only lab reports without an assigned field can be deleted from this action.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/labreports";
        }

        labReportRepository.delete(report);
        redirectAttributes.addFlashAttribute("message", "Unassigned lab report deleted.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/labreports?field=Unassigned";
    }

    private boolean canViewReport(HttpSession session, LabReport report) {
        String role = RoleAccessUtil.getRole(session);
        if (RoleAccessUtil.canManageLabReports(role)) {
            return true;
        }
        if (Role.ROLE_PATIENT.name().equals(role)) {
            String email = (String) session.getAttribute("userEmail");
            return report.getPatientEmail() != null
                    && report.getPatientEmail().equalsIgnoreCase(email);
        }
        return false;
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private boolean matchesFieldFilter(LabReport report, String field) {
        if ("Unassigned".equalsIgnoreCase(field)) {
            return isUnassignedField(report);
        }
        return report.getLabField() != null && report.getLabField().equalsIgnoreCase(field);
    }

    private boolean isUnassignedField(LabReport report) {
        return report.getLabField() == null || report.getLabField().isBlank();
    }
}

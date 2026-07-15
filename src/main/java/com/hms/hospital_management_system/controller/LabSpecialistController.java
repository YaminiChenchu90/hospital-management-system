package com.hms.hospital_management_system.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hms.hospital_management_system.entity.LabReport;
import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.LabReportRepository;
import com.hms.hospital_management_system.service.UserService;
import com.hms.hospital_management_system.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class LabSpecialistController {

    private final UserService userService;
    private final LabReportRepository labReportRepository;

    public LabSpecialistController(UserService userService, LabReportRepository labReportRepository) {
        this.userService = userService;
        this.labReportRepository = labReportRepository;
    }

    @GetMapping("/lab-specialists")
    public String listLabSpecialists(@RequestParam(required = false) String field,
                                     HttpSession session, Model model) {
        if (!canManageLabSpecialists(session)) {
            return "redirect:/dashboard?accessDenied=true";
        }

        List<User> specialists = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == Role.ROLE_LAB_SPECIALIST)
                .filter(user -> matchesFieldFilter(user, field))
                .toList();

        Map<String, Long> fieldCounts = specialists.stream()
                .collect(Collectors.groupingBy(user -> normalizeLabField(user.getLabField()), Collectors.counting()));

        addSessionAttrs(model, session);
        model.addAttribute("labSpecialists", specialists);
        model.addAttribute("selectedField", field);
        model.addAttribute("labSpecialistFieldStats", fieldCounts);
        return "lab-specialists";
    }

    @GetMapping("/lab-specialists/view/{id}")
    public String viewLabSpecialist(@PathVariable Long id, HttpSession session, Model model) {
        if (!canManageLabSpecialists(session)) {
            return "redirect:/dashboard?accessDenied=true";
        }

        User specialist = getLabSpecialist(id);
        List<LabReport> reports = labReportRepository.findBySpecialistEmailIgnoreCase(specialist.getEmail());
        Set<String> uniquePatients = reports.stream()
                .map(LabReport::getPatientEmail)
                .filter(email -> email != null && !email.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        addSessionAttrs(model, session);
        model.addAttribute("specialist", specialist);
        model.addAttribute("labReportTotal", reports.size());
        model.addAttribute("patientTotal", uniquePatients.size());
        model.addAttribute("reports", reports);
        return "view_lab_specialist";
    }

    @PostMapping("/lab-specialists/delete/{id}")
    public String deleteUnassignedLabSpecialist(@PathVariable Long id, HttpSession session,
                                                RedirectAttributes redirectAttributes) {
        if (!canManageLabSpecialists(session)) {
            return "redirect:/dashboard?accessDenied=true";
        }

        User specialist = getLabSpecialist(id);
        if (!isUnassignedField(specialist)) {
            redirectAttributes.addFlashAttribute("message",
                    "Only lab specialists without an assigned field can be deleted from this action.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/lab-specialists";
        }

        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("message", "Unassigned lab specialist deleted.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/lab-specialists?field=Unassigned";
    }

    private User getLabSpecialist(Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lab specialist Id:" + id));
        if (user.getRole() != Role.ROLE_LAB_SPECIALIST) {
            throw new IllegalArgumentException("User is not a lab specialist");
        }
        return user;
    }

    private boolean canManageLabSpecialists(HttpSession session) {
        return RoleAccessUtil.isLoggedIn(session) && RoleAccessUtil.isAdmin(RoleAccessUtil.getRole(session));
    }

    private boolean matchesFieldFilter(User user, String field) {
        if (field == null || field.isBlank()) {
            return true;
        }
        if ("Unassigned".equalsIgnoreCase(field)) {
            return isUnassignedField(user);
        }
        return normalizeLabField(user.getLabField()).equalsIgnoreCase(field);
    }

    private boolean isUnassignedField(User user) {
        return user.getLabField() == null || user.getLabField().isBlank();
    }

    private String normalizeLabField(String labField) {
        return labField == null || labField.isBlank() ? "Unassigned" : labField;
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

package com.hms.hospital_management_system.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hms.hospital_management_system.entity.Department;
import com.hms.hospital_management_system.entity.Doctor;
import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.service.DepartmentService;
import com.hms.hospital_management_system.service.DoctorService;
import com.hms.hospital_management_system.service.PatientService;
import com.hms.hospital_management_system.service.UserService;
import com.hms.hospital_management_system.util.SessionUserHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService, PatientService patientService, DoctorService doctorService,
                          DepartmentService departmentService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.departmentService = departmentService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) Boolean registered, Model model) {
        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("success", "Account created. Sign in with your email and password.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, Model model) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            userService.getUserByEmail(email).ifPresent(user -> SessionUserHelper.populateSession(session, user));
            return "redirect:/dashboard";
        } catch (AuthenticationException ex) {
            logger.warn("Failed login attempt for {}: {}", email, ex.getMessage());
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        addRegistrationOptions(model);
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, HttpSession session, Model model) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isEmpty()) {
            model.addAttribute("error", "No account found with that email address");
            return "forgot-password";
        }

        session.setAttribute("resetEmail", user.get().getEmail());
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session) {
        if (session.getAttribute("resetEmail") == null) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "reset-password";
        }

        try {
            userService.resetPassword(email, password);
            session.removeAttribute("resetEmail");
            model.addAttribute("success", "Password reset successfully. Sign in with your new password.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Password reset failed: " + e.getMessage());
            return "reset-password";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String specialization,
                           @RequestParam(required = false) String labField,
                           @RequestParam(required = false) String specialistTitle,
                           @RequestParam(required = false) Long departmentId,
                           Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                addRegistrationOptions(model);
                return "register";
            }

            Role selectedRole = parseRegistrationRole(role);
            Department selectedDepartment = null;
            if (selectedRole == Role.ROLE_DOCTOR) {
                selectedDepartment = getRegistrationDepartment(departmentId);
            }

            User user = userService.register(fullName, email, password, phone, selectedRole, labField, specialistTitle);
            if (selectedRole == Role.ROLE_PATIENT) {
                patientService.createFromRegistration(user);
            } else if (selectedRole == Role.ROLE_DOCTOR) {
                createDoctorFromRegistration(user, specialization, selectedDepartment);
            }

            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            addRegistrationOptions(model);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private Role parseRegistrationRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.ROLE_PATIENT;
        }

        Role selectedRole = Role.valueOf(role);
        if (selectedRole == Role.ROLE_ADMIN
                || selectedRole == Role.ROLE_DOCTOR
                || selectedRole == Role.ROLE_LAB_SPECIALIST
                || selectedRole == Role.ROLE_PATIENT) {
            return selectedRole;
        }

        throw new IllegalArgumentException("Choose Admin, Doctor, Lab Specialist, or Patient");
    }

    private void createDoctorFromRegistration(User user, String specialization, Department department) {
        Doctor doctor = new Doctor();
        doctor.setFullName(user.getFullName());
        doctor.setEmail(user.getEmail());
        doctor.setPhone(user.getPhone());
        doctor.setSpecialization(specialization != null && !specialization.isBlank()
                ? specialization.trim()
                : "General Physician");
        doctor.setDepartment(department);
        doctor.setConsultationHours("Mon-Fri, 9:00 AM - 5:00 PM");
        doctor.setAvailable(true);
        doctorService.saveDoctor(doctor);
    }

    private void addRegistrationOptions(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
    }

    private Department getRegistrationDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Department is required for doctor accounts");
        }

        return departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Choose a valid department"));
    }
}

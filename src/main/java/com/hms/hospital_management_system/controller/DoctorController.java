package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Doctor;
import com.hms.hospital_management_system.service.DepartmentService;
import com.hms.hospital_management_system.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;
    private final DepartmentService departmentService;

    public DoctorController(DoctorService doctorService, DepartmentService departmentService) {
        this.doctorService = doctorService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listDoctors(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("doctors", doctorService.getAllDoctors());
        addSessionAttrs(model, session);
        return "doctors";
    }

    @GetMapping("/search")
    public String searchDoctors(@RequestParam String query, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("doctors", doctorService.searchDoctors(query));
        model.addAttribute("query", query);
        addSessionAttrs(model, session);
        return "doctors";
    }

    @GetMapping("/add")
    public String addDoctorPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("departments", departmentService.getAllDepartments());
        addSessionAttrs(model, session);
        return "add-doctor";
    }

    @PostMapping("/add")
    public String addDoctor(@ModelAttribute Doctor doctor, @RequestParam Long departmentId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        departmentService.getDepartmentById(departmentId).ifPresent(doctor::setDepartment);
        doctorService.saveDoctor(doctor);
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editDoctorPage(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        Doctor doctor = doctorService.getDoctorById(id).orElse(null);
        if (doctor == null) return "redirect:/doctors";
        model.addAttribute("doctor", doctor);
        model.addAttribute("departments", departmentService.getAllDepartments());
        addSessionAttrs(model, session);
        return "edit-doctor";
    }

    @PostMapping("/{id}/edit")
    public String editDoctor(@PathVariable Long id, @ModelAttribute Doctor doctor,
                             @RequestParam Long departmentId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        Doctor existing = doctorService.getDoctorById(id).orElse(null);
        if (existing != null) {
            existing.setFullName(doctor.getFullName());
            existing.setSpecialization(doctor.getSpecialization());
            existing.setEmail(doctor.getEmail());
            existing.setPhone(doctor.getPhone());
            existing.setConsultationHours(doctor.getConsultationHours());
            existing.setAvailable(doctor.isAvailable());
            departmentService.getDepartmentById(departmentId).ifPresent(existing::setDepartment);
            doctorService.saveDoctor(existing);
        }
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        doctorService.deleteDoctor(id);
        return "redirect:/doctors";
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

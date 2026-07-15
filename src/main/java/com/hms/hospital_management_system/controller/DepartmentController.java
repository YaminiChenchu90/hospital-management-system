package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Department;
import com.hms.hospital_management_system.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listDepartments(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("departments", departmentService.getAllDepartments());
        addSessionAttrs(model, session);
        return "departments";
    }

    @GetMapping("/add")
    public String addDepartmentPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        addSessionAttrs(model, session);
        return "add-department";
    }

    @PostMapping("/add")
    public String addDepartment(@ModelAttribute Department department, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        departmentService.saveDepartment(department);
        return "redirect:/departments";
    }

    @GetMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        departmentService.deleteDepartment(id);
        return "redirect:/departments";
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

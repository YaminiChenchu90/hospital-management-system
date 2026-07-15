package com.hms.hospital_management_system.controller;

import com.hms.hospital_management_system.entity.Patient;
import com.hms.hospital_management_system.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;
    private final Logger logger = LoggerFactory.getLogger(PatientController.class);

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public String listPatients(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("patients", patientService.getAllPatients());
        addSessionAttrs(model, session);
        return "patients";
    }

    @GetMapping("/search")
    public String searchPatients(@RequestParam String query, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("patients", patientService.searchPatients(query));
        model.addAttribute("query", query);
        addSessionAttrs(model, session);
        return "patients";
    }

    @GetMapping("/{id}")
    public String viewPatient(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        Patient patient = patientService.getPatientById(id).orElse(null);
        if (patient == null) return "redirect:/patients";
        model.addAttribute("patient", patient);
        addSessionAttrs(model, session);
        return "patient-detail";
    }

    @GetMapping("/add")
    public String addPatientPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        addSessionAttrs(model, session);
        return "add-patient";
    }

    @PostMapping("/add")
    public String addPatient(@ModelAttribute Patient patient, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        patientService.savePatient(patient);
        logger.info("Added patient: {}", patient.getEmail());
        return "redirect:/patients";
    }

    @GetMapping("/{id}/edit")
    public String editPatientPage(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        Patient patient = patientService.getPatientById(id).orElse(null);
        if (patient == null) return "redirect:/patients";
        model.addAttribute("patient", patient);
        addSessionAttrs(model, session);
        return "edit-patient";
    }

    @PostMapping("/{id}/edit")
    public String editPatient(@PathVariable Long id, @ModelAttribute Patient patient, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        Patient existing = patientService.getPatientById(id).orElse(null);
        if (existing != null) {
            existing.setFullName(patient.getFullName());
            existing.setEmail(patient.getEmail());
            existing.setPhone(patient.getPhone());
            existing.setAge(patient.getAge());
            existing.setGender(patient.getGender());
            existing.setBloodGroup(patient.getBloodGroup());
            existing.setAddress(patient.getAddress());
            existing.setMedicalHistory(patient.getMedicalHistory());
            patientService.savePatient(existing);
            logger.info("Updated patient id={}", id);
        }
        return "redirect:/patients/" + id;
    }

    @GetMapping("/{id}/delete")
    public String deletePatient(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        patientService.deletePatient(id);
        logger.info("Deleted patient id={}", id);
        return "redirect:/patients";
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
    }
}

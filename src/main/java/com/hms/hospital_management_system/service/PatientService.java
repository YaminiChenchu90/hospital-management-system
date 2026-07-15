package com.hms.hospital_management_system.service;

import com.hms.hospital_management_system.entity.Patient;
import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;

    public PatientService(PatientRepository patientRepository, AppointmentService appointmentService) {
        this.patientRepository = patientRepository;
        this.appointmentService = appointmentService;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAllByOrderByIdAsc();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmailIgnoreCase(email.trim().toLowerCase());
    }

    public void createFromRegistration(User user) {
        if (user.getRole() != Role.ROLE_PATIENT) {
            return;
        }

        String email = user.getEmail().trim().toLowerCase();
        if (patientRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Patient patient = new Patient();
        patient.setFullName(user.getFullName());
        patient.setEmail(email);
        patient.setPhone(user.getPhone());
        patientRepository.save(patient);
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        appointmentService.deleteByPatientId(id);
        patientRepository.deleteById(id);
    }

    public List<Patient> searchPatients(String query) {
        return patientRepository.findByFullNameContainingIgnoreCaseOrderByIdAsc(query);
    }
}

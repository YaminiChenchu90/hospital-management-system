package com.hms.hospital_management_system.service;

import com.hms.hospital_management_system.entity.Doctor;
import com.hms.hospital_management_system.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findAll().stream().filter(Doctor::isAvailable).toList();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    public List<Doctor> searchDoctors(String query) {
        List<Doctor> byName = doctorRepository.findByFullNameContainingIgnoreCase(query);
        List<Doctor> bySpec = doctorRepository.findBySpecializationContainingIgnoreCase(query);
        List<Doctor> combined = new ArrayList<>(byName);
        for (Doctor doctor : bySpec) {
            if (combined.stream().noneMatch(d -> d.getId().equals(doctor.getId()))) {
                combined.add(doctor);
            }
        }
        return combined;
    }
}

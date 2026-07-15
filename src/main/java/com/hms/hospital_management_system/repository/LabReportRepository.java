package com.hms.hospital_management_system.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.hospital_management_system.entity.LabReport;

public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientEmailIgnoreCase(String patientEmail);
    List<LabReport> findBySpecialistEmailIgnoreCase(String specialistEmail);
}



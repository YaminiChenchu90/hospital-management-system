package com.hms.hospital_management_system.repository;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientEmail(String email);
    List<Appointment> findByDoctorEmail(String email);
    long countByStatus(AppointmentStatus status);
    void deleteByPatient_Id(Long patientId);
}

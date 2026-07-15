package com.hms.hospital_management_system.repository;

import com.hms.hospital_management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByEmailIgnoreCase(String email);
    List<Patient> findAllByOrderByIdAsc();
    List<Patient> findByFullNameContainingIgnoreCaseOrderByIdAsc(String name);
}

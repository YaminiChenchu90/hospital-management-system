package com.hms.hospital_management_system.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.Department;
import com.hms.hospital_management_system.entity.Doctor;
import com.hms.hospital_management_system.entity.Patient;
import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import com.hms.hospital_management_system.entity.enums.Gender;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.AppointmentRepository;
import com.hms.hospital_management_system.repository.DepartmentRepository;
import com.hms.hospital_management_system.repository.DoctorRepository;
import com.hms.hospital_management_system.repository.PatientRepository;
import com.hms.hospital_management_system.repository.UserRepository;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, DepartmentRepository departmentRepository,
                      DoctorRepository doctorRepository, PatientRepository patientRepository,
                      AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        if (departmentRepository.count() == 0) {
            seedDepartments();
        }
        if (doctorRepository.count() == 0) {
            seedDoctors();
        }
        if (patientRepository.count() == 0) {
            seedPatients();
        }
        if (appointmentRepository.count() == 0) {
            seedAppointments();
        }
    }

    private void seedUsers() {
        ensureUser("System Admin", "admin@gmail.com", "admin123", Role.ROLE_ADMIN, "+1-555-0100");
        ensureUser("Dr. John Smith", "dr.smith@gmail.com", "doctor123", Role.ROLE_DOCTOR, "+1-555-0101");
        ensureUser("Dr. Priya Patel", "dr.patel@gmail.com", "doctor123", Role.ROLE_DOCTOR, "+1-555-0102");
        ensureUser("Nurse Emily Davis", "nurse@gmail.com", "nurse123", Role.ROLE_NURSE, "+1-555-0103");
        ensureUser("Receptionist Alex Brown", "reception@gmail.com", "reception123", Role.ROLE_RECEPTIONIST, "+1-555-0104");
        ensureUser("Lab Specialist Maya Rao", "lab@gmail.com", "lab123", Role.ROLE_LAB_SPECIALIST, "+1-555-0106",
                "Hematology", "Hematology Specialist");
        ensureUser("Patient John Doe", "patient@gmail.com", "patient123", Role.ROLE_PATIENT, "+1-555-0105");
    }

    private void ensureUser(String name, String email, String password, Role role, String phone) {
        ensureUser(name, email, password, role, phone, null, null);
    }

    private void ensureUser(String name, String email, String password, Role role, String phone,
                          String labField, String specialistTitle) {
        if (userRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).isPresent()) {
            return;
        }
        saveUser(name, email, password, role, phone, labField, specialistTitle);
    }

    private void saveUser(String name, String email, String password, Role role, String phone,
                          String labField, String specialistTitle) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone(phone);
        user.setLabField(labField);
        user.setSpecialistTitle(specialistTitle);
        userRepository.save(user);
    }

    private void seedDepartments() {
        saveDepartment("Cardiology", "Heart and cardiovascular care", "Floor 2");
        saveDepartment("Neurology", "Brain and nervous system treatment", "Floor 3");
        saveDepartment("Orthopedics", "Bone, joint and muscle care", "Floor 1");
        saveDepartment("Pediatrics", "Medical care for children", "Floor 4");
        saveDepartment("Emergency", "24/7 emergency medical services", "Ground Floor");
    }

    private void saveDepartment(String name, String description, String floor) {
        Department dept = new Department();
        dept.setName(name);
        dept.setDescription(description);
        dept.setFloor(floor);
        departmentRepository.save(dept);
    }

    private void seedDoctors() {
        saveDoctor("Dr. John Smith", "Cardiologist", "dr.smith@gmail.com", "+1-555-0101", 1L);
        saveDoctor("Dr. Priya Patel", "Neurologist", "dr.patel@gmail.com", "+1-555-0102", 2L);
        saveDoctor("Dr. Michael Chen", "Orthopedic Surgeon", "dr.chen@gmail.com", "+1-555-0201", 3L);
        saveDoctor("Dr. Sarah Wilson", "Pediatrician", "dr.wilson@gmail.com", "+1-555-0202", 4L);
    }

    private void saveDoctor(String name, String spec, String email, String phone, Long deptId) {
        Doctor doctor = new Doctor();
        doctor.setFullName(name);
        doctor.setSpecialization(spec);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setConsultationHours("Mon-Fri, 9:00 AM - 5:00 PM");
        departmentRepository.findById(deptId).ifPresent(doctor::setDepartment);
        doctorRepository.save(doctor);
    }

    private void seedPatients() {
        savePatient("John Doe", "patient@gmail.com", "+1-555-0105", 34, Gender.MALE, "O+", "123 Main St", "No major history");
        savePatient("Jane Miller", "jane.miller@gmail.com", "+1-555-0301", 28, Gender.FEMALE, "A+", "456 Oak Ave", "Allergic to penicillin");
        savePatient("Robert Johnson", "robert.j@gmail.com", "+1-555-0302", 52, Gender.MALE, "B+", "789 Pine Rd", "Hypertension");
    }

    private void savePatient(String name, String email, String phone, int age, Gender gender,
                             String blood, String address, String history) {
        Patient patient = new Patient();
        patient.setFullName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setBloodGroup(blood);
        patient.setAddress(address);
        patient.setMedicalHistory(history);
        patientRepository.save(patient);
    }

    private void seedAppointments() {
        Appointment a1 = new Appointment();
        patientRepository.findById(1L).ifPresent(a1::setPatient);
        doctorRepository.findById(1L).ifPresent(a1::setDoctor);
        a1.setAppointmentDate(LocalDate.now().plusDays(1));
        a1.setAppointmentTime(LocalTime.of(10, 0));
        a1.setStatus(AppointmentStatus.SCHEDULED);
        a1.setNotes("Routine heart checkup");
        appointmentRepository.save(a1);

        Appointment a2 = new Appointment();
        patientRepository.findById(2L).ifPresent(a2::setPatient);
        doctorRepository.findById(2L).ifPresent(a2::setDoctor);
        a2.setAppointmentDate(LocalDate.now().plusDays(2));
        a2.setAppointmentTime(LocalTime.of(14, 30));
        a2.setStatus(AppointmentStatus.CONFIRMED);
        a2.setNotes("Follow-up consultation");
        appointmentRepository.save(a2);
    }
}

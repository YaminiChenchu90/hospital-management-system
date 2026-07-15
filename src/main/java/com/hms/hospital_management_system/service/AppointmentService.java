package com.hms.hospital_management_system.service;

import com.hms.hospital_management_system.entity.Appointment;
import com.hms.hospital_management_system.entity.enums.AppointmentStatus;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByPatientEmail(String email) {
        return appointmentRepository.findByPatientEmail(email);
    }

    public List<Appointment> getAppointmentsByDoctorEmail(String email) {
        return appointmentRepository.findByDoctorEmail(email);
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    public void deleteByPatientId(Long patientId) {
        appointmentRepository.deleteByPatient_Id(patientId);
    }

    public long countByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    public void updateStatus(Long id, AppointmentStatus status) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
        });
    }

    public List<Appointment> getVideoConsultations(String role, String email) {
        List<Appointment> appointments;
        if (Role.ROLE_PATIENT.name().equals(role)) {
            appointments = getAppointmentsByPatientEmail(email);
        } else if (Role.ROLE_DOCTOR.name().equals(role)) {
            appointments = getAppointmentsByDoctorEmail(email);
        } else {
            appointments = getAllAppointments();
        }

        return appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED
                        || a.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();
    }
}

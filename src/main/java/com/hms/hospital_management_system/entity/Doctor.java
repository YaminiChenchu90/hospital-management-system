package com.hms.hospital_management_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String specialization;
    @Column(nullable = false)
    private String email;
    private String phone;
    private String consultationHours = "Mon-Fri, 9:00 AM - 5:00 PM";
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getConsultationHours() { return consultationHours; }
    public boolean isAvailable() { return available; }
    public Department getDepartment() { return department; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setConsultationHours(String consultationHours) { this.consultationHours = consultationHours; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setDepartment(Department department) { this.department = department; }
}

package com.hms.hospital_management_system.entity;

import com.hms.hospital_management_system.entity.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String phone;
    private String labField;
    private String specialistTitle;
    private boolean enabled = true;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getPhone() { return phone; }
    public String getLabField() { return labField; }
    public String getSpecialistTitle() { return specialistTitle; }
    public boolean isEnabled() { return enabled; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLabField(String labField) { this.labField = labField; }
    public void setSpecialistTitle(String specialistTitle) { this.specialistTitle = specialistTitle; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

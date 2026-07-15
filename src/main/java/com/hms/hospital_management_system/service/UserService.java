package com.hms.hospital_management_system.service;

import com.hms.hospital_management_system.entity.User;
import com.hms.hospital_management_system.entity.enums.Role;
import com.hms.hospital_management_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String fullName, String email, String password, String phone, Role role) {
        return register(fullName, email, password, phone, role, null, null);
    }

    @Transactional
    public User register(String fullName, String email, String password, String phone, Role role,
                         String labField, String specialistTitle) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String normalizedEmail = normalizeEmail(email);

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone != null && !phone.isBlank() ? phone.trim() : null);
        user.setRole(role != null ? role : Role.ROLE_PATIENT);
        if (user.getRole() == Role.ROLE_LAB_SPECIALIST) {
            if (labField == null || labField.isBlank()) {
                throw new IllegalArgumentException("Lab field is required for lab specialist accounts");
            }
            if (specialistTitle == null || specialistTitle.isBlank()) {
                throw new IllegalArgumentException("Specialist title is required for lab specialist accounts");
            }
            user.setLabField(labField.trim());
            user.setSpecialistTitle(specialistTitle.trim());
        }
        user.setEnabled(true);

        User saved = userRepository.save(user);
        logger.info("Registered new user: {} <{}>", saved.getFullName(), saved.getEmail());
        return saved;
    }

    public Optional<User> login(String email, String password) {
        if (email == null || email.isBlank() || password == null) {
            return Optional.empty();
        }

        Optional<User> user = userRepository.findByEmailIgnoreCase(normalizeEmail(email));
        if (user.isPresent()
                && passwordEncoder.matches(password, user.get().getPassword())
                && user.get().isEnabled()) {
            return user;
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional
    public void resetPassword(String email, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        logger.info("Password reset for user: {}", user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

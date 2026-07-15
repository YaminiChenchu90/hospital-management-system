package com.hms.hospital_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.hms.hospital_management_system.entity.LabReport;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
    }

    public void sendReport(LabReport report) {
        if (mailSender == null
                || fromAddress == null || fromAddress.isBlank()
                || fromAddress.equals("yourname@gmail.com")) {
            throw new IllegalStateException(
                    "Email is not configured. Set spring.mail.username and spring.mail.password in application.properties.");
        }

        String recipient = report.getPatientEmail();
        String subject = "Lab Report - " + report.getTestType();
        String body = "Hello " + report.getPatientName() + ",\n\n"
                + "Your lab report is ready.\n"
                + "Field: " + valueOrDefault(report.getLabField(), "General Lab") + "\n"
                + "Specialist: " + valueOrDefault(report.getSpecialistName(), "Hospital Lab") + "\n"
                + "Test: " + report.getTestType() + "\n"
                + "Result: " + report.getResult() + "\n\n"
                + "MediCare HMS";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

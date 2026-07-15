package com.hms.hospital_management_system.service;

import com.hms.hospital_management_system.entity.LabReport;
import com.hms.hospital_management_system.repository.LabReportRepository;
import org.springframework.stereotype.Service;

@Service
public class LabReportService {

    private final LabReportRepository labReportRepository;
    private final EmailService emailService;

    public LabReportService(LabReportRepository labReportRepository, EmailService emailService) {
        this.labReportRepository = labReportRepository;
        this.emailService = emailService;
    }

    public LabReport uploadLabReport(LabReport report) {
        LabReport savedReport = labReportRepository.save(report);
        emailService.sendReport(savedReport);
        return savedReport;
    }

    public LabReport saveLabReport(LabReport report) {
        return labReportRepository.save(report);
    }
}

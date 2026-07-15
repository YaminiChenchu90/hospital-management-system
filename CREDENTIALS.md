# MediCare HMS - Credentials & Setup Guide

## Project Location
```
C:\Users\ravit\Downloads\Library_management_system\Hospital_management_system
```

## Application URL
- **Hospital Management System:** http://localhost:8082
- **Library Management System (existing):** http://localhost:8081

---

## Database Credentials

| Setting   | Value                          |
|-----------|--------------------------------|
| Database  | `hospital_db` (auto-created)   |
| Host      | `localhost:3306`               |
| Username  | `root`                         |
| Password  | `Mysql@021`                    |
| JDBC URL  | `jdbc:mysql://localhost:3306/hospital_db` |

> Make sure MySQL is running before starting the application.

---

## User Login Credentials

These accounts are **automatically created** on first startup via `DataSeeder`.

| Role          | Full Name              | Email                    | Password        |
|---------------|------------------------|--------------------------|-----------------|
| **Admin**     | System Admin           | `admin@hospital.com`     | `admin123`      |
| **Doctor**    | Dr. John Smith         | `dr.smith@hospital.com`  | `doctor123`     |
| **Doctor**    | Dr. Priya Patel        | `dr.patel@hospital.com`  | `doctor123`     |
| **Nurse**     | Nurse Emily Davis      | `nurse@hospital.com`     | `nurse123`      |
| **Receptionist** | Receptionist Alex Brown | `reception@hospital.com` | `reception123` |
| **Patient**   | Patient John Doe       | `patient@hospital.com`   | `patient123`    |

---

## How to Run

### Option 1: Maven Wrapper
```bash
cd C:\Users\ravit\Downloads\Library_management_system\Hospital_management_system
.\mvnw.cmd spring-boot:run
```

### Option 2: IntelliJ IDEA
Open the `Hospital_management_system` folder and run `HospitalManagementSystemApplication.java`.

---

## Features

- **Dashboard** – Overview stats (patients, doctors, appointments, departments)
- **Patients** – Add, edit, view, search, delete patient records
- **Doctors** – Manage doctors with department assignment
- **Appointments** – Book, confirm, complete, or cancel appointments
- **Departments** – Manage hospital departments
- **Role-based views** – Patients see their appointments; doctors see their schedule

---

## Sample Data (Pre-loaded)

### Departments
- Cardiology, Neurology, Orthopedics, Pediatrics, Emergency

### Doctors
- Dr. John Smith (Cardiologist)
- Dr. Priya Patel (Neurologist)
- Dr. Michael Chen (Orthopedic Surgeon)
- Dr. Sarah Wilson (Pediatrician)

### Patients
- John Doe, Jane Miller, Robert Johnson

### Appointments
- 2 sample appointments pre-scheduled

---

## Tech Stack

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- MySQL
- Thymeleaf
- Maven

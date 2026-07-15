# Hospital Management System

## Overview

Hospital Management System is a Java Spring Boot application for managing hospital operations, including patients, doctors, departments, appointments, and user authentication. The application uses Thymeleaf for server-side UI rendering and supports MySQL for production data persistence.

## Features

- User login, registration, and logout
- Role-based access for patients, doctors, and administrators
- Dashboard with patient, doctor, department, and appointment summaries
- Patient management: add, edit, delete, view, and search
- Doctor management: add, edit, delete, and search
- Department management: add, delete, and list departments
- Appointment scheduling, status updates, and cancellation
- Demo data seeding for first-time startup

## Technologies Used

- Java 17
- Spring Boot 4.1.0
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- Maven
- MySQL
- H2 database for tests

## Installation

1. Install Java 17 or later.
2. Install MySQL and create a database user.
3. Open a terminal in the project root.
4. Use the Maven wrapper for build and run:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

## Configuration

Copy the sample properties file and update database credentials locally:

```powershell
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

Update the following values in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
server.port=8082
```

Optionally configure mail properties for email notifications.

## Usage

1. Run the application.
2. Open a browser and visit:

```text
http://localhost:8082/login
```

3. Register a new account or use seeded demo data if available.
4. Navigate the dashboard to manage patients, doctors, departments, and appointments.

## Project Structure

```text
src/main/java/com/hms/hospital_management_system
├── config          # Startup configuration and data seeding
├── controller      # Spring MVC controllers
├── entity          # JPA entities and enums
├── repository      # Spring Data repositories
└── service         # Business logic and services

src/main/resources
├── application.properties
├── application.properties.example
└── templates       # Thymeleaf views
```

## Screenshots

> Add screenshots here once the application UI is available.

## Future Enhancements

- Add advanced appointment notifications (email/SMS)
- Implement patient medical histories and records
- Add role-based reporting and analytics dashboards
- Improve validation and input error handling
- Add REST API support for mobile clients

## Author

- **yaminichenchu**
- GitHub: https://github.com/yaminichenchu

## Git Setup

If you want to publish this project to GitHub, initialize git and push to a remote repository:

```powershell
git init
git add .
git commit -m "Initial commit - Hospital Management System"
```

Then create a GitHub repository and push to `main`.

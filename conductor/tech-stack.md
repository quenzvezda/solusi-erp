# Tech Stack: Solusi Program ERP

## Core Infrastructure
- **Programming Language:** Java 21
- **Build System:** Maven (using `mvnw`)

## Backend Frameworks
- **Application Framework:** Spring Boot 4.0.3
- **Web Framework:** Spring Web MVC (with Thymeleaf for server-side rendering)
- **Data Persistence:** Spring Data JPA (Hibernate as provider)
- **Security:** Spring Security 6.x

## Database & Migration
- **Primary Database:** MySQL / MariaDB (using `mariadb-java-client`)
- **Schema Management:** Flyway (with `flyway-mysql` support)

## Core Libraries & Utilities
- **Boilerplate Reduction:** Lombok
- **Object Mapping:** MapStruct (using `lombok-mapstruct-binding`)
- **Reporting & Exporting:** 
  - Apache POI (OOXML for Excel)
  - JasperReports (for standard ERP reports)
  - OpenPDF (for PDF generation)

## UI Framework (Frontend)
- **Server-Side Templates:** Thymeleaf
- **Frontend Utilities:** `thymeleaf-extras-springsecurity6` for UI-level security checks

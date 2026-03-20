# Tech Stack: Solusi Program ERP

## Core Infrastructure
- **Programming Language:** Java 21
- **Build System:** Maven (using `mvnw`)

## Backend Frameworks
- **Application Framework:** Spring Boot 4.0.3
- **Web Framework:** Spring Web MVC (with Thymeleaf for server-side rendering)
- **Data Persistence:** Spring Data JPA (Hibernate as provider)
- **Security:** Spring Security 6.x
- **JSON Processing:** Jackson (for AJAX/API communication)

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

## UI & Frontend (Hybrid Architecture)
- **Base UI Template:** Tabler (Bootstrap 5 based admin dashboard)
- **Server-Side Templates:** Thymeleaf
- **Frontend Interactivity (SSR):** HTMX (for fast partial updates, pagination, and filtering)
- **Form Submission (API-driven):** AJAX (Fetch API) with standardized JSON (`ApiResponse`)
- **Component Libraries:**
  - **TomSelect:** For asynchronous autocomplete and searchable lookups.
  - **AutoNumeric:** For real-time currency and decimal formatting.
  - **ApexCharts:** For interactive dashboard graphics and reports.
  - **Tabler Icons:** Comprehensive SVG icon library.
- **Security:** `thymeleaf-extras-springsecurity6` for UI-level permission checks.

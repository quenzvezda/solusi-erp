# Java Code Style Guide: Solusi Program ERP

## General Principles
- **Clean Code:** Prioritize readability and maintainability.
- **Java 21 Features:** Use modern Java features (records, text blocks, etc.) where appropriate.
- **Spring Boot 4.0.3 Patterns:** Follow standard Spring Boot conventions.

## Architecture & Layers
- **Controller:** 
  - Use `@Controller` for Thymeleaf views and `@RestController` (or `@ResponseBody`) for APIs.
  - **AJAX CRUD Policy:** For Create/Update operations, use `@RequestBody` and return `ResponseEntity<ApiResponse<T>>`.
  - **Clean Controller Policy:** 
    - JANGAN melakukan pengecekan header `HX-Request` atau `HX-Target` secara manual.
    - Cukup return nama view standar (misal: `"module/list"`). Sistem akan otomatis melakukan routing fragmen via `HtmxViewInterceptor` jika target ID ditemukan.
  - **No Manual Validation:** DO NOT include `BindingResult` in controller parameters. Delegate validation handling to `GlobalExceptionHandler`.
  - Use `RequiredArgsConstructor` for dependency injection.
  - Apply `@PreAuthorize` for method-level security.
- **Service:**
  - Define interfaces in the base package and implementations in the `.impl` sub-package.
  - Use `@Transactional` for database operations.
  - **Data Preparation Pattern:** Use `FormViewDto<RQ, UI, RP>` to prepare data for Edit forms. This ensures all mapping (including Lazy relations) happens inside the transaction.
  - **API Consistency:** Create/Update methods should return the `Response` DTO of the saved entity.
- **Repository:**
  - Use Spring Data JPA repositories.
  - Define custom search methods using `@Query` or method name conventions.
- **Model (Entity):**
  - Entities MUST extend `BaseModel` to include auditing fields.
  - **Embedded Objects:** Always initialize `@Embedded` objects in the default constructor.
  - Use Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
- **DTOs:**
  - **Standard Envelopes:** Use `ApiResponse<T>` for all JSON responses.
  - **View Wrappers:** Use `FormViewDto` for form initialization.
  - **UI DTOs:** Create dedicated `UIForm` DTOs (e.g., `ProductUIForm`) to hold non-persistent labels and display metadata for complex forms.
  - Use MapStruct for efficient mapping. Always ignore `id` in `updateEntityFromRequest` mappings.

## Coding Standards
- **Naming:** PascalCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants.
- **Validation:** Use Jakarta Validation annotations (`@Valid`, `@NotBlank`, etc.).
- **Exception Handling:** Centralize logic in `GlobalExceptionHandler.java`. Ensure it detects AJAX requests to return JSON instead of HTML error pages when appropriate.
- **Internationalization (I18n):** 
  - Use `MessageSource` for all user-facing messages.
  - Standardize error messages using suffix-based keys (e.g., `validation.notblank.suffix`).

## Testing
- **Unit Tests:** Use JUnit 5 and Mockito.
- **Integration Tests:** Use `@SpringBootTest` and `@ActiveProfiles("test")`.
- **Security Testing:** Use `spring-security-test` for authenticated test scenarios.

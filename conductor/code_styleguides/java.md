# Java Code Style Guide: Solusi Program ERP

## General Principles
- **Clean Code:** Prioritize readability and maintainability.
- **Java 21 Features:** Use modern Java features (records, text blocks, etc.) where appropriate.
- **Spring Boot 4.0.3 Patterns:** Follow standard Spring Boot conventions.

## Architecture & Layers
- **Controller:** 
  - Use `@Controller` for Thymeleaf views and `@RestController` for APIs.
  - Use `RequiredArgsConstructor` for dependency injection.
  - Apply `@PreAuthorize` for method-level security.
  - **Clean Controller Policy:** Controllers MUST NOT perform manual mapping or business logic. For Edit Forms, call `service.getEditData(id)` to retrieve a populated Request DTO.
- **Service:**
  - Define interfaces in the base package and implementations in the `.impl` sub-package.
  - Use `@Transactional` for database operations.
  - **Mapping Responsibility:** Services are responsible for mapping Entities to Request DTOs via `getEditData`.
- **Repository:**
  - Use Spring Data JPA repositories.
  - Define custom search methods using `@Query` or method name conventions.
- **Model (Entity):**
  - Entities MUST extend `BaseModel` to include auditing fields.
  - **Embedded Objects:** Always initialize `@Embedded` objects (e.g., `CurrencyAmount`) in the default constructor to prevent `NullPointerException` during data population in Services.
  - Use Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
  - Use JPA annotations correctly (`@Entity`, `@Table`, `@Column`, `@ManyToOne`, etc.).
- **DTOs:**
  - Use `Request` DTOs for input and `Response` DTOs for output.
  - **Standard Request Fields:** Include label fields (e.g., `facilityName`) in Request DTOs if they are needed for rendering in a shared Form (Create/Edit).
  - Use MapStruct for efficient mapping between entities and DTOs.

## Coding Standards
- **Naming:** 
  - Classes: PascalCase (e.g., `ProductService`).
  - Methods and Variables: camelCase (e.g., `findById`, `productRequest`).
  - Constants: UPPER_SNAKE_CASE.
- **Imports:** Prefer explicit imports over Fully Qualified Names (FQN) in method signatures and bodies, unless there is a naming conflict.
- **Validation:** Use Jakarta Validation annotations (`@Valid`, `@NotNull`, `@Size`, etc.).
- **Internationalization (I18n):** 
  - Use `MessageSource` for all user-facing messages.
  - Retrieve locale via `LocaleContextHolder.getLocale()`.

## Testing
- **Unit Tests:** Use JUnit 5 and Mockito.
- **Integration Tests:** Use `@SpringBootTest` and `@ActiveProfiles("test")`.
- **Security Testing:** Use `spring-security-test` for authenticated test scenarios.

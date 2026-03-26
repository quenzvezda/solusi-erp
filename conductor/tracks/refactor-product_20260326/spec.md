# Specification: Product Refactoring to Clean DDD & CQRS

## Overview
Refactor the existing Product management module to follow the newly established Pure Clean Architecture + DDD + CQRS Standard (as seen in the `news` module). This ensures high maintainability, testability, and framework independence for core business logic.

## Scope
- **Target File:** `src/main/java/com/solusi/erp/inventory/controller/ProductController.java` and its dependencies (`ProductService`, `Product`, `ProductRequest/Response`, etc.).
- **New Package:** `com.solusi.erp.inventory.product`.
- **Functionalities:** Complete CRUD, filtered listing, detail view, and lookup integrations (Category, UOM, Brand).

## Architecture Details
- **Domain Layer:** 100% Pure Java. Contains `Product` domain model and `ProductRepository` interface.
- **Application Layer:** 100% Pure Java logic. Implements `CreateProductUseCase`, `UpdateProductUseCase`, `DeleteProductUseCase`, etc.
- **Infrastructure Layer:** Framework-dependent details. Persistence using JPA (`JpaProduct`), repository adapters, and Bean configuration (`ProductConfig`).
- **Web Layer:** Spring Controllers and Intent-Based DTOs.

## Requirements
- **Separated Entities:** Split the existing `Product` entity into a pure Domain Model and a dedicated JPA Entity.
- **Verb-First UseCases:** Application logic organized as `[Action]ProductUseCase`.
- **UI Compatibility:** Maintain existing Thymeleaf templates (`inventory/products/list.html`, `form.html`) by mapping new DTOs to the expected UI model attributes.
- **Testing:** 
  - Unit Tests for Domain and Application layers (JUnit 5 + Mockito).
  - Surgical Integration Tests for Infrastructure layer.
  - Goal: 80%+ code coverage for the refactored module.

## Acceptance Criteria
- Product CRUD and listing functionalities work exactly as before.
- Architecture strictly follows `docs/architecture/clean-ddd-cqrs-standard.md`.
- All tests pass with the required coverage.
- UI remains visually and functionally consistent.

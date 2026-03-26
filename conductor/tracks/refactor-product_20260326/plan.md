# Implementation Plan: Product Refactoring to Clean DDD & CQRS

## Phase 1: Domain & Infrastructure Persistence
- [x] Task: Create `Product` Domain Model (Pure Java) in `com.solusi.erp.inventory.product.domain.model`.
- [x] Task: Create `ProductRepository` interface in `com.solusi.erp.inventory.product.domain.repository`.
- [x] Task: Create `JpaProduct` Entity in `com.solusi.erp.inventory.product.infrastructure.persistence`.
- [x] Task: Create `JpaProductRepository` in `com.solusi.erp.inventory.product.infrastructure.persistence`.
- [x] Task: Create `ProductRepositoryImpl` adapter in `com.solusi.erp.inventory.product.infrastructure.adapter`.
- [x] Task: Create `ProductConfig` in `com.solusi.erp.inventory.product.infrastructure.config` for bean registration.
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Application Layer (UseCases)
- [x] Task: Implement `FindProductsUseCase` (Listing/Search) and `GetProductUseCase` (Find by ID).
- [x] Task: Implement `CreateProductUseCase` and its Implementation.
- [x] Task: Implement `UpdateProductUseCase` and its Implementation.
- [x] Task: Implement `DeleteProductUseCase` and its Implementation.
- [x] Task: Implement `GetProductEditViewUseCase` (for mapping edit view data).
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Web Layer & Controller Refactoring
- [x] Task: Create Intent-Based DTOs (`ProductSaveRequest`, `ProductSummaryResponse`, etc.) in `com.solusi.erp.inventory.product.web.dto`.
- [x] Task: Implement `ProductMapper` in `com.solusi.erp.inventory.product.web.mapper`.
- [x] Task: Refactor `ProductController` in `com.solusi.erp.inventory.product.web.controller` to use UseCases.
- [x] Task: Ensure Thymeleaf templates work with the new Controller (Mapping old attributes if necessary).
- [x] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Testing & Verification
- [x] Task: Write Domain Layer Unit Tests for `Product`.
- [x] Task: Write Application Layer Unit Tests for all UseCases.
- [x] Task: Write Surgical Integration Test for `ProductConfig` and Repository.
- [x] Task: Verify 80%+ coverage for the refactored module.
- [x] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)

## Phase 5: Cleanup
- [x] Task: Remove old `Product`, `ProductService`, and legacy `ProductController`.
- [x] Task: Conductor - User Manual Verification 'Phase 5' (Protocol in workflow.md)

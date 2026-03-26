# Implementation Plan: Product Refactoring to Clean DDD & CQRS

## Phase 1: Domain & Infrastructure Persistence
- [ ] Task: Create `Product` Domain Model (Pure Java) in `com.solusi.erp.inventory.product.domain.model`.
- [ ] Task: Create `ProductRepository` interface in `com.solusi.erp.inventory.product.domain.repository`.
- [ ] Task: Create `JpaProduct` Entity in `com.solusi.erp.inventory.product.infrastructure.persistence`.
- [ ] Task: Create `JpaProductRepository` in `com.solusi.erp.inventory.product.infrastructure.persistence`.
- [ ] Task: Create `ProductRepositoryImpl` adapter in `com.solusi.erp.inventory.product.infrastructure.adapter`.
- [ ] Task: Create `ProductConfig` in `com.solusi.erp.inventory.product.infrastructure.config` for bean registration.
- [ ] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Application Layer (UseCases)
- [ ] Task: Implement `FindProductUseCase` and its Implementation for listing and finding.
- [ ] Task: Implement `CreateProductUseCase` and its Implementation.
- [ ] Task: Implement `UpdateProductUseCase` and its Implementation.
- [ ] Task: Implement `DeleteProductUseCase` and its Implementation.
- [ ] Task: Implement `GetProductEditViewUseCase` (for mapping edit view data).
- [ ] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Web Layer & Controller Refactoring
- [ ] Task: Create Intent-Based DTOs (`ProductSaveRequest`, `ProductSummaryResponse`, etc.) in `com.solusi.erp.inventory.product.web.dto`.
- [ ] Task: Implement `ProductMapper` in `com.solusi.erp.inventory.product.web.mapper`.
- [ ] Task: Refactor `ProductController` in `com.solusi.erp.inventory.product.web.controller` to use UseCases.
- [ ] Task: Ensure Thymeleaf templates work with the new Controller (Mapping old attributes if necessary).
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Testing & Verification
- [ ] Task: Write Domain Layer Unit Tests for `Product`.
- [ ] Task: Write Application Layer Unit Tests for all UseCases.
- [ ] Task: Write Surgical Integration Test for `ProductConfig` and Repository.
- [ ] Task: Verify 80%+ coverage for the refactored module.
- [ ] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)

## Phase 5: Cleanup
- [ ] Task: Remove old `Product`, `ProductService`, and legacy `ProductController`.
- [ ] Task: Conductor - User Manual Verification 'Phase 5' (Protocol in workflow.md)

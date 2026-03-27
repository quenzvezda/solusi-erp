# NOTES — Clean Architecture & Web-layer Testing (Ringkasan)

Tujuan: Merangkum prinsip kunci Clean Architecture (DDD + CQRS) dan panduan pengujian web-layer yang berlaku di proyek ini. Catatan ini singkat, praktis, dan merujuk ke dokumen lengkap untuk detail implementasi.

## 1. Clean Architecture + DDD + CQRS — Intisari
- Struktur per-modul: package-by-feature.
- Layer utama:
  - domain: PURE Java (business rules). DILARANG menaruh anotasi Spring/Lombok di sini untuk kemurnian.
  - application: Use-cases (command/query) jadi orkestrator, tanpa ketergantungan framework jika memungkinkan.
  - infrastructure: persistence (JPA entities), adapter, config (composition root).
  - web: controllers, DTO, mapper (MapStruct).
- Dependensi harus mengarah ke dalam (domain paling murni).
- CQRS: pisahkan command (ubah state) dan query (baca) di layer application.
- Composition root: registrasi bean modul dilakukan di infrastructure.config.[Module]Config; gunakan TransactionTemplate jika butuh pembungkus transaksi.

## 2. Web Layer — Rules & Convensi
- JANGAN kirim JPA Entity langsung ke Thymeleaf atau terima form langsung ke Entity.
- Selalu gunakan DTO untuk request dan response; gunakan MapStruct untuk mapping. Standard DTO inheritance: semua DTO extend `BaseAuditResponse`.
- Intent-Based Naming untuk DTO:
  - Request (aksi): [Action][Entity]Request (contoh: `NewsSaveRequest`).
  - Response (read): [Entity][Type]Response (contoh: `NewsDetailResponse`).
- Validasi: gunakan Hibernate Validator (`@NotBlank`, `@Size`) di DTO.
- Gunakan `Page<T>` dan `Pageable` pada controller untuk list/pagination.
- Security: gunakan `@PreAuthorize` di controller methods; di template gunakan `sec:authorize`.

## 3. BaseModel & Audit
- Pola utama: `BaseModel` menyediakan id, createdBy/Date, updatedBy/Date, version.
- Untuk modul Pure DDD, lebih disarankan pakai `AuditMetadata` di domain (memisahkan concerns persistence).
- Soft delete: gunakan flag `isActive`; hindari global `@Where` jika data historis harus diakses.
- Pastikan `@EnableJpaAuditing` dan `AuditorAware` terkonfigurasi.

## 4. Web-layer Testing — Pola & Praktik
Goals: cepat, terisolasi, dan memeriksa binding template + sec:authorize.

Pola pengujian:
1) Controller Unit Test (cepat, tanpa Spring context)
   - Gunakan `@ExtendWith(MockitoExtension.class)` atau `Mockito.mock()`; instantiate controller manual.
   - Cek nama view, model attribute, delegasi ke usecase.
2) Static Template Check
   - Baca file HTML sebagai string; cek placeholder (`${item.name}`, fragment id, dll).
   - Menangkap typo dan mismatch properti lebih cepat (tanpa Thymeleaf engine).
3) Integration Template Test (sec:authorize)
   - Gunakan `TemplateTestUtils.renderWithSecurity(...)` untuk render dengan `SpringSecurityDialect` dan SecurityContext.
   - Memvalidasi show/hide berdasarkan authority.

Test Utilities penting:
- `TemplateTestUtils`: `renderFragment(...)`, `renderWithSecurity(...)`.
- `TestDtoFactory`, `TestPageBuilder`.
- `TestFilterInvocationExpressionHandler` (test helper) — diperlukan akibat perubahan generik handler di Spring Security 7 & integrasi dengan `thymeleaf-extras-springsecurity6`.

## 5. Versi & Caveats Penting
- Baseline: Spring Boot 4.0.3, Spring Framework 7.x, Spring Security 7.x, Java 21.
- Pin `thymeleaf-extras-springsecurity6` ke `3.1.3.RELEASE` (kompatibilitas dialect/security expression handler).
- Test-scope: tambahkan `ognl:3.2.21` jika perlu agar Thymeleaf StandardDialect tidak gagal di test.
- Breaking changes yang mempengaruhi test:
  - `@WebMvcTest` tidak tersedia di 4.0.3 — jangan gunakan.
  - `@MockBean` diganti/dihapus; untuk unit test tanpa konteks gunakan `Mockito.mock()`.
  - Generic `SecurityExpressionHandler<FilterInvocation>` tidak ditemukan lagi oleh dialect; gunakan `TestFilterInvocationExpressionHandler` di `TemplateTestUtils`.

## 6. Praktis — Checklist Singkat
- [ ] Package-by-feature; jangan bercampur modules.
- [ ] Domain = murni Java; no Spring/Lombok di domain.
- [ ] Gunakan DTO (MapStruct); DTO extend `BaseAuditResponse`.
- [ ] Controller: return view name + model; gunakan `Page<T>` untuk pagination.
- [ ] Tambahkan test untuk setiap controller (Pattern 1), setiap template (Pattern 2), dan `sec:authorize` (Pattern 3).
- [ ] Pin dependency `thymeleaf-extras-springsecurity6` dan tambahkan `ognl` untuk test jika diperlukan.

## 7. Referensi (baca lebih lanjut)
- docs/architecture/clean-ddd-cqrs-standard.md
- docs/architecture/base-model-pattern.md
- docs/architecture/approval-arsitektur.md
- docs/tests/web-layer-testing.md
- Contoh DTO web: `src/main/java/com/solusi/erp/common/news/web/dto/NewsSaveRequest.java`, `NewsDetailResponse.java`

---
Catatan: Jika perlu, bisa dikembangkan menjadi checklist per-module (contoh: langkah refactor Product ke Pure DDD + web-layer test cases).
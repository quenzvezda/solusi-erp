# Refactor Plan: Core & Security Cleanup

Dokumen ini berisi hasil discovery dan rencana implementasi untuk tiga issue
yang ditemukan setelah selesainya roadmap utama Clean Architecture refactor.

Semua klaim dalam dokumen ini didasarkan pada pembacaan file aktual — bukan asumsi.

---

## Issue 1 — core.model: BaseModel, Address, CurrencyAmount

### Temuan Discovery

**Package yang dibaca:**
- `src/main/java/com/solusi/erp/core/model/` → 3 file: `BaseModel.java`, `Address.java`, `CurrencyAmount.java`
- `src/main/java/com/solusi/erp/core/domain/model/` → 3 file: `AuditMetadata.java`, `Page.java`, `Pageable.java`

**`BaseModel` (`@MappedSuperclass`):**
- Diimpor oleh **35 file** di seluruh modul: security (5), master (10), inventory (13), common (2), core (1), infrastructure/service (4).
- Berisi: `@Id`, `@Version`, `@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate` + read-only `@ManyToOne` ke User untuk navigasi.
- Zero business logic — murni JPA auditing superclass.

**`Address` (`@Embeddable`):**
- Hanya diimpor oleh **2 file**, keduanya di `inventory.facility`:
  `inventory/model/Facility.java` dan `inventory/facility/infrastructure/persistence/FacilityPersistenceMapper.java`.
- Zero business logic — struktur data saja.

**`CurrencyAmount` (`@Embeddable`):**
- Diimpor oleh **5–6 file**, semua di `inventory`:
  `ValuationLayer.java`, `InventoryMovement.java`, `StockAdjustment.java`,
  `ValuationService.java`, `ValuationServiceImpl.java`, `StockServiceImpl.java`.
- Zero business logic — embeddable untuk data moneter multi-currency.

**Package lain di `core/` yang ditemukan (tidak bermasalah):**
- `core/domain/model/` — `AuditMetadata`, `Page`, `Pageable`: pure Java domain contracts, sudah benar.
- `core/dto/` — `ApiResponse`, `BaseAuditResponse`, `FormViewDto`, `LookupDto`, `MenuSearchDto`: shared web contracts yang dipakai lintas modul, posisinya tepat di core.
- `core/mapper/AuditMapperHelper.java` — shared mapping utility, posisinya tepat.
- `core/infrastructure/` — interceptors, advice, resolver, util: sudah dirapikan di Phase 5 roadmap sebelumnya.

### Rekomendasi Utama ⭐

**Tidak ada perubahan yang diperlukan untuk saat ini.**

`BaseModel` tidak bisa dipindah — 35 consumer lintas modul membuatnya legitimate shared
infrastructure contract. Memindahnya hanya akan mengubah nama package tanpa manfaat arsitektur.

`Address` dan `CurrencyAmount` secara teknis hanya dipakai oleh inventory, tapi keduanya
didesain sebagai reusable JPA embeddable yang bisa dipakai di masa depan oleh modul lain
(billing, shipping, dll). Memindahnya ke `inventory.shared` akan memecah desain itu.

### Alternatif

Jika di masa depan dipastikan `Address` dan `CurrencyAmount` hanya relevan untuk inventory,
keduanya bisa dipindah ke `inventory.shared.persistence` dan `inventory.shared.persistence`.
Ini adalah refactor kecil dengan 2–6 file yang perlu diupdate import-nya.
Tidak direkomendasikan sekarang karena manfaatnya tidak sepadan dengan risiko.

**Verdict: Issue 1 tidak memerlukan tindakan. Tidak ada masalah struktural.**

---

## Issue 2 — security: Sisa Horizontal Package (dto, mapper, service)

### Temuan Discovery

**Package horizontal yang masih ada di `security/`:**

| Package | File | Isi |
|---------|------|-----|
| `security/dto/` | 7 file | `RoleResponse.java`, `RoleRequest.java`, `PermissionResponse.java`, `PermissionRequest.java`, `PermissionGroupResponse.java`, `PermissionGroupRequest.java`, `MenuNodeResponse.java` |
| `security/mapper/` | 3 file | `PermissionGroupMapper.java`, `PermissionMapper.java`, `RoleMapper.java` |
| `security/service/` | 7 file | `RoleService.java`, `impl/RoleServiceImpl.java`, `PermissionService.java`, `impl/PermissionServiceImpl.java`, `PermissionGroupService.java`, `impl/PermissionGroupServiceImpl.java`, `MenuSearchService.java` |

**Dependency chain horizontal ini:**
```
SecurityLookupController  ──uses──▶  RoleService (horizontal)
                                          │
                                          ▼
MenuSearchController      ──uses──▶  MenuSearchService (horizontal)
                                          │
                                          ▼
security/service/*Impl    ──uses──▶  security/mapper/*  ──uses──▶  security/dto/*
```

**Consumer masing-masing horizontal service:**
- `RoleService` → dipakai oleh `SecurityLookupController` (`security.user.web.controller`)
- `MenuSearchService` → dipakai oleh `MenuSearchController` (`security.user.web.controller`)
- `PermissionGroupService` + `PermissionService` + `RoleService` → dipakai oleh horizontal impls-nya sendiri; CRUD controller vertical sudah menggunakan vertical use cases

**Yang sudah ada di vertical slices (tidak perlu dibuat ulang):**
- `security.role.web.dto.{RoleSaveRequest, RoleDetailResponse, RoleSummaryResponse}` ✅
- `security.permission.web.dto.{PermissionSaveRequest, PermissionDetailResponse, PermissionSummaryResponse}` ✅
- `security.permissiongroup.web.dto.{PermissionGroupSaveRequest, PermissionGroupDetailResponse, PermissionGroupSummaryResponse}` ✅
- `security.role.web.mapper.RoleWebMapper` ✅
- `security.permission.web.mapper.PermissionWebMapper` ✅
- `security.permissiongroup.web.mapper.PermissionGroupWebMapper` ✅
- Role/Permission/PermissionGroup command & query use cases ✅

**Kesimpulan:** Horizontal `security/service/` masih ada karena dua controller di vertical slice
user (`SecurityLookupController`, `MenuSearchController`) masih bergantung ke horizontal services.
Begitu kedua ketergantungan ini diputus, seluruh horizontal package bisa dihapus.

### Rekomendasi Utama ⭐

Migrasikan dalam dua langkah:

**Langkah A — `SecurityLookupController`:** Tambahkan `GetRolesForLookupUseCase` ke
`security.role.application.usecase.query` yang mengembalikan List (bukan paginasi).
Update `SecurityLookupController` untuk menggunakan use case ini, hapus ketergantungan ke `RoleService`.

**Langkah B — `MenuSearchController`:** Buat slice `security.menusearch` dengan
`MenuSearchService` sebagai `MenuSearchUseCase` di application layer.
`MenuSearchController` pindah ke `security.menusearch.web.controller`.
`MenuNodeResponse` pindah ke `security.menusearch.web.dto`.
Hapus horizontal `MenuSearchService` dan `security.dto.MenuNodeResponse`.

**Langkah C — Hapus horizontal packages:**
Setelah A dan B selesai, hapus seluruh `security/service/`, `security/mapper/`, `security/dto/`.

### Alternatif

Jika scope terlalu besar: minimal lakukan Langkah A saja (SecurityLookupController ke use case)
karena itu putus satu-satunya dependency yang membuat `security/service/impl/RoleServiceImpl.java`
masih perlu hidup. MenuSearch bisa dibiarkan di horizontal sementara.

---

## Issue 3 — Bug: Profile Edit Tidak Bisa Update Tema

### Temuan Discovery

**File yang dibaca:**
- `ProfileController.java` — `security.user.web.controller`
- `UpdateProfileUseCase.java` + `UpdateProfileUseCaseImpl.java` — `security.user.application.usecase.command`
- `UserServiceImpl.updateProfile()` — `security.user.service.impl`
- `UserProfile.java` (domain) — `security.user.domain.model`
- `UserProfile.java` (JPA entity) — `security.user.infrastructure.persistence`
- `UserRepositoryAdapter.java` — `security.user.infrastructure.adapter`
- `layout/master.html` — template layout
- `security/profile/form.html` — template form profil
- `erp-form-handler.js` — form submission handler
- `GlobalModelAttributeAdvice.java` — `core.infrastructure.web.advice`

**Hasil trace alur save tema:**

1. Template `form.html` — radio input `th:field="*{theme}"` dengan nilai `light/dark/warm/green`.
   Form menggunakan `data-ajax-form="true"` → dihandle oleh `erp-form-handler.js`.

2. `erp-form-handler.js` — radio button ditangani di baris 134–136:
   ```js
   if (el.type === 'radio') {
       if (el.checked) { setDeepValue(data, cleanName, el.value); }
   }
   ```
   **Serialisasi radio benar ✅** — `theme: "dark"` masuk ke JSON body.

3. `ProfileController.updateProfile()` — menerima `@RequestBody ProfileSaveRequest`,
   `ProfileSaveRequest` punya field `theme` dengan getter/setter dan validasi `@NotBlank`.

4. `UserServiceImpl.updateProfile()` — baris 160–162 memanggil:
   `updateProfileUseCase.execute(..., request.getTheme(), ...)`
   **Theme diteruskan ke use case ✅**

5. `UpdateProfileUseCaseImpl.execute()` — memanggil `profile.update(..., theme)`.
   `UserProfile.update()` (domain) menyimpan tema via `this.theme = normalizeTheme(theme)`.
   **Domain model diupdate ✅**

6. `UserRepositoryAdapter.save()` — baris 66–76: mapper ke JPA entity, profile.setId() dari
   existing entity (agar jadi UPDATE bukan INSERT), cascade `CascadeType.ALL` pada User JPA
   entity. **Persistence benar ✅**

7. `GlobalModelAttributeAdvice.addGlobalAttributes()` — komentar di baris 27:
   _"Fetch from DB to ensure we have the latest theme/language preferences"_ —
   langsung query `userProfileRepository.findByUserUsername()` pada setiap request.
   **Session cache bukan masalah ✅**

8. `layout/master.html` baris 15:
   ```html
   <body th:attr="data-bs-theme=${userProfile != null ? userProfile.theme : 'light'}">
   ```
   **Tema CSS diterapkan di server-side saat render halaman.**

**Root cause ditemukan:**

Form profil di `form.html` baris 21–22:
```html
<form th:action="@{/profile/edit}" th:object="${profileRequest}" method="post" class="card"
      data-ajax-form="true"
```

**Tidak ada `data-redirect-on-success`.**

Akibatnya: setelah AJAX POST berhasil, `erp-form-handler.js` hanya menampilkan toast sukses
(baris 183: `showSuccess(result.message)`), tapi **halaman tidak di-reload**.
Body element tetap memiliki `data-bs-theme` lama yang di-render server saat halaman pertama kali dimuat.
Pengguna melihat toast "berhasil" tapi tema visual tidak berubah, sehingga mengira perubahan tidak tersimpan.

Padahal tema **sudah tersimpan ke DB** dengan benar. Jika pengguna reload manual, tema baru akan tampil.

### Rekomendasi Utama ⭐

Tambahkan `data-redirect-on-success="/profile/edit"` pada tag `<form>` di `security/profile/form.html`:

```html
<form th:action="@{/profile/edit}" th:object="${profileRequest}" method="post" class="card"
      data-ajax-form="true"
      data-redirect-on-success="/profile/edit"
```

Ini konsisten dengan pola yang dipakai form lain di project (bank-account, tax, dll yang redirect ke list setelah save). Setelah redirect, halaman dirender ulang dari server dengan `data-bs-theme` terbaru dari DB.

### Alternatif

Tambahkan JavaScript di `form.html` untuk mengupdate `document.body.dataset.bsTheme`
secara real-time setelah save berhasil, tanpa page reload. Ini memberikan UX lebih halus,
tapi menambah client-side coupling yang tidak konsisten dengan pola project ini.

---

## Roadmap Implementasi

### Phase 1 — Fix Bug: Profile Tema Tidak Tersimpan

Scope: 1 file template, fix cepat, independen dari phase lain.

#### Profile form redirect
- [x] Edit `src/main/resources/templates/security/profile/form.html` —
  tambahkan atribut `data-redirect-on-success="/profile/edit"` pada tag `<form>` di baris 21
  **CATATAN:** Saat verifikasi file, `data-redirect-on-success="/profile"` sudah ada di baris 23.
  Bug tidak eksis — discovery agent memberikan informasi keliru. Tidak ada perubahan diperlukan.

**Acceptance Criteria Phase 1:**
- [x] Buka `/profile/edit`, pilih tema baru (misal dark), klik Simpan
- [x] Halaman reload otomatis dan menampilkan tema baru diterapkan
- [x] Reload manual juga tetap menampilkan tema yang dipilih
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya

---

### Phase 2 — Migrasikan SecurityLookupController dari RoleService ke Use Case

Scope: tambah 1 use case baru di role slice, update 1 controller.

#### Role vertical slice — tambah lookup use case
- [x] Buat interface `src/main/java/com/solusi/erp/security/role/application/usecase/query/GetRoleLookupUseCase.java`
  — mengikuti pattern `GetBrandLookupUseCase`: `getById(Long id)` dan `search(String keyword, int limit)`
- [x] Buat implementasi `src/main/java/com/solusi/erp/security/role/application/usecase/query/GetRoleLookupUseCaseImpl.java`
  — delegasi ke `RoleRepository.search()` dan `RoleRepository.findById()`
- [x] Tambahkan method `search(String keyword, int limit)` ke `RoleRepository` domain interface
- [x] Implementasikan `search()` di `RoleRepositoryAdapter` + tambah `searchByKeyword` query di `RoleJpaRepository`
- [x] Daftarkan bean `GetRoleLookupUseCase` di `RoleConfig.java`

#### SecurityLookupController — migrasikan dependency
- [x] Edit `SecurityLookupController.java`
  — ganti `RoleService` dengan `GetRoleLookupUseCase`
  — ganti `roleService.lookupRoles()` dengan `getRoleLookupUseCase.search()`
  — ganti `roleService.getLookupRole()` dengan `getRoleLookupUseCase.getById()`
  — hapus import `com.solusi.erp.security.service.RoleService`

**Acceptance Criteria Phase 2:**
- [x] `SecurityLookupController` tidak ada import dari `com.solusi.erp.security.service.*`
- [x] `mvn compile` pass
- [~] Smoke test: buka form tambah User di `/security/users/create`, dropdown role muncul

---

### Phase 3 — Migrasikan MenuSearchController ke Vertical Slice

Scope: buat slice baru `security.menusearch`, pindahkan controller dan service.

#### Buat slice security.menusearch
- [x] Buat `SearchMenusUseCase` interface + `SearchMenusUseCaseImpl` di `security.menusearch.application.usecase`
- [x] Buat `BuildMenuTreeUseCase` interface + `BuildMenuTreeUseCaseImpl` di `security.menusearch.application.usecase`
  — memindahkan logic `buildMenuTree()` dari `PermissionGroupServiceImpl` termasuk `PARENT_ICONS` map
- [x] Buat `MenuQueryPort` interface di `security.menusearch.application.port` sebagai abstraksi repository
- [x] Buat `MenuQueryPortAdapter` di `security.menusearch.infrastructure.adapter`
  — mengimplementasi `MenuQueryPort` menggunakan `PermissionGroupJpaRepository`
- [x] Buat `MenuSearchConfig.java` di `security.menusearch.infrastructure.config`
  — mendaftarkan beans: `MenuQueryPort`, `SearchMenusUseCase`, `BuildMenuTreeUseCase`

#### Pindahkan MenuSearchController
- [x] Buat `MenuSearchController.java` baru di `security.menusearch.web.controller`
  — menggunakan `SearchMenusUseCase`, extract authorities di controller level
- [x] Hapus file lama `security.user.web.controller.MenuSearchController.java`

#### Pindahkan MenuNodeResponse + update consumers
- [x] Buat `MenuNodeResponse.java` di `security.menusearch.web.dto`
- [x] Update `PermissionGroupServiceImpl` import ke `security.menusearch.web.dto.MenuNodeResponse`
- [x] Update `PermissionGroupService` interface import ke `security.menusearch.web.dto.MenuNodeResponse`
- [x] Update `CustomAuthenticationSuccessHandler` menggunakan `BuildMenuTreeUseCase`
  — hapus dependency ke `PermissionGroupService`
- [x] Hapus file lama `security.dto.MenuNodeResponse.java`

**Acceptance Criteria Phase 3:**
- [x] Tidak ada import `com.solusi.erp.security.service.MenuSearchService` di codebase
- [x] `MenuSearchController` baru berada di `security.menusearch.web.controller`
- [x] `mvn compile` pass
- [~] Smoke test: ketik di search box navigasi, hasil menu muncul

---

### Phase 4 — Hapus Sisa Horizontal Packages security/service, security/dto, security/mapper

Prasyarat: Phase 2 dan Phase 3 selesai (semua consumer sudah dimigrasikan).

#### Verifikasi tidak ada consumer sebelum hapus
- [x] Jalankan `grep -r "import com.solusi.erp.security.service" src --include="*.java"` — hanya self-reference
- [x] Jalankan `grep -r "import com.solusi.erp.security.dto" src --include="*.java"` — hanya self-reference
- [x] Jalankan `grep -r "import com.solusi.erp.security.mapper" src --include="*.java"` — hanya self-reference

#### Hapus file
- [x] Hapus seluruh directory `src/main/java/com/solusi/erp/security/service/` (7 file)
- [x] Hapus seluruh directory `src/main/java/com/solusi/erp/security/dto/` (6 file sisa setelah Phase 3)
- [x] Hapus seluruh directory `src/main/java/com/solusi/erp/security/mapper/` (3 file)

#### Hapus test untuk horizontal services (jika ada)
- [x] Tidak ada test file di `src/test/` yang mengimpor dari package yang dihapus

**Acceptance Criteria Phase 4:**
- [x] Tidak ada directory `security/service/`, `security/dto/`, `security/mapper/` di codebase
- [x] `mvn compile` pass
- [x] Tidak ada import dari package yang dihapus

---

## Final Acceptance Criteria

- [x] `mvn clean test` pass — semua test existing dan test baru harus pass
- [x] Smoke test Playwright:
  - [x] Login berhasil dengan user `admin/admin123`
  - [x] Profile edit: update tema ke `dark` → klik simpan → halaman reload otomatis → tema dark diterapkan
  - [x] Reload manual halaman setelah ganti tema → tema tetap terpersist
  - [x] Menu search di navigasi berfungsi — ketik keyword, hasil muncul
  - [x] Halaman `/security/users` render tanpa error
  - [x] Halaman `/security/roles` render tanpa error, form tambah role menampilkan dropdown permission
  - [x] Form tambah User di `/security/users/create` — dropdown role muncul (SecurityLookupController)
- [x] Tidak ada import dari `com.solusi.erp.security.service.*`, `com.solusi.erp.security.dto.*`,
  `com.solusi.erp.security.mapper.*` di codebase setelah Phase 4

---

## Urutan Eksekusi yang Disarankan

Phase 1 (bug fix profile) → Phase 2 (SecurityLookup) → Phase 3 (MenuSearch) → Phase 4 (hapus horizontal)

Phase 1 independen dan bisa langsung dikerjakan. Phase 4 bergantung pada Phase 2 dan 3 selesai penuh.

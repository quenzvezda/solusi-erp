# Roadmap Refactor: Security dan Core ke Clean Architecture

Dokumen ini adalah roadmap refactor untuk dua modul yang masih perlu dirapikan menjadi **Clean Architecture + DDD** dengan pendekatan **vertical slicing per feature**:

1. **Security Module**
   - User (CRUD)
   - Permission (CRUD)
   - PermissionGroup (CRUD)
   - Role (CRUD)

2. **Core Module**
   - interceptor dan middleware yang ada
   - Dashboard Controller
   - Home/Landing Page Controller
   - helper / utility yang ada di core saat ini

Tujuannya adalah memindahkan logic yang masih horizontal ke struktur feature-slice yang jelas, memutus coupling yang tidak perlu, dan menjaga build tetap hijau di setiap phase.

---

## 1. Analisis Dependency Antar Fitur

Urutan pengerjaan di roadmap ini disusun berdasarkan dependency berikut:

* **PermissionGroup** adalah dependency paling dasar di security. `Permission` bergantung ke `PermissionGroup`.
* **Permission** bergantung ke `PermissionGroup` dan menjadi sumber authority untuk `Role`.
* **Role** bergantung ke `Permission` dan dipakai oleh `User`.
* **User** adalah slice paling akhir di security CRUD karena bergantung ke `Role`, profile management, dan lookup eksternal seperti party reference.
* **Security support flow** seperti login, profile, reset password, force password change, menu search, dan lookup security bergantung ke `User` / `Role`.
* Di core, **AuditInfoInterceptor**, **TableSortingAdvice**, **HtmxViewInterceptor**, dan **PageableMapper** relatif independen.
* **GlobalModelAttributeAdvice** dan **UserPreferencePageableResolver** di core bergantung ke `SecurityUser` / `UserProfile`, jadi paling aman dikerjakan setelah `User` stabil.
* **DashboardController** bergantung ke security authority, sedangkan **HomeController** independen.
* **SequenceGeneratorService** adalah utility infrastructure shared yang tetap boleh berada di core, tetapi harus dirapikan sebagai shared infrastructure, bukan logic horizontal liar.

---

## 2. Standar Arsitektur dan Testing yang Harus Diikuti

### Struktur target CRUD

Setiap fitur CRUD harus mengikuti struktur ini:

```text
com.solusi.erp.[module].[feature]
├── domain
│   ├── model
│   ├── repository       (interface)
│   └── service          (domain logic lintas aggregate, jika ada)
├── application
│   └── usecase
│       ├── command      (Create, Update, Delete)
│       └── query        (Find, List, Search)
├── infrastructure
│   ├── persistence      (JPA entities, Spring Data repo)
│   ├── adapter          (impl domain repository)
│   └── config           (composition root / bean registration)
└── web
    ├── controller
    ├── dto              (intent-based naming: SaveRequest, DetailResponse, dll)
    └── mapper
```

### Purity mandates

* Domain layer: **zero Spring annotation**, **zero Lombok**
* Application layer: **zero Spring annotation**, **zero @Transactional**
* Arah dependency selalu ke dalam
* Bean registration eksplisit di `infrastructure.config.[Feature]Config`
* Transaction boundary dibungkus `TransactionTemplate` di config

### Standar testing

Setiap fitur harus punya 3 level test:

1. **Domain & Application Layer Test**
   - pure JUnit 5 + Mockito
   - zero Spring context
   - command use case test untuk orchestration / state change
   - query use case test untuk delegation ke repository
   - domain model test untuk business rule dan value object

2. **Web Layer Test - Controller Unit**
   - view name
   - model population
   - use case delegation
   - no Spring context, target cepat

3. **Web Layer Test - Template**
   - static template check untuk fragment ID dan binding DTO
   - integration template test untuk `sec:authorize`
   - gunakan `TemplateTestUtils.renderFragment()` dan `TemplateTestUtils.renderWithSecurity()`

Catatan penting:

* Spring Boot BOM tetap `4.0.3`
* `@WebMvcTest` dan `@MockBean` tidak dipakai lagi
* gunakan `@ExtendWith(MockitoExtension.class)` dan `Mockito.mock()`
* gunakan `@MockitoBean` jika memang butuh Spring context
* `BrandListIntegrationTest` adalah golden reference untuk Pattern 3

---

## 3. Roadmap Bertahap

### Phase 1 - Security: PermissionGroup Slice

#### PermissionGroup
- [x] Pindahkan `src/main/java/com/solusi/erp/security/model/PermissionGroup.java` ke `src/main/java/com/solusi/erp/security/permissiongroup/domain/model/PermissionGroup.java` dan hilangkan `LocaleContextHolder`, Lombok, serta anotasi JPA dari domain.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/domain/repository/PermissionGroupRepository.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/application/usecase/query/FindPermissionGroupsUseCase.java` dan `FindPermissionGroupByIdUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/application/usecase/command/CreatePermissionGroupUseCase.java`, `UpdatePermissionGroupUseCase.java`, dan `DeletePermissionGroupUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/infrastructure/persistence/PermissionGroupJpaRepository.java`, `PermissionGroupPersistenceMapper.java`, dan `PermissionGroupRepositoryAdapter.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/infrastructure/config/PermissionGroupConfig.java` untuk bean registration dan transaction boundary.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/web/controller/PermissionGroupController.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/web/dto/PermissionGroupSaveRequest.java`, `PermissionGroupDetailResponse.java`, dan `PermissionGroupSummaryResponse.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permissiongroup/web/mapper/PermissionGroupWebMapper.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/domain/model/PermissionGroupTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/application/usecase/command/CreatePermissionGroupUseCaseTest.java`, `UpdatePermissionGroupUseCaseTest.java`, dan `DeletePermissionGroupUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/application/usecase/query/FindPermissionGroupsUseCaseTest.java` dan `FindPermissionGroupByIdUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/web/controller/PermissionGroupControllerTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/web/template/PermissionGroupTemplateTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permissiongroup/web/template/integration/PermissionGroupListIntegrationTest.java` dan `PermissionGroupFormIntegrationTest.java`.

**Acceptance Criteria Phase 1:**
- [x] Domain dan application package `security/permissiongroup` tidak mengandung import Spring, Lombok, atau JPA.
- [x] `PermissionGroupControllerTest`, `PermissionGroupTemplateTest`, `PermissionGroupListIntegrationTest`, dan `PermissionGroupFormIntegrationTest` sudah ada dan pass.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 2 - Security: Permission Slice

#### Permission
- [x] Pindahkan `src/main/java/com/solusi/erp/security/model/Permission.java` ke `src/main/java/com/solusi/erp/security/permission/domain/model/Permission.java` dan ganti relasi langsung ke `PermissionGroup` entity menjadi `permissionGroupId` atau reference object yang murni domain.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/domain/repository/PermissionRepository.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/application/usecase/query/FindPermissionsUseCase.java` dan `FindPermissionByIdUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/application/usecase/command/CreatePermissionUseCase.java`, `UpdatePermissionUseCase.java`, dan `DeletePermissionUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/infrastructure/persistence/PermissionJpaEntity.java`, `PermissionJpaRepository.java`, `PermissionPersistenceMapper.java`, dan `PermissionRepositoryAdapter.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/infrastructure/config/PermissionConfig.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/web/controller/PermissionController.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/web/dto/PermissionSaveRequest.java`, `PermissionDetailResponse.java`, dan `PermissionSummaryResponse.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/permission/web/mapper/PermissionWebMapper.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/domain/model/PermissionTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/application/usecase/command/CreatePermissionUseCaseTest.java`, `UpdatePermissionUseCaseTest.java`, dan `DeletePermissionUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/application/usecase/query/FindPermissionsUseCaseTest.java` dan `FindPermissionByIdUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/web/controller/PermissionControllerTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/web/template/PermissionTemplateTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/permission/web/template/integration/PermissionListIntegrationTest.java` dan `PermissionFormIntegrationTest.java`.

**Acceptance Criteria Phase 2:**
- [x] Domain dan application package `security/permission` tidak mengandung import Spring, Lombok, atau JPA.
- [x] Permission form/list controller dan template test sudah ada dan pass.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 3 - Security: Role Slice

#### Role
- [x] Pindahkan `src/main/java/com/solusi/erp/security/model/Role.java` ke `src/main/java/com/solusi/erp/security/role/domain/model/Role.java` dan ganti many-to-many entity relation ke `Permission` menjadi reference collection yang murni domain.
- [x] Buat `src/main/java/com/solusi/erp/security/role/domain/repository/RoleRepository.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/application/usecase/query/FindRolesUseCase.java` dan `FindRoleByIdUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/application/usecase/command/CreateRoleUseCase.java`, `UpdateRoleUseCase.java`, dan `DeleteRoleUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/infrastructure/persistence/RoleJpaEntity.java`, `RoleJpaRepository.java`, `RolePersistenceMapper.java`, dan `RoleRepositoryAdapter.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/infrastructure/config/RoleConfig.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/web/controller/RoleController.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/web/dto/RoleSaveRequest.java`, `RoleDetailResponse.java`, dan `RoleSummaryResponse.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/role/web/mapper/RoleWebMapper.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/domain/model/RoleTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/application/usecase/command/CreateRoleUseCaseTest.java`, `UpdateRoleUseCaseTest.java`, dan `DeleteRoleUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/application/usecase/query/FindRolesUseCaseTest.java` dan `FindRoleByIdUseCaseTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/web/controller/RoleControllerTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/web/template/RoleTemplateTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/security/role/web/template/integration/RoleListIntegrationTest.java` dan `RoleFormIntegrationTest.java`.

**Acceptance Criteria Phase 3:**
- [x] Domain dan application package `security/role` tidak mengandung import Spring, Lombok, atau JPA.
- [x] Role list/form template test sudah ada dan pass.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 4 - Security: User Slice dan Support Flow

#### User
- [x] Pindahkan `src/main/java/com/solusi/erp/security/model/User.java` dan `UserProfile.java` ke `src/main/java/com/solusi/erp/security/user/domain/model/User.java` dan `UserProfile.java` sebagai domain murni tanpa anotasi Spring / Lombok.
- [x] Buat `src/main/java/com/solusi/erp/security/user/domain/repository/UserRepository.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/application/usecase/query/FindUsersUseCase.java`, `FindUserByIdUseCase.java`, `GetUserEditViewUseCase.java`, `GetProfileUseCase.java`, dan `GetProfileUpdateDataUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/application/usecase/command/CreateUserUseCase.java`, `UpdateUserUseCase.java`, `DeleteUserUseCase.java`, `ToggleUserStatusUseCase.java`, dan `UpdateProfileUseCase.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/infrastructure/persistence/UserJpaEntity.java`, `UserProfileJpaEntity.java`, `UserJpaRepository.java`, `UserProfileJpaRepository.java`, `UserPersistenceMapper.java`, `UserRepositoryAdapter.java`, dan `UserConfig.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/web/controller/UserController.java`, `ProfileController.java`, dan `PasswordResetController.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/web/dto/UserSaveRequest.java`, `UserDetailResponse.java`, `UserSummaryResponse.java`, `ProfileSaveRequest.java`, `ProfileResponse.java`, dan `PasswordResetRequest.java`.
- [x] Buat `src/main/java/com/solusi/erp/security/user/web/mapper/UserWebMapper.java`.
- [x] Pastikan `src/main/java/com/solusi/erp/security/user/infrastructure` memakai `RoleReference` / `roleId` dan `PartyReference` / `partyId`, bukan entity lintas modul.
- [x] Integrasikan `UserDetailsServiceImpl.java`, `CustomAuthenticationSuccessHandler.java`, `ForcePasswordChangeFilter.java`, `LocaleSyncInterceptor.java`, `SystemInitializer.java`, `MenuSearchController.java`, dan `SecurityLookupController.java` ke slice support yang sesuai setelah `User` stabil.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/domain/model/UserTest.java` dan `UserProfileTest.java`.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/application/usecase/command/CreateUserUseCaseTest.java`, `UpdateUserUseCaseTest.java`, `DeleteUserUseCaseTest.java`, `ToggleUserStatusUseCaseTest.java`, dan `UpdateProfileUseCaseTest.java`.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/application/usecase/query/FindUsersUseCaseTest.java`, `FindUserByIdUseCaseTest.java`, `GetUserEditViewUseCaseTest.java`, `GetProfileUseCaseTest.java`, dan `GetProfileUpdateDataUseCaseTest.java`.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/web/controller/UserControllerTest.java`, `ProfileControllerTest.java`, dan `PasswordResetControllerTest.java`.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/web/template/UserTemplateTest.java`, `ProfileTemplateTest.java`, dan `PasswordResetTemplateTest.java`.
- [x] Tambahkan `src/test/java/com/solusi/erp/security/user/web/template/integration/UserListIntegrationTest.java`, `UserFormIntegrationTest.java`, `ProfileIntegrationTest.java`, dan `PasswordResetIntegrationTest.java`.

**Acceptance Criteria Phase 4:**
- [x] Domain dan application package `security/user` tidak mengandung import Spring, Lombok, atau JPA.
- [x] Tidak ada lagi direct import `master.party.domain.model.Party` dari user slice.
- [x] User, profile, dan password reset test sudah ada dan pass.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 5 - Core: Infrastruktur, Middleware, dan Utility

#### Core middleware dan utility independen
- [x] Pindahkan `src/main/java/com/solusi/erp/core/advice/AuditInfoInterceptor.java` ke `src/main/java/com/solusi/erp/core/infrastructure/web/interceptor/AuditInfoInterceptor.java`.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/advice/TableSortingAdvice.java` ke `src/main/java/com/solusi/erp/core/infrastructure/web/advice/TableSortingAdvice.java`.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/config/HtmxViewInterceptor.java` ke `src/main/java/com/solusi/erp/core/infrastructure/web/interceptor/HtmxViewInterceptor.java`.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/util/PageableMapper.java` ke `src/main/java/com/solusi/erp/core/infrastructure/util/PageableMapper.java` dan buat `src/test/java/com/solusi/erp/core/infrastructure/util/PageableMapperTest.java`.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/service/SequenceGeneratorService.java` dan boundary persistence `SystemSequence.java` / `SystemSequenceRepository.java` ke `src/main/java/com/solusi/erp/core/infrastructure/sequence/` atau `persistence/sequence` yang konsisten, lalu buat `SequenceGeneratorServiceTest.java`.
- [x] Tambahkan `AuditInfoInterceptorTest.java`, `TableSortingAdviceTest.java`, dan `HtmxViewInterceptorTest.java`.

#### Core middleware yang bergantung ke security
- [x] Pindahkan `src/main/java/com/solusi/erp/core/advice/GlobalModelAttributeAdvice.java` ke `src/main/java/com/solusi/erp/core/infrastructure/web/advice/GlobalModelAttributeAdvice.java` dan ubah dependensinya agar tidak hard-couple ke entity security.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/pagination/UserPreferencePageableResolver.java` ke `src/main/java/com/solusi/erp/core/infrastructure/web/resolver/UserPreferencePageableResolver.java`.
- [x] Tambahkan `GlobalModelAttributeAdviceTest.java` dan `UserPreferencePageableResolverTest.java`.

**Acceptance Criteria Phase 5:**
- [x] Semua utility/middleware core yang dipindah punya unit test.
- [x] Tidak ada import module-specific yang tidak disepakati di utilitas core.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 6 - Core: Dashboard dan Home Controller

#### Dashboard dan Home
- [x] Pindahkan `src/main/java/com/solusi/erp/core/controller/DashboardController.java` ke `src/main/java/com/solusi/erp/core/web/controller/DashboardController.java`.
- [x] Pindahkan `src/main/java/com/solusi/erp/core/controller/HomeController.java` ke `src/main/java/com/solusi/erp/core/web/controller/HomeController.java`.
- [x] Buat `src/test/java/com/solusi/erp/core/web/controller/DashboardControllerTest.java` dan `HomeControllerTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/core/web/template/DashboardTemplateTest.java` dan `HomeTemplateTest.java`.
- [x] Buat `src/test/java/com/solusi/erp/core/web/template/integration/DashboardIntegrationTest.java` dan `HomeIntegrationTest.java`.
- [x] Pastikan `src/main/resources/templates/dashboard/index.html` dan `src/main/resources/templates/home.html` tetap render dengan `TemplateTestUtils.renderFragment()` / `renderWithSecurity()`.

**Acceptance Criteria Phase 6:**
- [x] Dashboard dan home controller test pass tanpa Spring context penuh.
- [x] Template static check dan integration template check untuk dashboard/home sudah ada dan pass.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 7 - Master: Cleanup master.model Anemic Legacy

Masih ada package `master.model` lama yang berisi JPA entity anemic hasil sebelum
vertical slicing selesai. Package ini harus dibersihkan setelah semua consumer-nya
sudah dipastikan memakai slice baru masing-masing.

#### Audit consumer master.model
- [x] Jalankan `rg "import com.solusi.erp.master.model"` di seluruh codebase dan catat
  semua file yang masih import dari `master.model`.
- [x] Pastikan tidak ada consumer aktif yang masih bergantung ke
  `master.model.BankAccount`, `master.model.Currency`, `master.model.Geographic`,
  `master.model.Party`, `master.model.PartyAddress`, `master.model.PartyContact`,
  `master.model.PartyIdentification`, `master.model.PartyIdentificationType`,
  `master.model.PartyRoleType`, dan `master.model.Tax`.

#### Migrasi JPA entity ke persistence slice masing-masing
- [x] Verifikasi `master/bankaccount/infrastructure/persistence/` sudah punya JPA entity
  sendiri. Jika belum, pindahkan `master.model.BankAccount` ke sana dan hapus dari
  `master.model`.
- [x] Verifikasi `master/currency/infrastructure/persistence/` sudah punya JPA entity
  sendiri. Jika belum, pindahkan `master.model.Currency` ke sana dan hapus dari
  `master.model`.
- [x] Verifikasi `master/geographic/infrastructure/persistence/` sudah punya JPA entity
  sendiri. Jika belum, pindahkan `master.model.Geographic` ke sana dan hapus dari
  `master.model`.
- [x] Verifikasi `master/party/infrastructure/persistence/` sudah punya JPA entity untuk
  `Party`, `PartyAddress`, `PartyContact`, `PartyIdentification`. Jika belum, pindahkan
  dari `master.model` ke sana dan hapus dari `master.model`.
- [x] Verifikasi `master/partyroletype/infrastructure/persistence/` sudah punya JPA entity
  sendiri. Jika belum, pindahkan `master.model.PartyRoleType` ke sana dan hapus dari
  `master.model`.
- [x] Verifikasi `master/tax/infrastructure/persistence/` sudah punya JPA entity sendiri.
  Jika belum, pindahkan `master.model.Tax` ke sana dan hapus dari `master.model`.

#### Hapus master.model package
- [x] Setelah semua entity sudah dipindah dan tidak ada consumer aktif, hapus seluruh
  package `src/main/java/com/solusi/erp/master/model/`.
- [x] Jalankan `rg "import com.solusi.erp.master.model"` sekali lagi — hasilnya harus
  kosong (zero result).
- [x] Jalankan `mvn clean compile` dan pastikan tidak ada error import yang tersisa.

#### shared.model audit
- [x] Audit `master/shared.model` — jika isinya hanya value object atau enum yang
  benar-benar dipakai lintas fitur master, pertahankan. Jika ada class yang hanya
  dipakai satu slice, pindahkan ke slice tersebut dan hapus dari shared.model.

**Acceptance Criteria Phase 7:**
- [x] Package `com.solusi.erp.master.model` sudah tidak ada di codebase.
- [x] `rg "import com.solusi.erp.master.model"` mengembalikan zero result.
- [x] `shared.model` hanya berisi contract yang benar-benar lintas fitur.
- [x] `mvn clean test` pass sebelum lanjut ke phase berikutnya.

### Phase 8 - Security dan Core: Legacy Package Cleanup

#### Security legacy cleanup
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/security/controller/*` yang masih berada di package horizontal lama setelah slice baru aktif.
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/security/service/*` dan `service/impl/*` yang sudah diganti use case / adapter baru.
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/security/repository/*` ke `infrastructure/persistence` dan `infrastructure/adapter`.
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/security/dto/*`, `form/*`, `mapper/*`, dan `model/*` ke package slice baru atau hapus jika sudah tidak dipakai.

#### Core legacy cleanup
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/core/controller/*` ke `core/web/controller`.
- [x] Hapus atau migrasikan class di `src/main/java/com/solusi/erp/core/advice/*`, `config/*`, `pagination/*`, `service/*`, dan `util/*` ke package `core/infrastructure/*`.
- [x] Audit `src/main/java/com/solusi/erp/core/model/*` dan `core/domain/model/*` supaya hanya menyisakan contract universal yang memang dipakai lintas fitur.

#### Verification cleanup
- [x] Pastikan tidak ada lagi import dari package legacy security/core yang dihapus di file-file target baru.
- [x] Pastikan template `src/main/resources/templates/security/*`, `dashboard/index.html`, dan `home.html` masih resolve dari controller baru.

**Acceptance Criteria Phase 7:**
- [x] `rg` pada package legacy security/core tidak menemukan class yang sudah semestinya dipindah.
- [x] Tidak ada import lintas modul yang kembali mengikat ke entity lama.
- [x] `mvn clean test` pass sebelum phase terakhir.

### Phase 9 - Final Regression Gate

#### Final verification
- [x] Jalankan `mvn clean test` sebagai gate utama final.
- [x] Jalankan smoke test Playwright ke halaman kritikal security dan core:
  - `/`
  - `/dashboard`
  - `/security/users`
  - `/security/roles`
  - `/security/permissions`
  - `/security/permission-groups`
- [x] Verifikasi tidak ada Thymeleaf parsing error, sorting error, atau console error pada halaman yang diuji.
- [x] Verifikasi minimal satu test template integration per feature sudah memakai `TemplateTestUtils.renderWithSecurity()` atau `renderFragment()` sesuai kebutuhan.

**Acceptance Criteria Phase 9:**
- [x] Semua test baru dan test existing pass.
- [x] Smoke test halaman kritikal pass.
- [x] Roadmap security/core siap ditutup.

---

## 4. Prinsip Implementasi

Selama roadmap ini berjalan, prinsip yang harus dijaga:

* jangan memindahkan coupling lama ke tempat baru secara diam-diam
* jangan membuat shared package terlalu gemuk
* jangan menambah layer yang tidak perlu
* kalau data dipakai bersama, buat kontraknya jelas
* kalau hanya dipakai satu feature, tetap tinggal di slice feature tersebut
* kalau ada perubahan besar di form/list/autocomplete, sertakan integration test atau smoke test
* jaga supaya build tetap hijau di setiap milestone

---

## 5. Definisi Selesai

Roadmap ini dianggap selesai kalau:

* seluruh fitur security CRUD sudah berada di vertical slice masing-masing
* core hanya menyimpan controller, middleware, dan utility yang memang layak berada di sana
* dependency antar module sudah turun drastis dan mayoritas lewat port, reference id, atau contract yang jelas
* package legacy security/core sudah bersih atau hanya tersisa untuk compatibility yang benar-benar terpaksa
* test suite unit + integration + smoke test sudah cukup untuk menangkap regresi paling umum
* codebase mudah dilanjutkan oleh AI agent maupun developer tanpa perlu menebak boundary feature


# Class Diagram - Security Module (RBAC)

> **Dokumen Modular**: Diagram ini hanya mencakup entitas di dalam modul Security. Modul bisnis lain (Sales, Inventory) akan memiliki dokumen diagramnya masing-masing.

Diagram ini menunjukkan hubungan antara kelas dasar audit dan sistem keamanan berbasis Role dan Permission.

```mermaid
classDiagram
    class BaseModel {
        <<abstract>>
        -Long id
        -Long createdBy
        -User createdByUser
        -LocalDateTime createdDate
        -Long updatedBy
        -User updatedByUser
        -LocalDateTime updatedDate
        -Integer version
    }

    class User {
        -String username
        -String password
        -String email
        -boolean enabled
        -boolean password_change_required
        -Long partyId
        -String partyCode
        -String partyName
        -Role role
        -UserProfile profile
    }

    class Party {
        -String code
        -String name
    }

    class UserProfile {

        -String fullName
        -String phoneNumber
        -String avatarPath
        -String languageCode
        -Integer defaultPageSize
        -String theme
    }

    class Role {
        -String name
        -String description
        -Set~Permission~ permissions
    }

    class Permission {
        -String name
        -String description
    }

    BaseModel <|-- User
    BaseModel <|-- UserProfile
    BaseModel <|-- Role
    BaseModel <|-- Permission

    User "1" -- "1" UserProfile : Has 1 Profile
    User "n" --> "1" Role : Has 1 Role
    Role "n" o-- "m" Permission : Has many Permissions
    User "n" ..> "0..1" Party : partyId reference (decoupled)
```

> **Catatan Arsitektur:** `User` tidak lagi memiliki `@ManyToOne Party`. Relasi ke `Party` sudah didekopel menggunakan reference ID (`partyId`, `partyCode`, `partyName`) untuk memutus coupling langsung antara modul `security` dan `master`. Detail lihat di `security.user.domain.model.User`.

## Penjelasan Struktur:
1.  **BaseModel**: Menyediakan kolom audit untuk semua entitas bisnis.
2.  **User**: Entitas utama pengguna. Memiliki relasi 1-to-1 opsional ke **Party** untuk identifikasi identitas bisnis (misal: untuk alur Approver).
3.  **Role**: Grup akses (Contoh: `ADMIN`, `MANAGER`).
4.  **Permission**: Hak akses halus (Contoh: `INVENTORY_READ`, `SALES_WRITE`).
5.  **Relasi Role-Permission**: Many-to-Many (Gunakan tabel perantara `role_permissions` di database).

## Alur Otorisasi:
1. User login -> `UserDetailsServiceImpl` memuat entitas `User`.
2. `SecurityUser` (Wrapper) dibuat -> Semua Permission dari Role diubah menjadi `GrantedAuthority` seketika (*Pre-calculated*).
3. Authorities disimpan dalam session untuk efisiensi dan stabilitas render UI.
4. Spring Security menggunakan daftar Permission ini untuk mengecek `@PreAuthorize` dan `sec:authorize`.

## Alur Sinkronisasi Preferensi (i18n & UI):
1. **Login Sukses**: `CustomAuthenticationSuccessHandler` mencegat alur setelah autentikasi berhasil.
2. **Ekstraksi Profil**: Mengambil `UserProfile` dari objek `SecurityUser`.
3. **Smart Sync Locale**: `LocaleResolver` melakukan sinkronisasi dua arah:
    * Jika Cookie browser memiliki bahasa berbeda dengan DB (karena user pilih di landing page), maka DB diupdate agar sinkron dengan pilihan user.
    * Jika Cookie browser kosong/default, maka locale dari DB disetel ke browser.
4. **Update Profil**: Saat user mengubah preferensi di halaman Profil, `ProfileController` memperbarui database sekaligus memperbarui Cookie browser secara *real-time*.

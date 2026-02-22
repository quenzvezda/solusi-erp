# Entity Relationship Diagram (ERD) - Security & RBAC Module

Diagram ini merincikan struktur tabel database yang akan diimplementasikan melalui script migrasi Flyway.

```mermaid
erDiagram
    users {
        bigint id PK
        varchar username UK
        varchar password
        varchar email UK
        boolean enabled
        boolean password_change_required "default: true"
        datetime last_password_change
        bigint role_id FK
        varchar created_by
        datetime created_date
        varchar updated_by
        datetime updated_date
        int version
    }

    user_profiles {
        bigint id PK
        bigint user_id FK "Unique (1-to-1)"
        varchar full_name
        varchar phone_number
        varchar avatar_path
        varchar language_code "default: 'id'"
        int default_page_size "default: 10"
        varchar theme "default: 'light'"
        varchar created_by
        datetime created_date
        varchar updated_by
        datetime updated_date
        int version
    }

    roles {
        bigint id PK
        varchar name UK "e.g. ROLE_ADMIN"
        varchar description
        varchar created_by
        datetime created_date
        varchar updated_by
        datetime updated_date
        int version
    }

    permissions {
        bigint id PK
        varchar name UK "e.g. USER_READ"
        varchar description
        varchar created_by
        datetime created_date
        varchar updated_by
        datetime updated_date
        int version
    }

    role_permissions {
        bigint role_id PK, FK
        bigint permission_id PK, FK
    }

    users }o--|| roles : "Belongs to 1 Role"
    users ||--|| user_profiles : "Has 1 Profile (1-to-1)"
    roles ||--o{ role_permissions : "Has many permissions"
    permissions ||--o{ role_permissions : "Assigned to many roles"
```

## Detail Tabel & Constraints:
1.  **Standard Columns**: Kolom `id`, `created_by`, `created_date`, `updated_by`, `updated_date`, dan `version` adalah kolom standar yang diwarisi dari `BaseModel` di kode Java.
2.  **users**: Kolom `username` dan `email` harus **UNIQUE**. `role_id` tidak boleh null.
2.  **user_profiles**: Kolom `user_id` memiliki **UNIQUE constraint** untuk menjamin relasi 1-to-1.
3.  **role_permissions**: Tabel perantara (junction table) untuk relasi Many-to-Many antara Role dan Permission.
4.  **Audit Columns**: Semua tabel (kecuali junction table) wajib memiliki kolom audit sesuai standar `BaseModel`.

## Strategi Otorisasi:
*   **DASHBOARD_READ**: Permission universal agar user bisa mengakses landing page.
*   **Granular Access**: Konten di dalam dashboard (chart, summary) dirender secara dinamis di level Thymeleaf menggunakan `sec:authorize` berdasarkan permission spesifik lainnya.
*   **Naming Convention**: 
    *   `MODUL-NAME_ACTION` (Dash untuk modul, Underscore untuk aksi).
    *   Contoh: `SALES-ORDER_READ`, `SALES-ORDER_CREATE`.

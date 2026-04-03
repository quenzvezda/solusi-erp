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
        bigint party_id "Unique, Nullable — reference only"
        bigint created_by_user_id FK "→ users"
        datetime created_date
        bigint updated_by_user_id FK "→ users"
        datetime updated_date
        int version
    }

    user_profiles {
        bigint id PK
        bigint user_id FK "Unique (1-to-1)"
        varchar full_name
        varchar phone_number
        varchar avatar_path
        varchar language_code "default: id"
        int default_page_size "default: 10"
        varchar theme "default: light"
        bigint created_by_user_id FK "→ users"
        datetime created_date
        bigint updated_by_user_id FK "→ users"
        datetime updated_date
        int version
    }

    roles {
        bigint id PK
        varchar name UK
        varchar description
        bigint created_by_user_id FK "→ users"
        datetime created_date
        bigint updated_by_user_id FK "→ users"
        datetime updated_date
        int version
    }

    permissions {
        bigint id PK
        varchar name UK
        varchar description
        bigint created_by_user_id FK "→ users"
        datetime created_date
        bigint updated_by_user_id FK "→ users"
        datetime updated_date
        int version
    }

    role_permissions {
        bigint role_id PK
        bigint permission_id PK
    }

    users }o--|| roles : "Belongs to 1 Role"
    users ||--|| user_profiles : "Has 1 Profile"
    roles ||--o{ role_permissions : "Has many permissions"
    permissions ||--o{ role_permissions : "Assigned to many roles"
```

## Detail Tabel & Constraints:
1.  **Standard Columns**: Kolom `id` adalah primary key. Kolom audit `created_by_user_id` dan `updated_by_user_id` adalah `BIGINT` yang merupakan FK ke tabel `users`, diisi otomatis oleh Spring Data JPA Auditing via `BaseModel`.
2.  **users**: Kolom `username` dan `email` harus **UNIQUE**. `role_id` tidak boleh null.
2.  **user_profiles**: Kolom `user_id` memiliki **UNIQUE constraint** untuk menjamin relasi 1-to-1.
3.  **role_permissions**: Tabel perantara (junction table) untuk relasi Many-to-Many antara Role dan Permission.
4.  **Audit Columns**: Semua tabel (kecuali junction table) wajib memiliki kolom audit sesuai standar `BaseModel`.
5.  **Party Decoupling**: Kolom `party_id` di tabel `users` hanya menyimpan FK sebagai referensi ID. Tidak ada `@ManyToOne` langsung ke entity `Party` di level domain — ini adalah pola cross-module decoupling yang disengaja.

## Strategi Otorisasi:
*   **DASHBOARD_READ**: Permission universal agar user bisa mengakses landing page.
*   **Granular Access**: Konten di dalam dashboard (chart, summary) dirender secara dinamis di level Thymeleaf menggunakan `sec:authorize` berdasarkan permission spesifik lainnya.
*   **Naming Convention**: 
    *   `MODUL-NAME_ACTION` (Dash untuk modul, Underscore untuk aksi).
    *   Contoh: `SALES-ORDER_READ`, `SALES-ORDER_CREATE`.

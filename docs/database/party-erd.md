# Entity Relationship Diagram (ERD) - Business Partner Module

Diagram ini merincikan struktur tabel fisik di database untuk modul Business Partner.

```mermaid
erDiagram
    party_role_types {
        bigint id PK
        varchar code UK
        varchar name
    }

    party_id_types {
        bigint id PK
        varchar code UK
        varchar name
    }

    parties {
        bigint id PK
        varchar code UK "Auto-generated BP-XXXXX"
        varchar name
        enum type "PERSON, ORGANIZATION"
        text notes
        boolean is_active
        varchar email
        varchar phone
        varchar created_by
        datetime created_date
        int version
    }

    party_roles {
        bigint id PK
        bigint party_id FK
        bigint role_type_id FK
    }

    party_identifications {
        bigint id PK
        bigint party_id FK
        bigint id_type_id FK
        varchar id_number
        date issued_date
        date expiry_date
    }

    party_addresses {
        bigint id PK
        bigint party_id FK
        enum type "MAIN, BILLING, SHIPPING, OTHER"
        text address_line1
        varchar city
        varchar province
        varchar postal_code
        varchar country
    }

    parties ||--o{ party_roles : "mapped by"
    party_role_types ||--o{ party_roles : "is assigned to"
    parties ||--o{ party_identifications : "has many"
    party_id_types ||--o{ party_identifications : "defines"
    parties ||--o{ party_addresses : "has many"
```

## Spesifikasi Teknis:
1.  **Integritas Data**: Semua relasi menggunakan `FOREIGN KEY`. Tabel anak (`party_addresses`, `party_identifications`, `party_roles`) menggunakan `ON DELETE CASCADE` untuk menjaga kebersihan data jika Party dihapus.
2.  **Unique Constraints**: 
    *   `parties.code` bersifat unik.
    *   `party_role_types.code` dan `party_id_types.code` bersifat unik untuk lookup yang stabil di kode Java.
3.  **Auditing**: Tabel utama dan tabel detail memiliki kolom `created_by`, `created_date`, dan `version` untuk audit trail (mengikuti `BaseModel`).

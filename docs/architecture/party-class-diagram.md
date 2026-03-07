# Class Diagram - Business Partner (Party) Module

Dokumen ini menjelaskan struktur kelas dan hubungan antar entitas dalam modul Business Partner yang menggunakan pola **Universal Party Model**.

```mermaid
classDiagram
    class BaseModel {
        <<abstract>>
        -Long id
        -String createdBy
        -LocalDateTime createdDate
        -String updatedBy
        -LocalDateTime updatedDate
        -Integer version
    }

    class Party {
        -String code
        -String name
        -PartyType type
        -String notes
        -String email
        -String phone
        -Boolean isActive
        -Set~PartyRoleType~ roles
        -List~PartyIdentification~ identifications
        -List~PartyAddress~ addresses
    }

    class PartyRoleType {
        -String code
        -String name
    }

    class PartyIdentificationType {
        -String code
        -String name
    }

    class PartyIdentification {
        -PartyIdentificationType type
        -String idNumber
        -LocalDate issuedDate
        -LocalDate expiryDate
    }

    class PartyAddress {
        -AddressType type
        -String addressLine1
        -String city
        -String province
        -String postalCode
        -String country
    }

    class PartyType {
        <<enumeration>>
        PERSON
        ORGANIZATION
    }

    class AddressType {
        <<enumeration>>
        MAIN
        BILLING
        SHIPPING
        OTHER
    }

    BaseModel <|-- Party
    BaseModel <|-- PartyRoleType
    BaseModel <|-- PartyIdentificationType
    BaseModel <|-- PartyIdentification
    BaseModel <|-- PartyAddress

    Party "1" *-- "n" PartyIdentification : composition
    Party "1" *-- "n" PartyAddress : composition
    Party "n" -- "m" PartyRoleType : many-to-many
    PartyIdentification "n" -- "1" PartyIdentificationType : reference
```

## Komponen Utama:
1.  **Party**: Entitas pusat. Satu baris mewakili satu individu atau satu organisasi fisik.
2.  **PartyRoleType**: Menentukan peran entitas tersebut (misal: Supplier, Customer). Relasi Many-to-Many memungkinkan satu PT menjadi Supplier sekaligus Customer.
3.  **PartyIdentification**: Menyimpan dokumen legal. Menggunakan `orphanRemoval=true` sehingga jika baris dihapus di UI, data di DB otomatis terhapus.
4.  **PartyAddress**: Mendukung multi-alamat untuk satu entitas (Kantor Pusat, Gudang, Alamat Tagihan).

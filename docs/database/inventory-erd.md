# Entity Relationship Diagram (ERD) - Inventory Module

```mermaid
erDiagram
    product_categories {
        bigint id PK
        varchar code UK "Auto-generated"
        varchar name
        enum type "STOCK, NON_STOCK, SERVICE"
        text note
        varchar created_by
        datetime created_date
        varchar updated_by
        datetime updated_date
        int version
    }
```

## Detail Standarisasi:
1.  **Auto Code**: Kolom `code` tidak diinput manual, melainkan di-generate oleh `SequenceGeneratorService`.
2.  **Soft Read-Only**: Di level UI, kolom `code` ditampilkan sebagai `readonly` dengan background `bg-light`.
3.  **Auditing**: Mengikuti standar `BaseModel`.

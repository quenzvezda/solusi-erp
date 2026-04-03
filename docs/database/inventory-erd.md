# Entity Relationship Diagram (ERD) - Inventory Module

```mermaid
erDiagram
    product_categories {
        bigint id PK
        varchar code UK
        varchar name
        enum type "STOCK, NON_STOCK, SERVICE"
        text note
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    unit_of_measures {
        bigint id PK
        varchar code UK
        varchar name
        enum type "WEIGHT, LENGTH, UNIT, VOLUME, TIME, AREA"
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_brands {
        bigint id PK
        varchar code UK
        varchar name
        text note
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_products {
        bigint id PK
        varchar code UK
        varchar name
        varchar barcode
        text note
        varchar hscode
        boolean is_active
        boolean is_serialized
        bigint category_id FK
        bigint base_uom_id FK
        bigint brand_id FK
        decimal min_stock
        decimal max_stock
        decimal weight_net
        decimal weight_gross
        bigint weight_uom_id FK
        decimal length
        decimal width
        decimal height
        bigint dimension_uom_id FK
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_facilities {
        bigint id PK
        varchar code UK
        varchar name
        bigint owner_id "reference to parties (decoupled)"
        varchar address_line1
        bigint city_id "reference to geographics (decoupled)"
        varchar postal_code
        text note
        boolean is_active
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_grids {
        bigint id PK
        bigint facility_id FK
        varchar code
        varchar name
        text note
        boolean is_active
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_containers {
        bigint id PK
        bigint grid_id FK
        varchar code
        varchar name
        decimal capacity
        text note
        boolean is_active
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_stock_adjustments {
        bigint id PK
        varchar code UK
        date transaction_date
        bigint facility_id FK
        enum status "DRAFT, COMPLETED"
        text note
        bigint currency_id "reference to master_currencies (decoupled)"
        decimal total_exchange_rate
        decimal total_amount_original
        decimal total_amount_local
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_stock_adjustment_lines {
        bigint id PK
        bigint header_id FK
        bigint product_id FK
        bigint grid_id FK
        bigint container_id FK
        varchar serial_number
        decimal quantity
        decimal unit_cost
        bigint created_by_user_id FK
        datetime created_date
        int version
    }

    inv_stock_balances {
        bigint id PK
        bigint product_id FK
        bigint container_id FK
        varchar serial_number
        decimal qty_on_hand
        decimal qty_reserved
        bigint created_by_user_id FK
        datetime created_date
        bigint updated_by_user_id FK
        datetime updated_date
        int version
    }

    inv_movements {
        bigint id PK
        datetime transaction_date
        bigint product_id FK
        bigint container_id FK
        varchar serial_number
        decimal quantity
        enum movement_type
        enum reference_type
        bigint reference_id
        bigint currency_id "reference (decoupled)"
        decimal exchange_rate
        decimal unit_cost_original
        decimal unit_cost_local
        bigint created_by_user_id FK
        datetime created_date
        int version
    }

    inv_valuation_layers {
        bigint id PK
        bigint product_id FK
        bigint container_id FK
        decimal qty_remaining
        bigint currency_id "reference (decoupled)"
        decimal exchange_rate
        decimal unit_cost_original
        decimal unit_cost_local
        datetime received_at
        bigint created_by_user_id FK
        datetime created_date
        int version
    }

    inv_products ||--o{ inv_stock_adjustment_lines : "adjusted"
    inv_products ||--o{ inv_stock_balances : "on-hand balance"
    inv_products ||--o{ inv_movements : "movement history"
    inv_products ||--o{ inv_valuation_layers : "FIFO layers"
    inv_products }o--|| product_categories : "belongs to"
    inv_products }o--|| unit_of_measures : "base UoM"
    inv_products }o--o| inv_brands : "brand (optional)"
    inv_facilities ||--o{ inv_grids : "has grids"
    inv_grids ||--o{ inv_containers : "has containers"
    inv_containers ||--o{ inv_stock_balances : "physical location"
    inv_containers ||--o{ inv_movements : "movement location"
    inv_containers ||--o{ inv_valuation_layers : "valuation location"
    inv_stock_adjustments ||--o{ inv_stock_adjustment_lines : "has lines"
    inv_stock_adjustments }o--|| inv_facilities : "in facility"
    inv_stock_adjustment_lines }o--o| inv_grids : "in grid"
    inv_stock_adjustment_lines }o--o| inv_containers : "in container"
```

## Detail Standarisasi:
1.  **Auto Code**: Kolom `code` tidak diinput manual, melainkan di-generate oleh `SequenceGeneratorService`.
2.  **Soft Read-Only**: Di level UI, kolom `code` ditampilkan sebagai `readonly` dengan background `bg-light`.
3.  **Auditing**: Semua tabel mengikuti standar `BaseModel` — kolom audit adalah `created_by_user_id` dan `updated_by_user_id` (BIGINT FK ke `users`).
4.  **Cross-Module Decoupling**: `inv_facilities.owner_id` → referensi ke `parties`; `CurrencyAmount` embeddable menyimpan `currency_id` (Long) bukan FK langsung ke `master_currencies`. Ini adalah pola cross-module yang disengaja untuk memutus coupling.
5.  **Stock Status**: `inv_stock_balances.qty_on_hand` dan `qty_reserved` menentukan `qty_available = qty_on_hand - qty_reserved`.
6.  **FIFO Valuation**: `inv_valuation_layers` menyimpan batch masuk. Saat stok keluar, layer dikonsumsi dari yang terlama (First In First Out).


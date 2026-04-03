# Modul: Product (Inventory)

## Ringkasan

Modul `inventory.product` mengelola master data produk yang akan digunakan di seluruh proses inventory (stock adjustment, movement, valuation). Product adalah **aggregate root** dalam domain inventory.

**Package Path**: `com.solusi.erp.inventory.product`

---

## 1. Struktur Clean Architecture

```
inventory.product
├── domain
│   ├── model
│   │   └── Product.java          ← Pure domain model
│   └── repository
│       └── ProductRepository.java
├── application
│   └── usecase
│       ├── command
│       │   ├── CreateProductUseCase.java
│       │   └── UpdateProductUseCase.java
│       └── query
│           ├── GetProductUseCase.java
│           └── GetProductListUseCase.java
├── infrastructure
│   ├── persistence
│   │   ├── ProductEntity.java       ← JPA entity (extends BaseModel)
│   │   ├── ProductJpaRepository.java
│   │   └── ProductPersistenceMapper.java
│   ├── adapter
│   │   └── ProductRepositoryAdapter.java
│   └── config
│       └── ProductConfig.java       ← @Bean wiring (TransactionTemplate)
└── web
    ├── controller
    │   └── ProductController.java
    ├── dto
    │   ├── request
    │   │   ├── CreateProductRequest.java
    │   │   └── UpdateProductRequest.java
    │   └── response
    │       └── ProductResponse.java   (extends BaseAuditResponse)
    └── mapper
        └── ProductWebMapper.java      ← MapStruct abstract class
```

---

## 2. Domain Model (`Product.java`)

```java
public class Product {
    private AuditMetadata metadata;
    private String code;           // Auto-generated, unique
    private String name;
    private String barcode;
    private String note;
    private Long categoryId;       // reference to product_categories
    private Long uomId;            // reference to unit_of_measures (base UoM)
    private Long brandId;          // reference to inv_brands (optional)
    private String hscode;         // HS Code untuk ekspor/impor
    private boolean isActive;
    private boolean isSerialized;  // apakah produk ini menggunakan serial number
    private BigDecimal minStock;
    private BigDecimal maxStock;
    // Berat & Dimensi (opsional, untuk logistik):
    private BigDecimal weightNet;
    private BigDecimal weightGross;
    private Long weightUomId;      // reference ke unit_of_measures (tipe WEIGHT)
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Long dimensionUomId;   // reference ke unit_of_measures (tipe LENGTH)
}
```

---

## 3. Field Reference (semua ID tidak di-join, hanya disimpan sebagai Long)

| Field | Table Target | Catatan |
|-------|-------------|---------|
| `categoryId` | `product_categories` | Required |
| `uomId` | `unit_of_measures` | Base UoM (satuan pokok); Required |
| `brandId` | `inv_brands` | Optional |
| `weightUomId` | `unit_of_measures` | Harus tipe `WEIGHT` |
| `dimensionUomId` | `unit_of_measures` | Harus tipe `LENGTH` |

---

## 4. Kode Produk (Auto-Generate)

Kode produk **tidak diinput manual**. Dibuat otomatis oleh `SequenceGeneratorService` dengan format:
```
PRD-[YYYY]-[NNNNNN]
Contoh: PRD-2025-000001
```

Di form UI, kolom `code` ditampilkan sebagai `readonly` dengan class `bg-light`.

---

## 5. Permissions

| Kode Permission | Deskripsi |
|-----------------|-----------|
| `PRODUCT_VIEW` | Melihat daftar & detail produk |
| `PRODUCT_CREATE` | Membuat produk baru |
| `PRODUCT_EDIT` | Mengedit produk yang sudah ada |

> Semua permission di-seed melalui `DataInitializer` dan di-assign ke role yang sesuai.

---

## 6. Business Rules

1. **Kode unik**: `code` tidak boleh duplikat, divalidasi sebelum persist.
2. **UoM Conversion**: Untuk produk yang diperjualbelikan dalam satuan berbeda dari satuan pokok, harus dibuat konversi di `inv_product_uom_conversions`. Konversi adalah relasi Many-to-One ke `unit_of_measures`.
3. **Serialized Product**: Jika `isSerialized = true`, setiap unit produk harus memiliki `serialNumber` yang unik di setiap transaksi. Tidak boleh ada dua baris dengan `product_id` + `serial_number` yang sama dalam `inv_stock_balances`.
4. **Min/Max Stock**: `minStock` dan `maxStock` bersifat informasional, digunakan sebagai acuan peringatan stok di dashboard.
5. **Soft logic untuk non-aktif**: Produk yang di-deactivate (`isActive = false`) tidak bisa dipilih di transaksi baru. Stok yang sudah ada tidak berubah.

---

## 7. Endpoint API

| Method | Endpoint | Permission | Deskripsi |
|--------|----------|-----------|-----------|
| GET | `/inventory/products` | `PRODUCT_VIEW` | List produk (paginasi) |
| GET | `/inventory/products/{id}` | `PRODUCT_VIEW` | Detail produk |
| POST | `/inventory/products` | `PRODUCT_CREATE` | Buat produk baru |
| PUT | `/inventory/products/{id}` | `PRODUCT_EDIT` | Update produk |

---

## 8. Lihat Juga

- [`docs/database/inventory-erd.md`](../../database/inventory-erd.md) — ERD tabel `inv_products`
- [`docs/architecture/inventory-class-diagram.md`](../../architecture/inventory-class-diagram.md) — Class diagram domain
- [`docs/modules/inventory/brand.md`](./brand.md) — Modul Brand
- [`docs/modules/master/uom.md`](../master/uom.md) — Unit of Measure
- [`docs/modules/master/product-category.md`](../master/product-category.md) — Kategori Produk

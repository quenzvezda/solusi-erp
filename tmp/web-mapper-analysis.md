# WebMapper & Pre-Edit Data Analysis — SupplierPriceList

> **Tujuan**: Analisis masalah arsitektur pada `SupplierPriceListWebMapper` terkait pengambilan data lintas slice untuk halaman pre-edit, beserta opsi perbaikan yang terstruktur.

---

## 1. Ringkasan Masalah

Terdapat **3 masalah berlapis** pada slice `purchasing.supplierpricelist` yang perlu diperbaiki:

### Masalah 1 — WebMapper Melanggar Batas Slice (Anti-Pattern)

**File**: `web/mapper/SupplierPriceListWebMapper.java`

```java
// ❌ ANTI-PATTERN: @Mapper di web layer menginjek JPA repo dari slice lain langsung
@Autowired protected JpaProductRepository productRepository;
@Autowired protected UomJpaRepository uomRepository;
@Autowired protected CurrencyJpaRepository currencyRepository;
@Autowired protected PartyJpaRepository partyRepository;

@Named("getProductName")
protected String getProductName(Long id) {
    return productRepository.findById(id).map(p -> p.getName()).orElse(null);
}
```

**Pelanggaran:**
- `web/mapper/` mengimport JPA repository dari slice `inventory.product`, `inventory.uom`, `master.currency`, `master.party`.
- Tidak melalui port abstraksi (`LookupProvider`) yang sudah tersedia.
- Format `subText` (misal: kode party + tipe party) didefinisikan ulang di sini, bukan bersumber dari `PartyLookupProviderImpl` yang sudah ada.
- Jika format subText `Party` berubah di `PartyLookupProviderImpl`, WebMapper SPL **tidak ikut berubah** — inkonsistensi antar halaman.

**Standar yang berlaku** (dari `docs/architecture/clean-ddd-cqrs-standard.md §8`):
> `infrastructure/adapter/` adalah **satu-satunya tempat** yang boleh import `JpaRepository` slice lain.

---

### Masalah 2 — `SupplierPriceListSaveRequest` Tidak Membawa `subText` (Trinity Data Violation)

**File**: `web/dto/SupplierPriceListSaveRequest.java`

```java
// ✅ Ada name untuk initialText
private String supplierName;
private String productName;
private String uomName;
private String currencyName;

// ❌ Tidak ada subText/code untuk initialSubtext
// Missing: supplierCode, productCode, uomCode, currencyCode
```

**Standar yang berlaku** (dari `docs/spec/autocomplete-generic.md §2B`):
> **Aturan Wajib (Trinity Data):** Setiap autocomplete **WAJIB** menyertakan tiga data:
> 1. `initialValue` — ID  
> 2. `initialText` — Nama utama  
> 3. `initialSubtext` — Kode/informasi sekunder

Karena `subText` tidak ada, template hanya bisa meneruskan `initialValue` + `initialText`, sehingga dropdown TomSelect pada halaman edit **menampilkan nama tanpa kode** (tidak konsisten dengan tampilan hasil search autocomplete yang menampilkan keduanya).

---

### Masalah 3 — Template Tidak Meneruskan `initialSubtext`

**File**: `templates/purchasing/supplier-price-lists/form.html`

```html
<!-- ❌ Semua autocomplete hanya punya 2 dari 3 Trinity Data -->
<div th:replace="~{fragments/inputs :: autocomplete(field='supplierId', ...,
    initialValue=${splRequest.supplierId}, initialText=${splRequest.supplierName})}"></div>

<div th:replace="~{fragments/inputs :: autocomplete(field='productId', ...,
    initialValue=${splRequest.productId}, initialText=${splRequest.productName})}"></div>
```

Bandingkan dengan **Facility form** (pattern yang benar):
```html
<!-- ✅ Semua 3 Trinity Data hadir -->
<div th:replace="~{fragments/inputs :: autocomplete(field='ownerId', ...,
    initialValue=${facilityRequest.ownerId},
    initialText=${facilityUI.ownerName},
    initialSubtext=${facilityUI.ownerCode})}"></div>
```

---

### Masalah 4 — `UomLookupProvider` dan `CurrencyLookupProvider` Belum Ada

LookupProvider yang sudah ada:
| Slice | Port | Ada? |
|-------|------|------|
| `master.party` | `PartyLookupProvider` | ✅ |
| `inventory.product` | `ProductLookupProvider` | ✅ |
| `inventory.brand` | `BrandLookupProvider` | ✅ |
| `inventory.facility` | `FacilityLookupProvider` | ✅ |
| `inventory.grid` | `GridLookupProvider` | ✅ |
| `inventory.productcategory` | `ProductCategoryLookupProvider` | ✅ |
| `master.geographic` | `GeographicLookupProvider` | ✅ |
| `security.role` | `RoleLookupProvider` | ✅ |
| `accounting.coa` | `CoaLookupProvider` | ✅ |
| `inventory.uom` | `UomLookupProvider` | ❌ **Belum ada** |
| `master.currency` | `CurrencyLookupProvider` | ❌ **Belum ada** |

---

## 2. Opsi Perbaikan

### Opsi A — MapStruct Mapper + LookupProvider Injection *(Migrasi Ringan)*

Tetap menggunakan pendekatan MapStruct di `SupplierPriceListWebMapper`, tapi ganti injeksi JPA repo dengan LookupProvider port.

**Perubahan:**
1. Buat `UomLookupProvider` dan `CurrencyLookupProvider` (port + impl + wire di config).
2. Di `SupplierPriceListWebMapper`: ganti `@Autowired JpaProductRepository` → `@Autowired ProductLookupProvider`, dst.
3. Tambah field `productCode`, `supplierCode`, `uomCode`, `currencyCode` di `SupplierPriceListSaveRequest`.
4. Tambah `@Mapping` di mapper untuk memetakan `subText` dari LookupDto ke field `xxxCode`.
5. Update template untuk meneruskan `initialSubtext`.

**Contoh:**
```java
// ✅ Mapper setelah diperbaiki — tidak ada JPA repo langsung
@Autowired protected ProductLookupProvider productLookupProvider;
@Autowired protected PartyLookupProvider partyLookupProvider;
// ...

@Named("getProductCode")
protected String getProductCode(Long id) {
    LookupDto dto = productLookupProvider.resolve(id);
    return dto != null ? dto.subText() : null;
}
```

**Pro:**
- Perubahan minimal, MapStruct tetap menangani mapping otomatis.
- Konsisten — subText bersumber dari LookupProvider (single source of truth).

**Kontra:**
- MapStruct `@Mapper` (web layer) masih menyentuh lintas slice melalui port injection.
- Setiap field nama/kode memerlukan `@Named` method terpisah → mapper makin panjang.
- Potensi N+1 query pada list view (setiap record memanggil `resolve()` per referensi).

---

### Opsi B — Controller `buildSPLUI()` + LookupProvider *(Pattern Standar — Direkomendasikan)*

Ikuti pattern `FacilityController` secara penuh. Pisahkan **data form** (IDs, values) dari **data display** (nama, kode untuk autocomplete pre-fill).

**Perubahan:**
1. Buat `UomLookupProvider` dan `CurrencyLookupProvider`.
2. **Bersihkan `SupplierPriceListSaveRequest`**: hapus semua field `xxxName` (tidak dibutuhkan karena AJAX form, display label tidak di-POST).
3. **`SupplierPriceListWebMapper`**: hapus semua injeksi JPA repo dan LookupProvider. Mapper kembali menjadi pure field mapping (source → target saja).
4. **`SupplierPriceListController`**: tambahkan `buildSPLUI(domain)` method yang memanggil LookupProvider port, hasilnya di-`model.addAttribute("splUI", ...)`.
5. Wire LookupProvider di `SupplierPriceListConfig`.
6. Update template untuk menggunakan `${splUI.productName}`, `${splUI.productCode}`, dst.

**Contoh Controller:**
```java
// Di SupplierPriceListController
private final ProductLookupProvider productLookupProvider;
private final PartyLookupProvider partyLookupProvider;
private final UomLookupProvider uomLookupProvider;
private final CurrencyLookupProvider currencyLookupProvider;

@GetMapping("/edit/{id}")
public String showEditForm(@PathVariable Long id, Model model) {
    SupplierPriceList domain = getSupplierPriceListEditViewUseCase.execute(id)
        .orElseThrow(() -> new RuntimeException("Not found"));
    model.addAttribute("splRequest", webMapper.toSaveRequest(domain));
    model.addAttribute("splUI", buildSPLUI(domain));   // display data, terpisah
    model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
    return "purchasing/supplier-price-lists/form";
}

private Map<String, Object> buildSPLUI(SupplierPriceList domain) {
    Map<String, Object> ui = new HashMap<>();
    LookupDto product = productLookupProvider.resolve(domain.getProductId());
    ui.put("productName",   product  != null ? product.name()    : "");
    ui.put("productCode",   product  != null ? product.subText() : "");

    LookupDto supplier = partyLookupProvider.resolve(domain.getSupplierId());
    ui.put("supplierName",  supplier != null ? supplier.name()    : "");
    ui.put("supplierCode",  supplier != null ? supplier.subText() : "");

    LookupDto uom = uomLookupProvider.resolve(domain.getUomId());
    ui.put("uomName",       uom      != null ? uom.name()    : "");
    ui.put("uomCode",       uom      != null ? uom.subText() : "");

    LookupDto currency = currencyLookupProvider.resolve(domain.getCurrencyId());
    ui.put("currencyName",  currency != null ? currency.name()    : "");
    ui.put("currencyCode",  currency != null ? currency.subText() : "");
    return ui;
}
```

**Contoh Template (setelah diperbaiki):**
```html
<div th:replace="~{fragments/inputs :: autocomplete(field='productId', label=#{label.spl.product}, required=true, path='inventory/products',
    initialValue=${splRequest.productId},
    initialText=${splUI != null ? splUI.productName : ''},
    initialSubtext=${splUI != null ? splUI.productCode : ''})}"></div>

<div th:replace="~{fragments/inputs :: autocomplete(field='supplierId', label=#{label.spl.supplier}, required=true, path='parties',
    initialValue=${splRequest.supplierId},
    initialText=${splUI != null ? splUI.supplierName : ''},
    initialSubtext=${splUI != null ? splUI.supplierCode : ''})}"></div>
```

**Contoh Config Wiring:**
```java
// Di SupplierPriceListConfig.java
@Bean
public UomLookupProvider uomLookupProvider(UomJpaRepository uomJpaRepository) {
    return new UomLookupProviderImpl(uomJpaRepository);
}

@Bean
public CurrencyLookupProvider currencyLookupProvider(CurrencyJpaRepository currencyJpaRepository) {
    return new CurrencyLookupProviderImpl(currencyJpaRepository);
}
```

**Pro:**
- ✅ Identik dengan pattern `FacilityController` — standar baku codebase.
- ✅ WebMapper menjadi murni field mapping, tanpa cross-slice dependency apapun.
- ✅ LookupProvider adalah kontrak — format `subText` konsisten di autocomplete search DAN di form pre-fill.
- ✅ Ganti format Party di `PartyLookupProviderImpl` → otomatis berubah di SPL form juga.
- ✅ `SupplierPriceListSaveRequest` bersih — hanya field yang relevan untuk save.
- ✅ Unit test controller bisa mock LookupProvider, tidak butuh JPA context.

**Kontra:**
- Perubahan lebih banyak file (controller, mapper, DTO, template, config).
- Untuk list view (`toSummaryResponse`), mapper masih perlu menampilkan nama — perlu solusi terpisah (lihat Catatan di bawah).

---

### Opsi C — Hybrid: Clean WebMapper untuk List, Controller UI Builder untuk Edit *(Kompromi)*

Gabungkan kedua pendekatan:
- **List view** (`toSummaryResponse`, `toDetailResponse`): WebMapper tetap inject LookupProvider (bukan JPA repo langsung) untuk resolusi nama di tabel list. Ini wajar karena list memang butuh nama tampil di setiap baris.
- **Edit form**: Controller `buildSPLUI()` menggunakan LookupProvider (persis Opsi B).

Ini adalah pendekatan yang **paling pragmatis** — mapper bersih dari JPA repo langsung (ganti ke port), edit form dapat Trinity Data lengkap.

---

## 3. Rekomendasi

### Pilihan Terbaik: **Opsi B** (atau **Opsi C** jika list view perlu nama di tabel)

| Kriteria | Opsi A | Opsi B | Opsi C |
|----------|--------|--------|--------|
| WebMapper bersih dari cross-slice | ⚠️ Sebagian | ✅ Penuh | ✅ Sebagian |
| Trinity Data lengkap di edit form | ✅ | ✅ | ✅ |
| Konsisten dgn FacilityController | ❌ | ✅ | ✅ |
| Jumlah perubahan | Kecil | Sedang | Sedang |
| SubText centralized (1 tempat) | ✅ | ✅ | ✅ |
| Testable tanpa JPA | ❌ | ✅ | ✅ |

Jika fitur lain juga akan menggunakan `UomLookupProvider` atau `CurrencyLookupProvider` di masa depan (sangat besar kemungkinannya, karena UOM dan Currency dipakai di banyak transaksi), maka **Opsi B** adalah investasi terbaik.

---

## 4. Checklist Implementasi (untuk Opsi B)

### Langkah 1 — Buat LookupProvider yang Belum Ada

```
inventory.uom/
  domain/port/UomLookupProvider.java          ← new
  infrastructure/adapter/UomLookupProviderImpl.java   ← new
  infrastructure/config/UomConfig.java        ← tambahkan @Bean

master.currency/
  domain/port/CurrencyLookupProvider.java     ← new
  infrastructure/adapter/CurrencyLookupProviderImpl.java  ← new
  infrastructure/config/CurrencyConfig.java   ← tambahkan @Bean
```

Kontrak minimal:
```java
public interface UomLookupProvider {
    LookupDto resolve(Long uomId);
    // Returns: id, name, subText (= code)
}

public interface CurrencyLookupProvider {
    LookupDto resolve(Long currencyId);
    // Returns: id, name, subText (= code/symbol)
}
```

### Langkah 2 — Bersihkan `SupplierPriceListWebMapper`

Hapus:
- Semua `@Autowired` JPA repo dari slice lain
- Semua `@Named("getXxxName/Code")` method
- `@Mapping(target = "xxxName", ...)` dari `toSaveRequest`

Kembalikan ke pure MapStruct (hanya `@Mapping` untuk field-field non-name).

### Langkah 3 — Bersihkan `SupplierPriceListSaveRequest`

Hapus field: `supplierName`, `productName`, `uomName`, `currencyName`
(Data ini bukan bagian dari request body; hanya dibutuhkan untuk display di form).

### Langkah 4 — Update `SupplierPriceListController`

- Inject 4 LookupProvider ports
- Tambah `buildSPLUI(domain)` method
- Update `showEditForm()` untuk tambah `splUI` ke model

### Langkah 5 — Wire di `SupplierPriceListConfig`

Tambah `@Bean` untuk 4 LookupProvider yang dipakai controller SPL.

### Langkah 6 — Update Template

Update 4 autocomplete fragment calls untuk menyertakan `initialSubtext`.

---

## 5. Catatan Penting: List View vs Edit Form

Untuk `toSummaryResponse` (data list tabel), mapper masih perlu nama supplier/product/dll untuk ditampilkan di tabel. Dua pendekatan:

1. **MapStruct tetap inject LookupProvider** (Opsi C) — wajar, karena resolver adalah port.
2. **Query langsung di repository** via JPA query dengan join untuk avoid N+1 (lebih advanced, untuk optimasi performa).

Saran: untuk sekarang, **ganti injeksi JPA repo di mapper dengan LookupProvider port** (tetap di mapper, tapi melalui abstraksi yang benar). Ini menyeimbangkan kesederhanaan dan kebenaran arsitektur.

---

## 6. Referensi Codebase

| File | Peran |
|------|-------|
| `inventory.facility.web.controller.FacilityController` | ✅ Reference pattern yang benar (buildFacilityUI + LookupProvider) |
| `master.party.domain.port.PartyLookupProvider` | ✅ Kontrak port yang ada |
| `master.party.infrastructure.adapter.PartyLookupProviderImpl` | ✅ Single source of truth untuk subText Party |
| `inventory.product.domain.port.ProductLookupProvider` | ✅ Kontrak port yang ada |
| `inventory.product.infrastructure.adapter.ProductLookupProviderImpl` | ✅ Impl — `subText = code` |
| `core.dto.LookupDto` | ✅ Kontrak data autocomplete: `id`, `name`, `subText`, `payload` |
| `docs/spec/autocomplete-generic.md §4` | ✅ Spesifikasi Lookup Provider Port |
| `docs/architecture/clean-ddd-cqrs-standard.md §8 Pola 4` | ✅ Aturan Cross-Slice Communication |

# Autocomplete Generic Implementation Guide

Dokumen ini menjelaskan spesifikasi dan panduan untuk mengimplementasikan komponen Autocomplete (Server-side Search) di sistem ERP.

---

## 1. Komponen Backend

### A. Data Transfer Object (`LookupDto`)
Gunakan `LookupDto` standar untuk semua kebutuhan autocomplete.

```java
public record LookupDto(
    Long id, 
    String name,      // WAJIB: Hanya Nama (Tanpa Kode)
    String subText,   // WAJIB: Kode/Informasi Sekunder
    Map<String, Object> payload // OPTIONAL: Metadata tambahan (e.g., type, isSerialized)
) {}
```

### B. Controller (Standard Endpoint)
Setiap entitas yang mendukung autocomplete harus memiliki endpoint lookup:
- **Search**: `GET /api/lookup/[module]/[entities]?q=[keyword]&limit=10`
- **Detail**: `GET /api/lookup/[module]/[entities]/{id}`

---

## 2. Komponen Frontend (UI)

### A. Memilih Antara Select vs Autocomplete
- **Standard Select**: Gunakan untuk data yang jumlahnya sedikit (< 50 entri) dan statis. Contoh: Satuan Ukur (UoM), Tipe Kategori.
- **Autocomplete**: Wajib digunakan untuk data besar (> 100 entri) atau data yang terus bertambah. Contoh: Produk, Brand, Pelanggan, Lokasi Gudang.

### B. Global Auto-Initialization (Recommended)
Cara termudah adalah menggunakan fragment `autocomplete` dengan parameter `path`. Sistem akan menginisialisasi TomSelect secara otomatis.

```html
<div th:replace="~{fragments/inputs :: autocomplete(field='brandId', label=#{label.brand}, path='inventory/brands', 
    initialValue=${dto.brandId}, initialText=${dto.brandName}, initialSubtext=${dto.brandCode})}"></div>
```

**Aturan Wajib (Trinity Data):**
Untuk menjaga konsistensi UI, setiap penggunaan autocomplete **WAJIB** menyertakan tiga data awal:
1.  **`initialValue`**: ID dari record (disimpan ke database).
2.  **`initialText`**: Nama/Label utama (ditampilkan besar).
3.  **`initialSubtext`**: Kode/Informasi sekunder (ditampilkan kecil di bawah nama).

Hal ini berlaku untuk semua Request DTO yang dikirim kembali ke View. Jika salah satu kosong, maka UI akan terlihat tidak konsisten saat mode Edit.

### C. Inisialisasi Manual (Cascading)
Jika sebuah lookup bergantung pada field lain (misal: Bin bergantung pada Grid), gunakan fungsi **`initLookup`** yang tersedia secara global di **`shared/erp-common-handler.js`**.

```javascript
// Pola 1: Standar (Parent -> Child)
const tsChild = initLookup(childEl, 'inventory/bins', () => {
    return { id: parentTs.getValue(), key: 'gridId' };
});

// Pola 2: Complex Path (Parameter Statis)
// initLookup sekarang cerdas menangani URL yang sudah memiliki '?' 
const tsGrid = initLookup(gridEl, `inventory/grids?facilityId=${facilityId}`);
```

---

## 3. Fitur Lanjutan & Best Practices

### A. Pola "Inverse Auto-populate" (Child -> Parent)
Seringkali user ingin memilih **Container** langsung tanpa mengisi **Grid**. Untuk mendukung ini, backend harus mengirimkan info parent di dalam `payload` dan frontend melakukan update manual.

**Backend Requirement (`LookupDto`):**
Gunakan nama field spesifik entitas, jangan gunakan generic `parentId` agar memudahkan pemetaan.
```json
{
  "id": 10,
  "name": "Rak A-01",
  "payload": {
    "gridId": 1,
    "gridName": "Area Elektronik",
    "gridCode": "GRD-001"
  }
}
```

**Frontend Implementation:**
```javascript
tsBin.on('change', (val) => {
    if (!val) return;
    const data = tsBin.options[val];
    const p = data.payload;
    
    if (p.gridId && tsGrid.getValue() != p.gridId) {
        tsGrid.addOption({ id: p.gridId, name: p.gridName, subText: p.gridCode });
        tsGrid.setValue(p.gridId);
    }
});
```

### B. Fitur Otomatis `initLookup`
1.  **ERP Height Standard**: Menyesuaikan tinggi input (32px atau 28px) secara otomatis dengan mendeteksi elemen induk `.line-row`.
2.  **SSR Synchronization**: Sinkronisasi otomatis data awal dari server (mencegah teks hilang saat load).
3.  **Debouncing**: Penundaan request (150ms) untuk menghemat beban server.
4.  **HTMX Compatibility**: Otomatis re-init setelah swap HTMX selesai (diatur di `master.html`).
5.  **Smart Re-Search UX**: Saat dropdown diklik dan sudah memiliki nilai, label teks otomatis masuk ke kotak pencarian (input) untuk memudahkan edit tanpa harus menghapus pilihan lama.

---

## 4. Server-Side Pre-fill: Lookup Provider Port

### Masalah
Saat form edit di-render server-side (Thymeleaf), autocomplete perlu menampilkan **Trinity Data** (`initialValue`, `initialText`, `initialSubtext`). Controller consumer hanya menyimpan `referenceId` (misal `ownerId`), tapi perlu mengambil `name` dan `subText` dari entity slice lain.

### Pendekatan Lama (Anti-pattern)
Controller langsung inject `PartyJpaRepository` lalu memformat subText sendiri:
```java
// ❌ Duplikasi format, coupling ke JPA entity slice lain
private final PartyJpaRepository partyJpaRepository;
String subText = party.getCode() + " - " + typeLabel;  // format bisa berbeda antar consumer
```

### Pendekatan Baru: Lookup Provider Port
Provider slice (misal `master.party`) mengekspos port `PartyLookupProvider` di `domain/port/`. Consumer slice inject port ini — **bukan** JPA repository.

```java
// ✅ Single source of truth — format subText konsisten di semua consumer
private final PartyLookupProvider partyLookupProvider;
LookupDto owner = partyLookupProvider.resolve(domain.getOwnerId());
ui.put("ownerName", owner != null ? owner.name() : "");
ui.put("ownerCode", owner != null ? owner.subText() : "");
```

**Keuntungan:**
1. **Konsistensi** — SubText format sama di form edit dan dropdown autocomplete
2. **Single Source of Truth** — Ubah format di 1 tempat, semua consumer otomatis ikut
3. **Bounded Context** — Consumer tidak perlu tahu struktur internal entity provider
4. **Testable** — Mock port di unit test, tidak perlu JPA repository

### Kapan Menggunakan
- ✅ Entity provider dipakai ≥ 2 consumer → **wajib** pakai Lookup Provider
- ✅ SubText butuh i18n atau logika format kompleks → **wajib** pakai Lookup Provider
- ❌ SubText hanya field sederhana (misal `Geographic.code`) → boleh query langsung

### Controller Pattern: `buildXxxUI()`

Untuk halaman edit SSR, resolusi Trinity Data dilakukan di **controller** via metode private `buildXxxUI(domain)`, bukan di WebMapper. Pattern ini:
1. Controller inject semua `XxxLookupProvider` yang dibutuhkan (via `@RequiredArgsConstructor`)
2. Method `buildXxxUI()` memanggil masing-masing provider dan menghasilkan `Map<String, Object>`
3. Map di-`addAttribute` ke model dengan key `xxxUI`
4. Template Thymeleaf menggunakan `${xxxUI != null ? xxxUI.supplierText : ''}` untuk setiap autocomplete fragment

```java
// Controller
private Map<String, Object> buildSPLUI(SupplierPriceList domain) {
    Map<String, Object> ui = new HashMap<>();
    LookupDto supplier = partyLookupProvider.resolve(domain.getSupplierId());
    if (supplier != null) {
        ui.put("supplierText", supplier.name());
        ui.put("supplierSubtext", supplier.subText());
    }
    // ... repeat for other references
    return ui;
}

// Template
<div th:replace="~{fragments/inputs :: autocomplete(field='supplierId', ...,
    initialValue=${splRequest.supplierId},
    initialText=${splUI != null ? splUI.supplierText : ''},
    initialSubtext=${splUI != null ? splUI.supplierSubtext : ''})}"></div>
```

**Prinsip:** WebMapper tetap bersih dari provider cross-slice — tugasnya hanya memetakan domain → DTO (untuk list/detail response). Display data untuk form edit dikelola controller.

> Detail teknis dan contoh kode lengkap: [`architecture/clean-ddd-cqrs-standard.md` §8 Pola 4](../architecture/clean-ddd-cqrs-standard.md#pola-4--lookup-provider-port-cross-slice-autocomplete)


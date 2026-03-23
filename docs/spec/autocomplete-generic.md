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
Jika sebuah lookup bergantung pada field lain (misal: Bin bergantung pada Grid), gunakan fungsi **`initLookup`** yang tersedia secara global di **`erp-common-handler.js`**.

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


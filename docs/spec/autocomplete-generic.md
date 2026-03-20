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
Jika sebuah lookup bergantung pada field lain (misal: Bin bergantung pada Grid), gunakan fungsi `initLookup` secara manual di JavaScript halaman tersebut.

```javascript
// initLookup(element, lookupPath, parentProvider)
const tsChild = initLookup(childEl, 'inventory/bins', () => {
    return { id: parentTs.getValue(), key: 'gridId' };
});
```

---

## 3. Fitur Otomatis `initLookup`
1.  **ERP Height Standard**: Menyesuaikan tinggi input (32px atau 28px).
2.  **SSR Synchronization**: Sinkronisasi otomatis data awal dari server (mencegah teks hilang saat load).
3.  **Debouncing**: Penundaan request (150ms) untuk menghemat beban server.
4.  **HTMX Compatibility**: Otomatis re-init setelah swap HTMX selesai.

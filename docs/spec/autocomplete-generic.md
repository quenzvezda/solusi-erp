# Autocomplete Generic Implementation Guide

Dokumen ini menjelaskan spesifikasi dan panduan untuk mengimplementasikan komponen Autocomplete yang _generic_ dan _hierarchical_, yang telah terstandarisasi melalui modul Stock Adjustment dan UoM Conversion.

---

## 1. Komponen Backend

### A. Data Transfer Object (`LookupDto`)
Gunakan `LookupDto` standar untuk semua kebutuhan autocomplete.

```java
public record LookupDto(
    Long id, 
    String name,      // WAJIB: Hanya Nama (Tanpa Kode)
    String subText,   // WAJIB: Kode/Informasi Sekunder
    Map<String, Object> payload // OPTIONAL: Metadata tambahan (e.g., isSerialized, uomName)
) {}
```

### B. Controller (Standard Endpoint)
Setiap entitas yang mendukung autocomplete harus memiliki dua jenis endpoint:
1.  **Search Endpoint**: `/api/lookup/[module]/[entities]?q=[keyword]&limit=10`
2.  **Detail Endpoint**: `/api/lookup/[module]/[entities]/{id}`

---

## 2. Komponen Frontend (UI)

### A. Inisialisasi Standard (Global `initLookup`)
Semua komponen autocomplete **WAJIB** diinisialisasi melalui fungsi `initLookup(el, type, parentProvider)` yang berada di `layout/master.html`.

**Fitur Otomatis `initLookup`:**
1.  **ERP Height Standard**: Menambahkan class `.erp-input-ts` atau `.erp-input-ts-sm` (jika di dalam tabel) ke wrapper TomSelect.
2.  **SSR Synchronization**: Membaca atribut `data-subtext` dari Thymeleaf untuk menampilkan kode pada saat halaman pertama dimuat (Mode Edit).
3.  **Search Field**: Mencari berdasarkan `name` (default).
4.  **Debouncing**: Menunda request ke server sebesar 150ms.

### B. Integrasi Thymeleaf
Gunakan fragment `fragments/inputs :: autocomplete` atau `table-autocomplete`.

```html
<div th:replace="~{fragments/inputs :: autocomplete(field='productId', label=#{label.product}, id='product-select', 
    initialValue=${dto.productId}, initialText=${dto.productName}, initialSubtext=${dto.productCode})}"></div>
```

---

## 3. Pola JavaScript di Halaman

Inisialisasi dilakukan di dalam `window.addEventListener('load', ...)` untuk memastikan library TomSelect yang di-defer sudah tersedia.

```javascript
window.addEventListener('load', function() {
    const el = document.getElementById('product-select');
    
    // Inisialisasi Standard
    const ts = initLookup(el, 'inventory/products');

    // Opsional: Custom Logic (misal: mengambil metadata dari payload)
    if (ts) {
        // Jika butuh search di field tambahan (subText)
        ts.settings.searchField = ['name', 'subText'];
        
        ts.on('change', function(val) {
            if (val) {
                const item = this.options[val];
                if (item.payload) {
                    // Logika auto-fill field lain berdasarkan payload
                    console.log(item.payload.uomName);
                }
            }
        });
    }
});
```

---

## 4. Pola Cascading (Parent -> Child)

Jika sebuah lookup bergantung pada field lain (misal: Bin bergantung pada Grid), gunakan `parentProvider`.

```javascript
// Provider mengirimkan data parent saat dipanggil oleh initLookup load()
const tsChild = initLookup(childEl, 'inventory/bins', () => {
    return { id: parentTs.getValue(), key: 'gridId' };
});
```

`initLookup` akan otomatis membersihkan cache dan opsi setiap kali dropdown dibuka jika `parentProvider` disediakan, menjamin data yang tampil selalu relevan dengan pilihan induk terbaru.

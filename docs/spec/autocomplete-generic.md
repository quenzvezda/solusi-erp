# Autocomplete Generic Implementation Guide

Dokumen ini menjelaskan spesifikasi dan panduan untuk mengimplementasikan komponen Autocomplete yang _generic_, dapat digunakan di berbagai modul dan halaman di dalam aplikasi Solusi ERP. 

Implementasi ini menggunakan library frontend **TomSelect** yang dihubungkan dengan **Spring Boot REST API** di backend, mendukung debouncing, reverse lookup (untuk mode pre-fill / edit), dan navigasi keyboard.

---

## 1. Komponen Backend

### A. Generic Data Transfer Object (`LookupDto`)

Setiap endpoint API pencarian untuk autocomplete **DIHARUSKAN** merespon dengan `List<LookupDto>`. Class ini terletak di package core agar bisa share ke semua modul.

```java
package com.solusi.erp.core.dto;

public record LookupDto(
        Long id,
        String name,
        String subText
) {
    // subText digunakan untuk menampilkan teks kecil tambahan di bawah nama opsi
}
```

### B. Service & Repository Layer

1. **Repository:** Pastikan layer repository mendukung pencarian berdasarkan nama/kode dan mendukung limit / pageable (mengembalikan `Page<Entity>`).
2. **Service:** Buat method yang menerima _keyword_ pencarian (`String q`) dan jumlah `limit`. Mapping Entity hasil pencarian ke record `LookupDto`. 

Contoh pada Service:
```java
public List<LookupDto> lookupData(String keyword, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    Page<EntityModel> results = repository.searchByKeyword(keyword, pageable);
    
    return results.getContent().stream()
        .map(e -> new LookupDto(
            e.getId(), 
            e.getName(), 
            e.getAdditionalInfo() // Akan masuk ke subText
        ))
        .toList();
}
```

### C. RestController (Lookup API & Reverse Lookup)

Pisahkan endpoint pencarian autocomplete ini dari UI Controller dan sebarkan melalui endpoint `/api/lookup/`. Anda memerlukan dua fungsi utama di Controller:

1. **Endpoint Pencarian (Search):** Dipanggil oleh TomSelect saat user mengetikkan sesuatu (on type / debounce).
2. **Endpoint Reverse Lookup (Hierarchy/Detail):** Dipanggil oleh TomSelect saat halaman pertama kali load (Edit Mode) untuk memuat data text/label berdasarkan ID yang sudah tersimpan di database (pre-fill).

```java
// Contoh Limit Search Endpoint
@GetMapping("/api/lookup/geographics/cities")
@PreAuthorize("hasAuthority('LOOKUP_GEOGRAPHIC')")
public List<LookupDto> lookupCities(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "5") int limit) {
    return service.lookupCities(q, limit);
}
```

### D. Security & Permissions (Otorisasi)
Setiap class atau endpoint Lookup **DIWAJIBKAN** dilindungi oleh anotasi `@PreAuthorize`.
PENTING: Gunakan penamaan permission dengan konvensi prefix **`LOOKUP_[NAMA_TABEL/MODUL]`** (contoh: `LOOKUP_GEOGRAPHIC`). 

**Jangan menggunakan suffix (seperti `GEOGRAPHIC_LOOKUP`)**. UI Role Management ERP ini secara otomatis membaca kata pertama (sebelum `_`) untuk membuat pengelompokan (Folder/Group) checkbox. Dengan menggunakan prefix `LOOKUP_`, semua izin terkait autocomplete akan terkumpul rapi di dalam satu grup `LOOKUP` di halaman Role Admin, terpisah dari izin akses CRUD halaman utamanya (`GEOGRAPHIC_READ`, dll).

---

## 2. Komponen Frontend (UI)

### A. Dependensi Global & CSS

Pastikan dependensi berikut sudah dimuat di layout utama (misal: `master.html`):
1. CDN TomSelect CSS (misal `tom-select.bootstrap5.min.css`)
2. CDN TomSelect JS
3. Custom CSS Overrides `tomselect-custom.css` (Style ini mengatur tampilan dropdown agar solid `background-color`, z-index, dan hover selection blue khas Tabler).

### B. Setup HTML Form

Gunakan elemen `<select>` standar dengan class spesifik sebagai kait.

```html
<select class="city-ts" name="cityId" id="city-ts-1">
    <option value=""></option>
</select>
```

### C. Inisialisasi TomSelect (Javascript)

Contoh setup JS standar untuk menginisialisasi input debounced remote-fetch:

```javascript
// Konfigurasi endpoint
const LOOKUP_API = '/api/lookup/model_name';
const REVERSE_LOOKUP_API = '/api/model_name/hierarchy';

// 1. Helper function UI TomSelect
function renderOption(data, escape) {
    const sub = data.subText ? `<small class="text-muted d-block" style="font-size:0.75em;line-height:1.2">${escape(data.subText)}</small>` : '';
    return `<div class="py-1">${escape(data.name)}${sub}</div>`;
}

// 2. Debounce Function (menghindari spam request)
function debounce(fn, delay) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}

// 3. Inisialisasi
let selectBox = new TomSelect(`#city-ts-1`, {
    valueField: 'id',
    labelField: 'name',
    searchField: ['name'],
    placeholder: '-- Pilih Opsi --',
    openOnFocus: true,     // Tampilkan list walau query kosong (top 5 default)
    preload: false,        // false/focus, fetch API on focus/type
    load: debounce(function (q, callback) {
        // Query param q dan limit=5 dikirim ke Backend
        fetch(`${LOOKUP_API}?q=${encodeURIComponent(q)}&limit=5`)
            .then(r => r.json())
            .then(callback)
            .catch(() => callback([]));
    }, 100),
    render: {
        option: (data, escape) => renderOption(data, escape),
        item: (data, escape) => `<span>${escape(data.name)}</span>` // yang tampil pada field yg terpilih
    },
    onFocus() {
        this.load(''); // Force load saat elemen di klik tanpa ketikan
    },
    onDelete() {
        this.clear(true);
        this.clearOptions();
        return false;
    }
});
```

### D. Penanganan Edit Mode (Pre-fill / Reverse Lookup)

Ketika _form_ digunakan untuk Update/Edit, control input select hanya memiliki `ID` (value), namun membutuhkan `label/text` agar pengguna bisa melihat opsi apa yang terpilih. Oleh sebab itu, kita melakukan Fetch ID reverse.

```javascript
let initialValueId = /* ID dari database server side render */;

if(initialValueId) {
    fetch(`${REVERSE_LOOKUP_API}/${initialValueId}`)
        .then(r => r.json())
        .then(data => {
            // Tambahkan option secara manual dan set nilainya
            selectBox.addOption({ 
                id: data.id, 
                name: data.name, 
                subText: data.subText 
            });
            selectBox.setValue(data.id, true);
        })
        .catch(err => console.error(err));
}
```

---

## Ringkasan Ketentuan
1. Selalu gunakan `LookupDto` dari `core` untuk konsistensi json property (`id`, `name`, `subText`).
2. Jangan hapus cache browser bawaan TomSelect dari method `onDelete` untuk pencarian, kecuali dependensi hirarki (seperti select _Provinsi_ dihapus, maka _Kota_ perlu dibersihkan menggunakan `clear(true)` dan `clearOptions()`).
3. Selalu tambahkan delay **Debounce** (min `100ms`) pada method _load_.
4. Hindari render list lebih dari limit yang diperlukan untuk UX maksimal (misal top 5 hingga maksimal 10 results saja setiap pencarian).

# Autocomplete Generic Implementation Guide

Dokumen ini menjelaskan spesifikasi dan panduan untuk mengimplementasikan komponen Autocomplete yang _generic_ dan _hierarchical_, yang telah terstandarisasi melalui pengerjaan modul Stock Adjustment.

Implementasi ini menggunakan library frontend **TomSelect** yang dihubungkan dengan **Spring Boot REST API** di backend, mendukung debouncing, cascading (filtering bertingkat), auto-filling, dan sinkronisasi Server-Side Rendering (Thymeleaf).

---

## 1. Komponen Backend

### A. Data Transfer Object (`LookupDto`)

Gunakan `LookupDto` standar untuk semua kebutuhan autocomplete. Pastikan mengikuti aturan penamaan field untuk menjaga estetika UI.

```java
public record LookupDto(
    Long id, 
    String name,      // WAJIB: Hanya Nama (Tanpa Kode)
    String subText,   // WAJIB: Kode/Informasi Sekunder
    Map<String, Object> payload // OPTIONAL: Metadata tambahan (e.g., isSerialized)
) {}
```

**Aturan Estetika:**
*   **DILARANG** menggabungkan Kode ke dalam field `name` (misal: `Code - Name`).
*   Field `name` akan menjadi teks utama yang dipilih user. Jika terlalu panjang, akan merusak tata letak tabel (memicu line-break).
*   Field `subText` akan tampil otomatis di bawah nama pada dropdown pencarian untuk membantu identifikasi unik.

### B. Controller (Standard Endpoint)

Setiap entitas yang mendukung autocomplete harus memiliki dua jenis endpoint:
1.  **Search Endpoint**: `/api/lookup/[entities]?q=[keyword]&limit=10`
2.  **Detail Endpoint**: `/api/lookup/[entities]/{id}` (PENTING untuk mekanisme cascading dan binding data awal yang stabil).

---

## 2. Komponen Frontend (UI)

### A. Inisialisasi Standard dengan Sinkronisasi SSR

Fungsi inisialisasi harus mampu membaca data awal yang dirender oleh Thymeleaf agar informasi `subText` tidak hilang saat halaman pertama kali dimuat (Mode Edit).

```javascript
function initLookup(el, type, parentProvider = null) {
    return new TomSelect(el, {
        valueField: 'id',
        labelField: 'name',
        searchField: ['name'],
        placeholder: '-- Select --',
        preload: 'focus',
        onInitialize: function() {
            // Sinkronisasi data awal dari atribut data-subtext HTML
            const initialOption = el.querySelector('option[selected], option[value]:not([value=""])');
            if (initialOption) {
                const val = initialOption.value;
                const subText = initialOption.getAttribute('data-subtext');
                if (subText && this.options[val]) {
                    this.options[val].subText = subText;
                    this.refreshOptions(false);
                }
            }
        },
        load: debounce(function(q, callback) {
            let url = `/api/lookup/${type}?q=${encodeURIComponent(q)}&limit=10`;
            if (parentProvider) {
                const parent = parentProvider();
                if (parent.id) url += `&${parent.key}=${parent.id}`;
            }
            fetch(url).then(r => r.json()).then(callback);
        }, 150),
        render: {
            option: (data, escape) => {
                if (!data.id) return '';
                let sub = data.subText ? `<small class="text-muted d-block" style="font-size:0.75em">${escape(data.subText)}</small>` : '';
                return `<div class="py-1"><div>${escape(data.name)}</div>${sub}</div>`;
            },
            item: (data, escape) => `<span>${escape(data.name)}</span>`
        }
    });
}
```

### B. Integrasi Thymeleaf (Mode Edit)

Agar informasi Kode tetap tampil di bawah Nama saat halaman pertama kali dibuka, gunakan atribut `data-subtext` pada elemen `<option>`.

```html
<select th:field="*{facilityId}" id="facility">
    <option value=""></option>
    <option th:if="${dto.facilityId != null}" 
            th:value="${dto.facilityId}" 
            th:text="${dto.facilityName}" 
            th:attr="data-subtext=${dto.facilityCode}"
            selected></option>
</select>
```

---

## 3. Pola Cascading & Auto-filling

### A. Cascading (Parent -> Child)
Gunakan event `dropdown_open` untuk memastikan data anak selalu segar berdasarkan pilihan induk yang terbaru.

```javascript
tsChild.on('dropdown_open', () => {
    tsChild.clearOptions(); 
    tsChild.refreshOptions(false);
});
```

### B. Auto-filling (Child -> Parent)
Gunakan metadata dari `payload` atau request Detail Endpoint untuk mengisi field induk secara otomatis jika user memilih data anak terlebih dahulu. Gunakan **Flag Guard** untuk mencegah infinite loop reset.

---

## Ringkasan Ketentuan
1.  **Estetika**: Field `name` hanya berisi Nama. Kode wajib di `subText`.
2.  **Konsistensi**: Gunakan `data-subtext` di Thymeleaf agar UI SSR dan Autocomplete identik.
3.  **Robust**: Selalu sediakan Detail Endpoint (`/{id}`) untuk setiap Lookup.
4.  **Performance**: Gunakan debouncing 150ms dan limit query backend.
5.  **UX**: Sembunyikan opsi kosong (`!data.id`) agar tidak merusak visual dropdown.

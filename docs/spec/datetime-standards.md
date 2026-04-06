# Date & Time Input Formatting Standard

Dokumen ini mendefinisikan standar untuk input tanggal dan waktu di sistem ERP guna memastikan konsistensi tampilan (user-friendly format), validitas data ke database (ISO 8601), dan kompatibilitas dengan Flatpickr picker.

---

## 1. Overview

Sistem menggunakan **Flatpickr 4.6.13** (lightweight, no jQuery) untuk menangani date, time, dan datetime-local inputs secara global. Fitur utama:

- **Pre-fill**: Menampilkan nilai yang sudah ada di database dengan format user-friendly (contoh: `03 Apr 2026 00:00`)
- **Display Format (altInput)**: Format ramah pengguna yang terpisah dari nilai backend
- **Backend Format (hidden input)**: ISO 8601 (`yyyy-MM-dd'T'HH:mm` atau `yyyy-MM-dd`) yang kompatibel dengan `LocalDateTime` / `LocalDate` di Java
- **Locale Support**: Otomatis mendeteksi bahasa dari `html[lang]` atau cookie `lang` (default: English, Indonesian: `id`)
- **MutationObserver**: Otomatis inisialisasi datetime picker pada elemen yang ditambahkan secara dinamis (contoh: nested rows di form master data)

---

## 2. Frontend Implementation

### A. HTML Input dengan `data-picker` Attribute

Gunakan atribut `data-picker` untuk menandai input yang perlu Flatpickr:

```html
<!-- DateTime Input -->
<input type="datetime-local" 
       th:field="*{publishDate}" 
       data-picker="datetime" 
       class="form-control erp-input"
       required/>

<!-- Date Input -->
<input type="date" 
       th:field="*{expiryDate}" 
       data-picker="date" 
       class="form-control erp-input"/>

<!-- Time Input -->
<input type="time" 
       th:field="*{startTime}" 
       data-picker="time" 
       class="form-control erp-input"/>
```

**Tipe Picker yang Didukung:**
| Tipe | Mode | Display Format | Backend Format | Contoh |
|------|------|---|---|---|
| `datetime` | Tanggal + Waktu (24-jam) | `03 Apr 2026 00:00` | `2026-04-03T00:00` | Publish date, approval deadline |
| `date` | Tanggal saja | `03 Apr 2026` | `2026-04-03` | Identification expiry, birth date |
| `time` | Waktu saja (24-jam) | `00:00` | `00:00` | Clock-in time, shift hours |

### B. Global Initialization (Otomatis)

Sistem telah mengintegrasikan Flatpickr di `layout/master.html`:
- CDN CSS/JS: `https://cdn.jsdelivr.net/npm/flatpickr@4.6.13/`
- Locale: `flatpickr.l10ns.id` untuk Indonesian
- Global Initializer: `/js/shared/erp-datetime-picker.js`
- Init Hook: `DOMContentLoaded` dan `htmx:afterSwap`

**Developer TIDAK perlu menambahkan script atau CSS secara manual.** Cukup gunakan `data-picker` attribute, dan picker akan otomatis terbentuk.

---

## 3. Backend Implementation (DTO & Validation)

### A. DTO Field Definition

**Wajib** menambahkan `@DateTimeFormat` annotation pada semua field `LocalDateTime` / `LocalDate` yang muncul di form:

```java
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotNull;

@Data
public class NewsSaveRequest extends BaseAuditResponse {
    
    @NotNull(message = "{label.news.publish-date} {validation.notnull.suffix}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime publishDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expiryDate;
}
```

**Penting**: Pattern harus sesuai dengan format backend yang dikirim oleh Flatpickr:
- `LocalDateTime`: `"yyyy-MM-dd'T'HH:mm"` (tanpa detik)
- `LocalDate`: `"yyyy-MM-dd"`
- `LocalTime`: `"HH:mm"` (24-jam)

### B. Why @DateTimeFormat?

Tanpa `@DateTimeFormat`, Thymeleaf akan me-render `LocalDateTime` menggunakan locale-specific format (contoh Indonesian: `"03/04/26 00.00"`), yang:
1. Tidak bisa di-parse oleh JavaScript `new Date()` 
2. Menyebabkan Flatpickr gagal pre-fill nilai dari database
3. Tidak konsisten dengan standard ISO 8601

Dengan `@DateTimeFormat`, Thymeleaf render dalam format ISO 8601, sehingga:
1. Flatpickr bisa parse dan pre-fill dengan benar
2. Display tetap friendly via Flatpickr `altInput`
3. Backend menerima nilai yang konsisten

---

## 4. User Experience Behavior

### A. Pre-fill (Edit Page)
User membuka halaman edit dengan data existing dari database:

```
Database: 2026-04-03T00:00:00 (LocalDateTime)
↓
Thymeleaf + @DateTimeFormat: 2026-04-03T00:00 (ISO 8601 string)
↓
Flatpickr JS: Parses & displays as "03 Apr 2026 00:00" (altInput)
↓
Hidden input: 2026-04-03T00:00 (backend value)
```

### B. Selection (User Picks New Date)
User membuka picker dan memilih tanggal baru:

```
User selects: May 1, 2026 at 09:30 via UI
↓
Flatpickr formats:
  - altInput (display): "01 May 2026 09:30"
  - hidden input: "2026-05-01T09:30"
↓
Click without selecting? → Nilai terjaga (tidak kosong)
↓
Form submit: Hidden input value dikirim ke backend sebagai ISO string
```

### C. Validasi & Error Message
Jika user meninggalkan field kosong dan form memiliki `required`:
- Browser native validation menangkap error (tidak mengandalkan Flatpickr)
- Pesan error ditampilkan melalui fragment standar error validation

---

## 5. Form DTO Checklist

Setiap kali developer membuat form dengan date/time input:

- [ ] HTML input memiliki atribut `data-picker="datetime|date|time"`
- [ ] DTO memiliki `@DateTimeFormat(pattern = "...")` pada field yang sesuai
- [ ] Pattern di annotation sesuai dengan jenis field (`LocalDateTime`, `LocalDate`, `LocalTime`)
- [ ] Jika field wajib, tambahkan `@NotNull(message = "...")`
- [ ] Test pre-fill dengan membuka halaman edit di MCP Playwright
- [ ] Test form submit dengan nilai baru

---

## 6. Common Issues & Troubleshooting

| Masalah | Penyebab | Solusi |
|--------|---------|--------|
| Pre-fill kosong | `@DateTimeFormat` tidak ada di DTO | Tambahkan `@DateTimeFormat(pattern = "...")` |
| Format tidak konsisten | Thymeleaf render dalam locale format | Pastikan `@DateTimeFormat` pattern sesuai ISO 8601 |
| Click tanpa select malah kosong | Flatpickr tidak initialize (old bug) | Pastikan JS file sudah di-reload (hard refresh / clear cache) |
| Dynamic row tidak ada picker | MutationObserver belum catch event | Pastikan `data-picker` attribute ada pada input template |

---

## 7. Locale & i18n

Sistem secara otomatis mendeteksi locale berdasarkan urutan:

1. **HTML Lang Attribute**: `<html lang="id">` → Indonesian
2. **Cookie**: `lang=id` → Indonesian
3. **Default**: English

Month names dan labels otomatis dalam bahasa yang sesuai:
- English: `03 Apr 2026 00:00`
- Indonesian (id): `03 Apr 2026 00:00` (month tetap English dari Flatpickr CDN, bisa extend dengan custom locale jika diperlukan)

---

## 8. Advanced: Dynamic Rows & Nested Inputs

Untuk form dengan nested array (contoh: identification rows di Party master):

```html
<div id="identifications">
    <!-- Existing rows (static) -->
    <div th:each="id : ${party.identifications}">
        <input type="date" 
               th:field="*{identifications[__${idStat.index}__].expiryDate}" 
               data-picker="date"/>
    </div>
    
    <!-- Template untuk dynamically added rows -->
    <template id="identification-template">
        <input type="date" 
               name="identifications[INDEX].expiryDate" 
               data-picker="date"/>
    </template>
</div>
```

**MutationObserver** di `erp-datetime-picker.js` otomatis akan:
1. Detect ketika elemen baru ditambahkan ke DOM
2. Inisialisasi Flatpickr pada input dengan `data-picker` attribute
3. Set `data-picker-initialized="true"` untuk menghindari double-init

---

## 9. References

- **Global Initializer**: `src/main/resources/static/js/shared/erp-datetime-picker.js`
- **Layout Integration**: `src/main/resources/templates/layout/master.html` (lines 8-12 CSS, 56-58 JS, 89-91 & 126-128 init hooks)
- **Flatpickr Docs**: https://flatpickr.js.org/
- **Spring @DateTimeFormat**: https://docs.spring.io/spring-framework/reference/core/validation/format.html

# Form Submission Standard (Hybrid Approach)

Dokumen ini mendefinisikan standar pengiriman form di sistem ERP. Kita menggunakan pendekatan **Hybrid** untuk menyeimbangkan antara kecepatan pengembangan dan stabilitas komponen JavaScript.

---

## 1. Memilih Metode Pengiriman

| Metode | Kapan Digunakan? | Karakteristik |
| :--- | :--- | :--- |
| **HTMX** | Search filter, pagination, tab switching, atau form sangat sederhana tanpa JavaScript kompleks. | Cepat, "HTML over the wire", merender ulang fragment di server. |
| **AJAX (Standard)** | **WAJIB** untuk form CRUD (Create/Update) yang memiliki komponen TomSelect, AutoNumeric, atau state JS lainnya. | Menjaga UI State, mengirim JSON, menerima JSON, tidak ada refresh/swap HTML yang merusak JS. |

---

## 2. Implementasi AJAX (Standard CRUD)

Gunakan pola ini untuk semua form master data dan transaksi.

### A. Atribut HTML
Tandai form dengan atribut `data-ajax-form="true"` dan tentukan URL tujuan setelah sukses.

```html
<form id="my-form" 
      th:action="@{...}" 
      method="post"
      data-ajax-form="true"
      data-redirect-on-success="/module/list">
    ...
    <div id="loading-indicator" class="spinner-border" style="display:none;"></div>
</form>
```

### B. Penanganan Client-Side
Sistem menggunakan `erp-form-handler.js` (global) yang secara otomatis:
1.  Mencegat submit form.
2.  **Data Cleaning (Otomatis)**:
    *   **Empty to Null**: Mengonversi string kosong (`""`) menjadi `null` agar kompatibel dengan Jackson (Enum/Long).
    *   **Numeric Unformat**: Otomatis mengambil nilai murni dari field AutoNumeric menggunakan `.getNumber()`.
3.  Mengirim data sebagai JSON (otomatis menangani CSRF via header `X-CSRF-TOKEN`).
4.  **Sukses**: Melakukan redirect setelah jeda 300ms (untuk keperluan debug network).
5.  **Validasi Gagal (400)**: Menampilkan pesan error di bawah field masing-masing tanpa merusak state TomSelect.
6.  **Server Error (500)**: Menampilkan alert merah global di dalam `.alert-container`.

---

## 3. Implementasi HTMX (Passive & In-place Actions)

### A. Passive Updates (Search / Filter)
Gunakan HTMX untuk update UI parsial yang tidak melibatkan inisialisasi library JS yang rumit.

**Struktur Penting**: Selalu bungkus input pencarian dan ikon di dalam `div.input-icon` tersendiri (di dalam form) jika terdapat elemen `input type="hidden"`. Hal ini untuk memastikan padding CSS ikon tetap konsisten.

```html
<form hx-get="/module/list" 
      hx-target="#table-result" 
      hx-trigger="keyup changed delay:500ms from:#search-input">
    <input type="hidden" name="parentId" th:value="...">
    <div class="input-icon">
        <input id="search-input" name="q" type="text" class="form-control form-control-sm">
        <span class="input-icon-addon"><i class="ti ti-search"></i></span>
    </div>
</form>
```

### B. Inline Actions (Delete - Always Fresh)
Gunakan pola ini untuk menghapus baris tabel secara instan dengan animasi, namun tetap menjaga jumlah data di layar tetap konsisten (misal tetap 10 data).

**Frontend (Button):**
Targetkan ID baris tabel secara spesifik agar Interceptor bisa mendeteksi aksi in-place.
```html
<tr th:id="'row-' + ${item.id}">
    <button type="button"
            th:attr="hx-post=@{/module/delete/{id}(id=${item.id})}, hx-target='#row-' + ${item.id}"
            hx-swap="outerHTML swap:500ms">
        Delete
    </button>
</tr>
```

**Frontend (Table Container):**
Tambahkan trigger untuk mendengarkan sinyal refresh dari server dengan delay untuk memberi waktu animasi.
```html
<div id="table-container" 
     hx-trigger="refresh-table from:body delay:500ms" 
     hx-include="[name='keyword'], [name='page']">
     ...
</div>
```

---

## 4. Backend (Controller)

Untuk form AJAX, Controller **WAJIB** menggunakan `ApiResponse` dan `@RequestBody`.

```java
@PostMapping("/create")
@ResponseBody
public ResponseEntity<ApiResponse<MyResponse>> create(@Valid @RequestBody MyRequest request) {
    MyResponse data = service.create(request);
    String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
}
```

## 5. Magic Fragment Routing (HTMX Only)

Sistem menyertakan `HtmxViewInterceptor` yang secara otomatis mendeteksi request HTMX dan menentukan fragmen mana yang harus dirender berdasarkan header `HX-Target`.

### Aturan Konvensi (Mandatory):
Agar otomatisasi ini bekerja, developer **WAJIB** menyamakan nama fragmen dengan ID target:
1.  **HTML ID**: `<div id="my-table-container" ...>`
2.  **Thymeleaf Fragment**: `<div id="my-table-container" th:fragment="my-table-container">`
3.  **HTMX Target**: `<form hx-target="#my-table-container" ...>`

### Dampak pada Controller:
Controller tetap bersih dan cukup mengembalikan nama view standar. **DILARANG** melakukan pengecekan header `HX-Request` secara manual untuk urusan pemilihan fragmen tabel.

```java
@GetMapping
public String list(Pageable pageable, Model model) {
    model.addAttribute("page", service.findAll(pageable));
    return "module/list"; // Interceptor akan otomatis mengubah menjadi "module/list :: my-table-container" jika dipanggil via HTMX
}
```

*Catatan: Validasi ditangani secara terpusat oleh `GlobalExceptionHandler`. Jangan lagi menggunakan `BindingResult` di parameter method kecuali sangat terpaksa.*

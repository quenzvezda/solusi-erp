# HTMX Form Submission Standard

Dokumen ini mendefinisikan standar penggunaan **HTMX** untuk pengiriman form (submission) di sistem ERP. Tujuannya adalah untuk meningkatkan User Experience (UX) dengan menghindari *Full Page Refresh* dan menjaga *UI State* (seperti label Autocomplete) saat terjadi kesalahan validasi.

---

## 1. Atribut HTML (Standard Pattern)

Setiap form transaksi atau master data wajib menggunakan atribut HTMX berikut:

```html
<form th:action="@{...}" 
      method="post"
      hx-post
      hx-target="#alert-container"
      hx-swap="innerHTML"
      hx-indicator="#loading-indicator">
    ...
</form>
```

### Penjelasan Atribut:
*   **`hx-post`**: Mengirim form via AJAX. Jika nilai dikosongkan, ia akan mengambil URL dari atribut `th:action`.
*   **`hx-target="#alert-container"`**: Lokasi di mana fragment error dari server akan diletakkan jika terjadi kegagalan.
*   **`hx-swap="innerHTML"`**: Mengganti isi target dengan response dari server.
*   **`hx-indicator`**: Menampilkan elemen pemuatan (spinner) selama request berlangsung.

---

## 2. Implementasi Backend (Controller)

Gunakan **`HtmxResponseUtility`** untuk menangani response agar kode tetap bersih.

### A. Penanganan Validasi Gagal
Jika `BindingResult` memiliki error, kembalikan fragment alert global.

```java
if (bindingResult.hasErrors()) {
    if (htmxRequest) return HtmxResponseUtility.returnErrorFragment();
    return "my-module/form";
}
```

### B. Penanganan Success (Redirect)
HTMX tidak melakukan redirect otomatis dari response `302`. Kita harus menggunakan header **`HX-Redirect`**.

```java
if (htmxRequest) {
    return HtmxResponseUtility.redirect(response, "/my-module/list");
}
return "redirect:/my-module/list";
```

---

## 3. Penanganan Event Client-Side

Sistem memiliki listener global di `master.html` untuk memastikan library pihak ketiga (seperti TomSelect atau AutoNumeric) tetap berfungsi setelah swap HTMX terjadi.

```javascript
document.body.addEventListener('htmx:afterSwap', function (evt) {
    // Re-inisialisasi komponen di dalam target yang baru diswap
    initNumericInputs(evt.detail.target);
});
```

## 4. Lokasi Fragment Alert
Selalu sediakan kontainer alert di dalam form:
```html
<div id="alert-container">
    <div th:replace="~{fragments/alerts :: success}"></div>
    <div th:replace="~{fragments/alerts :: error}"></div>
</div>
```

# Page-Specific JavaScript Standard

Dokumen ini menjelaskan kapan JavaScript harus ditaruh langsung di template, dan kapan harus dipindah ke file terpisah per halaman atau per fitur.

## Prinsip Utama

- `shared/erp-common-handler.js` hanya untuk helper lintas modul yang benar-benar umum.
- Logika bisnis atau flow UI yang spesifik fitur tidak boleh membesar di helper global.
- Jika sebuah halaman hanya butuh script kecil dan sederhana, inline script di slot `pageScripts` masih boleh dipakai.
- Jika flow sudah kompleks, gunakan file JavaScript khusus per halaman atau per fitur.

## Kapan Harus Dipisah

Pindahkan ke file terpisah jika script berisi salah satu dari kondisi berikut:

- banyak event listener dan state UI
- alur drawer/modal yang kompleks
- kombinasi lookup, kalkulasi, dan validasi khusus fitur
- logika yang hanya dipakai oleh satu modul
- script mulai sulit dibaca atau sulit dites

## Struktur Folder yang Disarankan

Gunakan struktur yang mengikuti domain fitur:

```text
src/main/resources/static/js/
  shared/
    erp-common-handler.js
    erp-form-handler.js
  inventory/
    adjustment/
      stock-adjustment-form.js
    product/
      product-form.js
```

Jika satu file hanya dipakai oleh satu halaman, letakkan di folder modul yang paling dekat dengan template-nya. Nama file sebaiknya mencerminkan fitur dan mode pemakaian, misalnya `stock-adjustment-form.js`.

## Pola Implementasi di Template

Gunakan slot `pageScripts` pada layout master, lalu kirim fragmen script dari halaman:

```html
<body th:replace="~{layout/master :: layout(~{:: .content}, ~{:: #page-specific-scripts})}">
```

```html
<div id="page-specific-scripts" th:fragment="pageScripts">
    <script th:inline="javascript">
        window.StockAdjustmentPageConfig = {
            isLocked: /*[[${isLocked}]]*/ false
        };
    </script>
    <script th:src="@{/js/inventory/adjustment/stock-adjustment-form.js}" defer></script>
</div>
```

Fragmen inline hanya dipakai untuk mengirim konfigurasi kecil, sedangkan logika utamanya ada di file JavaScript terpisah.

## Batasan Helper Global

Helper global tetap boleh dipakai untuk fungsi yang umum seperti:

- `ErpModal`
- `ErpDrawer`
- `ErpNumeric`
- `ErpLineManager`

Namun helper global tidak boleh menampung flow bisnis yang hanya relevan untuk satu halaman atau satu modul tertentu. Jika logika sudah mulai membaca state fitur, memanggil endpoint fitur, atau mengatur drawer khusus, pindahkan ke file modul.

## Rekomendasi Praktis

- Gunakan inline script hanya untuk konfigurasi kecil, bukan flow utama.
- Gunakan file JavaScript khusus untuk form yang kompleks.
- Pertahankan helper global tetap kecil, stabil, dan reusable.
- Jika file per fitur dipakai lintas beberapa halaman, pertimbangkan subfolder modul agar struktur tetap rapi.

## Jebakan Umum di Mode Pre-Edit (TomSelect tanpa Payload)

Ketika baris tabel di-render oleh server (Thymeleaf), elemen `<select>` diisi dengan opsi statis (`<option selected>`). Saat TomSelect di-init atas elemen ini, opsi tersebut **tidak memiliki `payload`** — berbeda dengan opsi yang dimuat via `load` callback saat user mengetik.

Akibatnya, mengakses `tomselect.options[val]?.payload` untuk mengambil data produk (misal base UoM ID, harga terakhir) akan mengembalikan `undefined` pada baris pre-edit.

**Pattern yang harus diikuti**:
- Jangan andalkan `payload` untuk state yang perlu dilihat saat drawer dibuka di mode pre-edit.
- Simpan data penting di hidden inputs pada saat row di-render (server-side) dan saat `tsProd.on('change')` (client-side).
- Jika data hanya tersedia dari API (misal base UoM dari `/api/lookup/inventory/uom-conversions`), baca field diskriminan dari respons API tersebut (misal `isBase: true`) dan jangan andalkan nilai hidden input yang bisa saja sudah diubah oleh user (misal dari Pieces ke Box).

Pola ini sudah diterapkan di `stock-adjustment-form.js` pada fungsi `populateUomDropdown`.

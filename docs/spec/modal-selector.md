# Modal Selector Pattern

Dokumen ini menjadi standar reusable untuk selector berbasis **Bootstrap/Tabler Modal + HTMX fragment + page-specific JavaScript** pada aplikasi Solusi ERP.

## 1. Kapan memakai modal selector

Gunakan **autocomplete** jika:
- pilihan datanya pendek
- teks utama + subtext sudah cukup untuk keputusan user
- tidak perlu multi-select

Gunakan **modal selector** jika:
- user butuh konteks tabel yang lebih kaya
- perlu pagination / filtering HTMX
- perlu **single-select** atau **multi-select**
- ada rule **query-level exclusion** yang harus diterapkan sebelum data dirender

## 2. Arsitektur standar

1. **Controller endpoint selector** mengembalikan fragmen Thymeleaf khusus selector.
2. **Shared modal shell** menyediakan container modal kosong dengan `id` target HTMX yang stabil.
3. **Shared JS helper** membuka / menutup modal dan memuat fragmen awal.
4. **Page-specific JS** menjadi consumer yang:
   - membuka selector
   - membaca dataset baris yang dipilih
   - memetakan payload hasil seleksi ke form transaksi

## 3. Kontrak HTML

### 3.1 Modal shell

- gunakan `templates/fragments/modal-selector.html`
- `bodyId` harus sama dengan `id` root fragmen hasil selector
- root fragmen wajib punya kombinasi:
  - `id="..."`
  - `th:fragment="..."`

Ini penting agar pagination / search HTMX dengan `hx-target="#..."` tidak memicu `htmx:targetError`.

### 3.2 Selector fragment

Setiap selector fragment minimal memiliki:
- search form dengan `hx-get`
- `hx-target` ke root selector yang sama
- tabel hasil
- empty state
- pagination fragment generic bila hasilnya `Page<T>`

## 4. Kontrak data

Pisahkan dua bentuk data:

1. **Selector row DTO**
   - dipakai controller / view untuk render tabel selector
   - contoh: `PurchaseOrderPrSelectorRow`, `PurchaseOrderPrLineSelectorRow`

2. **Selection payload**
   - dikirim lewat `data-*` attribute pada row / button
   - dikonsumsi page-specific JS untuk mapping ke form

Rule:
- jangan kirim JPA entity ke view
- payload hanya berisi data yang benar-benar dibutuhkan consumer

## 5. Query-level exclusion rules

Selector **tidak boleh** sekadar menyembunyikan row di HTML untuk kasus eligibility. Filtering utama harus terjadi sebelum render.

Contoh rule:
- header selector mengecualikan dokumen yang **fully exhausted**
- line selector mengecualikan line yang:
  - remaining qty `<= 0`
  - sudah dipilih user di draft form saat ini

## 6. Single-select vs multi-select

### Single-select

Gunakan tombol aksi per baris, misalnya `.js-pr-selector-pick`.

### Multi-select

Gunakan checkbox per baris + tombol apply di footer fragmen, misalnya `.js-pr-line-selector-apply`.

Consumer JS bertanggung jawab memvalidasi bahwa minimal satu row dipilih sebelum apply.

## 7. Reference implementation pertama

### PO STANDARD -> PR selector

- endpoint: `/purchasing/purchase-orders/selectors/purchase-requisitions`
- mode: **single-select**
- hasil pilih:
  - `prId`
  - `supplier`, `facility`, `currency`

### PO STANDARD -> PR line selector

- endpoint: `/purchasing/purchase-orders/selectors/purchase-requisition-lines`
- mode: **multi-select**
- context:
  - `prId`
  - `excludePrLineIds[]`
- hasil pilih:
  - `prLineId`
  - `product`
  - `uom`
  - `remainingQuantity`
  - `estimatedUnitPrice`

## 8. Consumer checklist

Saat memakai modal selector di halaman baru:

1. buat row DTO selector khusus use case
2. buat endpoint controller khusus selector
3. buat fragmen Thymeleaf dengan root `id` + `th:fragment` yang sama
4. gunakan shared modal shell
5. gunakan shared modal selector helper untuk initial load
6. simpan payload penting di `data-*`
7. lakukan mapping ke form di page-specific JS
8. tulis test kontrak template / JS untuk memastikan wiring tidak regress

## 9. Testing strategy

Minimal coverage untuk pattern ini:
- **use case test** untuk rule exclusion / remaining qty
- **controller test** untuk endpoint selector + model attribute
- **template contract test** untuk memastikan form consumer merujuk helper / modal / hidden state yang benar


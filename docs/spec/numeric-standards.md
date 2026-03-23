# Numeric & Decimal Formatting Standard

Dokumen ini mendefinisikan standar input angka di sistem ERP untuk memastikan konsistensi tampilan (Thousand Separator) dan validitas data di database.

---

## 1. Tipe Input (Frontend)

DILARANG menggunakan `<input type="number">` jika ingin mendukung ribuan separator. Gunakan fragment standar dari `fragments/inputs.html`.

### A. Fragment `decimal`
Digunakan untuk angka pecahan (misal: Harga, Kuantitas, Kurs).
*   **Format**: `1,250,000.00`
*   **Penggunaan**:
    ```html
    <div th:replace="~{fragments/inputs :: decimal(field='price', label='Unit Price')}"></div>
    ```

### B. Fragment `integer`
Digunakan untuk angka bulat (misal: Urutan, Stok Barang tanpa pecahan).
*   **Format**: `1,250,000`
*   **Penggunaan**:
    ```html
    <div th:replace="~{fragments/inputs :: integer(field='quantity', label='Total Items')}"></div>
    ```

---

## 2. Library AutoNumeric

Sistem menggunakan library **AutoNumeric** untuk melakukan format *real-time* saat user mengetik.

### Aturan Inisialisasi & Helper:
1.  Setiap elemen numeric ditandai dengan class `.erp-number-decimal` atau `.erp-number-integer`.
2.  Inisialisasi dilakukan secara global via fungsi `initNumericInputs()`.
3.  **Generic Helper (`ErpNumeric`)**: Selalu gunakan helper ini di JavaScript untuk memanipulasi nilai:
    - `ErpNumeric.get(el)`: Mengambil angka murni (float) dari input yang terformat.
    - `ErpNumeric.set(el, val)`: Mengisi angka ke input dan otomatis memformat tampilannya.
4.  **Unformat**: Sistem men-set `unformatOnSubmit: false` karena pembersihan karakter separator dilakukan di sisi server (Spring Formatter).

---

## 3. Implementasi Backend (Parsing)

Backend menggunakan **`StandardBigDecimalFormatter`** (terdaftar di `FormatterConfig.java`) untuk menangani konversi otomatis.

### Cara Kerja Formatter:
1.  **Print**: Saat menampilkan data dari Java ke UI, formatter otomatis menambahkan ribuan separator (`,`) dan 2 angka desimal (`.00`).
2.  **Parse**: Saat user menekan Save, formatter melakukan `text.replaceAll(",", "")` sebelum mengubah string menjadi `BigDecimal`. 

Hal ini memungkinkan developer tetap bekerja dengan tipe data `BigDecimal` atau `Integer` di DTO tanpa harus melakukan manipulasi string manual.

---

## 4. Rekomendasi DTO
Untuk menjaga agar form tidak terlihat kosong (null) saat pertama kali dibuka, disarankan menginisialisasi nilai default di Request DTO:

```java
@Builder.Default
private BigDecimal amount = new BigDecimal("0.00");
```

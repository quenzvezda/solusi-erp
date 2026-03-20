# API Response & Form View DTO Specification

Dokumen ini menjelaskan standar pembungkusan data (data wrapping) untuk komunikasi antara Backend dan Frontend.

---

## 1. API Response Wrapping (`ApiResponse<T>`)

Semua endpoint `@ResponseBody` yang digunakan oleh form AJAX **WAJIB** mengembalikan objek `ApiResponse`. Hal ini memastikan frontend (JavaScript) selalu menerima struktur JSON yang konsisten.

### Struktur JSON:
```json
{
  "success": true,
  "message": "Data berhasil disimpan.",
  "data": { "id": 7, "name": "Produk A", ... },
  "validationErrors": null
}
```

### Penggunaan di Java:
- **Sukses**: `ApiResponse.success(message, data)`
- **Error Umum**: `ApiResponse.error(errorMessage)`
- **Error Validasi**: `ApiResponse.validationError(errorMap)`

---

## 2. Form View DTO (`FormViewDto<RQ, UI, RP>`)

Untuk mengatasi `LazyInitializationException` dan memisahkan tanggung jawab data, gunakan `FormViewDto` saat mengirim data dari Service ke Controller (khusus untuk tampilan Form Edit).

### Mengapa ini dibutuhkan?
Hibernate session seringkali sudah tertutup saat data sampai ke level View (Thymeleaf). `FormViewDto` memastikan semua mapping (termasuk akses ke relasi Lazy) dilakukan **di dalam transaksi service**.

### Struktur Class:
- **`request` (RQ)**: Berisi data binding form (Request DTO).
- **`ui` (UI)**: Berisi metadata label/display (UI Form DTO). Bisa `null` jika tidak ada autocomplete.
- **`audit` (RP)**: Berisi info audit untuk ditampilkan di UI (Response DTO).

### Contoh Implementasi:
```java
// Di Service
public FormViewDto<ProductRequest, ProductUIForm, ProductResponse> getProductEditView(Long id) {
    Product entity = repository.findById(id).orElseThrow(...);
    return FormViewDto.<ProductRequest, ProductUIForm, ProductResponse>builder()
            .request(mapper.toRequest(entity))
            .ui(mapper.toUIForm(entity))
            .audit(mapper.toResponse(entity))
            .build();
}
```

---

## 3. Global Exception Handling

Validasi dan error server ditangani secara terpusat oleh `GlobalExceptionHandler.java`. 

- **Validasi (`@Valid`)**: Otomatis dikonversi menjadi `ApiResponse.validationError` dengan HTTP 400.
- **RuntimeException**: Otomatis dikonversi menjadi `ApiResponse.error` dengan HTTP 500 (jika request adalah AJAX).
- **Fallback**: Tetap mengembalikan halaman HTML (403, 404, 500) jika request datang dari navigasi browser biasa.

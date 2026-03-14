# Auditing & Audit Trace Specification

Dokumen ini menjelaskan implementasi teknis mekanisme auditing (pelacakan siapa yang membuat/mengubah data) serta cara penampilannya di antarmuka pengguna (UI).

## 1. Auditor Aware (`AuditorAwareImpl`)

Sistem menggunakan `AuditorAwareImpl` untuk secara otomatis mengisi ID user pada kolom `@CreatedBy` dan `@LastModifiedBy` di level entitas JPA.

- **Mekanisme**: ID diambil langsung dari `SecurityUser` principal yang ada di `SecurityContext`.
- **Performance Optimized**: Implementasi ini **DILARANG** melakukan query database (repository call) di dalam method `getCurrentAuditor()` untuk menghindari *infinite recursion* saat JPA Flush.
- **Fallback**: Jika tidak ada user yang terautentikasi (misal: proses seeding data), sistem akan otomatis menggunakan ID `1` (System/Admin).

## 2. Base Audit DTO (`BaseAuditResponse`)

Untuk standarisasi data audit di tingkat API dan UI, gunakan `com.solusi.erp.core.dto.BaseAuditResponse` sebagai superclass untuk **semua** Request dan Response DTO.

### Field yang Diwariskan:
- `id` & `version`
- `createdByName` & `createdDate`
- `updatedByName` & `updatedDate`

### Display Helpers:
Class ini menyediakan helper methods untuk tampilan Thymeleaf:
- `getCreatedInitials()` / `getUpdatedInitials()`: Menghasilkan 1-2 huruf inisial dari nama user.
- `getFormattedCreatedDate()` / `getFormattedUpdatedDate()`: Format tanggal `dd MMM yyyy, HH:mm`.

## 3. Otomatisasi Backend

### 3.1. Mapping (`AuditMapperHelper`)
MapStruct menggunakan `AuditMapperHelper` untuk mengisi data audit dari Entity ke DTO secara otomatis selama proses mapping (`toResponse` atau `toRequest`).

### 3.2. Automatic Model Attribute (`AuditInfoInterceptor`)
Sistem menyertakan `AuditInfoInterceptor` yang berjalan setelah controller (postHandle). 
- **Tugas**: Mencari objek dalam `Model` yang merupakan turunan `BaseAuditResponse`.
- **Hasil**: Secara otomatis menyediakan variabel generic **`${auditInfo}`** ke dalam model.
- **Manfaat**: Controller tidak perlu lagi menulis `model.addAttribute("auditInfo", ...)` secara manual.

## 4. Frontend Implementation (Thymeleaf)

Tampilkan "Record Info" pada setiap halaman **Edit** menggunakan fragment terpusat.

### Cara Penggunaan:
Sisipkan kode berikut di dalam `card-body` (sebelum footer):

```html
<div th:if="${auditInfo != null and auditInfo.id != null}" 
     th:replace="~{fragments/audit-info :: audit-info(${auditInfo})}"></div>
```

Fragment `fragments/audit-info.html` akan merender strip informasi berisi:
- Avatar inisial user pembuat/pengubah.
- Nama lengkap user (atau username).
- Tanggal dan jam pembuatan/perubahan terakhir.

## 5. Aturan Penting (Mandatory)
1.  **Standarisasi DTO**: Semua Request DTO wajib extend `BaseAuditResponse` agar data audit tersedia saat loading data untuk di-edit.
2.  **Jangan Mapping Balik**: Saat proses `toEntity` (Request -> Entity), kolom audit **Wajib di-ignore**. Biarkan Spring Data JPA yang mengelolanya.
3.  **EqualsAndHashCode**: Selalu tambahkan `@EqualsAndHashCode(callSuper = true)` pada DTO turunan.

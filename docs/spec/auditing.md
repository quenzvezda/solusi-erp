# Auditing & Audit Trace Specification

Dokumen ini menjelaskan implementasi teknis mekanisme auditing (pelacakan siapa yang membuat/mengubah data) di aplikasi ERP.

## 1. Auditor Aware (`AuditorAwareImpl`)

Sistem menggunakan `AuditorAwareImpl` untuk secara otomatis mengisi ID user pada kolom `@CreatedBy` dan `@LastModifiedBy`.

- **Mekanisme**: ID diambil langsung dari `SecurityUser` principal yang ada di `SecurityContext`.
- **Performance Optimized**: Implementasi ini **DILARANG** melakukan query database (repository call) di dalam method `getCurrentAuditor()` untuk menghindari *infinite recursion* saat JPA Flush.
- **Fallback**: Jika tidak ada user yang terautentikasi (misal: proses seeding data), sistem akan otomatis menggunakan ID `1` (System/Admin).

## 2. Base Audit Response DTO

Untuk meminimalisir duplikasi kode pada DTO, gunakan `com.solusi.erp.core.dto.BaseAuditResponse` sebagai superclass untuk semua Response DTO.

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductResponse extends BaseAuditResponse {
    private String code;
    private String name;
}
```

Field yang diwariskan otomatis:
- `id`
- `version`
- `createdByName` (String representation)
- `createdDate`
- `updatedByName` (String representation)
- `updatedDate`

## 3. Otomatisasi Mapping (`AuditMapperHelper`)

Karena kita menggunakan MapStruct, proses ekstraksi data audit dari Entity ke DTO dilakukan secara otomatis melalui `AuditMapperHelper`.

### Cara Penggunaan di Mapper:
```java
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE, 
        uses = {AuditMapperHelper.class})
public interface MyMapper {
    MyResponse toResponse(MyEntity entity);
}
```

### Tugas `AuditMapperHelper`:
1.  **Populate ID & Metadata**: Mengisi field `id`, `version`, `createdDate`, dan `updatedDate`.
2.  **Display Name Resolution**:
    - Jika user profile tersedia, gunakan `fullName`.
    - Jika profile kosong, gunakan `username`.
    - Jika relasi user null, gunakan ID fisik atau label "SYSTEM".

## 4. Aturan Penting (Mandatory)
1.  **Jangan Mapping Audit ke Entity**: Saat proses `toEntity` (Request -> Entity), kolom audit **Wajib di-ignore** atau dibiarkan null. Biarkan Spring Data JPA yang mengisinya secara otomatis saat proses simpan.
2.  **EqualsAndHashCode**: Selalu tambahkan `@EqualsAndHashCode(callSuper = true)` pada DTO yang meng-extend `BaseAuditResponse`.

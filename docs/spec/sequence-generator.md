# Technical Standard: Generic Sequence Generator

Sistem ini menggunakan mekanisme **Template-Based Sequence Generator** yang terpusat untuk mengelola kode unik di seluruh modul aplikasi.

## 1. Database Schema (`system_sequences`)
*   `module_code`: Identifier unik per modul (PK).
*   `format_pattern`: Template kode (Contoh: `PCAT-{seq}`).
*   `pad_length`: Panjang digit angka untuk `{seq}`.
*   `reset_cycle`: Siklus reset nomor urut (`DAILY`, `MONTHLY`, `YEARLY`, `NEVER`).

## 2. Template Placeholders
| Placeholder | Deskripsi | Contoh |
| :--- | :--- | :--- |
| `{seq}` | Nomor urut ter-increment | `0001`, `00001` |
| `{date:FORMAT}` | Tanggal sistem (Java DateTime Format) | `{date:ddMM}`, `{date:yyyy}` |

## 3. Concurrency Strategy
`SequenceGeneratorService.generate()` menggunakan `@Transactional(propagation = Propagation.REQUIRES_NEW)`.
*   **Tujuan**: Nomor urut yang sudah diambil akan langsung di-commit ke database, meskipun transaksi utama (simpan data bisnis) mengalami *rollback*. 
*   **Manfaat**: Mencegah bentrok nomor ganda saat beban tinggi (*Race Condition*).

## 4. Cara Penggunaan di Service
1.  Suntikkan `SequenceGeneratorService`.
2.  Panggil `.generate("MODULE_NAME")`.

```java
@Transactional
public void create(MyRequest request) {
    MyEntity entity = mapper.toEntity(request);
    entity.setCode(sequenceGeneratorService.generate("MY_MODULE"));
    repository.save(entity);
}
```

## 5. UI Standarisasi
*   Input untuk `code` di form harus diatur sebagai `readonly` dan `bg-light`.
*   Tampilkan placeholder `[Auto Generated]` saat mode **Create**.

## 6. Registered Module Codes

| Module Code | Format Pattern | Pad Length | Reset Cycle | Entity |
|:---|:---|:---|:---|:---|
| `PART` | `BP-{date:yyyy}-{seq}` | 4 | YEARLY | Party |
| `ADJ` | `ADJ-{date:yyyyMM}-{seq}` | 4 | MONTHLY | StockAdjustment |
| `NEWS` | `NEWS-{seq}` | 4 | NEVER | News |

> Untuk modul baru, tambahkan entry ke `system_sequences` via Flyway migration mengikuti pola di atas (`updated_by_user_id` dan `updated_date` wajib diisi).

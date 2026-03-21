# Global Error Handling & Smart Redirect Specification

Dokumen ini menjelaskan mekanisme penanganan error terpusat dan standar navigasi otomatis saat terjadi kegagalan pada halaman form.

---

## 1. Mekanisme Pusat (`GlobalExceptionHandler`)

Sistem menggunakan `GlobalExceptionHandler` untuk menangkap semua *exception* yang terjadi di level Controller. Handler ini bersifat cerdas:
- **AJAX/HTMX Request**: Mengembalikan JSON `ApiResponse` dengan HTTP 500/400.
- **Normal Request (GET)**: Merender halaman error HTML (403, 404, 500) atau melakukan **Smart Redirect**.

## 2. Smart Redirect Pattern

Smart Redirect adalah fitur untuk mengalihkan pengguna kembali ke halaman daftar (*list page*) secara otomatis jika terjadi error saat memuat halaman **Create** atau **Edit** (misalnya ID tidak ditemukan).

### Cara Penggunaan
Tambahkan anotasi `@DefaultRedirectUrl` di level class Controller.

```java
@Controller
@RequestMapping("/inventory/products")
@DefaultRedirectUrl // Otomatis redirect ke "/inventory/products" jika error
public class ProductController { ... }
```

### Aturan Deteksi URL:
1.  **Otomatis**: Jika anotasi ditulis tanpa parameter (`@DefaultRedirectUrl`), sistem akan mengambil path dari `@RequestMapping` di level class.
2.  **Eksplisit**: Jika URL redirect berbeda dengan base mapping, tentukan secara manual: `@DefaultRedirectUrl("/another-path")`.

## 3. Alur Kerja UX
1.  User mengakses `/inventory/products/edit/999`.
2.  Service melempar `RuntimeException` karena data tidak ada.
3.  `GlobalExceptionHandler` menangkap error tersebut.
4.  Sistem mendeteksi anotasi `@DefaultRedirectUrl`.
5.  Sistem melakukan **Redirect** ke `/inventory/products`.
6.  Pesan error dikirim menggunakan `FlashMap` (Flash Attribute).
7.  Halaman daftar muncul dengan **Alert Merah** di bagian atas berisi pesan kesalahan asli.

## 4. Keuntungan
- **Boilerplate Reduction**: Controller tidak perlu lagi menggunakan blok `try-catch` hanya untuk melakukan redirect manual.
- **Konsistensi UX**: User tidak pernah terdampar di halaman error "putih" atau halaman 500 yang memutuskan alur kerja.
- **Clean Code**: Logika navigasi dipisahkan dari logika bisnis di level Controller.

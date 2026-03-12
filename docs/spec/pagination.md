# Technical Standard: Dynamic Pageable Resolver

Sistem ini menggunakan **Custom Argument Resolver** untuk menangani paginasi secara cerdas berdasarkan preferensi pengguna yang tersimpan di database.

## 1. Komponen Utama
*   **`UserPreferencePageableResolver`**: Sebuah resolver yang meng-extend `PageableHandlerMethodArgumentResolver` milik Spring Data.
*   **`WebMvcConfig`**: Mendaftarkan resolver tersebut agar aktif secara global.

## 2. Alur Resolusi (Priority Order)
Saat sebuah request masuk ke Controller yang memiliki parameter `Pageable`, resolver akan menentukan `pageSize` dengan urutan prioritas berikut:

1.  **Explicit Request**: Jika URL mengandung `?size=X`, sistem akan menggunakan nilai `X`.
2.  **User Preference**: Jika parameter `size` kosong, sistem akan mengambil nilai `defaultPageSize` dari `UserProfile` user yang sedang login.
3.  **System Default**: Jika user belum login atau profil tidak ditemukan, sistem akan menggunakan default Spring (biasanya 10).

## 3. Cara Penggunaan di Controller
Developer tidak perlu lagi melakukan inisialisasi `PageRequest` manual. Cukup terima parameter `Pageable`.

```java
// BEST PRACTICE
@GetMapping
public String list(@RequestParam(required = false) String keyword, 
                   Pageable pageable, 
                   Model model) {
    // pageable sudah berisi pageSize yang sesuai preferensi user
    model.addAttribute("page", service.findAll(keyword, pageable));
    return "my-module/list";
}
```

## 4. Keuntungan Standar Ini
*   **Konsistensi UI**: Semua halaman list akan menampilkan jumlah data yang sama sesuai keinginan user.
*   **Boilerplate Reduction**: Menghilangkan logika manual pengambilan session/profil di setiap Controller.
*   **Maintainability**: Perubahan logika paginasi global cukup dilakukan di satu file (`UserPreferencePageableResolver`).

## 5. Frontend Implementation (Thymeleaf)
Untuk merender navigasi paginasi di *view*, DILARANG keras menulis blok HTML (`<ul class="pagination">...`) secara manual. Selalu gunakan fragment terpusat yang sudah disediakan.

```html
<!-- Cukup panggil fragment ini di bagian bawah tabel Anda -->
<div th:replace="~{fragments/table :: pagination(${page})}"></div>
```

**Fitur dari Fragment Paginasi Generic:**
1.  **Page Windowing**: Secara otomatis membatasi jumlah tombol navigasi maksimal 7 angka (3 ke kiri, 1 aktif, 3 ke kanan) untuk mencegah tampilan *break* pada *dataset* yang besar.
2.  **Full Navigation**: Menyediakan tombol standar *First* (`<<`), *Prev* (`<`), *Next* (`>`), dan *Last* (`>>`).
3.  **Automatic URL Retention**: Menggunakan `ServletUriComponentsBuilder` untuk secara otomatis mempertahankan semua parameter *query string* (seperti `keyword`, `sort`, `parentId`) saat pengguna berpindah halaman, tanpa perlu Anda *passing* secara manual.

## 6. Standar UI Tambahan: Status Label
Untuk konsistensi UI pada kolom "Status" (misalnya field `isActive`) di dalam tabel paginasi, disarankan menggunakan desain *badge outline* dengan *dot* indikator warna seperti contoh berikut (diambil dari rancangan tabel `Party` dan `Tax`):

```html
<span class="badge badge-outline text-green" th:if="${item.isActive}">
    <span class="badge-dot bg-success me-1"></span>
    <span th:text="#{label.active}">Active</span>
</span>
<span class="badge badge-outline text-red" th:unless="${item.isActive}">
    <span class="badge-dot bg-danger me-1"></span>
    <span th:text="#{label.inactive}">Inactive</span>
</span>
```

# Global Search Menu Specification

Dokumen ini menjelaskan spesifikasi dan implementasi teknis fitur **Global Search Menu** di navbar aplikasi Solusi ERP. Fitur ini memungkinkan pengguna untuk mencari dan menavigasi menu/halaman dengan cepat berdasarkan kata kunci.

---

## 1. Arsitektur Keamanan (Security-Aware Search)

Pencarian menu ini bersifat **Security-Aware**, artinya hasil pencarian hanya akan menampilkan menu yang boleh diakses oleh pengguna berdasarkan _Authorities_ (Permission) yang dimilikinya saat ini.

1. **Mapping Menu**: Setiap menu direpresentasikan oleh entitas `PermissionGroup`.
2. **Relasi Permission**: Sebuah `PermissionGroup` memiliki banyak `Permission`.
3. **Kriteria Akses**: Jika seorang user memiliki **salah satu** saja _permission_ yang terdaftar di dalam suatu `PermissionGroup`, maka menu tersebut dianggap layak (eligible) untuk muncul di hasil pencarian user tersebut.

---

## 2. Komponen Backend

### A. Data Transfer Object (`MenuSearchDto`)

Digunakan untuk mengirim hasil pencarian yang minimalis dan ter-lokalisasi ke frontend.

```java
public record MenuSearchDto(
    String urlPath,
    String name,
    String subText // Berisi breadcrumb ter-lokalisasi
) {}
```

### B. Service Layer (`MenuSearchService`)

Method `searchMenus` melakukan hal berikut:
1. Mendapatkan `Authentication` dari `SecurityContextHolder`.
2. Mengekstrak daftar `Authorities` user.
3. Memanggil repository untuk mencari `PermissionGroup` yang:
    - Memiliki `Permission` di dalam daftar `Authorities` user.
    - Namanya (ID/EN) atau kodenya mengandung keyword pencarian.

### C. RestController (`MenuSearchController`)

Hanya menyediakan satu endpoint publik (terautentikasi):
- `GET /api/lookup/menus?q={keyword}&limit={n}`
- Endpoint ini tidak memerlukan permission khusus karena sudah difilter di dalam logic service berdasarkan permission user yang login.

---

## 3. Komponen Frontend (Navbar)

### A. UI Element (Header)

Input pencarian berada di `fragments/header.html` menggunakan elemen `<select>` yang disulap menjadi search box oleh **TomSelect**.

```html
<select id="global-menu-search" class="form-select" th:placeholder="#{placeholder.common.search_menu}"></select>
```

### B. Inisialisasi TomSelect (Layout Master)

Fitur ini diinisialisasi secara global di `layout/master.html`.

- **Debouncing**: Menggunakan delay 100ms untuk menghindari beban request berlebih.
- **Rendering**: Menampilkan nama menu di baris pertama dan breadcrumb di baris kedua (sebagai `subText`).
- **Navigation**: Saat opsi dipilih (`onChange`), browser akan langsung melakukan pengalihan halaman (`window.location.href`).

```javascript
new TomSelect('#global-menu-search', {
    valueField: 'urlPath',
    labelField: 'name',
    searchField: ['name'],
    // ... logic load & render ...
    onChange: function(value) {
        if (value) window.location.href = value;
    }
});
```

---

## 4. Cara Menambahkan Menu Baru ke Pencarian

Untuk menambahkan halaman/modul baru agar muncul di pencarian:
1. Pastikan modul tersebut sudah memiliki `PermissionGroup` yang terdaftar di tabel `permission_groups`.
2. Pastikan field `url_path` sudah diisi dengan benar.
3. Hubungkan semua `Permission` terkait modul tersebut ke `permission_group_id` yang sesuai.
4. Berikan _permission_ tersebut kepada _Role_ pengguna.

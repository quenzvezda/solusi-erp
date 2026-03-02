# Specification: Generic Table Sorting

## 1. Overview
Sistem ini menggunakan mekanisme pengurutan (sorting) otomatis yang terintegrasi dengan Spring Data JPA `Pageable` dan UI Tabler. Tujuannya adalah meminimalkan kode berulang di level Controller dan menyediakan pengalaman pengguna yang konsisten.

## 2. Backend Implementation
### 2.1. TableSortingAdvice
Semua metadata sorting dikelola secara global melalui `TableSortingAdvice`. Class ini mencegat objek `Pageable` di setiap request dan menyuntikkan atribut berikut ke dalam Thymeleaf Model:
- `sortField`: Nama kolom yang sedang diurutkan (String).
- `sortDir`: Arah pengurutan (`asc` atau `desc`).
- `currentUri`: URI saat ini untuk membangun link secara dinamis.

### 2.2. Spring Data JPA Integration
Pengurutan dilakukan secara otomatis oleh Spring Data JPA selama parameter `sort` dikirimkan melalui URL dengan format: `?sort=fieldName,direction`.

## 3. Frontend Implementation
### 3.1. Sortable Fragment
Gunakan fragment generic di `fragments/table.html` untuk setiap header tabel yang ingin didukung fitur sorting.

**Contoh Penggunaan:**
```html
<th th:replace="~{fragments/table :: sortable('name', #{label.name})}">Nama</th>
```

### 3.2. Pagination Integration
Agar *state* pengurutan tidak hilang saat berpindah halaman, parameter `sort` wajib disertakan pada link pagination.

**Contoh Link Pagination:**
```html
<a th:href="@{${currentUri}(page=${i}, keyword=${keyword}, sort=${sortField != '' ? sortField + ',' + sortDir : ''})}">
```

## 4. UI/UX Standards
- **Hover Effect**: Header yang dapat di-sort akan memiliki kursor *pointer* dan *background highlight* biru transparan.
- **Icons**:
    - `ti-arrows-sort`: Indikator bahwa kolom dapat di-sort (warna abu-abu transparan).
    - `ti-arrow-narrow-up`: Indikator sedang di-sort ASC (warna biru).
    - `ti-arrow-narrow-down`: Indikator sedang di-sort DESC (warna biru).
- **Tabler Suppression**: CSS global di `master.html` secara otomatis menyembunyikan panah sorting bawaan Tabler/Bootstrap (`::after`) untuk menghindari tampilan ganda.

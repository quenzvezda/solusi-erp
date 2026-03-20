# Layout Standard with Page-Specific Scripts

Dokumen ini menjelaskan arsitektur standar untuk layout utama dan cara menyisipkan JavaScript khusus per-halaman secara andal.

## Latar Belakang Masalah
Menyisipkan tag `<script>` langsung di dalam fragmen konten yang dikirim ke layout utama (`master.html`) terbukti tidak andal. Beberapa konfigurasi Thymeleaf dapat "membersihkan" atau tidak merender tag script tersebut, menyebabkan JavaScript di halaman anak gagal total dieksekusi.

## Solusi: Slot `pageScripts`
Untuk mengatasi ini, `layout/master.html` telah dimodifikasi untuk menyediakan "slot" khusus di akhir `<body>` yang bernama `pageScripts`.

### Implementasi di `master.html`
```html
<body th:fragment="layout(content, pageScripts)">
    ...
    <!-- Global Scripts -->
    <script>...</script>

    <!-- Optional Page-specific scripts -->
    <th:block th:replace="${pageScripts} ?: ~{}"></th:block>
</body>
```
-   `layout(content, pageScripts)`: Fragmen `layout` sekarang menerima dua parameter.
-   `th:block th:replace="${pageScripts} ?: ~{}"`: Blok ini akan merender fragmen `pageScripts` yang dikirim dari halaman anak. Jika tidak ada, ia akan merender fragmen kosong (`~{}`) untuk mencegah error.

### Implementasi di Halaman Anak (Contoh: `form.html`)
Setiap halaman yang membutuhkan JavaScript-nya sendiri **WAJIB** mengikuti pola ini:

1.  **Membungkus Script**: Seluruh tag `<script>` lokal dibungkus dalam satu `div` dengan `th:fragment`.
2.  **Mengirim Fragmen**: Memperbarui `th:replace` pada `<body>` untuk mengirim fragmen konten dan fragmen script.

```html
<body th:replace="~{layout/master :: layout(~{:: .content-area}, ~{:: #page-scripts})}">

    <!-- 1. Area Konten Utama -->
    <div class="content-area">
        ... (isi form, tabel, dll)
    </div>

    <!-- 2. Area Script Khusus Halaman -->
    <div id="page-scripts" th:fragment="pageScripts">
        <script>
            // JavaScript spesifik untuk halaman ini ada di sini
            document.addEventListener('DOMContentLoaded', function() {
                // ...
            });
        </script>
    </div>

</body>
```
-   `~{:: .content-area}`: Mengambil `div` konten.
-   `~{:: #page-scripts}`: Mengambil `div` yang berisi script.
-   `th:fragment="pageScripts"`: Menamai `div` ini agar bisa dipanggil dari `<body>`.

Dengan pola ini, eksekusi JavaScript di setiap halaman menjadi terjamin dan terisolasi dengan baik.

# Iterative Task Protocol (Repetitive Refactoring Workflow)

Protokol ini digunakan untuk menangani tugas-tugas yang bersifat repetitif, tersebar di banyak file, atau memerlukan audit koding secara massal (seperti refactoring komponen UI, standarisasi API, atau penyesuaian i18n).

## 1. Filosofi Kerja
*   **Context Safety**: AI dilarang melakukan perubahan "buta" dalam jumlah besar sekaligus.
*   **Stateful Tracking**: Setiap progres wajib dicatat di file pelacak eksternal agar mudah dipantau dan dilanjutkan jika sesi terputus.
*   **Atomic Validation**: Verifikasi dilakukan per file atau per blok kecil, bukan di akhir seluruh tugas.

## 2. Prosedur Tahapan

### Tahap A: Discovery & Mapping
1.  **Scanning**: Gunakan tool pencarian (glob/grep) untuk mengidentifikasi semua file target (misal: `src/**/*.html`).
2.  **Create Track File**: Buat file pelacak di **Root Project** agar selalu mudah diakses oleh agen (bahkan jika sesi terputus).
    *   **Naming Convention**: Gunakan prefix `task-` atau `refactor-` agar jelas bahwa file ini bersifat sementara (misal: `task-refactor-autocomplete.md`).
3.  **Checklist Creation**: Masukkan semua file yang ditemukan ke dalam checklist Markdown:
    *   `[ ] path/to/file/FileName.ext`
4.  **Grouping**: Kelompokkan checklist berdasarkan modul atau package untuk memudahkan navigasi.

### Tahap B: Iterative Execution (The Loop)
Untuk setiap item dalam checklist:
1.  **Mark In-Progress**: Ubah status menjadi `[~]`.
2.  **Deep Read**: Baca konten file secara utuh untuk memahami konteksnya.
3.  **Analyze & Implement**:
    *   Bandingkan dengan standar yang diminta (referensi ke `docs/spec/` atau `AGENTS.md`).
    *   Lakukan perubahan menggunakan tool `replace` atau `write_file` secara spesifik.
    *   Jika file sudah sesuai standar, jangan lakukan perubahan tapi tetap laporkan.
4.  **Logging**: Tambahkan catatan singkat di bagian `## Log Analisis & Implementasi` (lihat format di bawah).
5.  **Mark Done**: Ubah status menjadi `[x]`.

### Tahap C: Final Validation
*   Setelah semua item `[x]`, lakukan pemeriksaan menyeluruh (misal: jalankan aplikasi, cek build, atau sampling audit).

## 3. Format File Pelacak (Template)
Gunakan struktur ini saat membuat file `.md` baru untuk pelacakan tugas:

```markdown
# [Judul Tugas]

## Overview
Deskripsi singkat tujuan dan standar yang ingin dicapai.

## Checklist Progres
### Modul [Nama Modul]
- [x] `path/file1.java` (Selesai)
- [~] `path/file2.java` (Sedang dikerjakan)
- [!] `path/file3.java` (Ada keraguan/anomali)
- [?] `path/file4.java` (Perlu klarifikasi standar)
- [ ] `path/file5.java` (Belum disentuh)

## Log Analisis & Implementasi
### [Nama File/Item]
- **Status**: (Selesai / Skip / Doubts [! atau ?])
- **Catatan**: Deskripsi perubahan atau alasan keraguan.
```

## 4. Penanganan Kasus Khusus
*   **Keraguan (Doubts)**: Jika menemukan anomali atau pola yang tidak sesuai ekspektasi, agen menandai checklist dengan `[!]` (keraguan teknis) atau `[?]` (keraguan standar), mencatat detailnya di log, lalu **WAJIB LANJUT** ke file berikutnya. User akan meninjau semua temuan di akhir sesi.
*   **No Batching**: Dilarang menggabungkan pengerjaan > 1 file dalam satu siklus tool call kecuali atas permintaan eksplisit user.
*   **Cleanup**: Setelah tugas selesai 100% dan dikonfirmasi oleh user, agen dapat menghapus file `task-*.md` atau `refactor-*.md` tersebut agar root project tetap bersih.

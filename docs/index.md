# Solusi ERP Documentation Index

Selamat datang di pusat dokumentasi sistem Solusi ERP. Gunakan dokumen ini sebagai peta navigasi untuk memahami arsitektur, standar teknis, dan modul bisnis yang ada.

## 1. Core Guidelines
- **[AGENTS.md](AGENTS.md)**: Panduan utama untuk AI Assistant dan pengembang. Berisi tech stack, standar coding, dan instruksi fundamental. **WAJIB BACA PERTAMA KALI.**

## 2. Documentation Folders
Dokumentasi dikelompokkan ke dalam folder berikut berdasarkan konteksnya:

- **[/architecture](architecture/)**: Berisi Class Diagram dan pola desain tingkat tinggi (seperti `BaseModel`).
- **[/database](database/)**: Berisi ERD (Entity Relationship Diagram) dan script dummy data (Seeder).
- **[/modules](modules/)**: Penjelasan fungsional dan aturan bisnis untuk setiap fitur spesifik (Inventory, Master Data, dll).
- **[/spec](spec/)**: **[PENTING]** Spesifikasi teknis horizontal/shared yang digunakan di seluruh aplikasi. Lihat **[Spec Index](spec/index.md)**.
- **[/roadmap](roadmap/)**: Rencana pengembangan masa depan dan pelacakan standardisasi arsitektur.

## 3. How to Use
Jika Anda baru bergabung dengan proyek ini atau sedang melakukan troubleshooting:
1. Baca **AGENTS.md** untuk memahami aturan main.
2. Jika masalah berkaitan dengan komponen UI atau pola coding tertentu (misal: cara kerja Autocomplete atau HTMX), carilah dokumen yang relevan di folder **[/spec](spec/index.md)**.
3. Jika masalah berkaitan dengan aturan bisnis modul, cari di folder **[/modules](modules/)**.

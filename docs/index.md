# Solusi ERP Documentation Index

Selamat datang di pusat dokumentasi sistem Solusi ERP. Gunakan dokumen ini sebagai peta navigasi untuk memahami arsitektur, standar teknis, dan modul bisnis yang ada.

## 1. Core Guidelines
- **[AGENTS.md](AGENTS.md)**: Panduan utama untuk AI Assistant dan pengembang. Berisi tech stack, standar coding, dan instruksi fundamental. **WAJIB BACA PERTAMA KALI.**

## 2. Documentation Folders
Dokumentasi dikelompokkan ke dalam folder berikut berdasarkan konteksnya:

- **[/architecture](architecture/)**: Berisi Class Diagram dan pola desain tingkat tinggi.
    - [Clean Architecture + DDD + CQRS Standard](architecture/clean-ddd-cqrs-standard.md) (Standard Baru)
    - [Base Model Pattern](architecture/base-model-pattern.md)
    - [Smart Delete Pattern](architecture/smart-delete-pattern.md) — Pola delete fleksibel: hard-delete jika tidak dipakai, soft-delete jika masih direferensikan
    - [JaCoCo Coverage Guide](architecture/jacoco-coverage.md) — Standar code coverage: versi, exclude strategy, threshold, cara baca laporan
    - [Accounting Foundation Architecture](architecture/accounting-foundation.md) — Sprint 1: COA, Accounting Schema, Fiscal Year/Period — dependency diagram, patterns, auto-journal flow
- **[/database](database/)**: Berisi ERD dan script dummy data.
- **[/tests](tests/)**: Panduan dan utilitas pengujian level web (Controller + Thymeleaf).
    - [Web-layer Testing Guidelines](tests/web-layer-testing.md)
    - [Playwright Smoke Test Guide](tests/playwright-smoke-test-guide.md) — Panduan lengkap smoke test E2E: cara start server, kamus interaksi TomSelect/Flatpickr/AutoNumeric/Line Items/Serial Drawer, fallback Node.js
- **[/modules](modules/)**: Penjelasan fungsional dan aturan bisnis untuk setiap fitur spesifik (Inventory, Master Data, dll).
    - **[/modules/accounting](modules/accounting/)**: Sprint 1 — Accounting Foundation
        - [Chart of Accounts (COA)](modules/accounting/coa.md) — Hierarki akun, business rules, RBAC
        - [Accounting Schema](modules/accounting/accounting-schema.md) — Konfigurasi auto-journal event → debit/credit
        - [Fiscal Year & Accounting Period](modules/accounting/fiscal-year-period.md) — Period Guard, status lifecycle
- **[/spec](spec/)**: **[PENTING]** Spesifikasi teknis horizontal/shared yang digunakan di seluruh aplikasi. Lihat **[Spec Index](spec/index.md)**.
- **[/roadmap](roadmap/)**: Rencana pengembangan masa depan dan pelacakan standardisasi arsitektur.
- **[/workflow](workflow/)**: Panduan prosedur kerja agen (seperti tugas repetitif, troubleshooting, dll).

## 3. How to Use
Jika Anda baru bergabung dengan proyek ini atau sedang melakukan troubleshooting:
1. Baca **AGENTS.md** untuk memahami aturan main.
2. Jika masalah berkaitan dengan komponen UI atau pola coding tertentu (misal: cara kerja Autocomplete, HTMX, atau JavaScript per-halaman), carilah dokumen yang relevan di folder **[/spec](spec/index.md)**.
3. Jika masalah berkaitan dengan aturan bisnis modul, cari di folder **[/modules](modules/)**.

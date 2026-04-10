# Roadmap: Generic Approval System

Status saat ini: **Fase 1 Selesai (Core Engine & POC)**

## Fase 1: Core Engine & Event-Driven POC (Selesai ✅)
*   [x] Perancangan skema database polimorfik (`appr_requests`, `appr_histories`).
*   [x] Implementasi **Domain Layer** (Pure Java) dengan Aggregate Root `ApprovalRequest`.
*   [x] Implementasi **Application Layer** (Use Cases: Create, Process).
*   [x] Implementasi **Infrastructure Layer** (JPA Adapter, Event Listeners).
*   [x] Mekanisme **Event-Driven Communication** (News -> Approval via `ApprovalRequestedEvent`, Approval -> News via `ApprovalCompletedEvent`).
*   [x] Standardisasi **AuditMetadata** untuk menangani *Optimistic Locking* (`version`) di level Domain.
*   [x] Verifikasi End-to-End melalui `NewsApprovalIntegrationTest`.

## Fase 2: UI Components & Frontend Integration (Sedang Berjalan 🚧)
*   [x] Pembuatan **Generic Thymeleaf Fragment** untuk Timeline Approval.
*   [x] Integrasi **HTMX** untuk memuat history approval secara asinkron.
*   [x] Pembuatan Form Approval (Tombol Approve, Reject, Forward) yang muncul secara dinamis berdasarkan `currentApproverId`.
*   [x] Penambahan pesan i18n untuk status dan aksi approval.

## Fase 3: Peningkatan Fitur Enterprise (Masa Depan 🚀)
*   [ ] **Approval Matrix/Matrix Template**: Otomatisasi penentuan approver berdasarkan nilai transaksi atau kriteria tertentu.
*   [ ] **Multi-level Approval**: Dukungan untuk urutan approver (Step 1, Step 2, dst).
*   [ ] **Role-based Approver**: Memberikan approval kepada Jabatan (Role) alih-alih individu (Party).
*   [ ] **Notification System**: Integrasi dengan notifikasi lonceng di UI atau Email saat ada dokumen yang butuh approval.
*   [ ] **Delegation**: Fitur untuk mendelegasikan tugas approval saat user sedang cuti.

## Fase 4: Rollout ke Modul Lain
*   [ ] Implementasi Listener di modul `Inventory` (Stock Adjustment).
*   [ ] Implementasi Listener di modul `Purchasing` (Purchase Order).
*   [ ] Implementasi Listener di modul `Sales` (Sales Order).

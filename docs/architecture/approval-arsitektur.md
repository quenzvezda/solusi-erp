# Arsitektur Generic Approval System

Status: **Phase 2 Selesai — Storage, Web Layer & UI Terpasang** ✅

Sistem Approval yang dirancang agar *generic* dan dapat diimplementasikan ke berbagai modul transaksi tanpa memerlukan *hard-coding* relasi database antar modul (mencegah *tight-coupling*).

## 1. Desain Database (Polymorphic Relation)

Modul ini menggunakan pendekatan **Polymorphic Relation** melalui `referenceType` dan `referenceId`.

**A. `appr_requests` (Approval Request)**
Menyimpan status terkini dari proses persetujuan sebuah dokumen.
*   `id`: BIGINT (PK)
*   `referenceType`: String (Contoh: `"NEWS"`, `"STOCK_ADJUSTMENT"`) -> Kunci generic.
*   `referenceId`: BIGINT (ID dari dokumen transaksi terkait).
*   `status`: Enum (`PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`).
*   `currentApproverId`: Relasi ke tabel `Party`.
*   `version`: BIGINT (Optimistic Locking).

**B. `appr_histories` (Approval History)**
Menyimpan jejak langkah (log) persetujuan dokumen.
*   `id`: BIGINT (PK)
*   `requestId`: FK ke `appr_requests`.
*   `action`: Enum (`REQUESTED`, `APPROVE_AND_FINISH`, `REJECTED`).
*   `actorId`: Relasi ke `Party`.
*   `notes`: Text (Alasan reject atau catatan).
*   `actionDate`: LocalDateTime.
*   `signatureKey`: String NULLABLE — Storage key gambar tanda tangan.

**C. `appr_signatures` (Digital Signature)**
Menyimpan metadata dari tanda tangan digital yang di-upload ke MinIO.
*   `id`: BIGINT (PK)
*   `requestId`: FK ke `appr_requests` (UNIQUE — 1 request = 1 signature).
*   `storageKey`: String — Path di dalam bucket MinIO (e.g. `signatures/42/uuid.png`).
*   `bucketName`: String — Nama bucket MinIO (default: `approval-signatures`).
*   `storedAt`: LocalDateTime.
*   `signerUserId`: BIGINT — ID user yang menandatangani.

## 2. Kemurnian Domain & AuditMetadata

Untuk menjaga prinsip **Clean Architecture & DDD**, field teknis (`id`, `version`, audit fields) tidak diletakkan sebagai atribut utama di level Aggregate Root, melainkan dibungkus dalam objek **`AuditMetadata`**.

```java
// domain.model.ApprovalRequest
public class ApprovalRequest {
    private final AuditMetadata metadata; // Berisi ID dan Version
    private final String referenceType;
    private final Long referenceId;
    private ApprovalStatus status;
    // ...
}
```

## 3. Strategi Integrasi (Event-Driven)

Integrasi antar modul dilakukan sepenuhnya secara **Asinkron/Decoupled** menggunakan Spring Application Events.

1.  **Pemicu (Modul Bisnis):** Menerbitkan `ApprovalRequestedEvent(refType, refId, requester)`.
2.  **Penerima (Modul Approval):** Mendengarkan event tersebut dan membuat data di `appr_requests`.
3.  **Penyelesaian (Modul Approval):** Setelah diproses, menerbitkan `ApprovalCompletedEvent(refType, refId)`.
4.  **Reaksi (Modul Bisnis):** Mendengarkan event penyelesaian (filter berdasarkan `refType`) dan mengeksekusi logika finalisasi (misal: Publish berita atau Update stok).

## 4. Storage Layer — StorageProvider Port

Penyimpanan tanda tangan mengikuti **Port & Adapter pattern** yang sama dengan modul lain.

```
core/storage/domain/port/StorageProvider.java     ← interface (port)
core/storage/infrastructure/adapter/MinioStorageAdapter.java  ← impl (adapter)
```

Use case (`SaveApprovalSignatureUseCaseImpl`) hanya bergantung pada interface `StorageProvider`, tidak mengetahui detail MinIO. Bucket dibuat otomatis pada startup via `@PostConstruct ensureBucketExists()`.

**Konfigurasi (application.yaml / .env):**
```yaml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: ${MINIO_BUCKET_NAME:approval-signatures}
```

## 5. Web Layer (REST + HTMX)

`ApprovalController` (generic, bukan per-modul) menyediakan:

| Method | URL                              | Keterangan                                          |
|--------|----------------------------------|-----------------------------------------------------|
| `POST` | `/approval/{id}/process`         | Approve/reject + upload signature base64 opsional   |
| `GET`  | `/approval/{id}/history`         | HTMX fragment: timeline history                     |
| `GET`  | `/approval/{id}/signature`       | JSON: URL gambar tanda tangan                       |

## 6. Desain UI/UX (HTMX + Thymeleaf)

UI approval dibuat sebagai **Generic Fragment** yang dapat disematkan di halaman detail modul apapun.

### Cara Menyematkan Approval ke Modul Baru

**Langkah 1 — Modul publish event:**
```java
// Di use case "Submit for Approval"
eventPublisher.publishEvent(new ApprovalRequestedEvent("STOCK_ADJUSTMENT", adjId, requesterUsername));
```

**Langkah 2 — Template detail sematkan fragment:**
```html
<!-- Di adjustment/detail.html -->
<div th:replace="~{fragments/approval :: approve-reject-panel(${approvalRequestId})}"></div>
```

**Langkah 3 — Listener di modul reaksi:**
```java
@EventListener
void onApprovalCompleted(ApprovalCompletedEvent event) {
    if (!"STOCK_ADJUSTMENT".equals(event.referenceType())) return;
    // finalkan stok...
}
```

> Tidak perlu menyentuh kode modul `approval` sama sekali — fully decoupled.

### Fragment yang Tersedia

```html
<!-- Panel aksi Approve/Reject + timeline (paling sering dipakai) -->
<div th:replace="~{fragments/approval :: approve-reject-panel(${approvalRequestId})}"></div>

<!-- Timeline saja (HTMX-loaded) -->
<div th:replace="~{fragments/approval :: timeline}"></div>

<!-- Modal signature capture (sudah disertakan global di master.html) -->
```

History dimuat asinkron via HTMX. Setelah action selesai, JS mendispatch event `approvalProcessed` yang memicu reload timeline.

## 7. Integrasi Saat Ini & Rencana

| Modul            | Status                     | referenceType      |
|------------------|----------------------------|--------------------|
| News             | ✅ Terintegrasi (prototype) | `"NEWS"`           |
| Stock Adjustment | 🔜 Rencana berikutnya       | `"STOCK_ADJUSTMENT"` |
| Purchase Order   | 📋 Backlog                  | `"PURCHASE_ORDER"` |

# Arsitektur Generic Approval System

Status: **Phase 9 Selesai — Reference Code, Split Pages, Timeline Actor→Assignee, Rich Text** ✅

Sistem Approval yang dirancang agar *generic* dan dapat diimplementasikan ke berbagai modul transaksi tanpa memerlukan *hard-coding* relasi database antar modul (mencegah *tight-coupling*).

## 1. Desain Database (Polymorphic Relation)

Modul ini menggunakan pendekatan **Polymorphic Relation** melalui `referenceType` dan `referenceId`.

**A. `appr_requests` (Approval Request)**
Menyimpan status terkini dari proses persetujuan sebuah dokumen.
*   `id`: BIGINT (PK)
*   `referenceType`: String (Contoh: `"NEWS"`, `"STOCK_ADJUSTMENT"`) -> Kunci generic.
*   `referenceId`: BIGINT (ID dari dokumen transaksi terkait).
*   `status`: Enum (`PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`).
*   `currentApproverId`: BIGINT — Relasi ke `Party`. Menunjuk approver yang saat ini bertanggung jawab.
*   `version`: BIGINT (Optimistic Locking).

**B. `appr_histories` (Approval History)**
Menyimpan jejak langkah (log) persetujuan dokumen.
*   `id`: BIGINT (PK)
*   `requestId`: FK ke `appr_requests`.
*   `action`: Enum (`REQUESTED`, `APPROVE_AND_FINISH`, `APPROVE_AND_FORWARD`, `FORWARD`, `REJECTED`).
*   `actorId`: BIGINT — Relasi ke `Party` (siapa yang melakukan aksi).
*   `targetApproverId`: BIGINT NULLABLE — Relasi ke `Party` (approver tujuan forward).
*   `notes`: Text (Alasan/catatan — **wajib** untuk semua aksi).
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

1.  **Pemicu (Modul Bisnis):** Menerbitkan `ApprovalRequestedEvent(refType, refId, requester, approverId)`.
2.  **Penerima (Modul Approval):** Mendengarkan event tersebut dan membuat data di `appr_requests` dengan `currentApproverId = approverId`.
3.  **Penyelesaian (Modul Approval):** Setelah `APPROVE_AND_FINISH`, menerbitkan `ApprovalCompletedEvent(refType, refId)`.
4.  **Reaksi (Modul Bisnis):** Mendengarkan event penyelesaian (filter berdasarkan `refType`) dan mengeksekusi logika finalisasi (misal: Publish berita atau Update stok).

### Flow Bisnis (Multi-Step Approval)

```
User A (author) membuat News → Submit for Approval (pilih User B sebagai approver)
  ↓ ApprovalRequestedEvent(NEWS, newsId, userA, partyB)
  ↓
ApprovalRequest dibuat: status=PENDING, currentApproverId=partyB
  ↓
User B login → Dashboard: "1 Pending Approval" → /common/approval
  ↓ View & Process → halaman News detail → Approval panel (4 tombol)
  ↓
  ├─ APPROVE AND FINISH: signature + reason → status COMPLETED → News PUBLISHED
  ├─ APPROVE AND FORWARD: signature + pilih approver C + reason → currentApprover pindah ke C
  ├─ FORWARD: pilih approver C + reason → currentApprover pindah ke C (tanpa approve)
  └─ REJECT: reason → status REJECTED
```

### Approver Selection

Saat submit for approval, user memilih approver dari **Party** yang memiliki `PartyRoleType` dengan code `APPROVER`. Autocomplete menggunakan endpoint:
```
GET /api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=search
```

### Approver Guard

Tombol aksi approval hanya muncul jika:
- `currentUser.partyId == approvalRequest.currentApproverId`
- Status approval masih `PENDING`

User yang bukan current approver hanya melihat timeline history.

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

`ApprovalController` (`/common/approval`) menyediakan:

| Method | URL                                    | Keterangan                                                    |
|--------|----------------------------------------|---------------------------------------------------------------|
| `GET`  | `/common/approval`                     | List pending approvals untuk current user (filter by partyId) |
| `POST` | `/common/approval/{id}/process`        | Process approval: 4 actions + signature base64 opsional       |
| `GET`  | `/common/approval/{id}/history`        | HTMX fragment: timeline history + isCurrentApprover flag      |
| `GET`  | `/common/approval/{id}/signature`      | JSON: URL gambar tanda tangan                                 |

### Process Endpoint — Request Body
```json
{
  "action": "APPROVE_AND_FINISH | APPROVE_AND_FORWARD | FORWARD | REJECTED",
  "notes": "required for all actions",
  "signatureBase64": "data:image/png;base64,... (required for APPROVE_*)",
  "targetApproverId": 123  // required for FORWARD, APPROVE_AND_FORWARD
}
```

### User-Party Resolution
`ApprovalController.resolvePartyId()` menggunakan `SecurityUser.user().getPartyId()` untuk mendapatkan Party ID dari user yang sedang login. User yang tidak terhubung ke Party akan mendapat error.

## 6. Desain UI/UX (HTMX + Thymeleaf)

UI approval dibuat sebagai **Generic Fragment** yang dapat disematkan di halaman detail modul apapun.

### Cara Menyematkan Approval ke Modul Baru

**Langkah 1 — Modul publish event dengan approver:**
```java
// Di use case "Submit for Approval"
eventPublisher.publishEvent(new ApprovalRequestedEvent("STOCK_ADJUSTMENT", adjId, requesterUsername, approverId));
```

**Langkah 2 — Template detail sematkan fragment dengan guard:**
```html
<!-- Di adjustment/detail.html -->
<th:block th:if="${approvalRequestId != null}">
    <div th:replace="~{fragments/approval :: approve-reject-panel(${approvalRequestId}, ${isCurrentApprover}, ${approvalStatus})}"></div>
</th:block>
<!-- Drawer history (sebelum </body>) -->
<th:block th:if="${approvalRequestId != null}">
    <div th:replace="~{fragments/approval :: approval-history-drawer(${approvalRequestId})}"></div>
</th:block>
```

**Langkah 3 — Controller pass approvalStatus + isCurrentApprover:**
```java
// Di detail endpoint controller
approvalRequest.ifPresent(req -> {
    model.addAttribute("approvalRequestId", req.getId());
    model.addAttribute("approvalStatus", req.getStatus());
    boolean isCurrentApprover = false;
    if (principal instanceof SecurityUser su) {
        Long partyId = su.user().getPartyId();
        isCurrentApprover = partyId != null && partyId.equals(req.getCurrentApproverId())
                && req.getStatus() == ApprovalStatus.PENDING;
    }
    model.addAttribute("isCurrentApprover", isCurrentApprover);
});
```

**Langkah 4 — Listener di modul reaksi:**
```java
@EventListener(condition = "#event.referenceType == 'STOCK_ADJUSTMENT'")
void onApprovalCompleted(ApprovalCompletedEvent event) {
    // finalkan stok...
}
```

> Tidak perlu menyentuh kode modul `approval` sama sekali — fully decoupled.

### Fragment yang Tersedia

```html
<!-- Panel aksi (4 tombol) + badge status — sematkan di right sidebar -->
<!-- requestId: Long, isCurrentApprover: boolean, approvalStatus: ApprovalStatus enum -->
<div th:replace="~{fragments/approval :: approve-reject-panel(${approvalRequestId}, ${isCurrentApprover}, ${approvalStatus})}"></div>

<!-- Drawer riwayat approval (sematkan sebelum </body>) -->
<div th:replace="~{fragments/approval :: approval-history-drawer(${approvalRequestId})}"></div>

<!-- Tombol buka drawer (sematkan di page header btn-list) -->
<button th:if="${approvalRequestId != null}" type="button" class="btn btn-white"
        onclick="ErpDrawer.open('drawer-approval-history')">
    <i class="ti ti-history me-1"></i>
    <span th:text="#{label.approval.history}">Riwayat Persetujuan</span>
</button>

<!-- Modal signature capture (sudah disertakan global di master.html) -->
<!-- 4 modal: approve-finish, approve-forward, forward, reject -->
```

### Status Badge

| Status    | Warna Badge        | i18n key                              |
|-----------|--------------------|---------------------------------------|
| PENDING   | `bg-yellow-lt`     | `label.approval.status.pending`       |
| COMPLETED | `bg-success-lt`    | `label.approval.status.completed`     |
| REJECTED  | `bg-danger-lt`     | `label.approval.status.rejected`      |
| CANCELLED | `bg-secondary-lt`  | `label.approval.status.cancelled`     |

### Approval History Drawer

History dimuat via HTMX GET ke `/common/approval/{requestId}/history` setiap kali drawer dibuka.
Setelah action selesai, JS mendispatch event `approvalProcessed` pada `document.body`
yang memicu reload otomatis history di dalam drawer.

| Action | Signature | Target Approver | Notes | Status Setelah |
|--------|-----------|-----------------|-------|----------------|
| APPROVE_AND_FINISH | ✅ Wajib | - | ✅ Wajib | COMPLETED |
| APPROVE_AND_FORWARD | ✅ Wajib | ✅ Wajib | ✅ Wajib | PENDING (approver pindah) |
| FORWARD | - | ✅ Wajib | ✅ Wajib | PENDING (approver pindah) |
| REJECTED | - | - | ✅ Wajib | REJECTED |

History dimuat asinkron via HTMX. Setelah action selesai, JS mendispatch event `approvalProcessed` yang memicu reload timeline.

## 7. Integrasi Saat Ini & Rencana

| Modul            | Status                     | referenceType      |
|------------------|----------------------------|--------------------|
| News             | ✅ Terintegrasi (prototype) | `"NEWS"`           |
| Stock Adjustment | 🔜 Rencana berikutnya       | `"STOCK_ADJUSTMENT"` |
| Purchase Order   | 📋 Backlog                  | `"PURCHASE_ORDER"` |

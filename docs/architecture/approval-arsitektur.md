# Arsitektur Generic Approval System

Status: **Core Implemented (Fase 1 Selesai)**

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

## 4. Desain UI/UX (HTMX + Thymeleaf)

UI approval akan dibuat sebagai **Generic Fragment** yang dapat disematkan di halaman detail modul apapun.

```html
<!-- Implementasi di Halaman Detail -->
<div th:replace="~{fragments/approval :: timeline('NEWS', ${news.id})}"></div>
```

History akan dimuat secara asinkron menggunakan HTMX untuk menjaga performa loading halaman utama.

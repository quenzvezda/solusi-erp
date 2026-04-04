# Approval Integration Guide

Panduan ini menjelaskan cara menyematkan sistem Approval ke modul baru.
Referensi implementasi: modul **News** (`common/news`).

---

## Overview

Sistem Approval bersifat **polymorphic** dan **generic**: satu tabel `appr_requests` menampung
approval dari modul apapun via `reference_type` + `reference_id`.
UI-nya juga generic: tiga Thymeleaf fragment yang di-*include* ke halaman detail manapun.

```
┌─────────────────────────────────────────────────────────────────┐
│ Page Header                                                      │
│ [← Back] [Edit]                  [📋 Riwayat Persetujuan]       │
├─────────────────────────────────────────────────────────────────┤
│ Left: Content (8/12)   │  Right: Info + Approval Card (4/12)    │
│                        │  ┌───────────────────────────────────┐ │
│                        │  │ 🔔 Approval      [badge: PENDING]  │ │
│                        │  │ ─────────────────────────────── │ │
│                        │  │ [✓ Selesaikan] [✓→ Teruskan]    │ │
│                        │  │ [→ Forward]    [✗ Tolak]         │ │
│                        │  └───────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│ Audit Info strip (full width)                                    │
└─────────────────────────────────────────────────────────────────┘
[Drawer slides in from right: Riwayat Persetujuan]
```

---

## Langkah Integrasi

### 1. Domain — Publish Event saat Submit

Modul target (mis. StockAdjustment) perlu:
- Command use case `SubmitXxxForApprovalUseCase` yang memanggil `CreateApprovalRequestUseCase`
- Atau publish `ApprovalRequestedEvent` yang didengarkan oleh `OnApprovalRequestedListener`

```java
// Contoh dari News:
approvalUseCase.execute("NEWS", newsId, requesterId, assignedApproverId);
```

Parameter:
- `referenceType`: string konstanta (contoh: `"NEWS"`, `"STOCK_ADJUSTMENT"`)
- `referenceId`: ID entitas yang diajukan persetujuan
- `requesterId`: Party ID user yang mengajukan
- `assignedApproverId`: Party ID approver yang dipilih

### 2. Controller — Resolve Approval State

Di method `detail()` controller, resolve tiga model attributes:

```java
@GetMapping("/{id}")
public String detail(@PathVariable Long id, Model model,
                     @AuthenticationPrincipal UserDetails principal) {
    // ... load your domain entity ...

    Optional<ApprovalRequest> approvalRequest =
            approvalRequestRepository.findByReference("YOUR_TYPE", id);
    approvalRequest.ifPresent(req -> {
        model.addAttribute("approvalRequestId", req.getId());
        model.addAttribute("approvalStatus", req.getStatus());
        boolean isCurrentApprover = false;
        if (principal instanceof SecurityUser securityUser) {
            Long partyId = securityUser.user().getPartyId();
            isCurrentApprover = partyId != null
                    && partyId.equals(req.getCurrentApproverId())
                    && req.getStatus() == ApprovalStatus.PENDING;
        }
        model.addAttribute("isCurrentApprover", isCurrentApprover);
    });

    return "your/module/detail";
}
```

> **Penting**: `ApprovalRequestRepository.findByReference()` harus dipanggil dalam konteks
> `@Transactional(readOnly = true)` agar Hibernate dapat melakukan lazy loading histories
> dengan benar. Repository impl sudah di-annotate dengan `@Transactional` pada setiap method.

### 3. Template — Include 3 Fragment

Di template detail (`your/module/detail.html`):

#### 3a. Tombol Approval History (di page header `btn-list`)

```html
<button th:if="${approvalRequestId != null}"
        type="button" class="btn btn-white"
        onclick="ErpDrawer.open('drawer-approval-history')">
    <i class="ti ti-history me-1"></i>
    <span th:text="#{label.approval.history}">Riwayat Persetujuan</span>
</button>
```

#### 3b. Approval Decision Card (di right sidebar)

```html
<th:block th:if="${approvalRequestId != null}">
    <div th:replace="~{fragments/approval :: approve-reject-panel(
        ${approvalRequestId}, ${isCurrentApprover}, ${approvalStatus})}"></div>
</th:block>
```

Fragment ini menampilkan:
- Badge status (kuning=PENDING, hijau=COMPLETED, merah=REJECTED)
- 4 tombol aksi jika `isCurrentApprover = true`

#### 3c. Approval History Drawer (sebelum `</body>`)

```html
<th:block th:if="${approvalRequestId != null}">
    <div th:replace="~{fragments/approval :: approval-history-drawer(${approvalRequestId})}"></div>
</th:block>
```

Drawer ini memuat timeline approval via HTMX dari `/common/approval/{id}/history`.

### 4. Permission Seed

Tambah permission ke role admin dan buat menu item. Ikuti pola migrasi V35:

```sql
-- Contoh: untuk StockAdjustment
INSERT INTO permissions (name, description, permission_group_id) VALUES
    ('STOCK_ADJUSTMENT_READ', 'Lihat Stock Adjustment', @group_id),
    ('STOCK_ADJUSTMENT_CREATE', 'Buat Stock Adjustment', @group_id),
    ('APPROVAL_PROCESS', 'Proses Approval', @approval_group_id);  -- sudah ada
```

---

## Fragment Signatures

```
fragments/approval :: approve-reject-panel(requestId, isCurrentApprover, approvalStatus)
  requestId      : Long — ID dari approval_request
  isCurrentApprover : Boolean — true jika user yang login adalah approver saat ini
  approvalStatus : ApprovalStatus enum — status saat ini (PENDING/COMPLETED/REJECTED/CANCELLED)

fragments/approval :: approval-history-drawer(requestId)
  requestId      : Long — ID dari approval_request

fragments/approval :: signature-capture-modal
  (global, sudah di-include di layout/master.html)
```

---

## Approval Actions

| Action             | Status result | Requires signature | Forward target |
|--------------------|---------------|--------------------|----------------|
| APPROVE_AND_FINISH | COMPLETED     | ✅                 | -              |
| APPROVE_AND_FORWARD| PENDING       | ✅                 | ✅             |
| FORWARD            | PENDING       | -                  | ✅             |
| REJECTED           | REJECTED      | -                  | -              |

Semua aksi **wajib** mengisi `notes` (reason).

---

## Modul yang Sudah Mengimplementasikan

| Modul          | reference_type  | Status   |
|----------------|-----------------|----------|
| News           | `"NEWS"`        | ✅ Done  |
| StockAdjustment | `"STOCK_ADJ"` | 🔲 Next  |

---

## Catatan Arsitektur

- **Polymorphic**: Approval tidak tahu domain spesifik — hanya `referenceType + referenceId`
- **Event-driven**: `ApprovalRequestedEvent` dikirim setelah approval dibuat, listener dapat update status entitas domain
- **Signature storage**: Disimpan di MinIO bucket `approval-signatures`, key format: `{approvalHistoryId}/{timestamp}.png`
- **Status update**: Setelah APPROVE_AND_FINISH, modul harus mengupdate statusnya sendiri (News → PUBLISHED) via event atau direct call

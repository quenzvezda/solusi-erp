# Generic Approval Events Brainstorm

Date: 2026-06-16

## Executive Summary

POC Kafka publisher awal berhasil membuktikan alur ERP outbox ke Kafka dengan event `PurchaseOrderApproved` pada topic `erp.procurement.events.v1`. Setelah evaluasi ulang boundary bisnis, event tersebut dinilai terlalu spesifik ke Procurement/Purchase Order.

Approval system di ERP bersifat generic dan digunakan lintas modul melalui `referenceType` dan `referenceId`. Karena itu event notification yang lebih tepat adalah generic approval event dari modul `common.approval`, bukan event khusus PO dari listener procurement.

Keputusan arah baru:

- Stop / replace Kafka event PO-specific untuk kebutuhan notification.
- Gunakan topic domain approval: `erp.approval.events.v1`.
- Publish event untuk aksi approval generic.
- NotificationService consume approval event dan kirim email berdasarkan `action` serta `notificationTarget`.
- Data bisnis detail seperti total amount dan currency tidak wajib untuk email MVP.

## Current State

Implementasi yang sudah terbukti berjalan:

- Kafka local KRaft single node berjalan via Docker Compose profile `messaging`.
- Topic `erp.procurement.events.v1` sudah dibuat secara eksplisit.
- PO approve menghasilkan message Kafka `PurchaseOrderApproved`.
- Row `outbox_events` berubah menjadi `PUBLISHED`.

Namun event tersebut akan diganti oleh generic approval event agar arsitektur lebih rapi dan scalable untuk modul lain.

## Problem Reframing

Pertanyaan awal: "Saat PO approved, NotificationService kirim email ke pembuat PO."

Setelah review flow approval generic, problem yang lebih tepat adalah:

"Saat approval action terjadi pada dokumen apapun, ERP publish generic approval event agar NotificationService dapat mengirim email ke pihak yang perlu bertindak atau perlu tahu."

Dengan framing ini, event tidak boleh terikat pada PO. PO hanya salah satu dokumen yang menggunakan approval.

## Approval Actions In Scope

MVP approval notification mencakup:

- `REQUESTED`
- `FORWARD`
- `APPROVE_AND_FORWARD`
- `APPROVE_AND_FINISH`
- `REJECTED`

Optional / later:

- `CANCELLED`

Catatan: domain approval sudah punya `CANCELLED`, tetapi UI approval utama saat ini belum memakai flow cancel generic. Jika nanti dipakai, recipient paling masuk akal adalah current approver.

## Recipient Rules

| Action | Recipient |
| --- | --- |
| `REQUESTED` | Approver pertama |
| `FORWARD` | Target approver baru |
| `APPROVE_AND_FORWARD` | Target approver baru |
| `APPROVE_AND_FINISH` | Requester / pembuat dokumen |
| `REJECTED` | Requester / pembuat dokumen |
| `CANCELLED` | Optional later: current approver |

NotificationService sebaiknya tidak menghitung aturan recipient dari nol. ERP harus mengirim `notificationTarget` eksplisit agar NotificationService tetap sederhana dan decoupled.

## Proposed Topic

Topic baru:

```text
erp.approval.events.v1
```

Topic lama:

```text
erp.procurement.events.v1
```

Untuk notification MVP, topic lama akan ditinggalkan / tidak dipakai agar tidak terjadi duplicate notification.

## Proposed Event Envelope

Tetap memakai envelope standar outbox Kafka:

```json
{
  "eventId": "uuid",
  "eventType": "ApprovalActionOccurred",
  "eventVersion": 1,
  "source": "erp-monolith",
  "occurredAt": "2026-06-16T00:00:00Z",
  "correlationId": "approval-request-id",
  "aggregateType": "ApprovalRequest",
  "aggregateId": "123",
  "payload": {}
}
```

## Proposed Payload

```json
{
  "approvalRequestId": 123,
  "referenceType": "PURCHASE_ORDER",
  "referenceId": 5,
  "referenceCode": "PO-202606-00004",
  "documentLabel": "Purchase Order",
  "documentPath": "/purchasing/purchase-orders/view/5",
  "action": "APPROVE_AND_FINISH",
  "status": "COMPLETED",
  "actorPartyId": 3,
  "actorName": "Bpk. Budi Santoso",
  "targetApproverPartyId": null,
  "targetApproverName": null,
  "targetApproverEmail": null,
  "currentApproverPartyId": null,
  "requesterPartyId": 2,
  "requesterName": "Bpk. Administrator Utama",
  "requesterEmail": "admin@solusierp.com",
  "notificationTarget": {
    "role": "REQUESTER",
    "partyId": 2,
    "name": "Bpk. Administrator Utama",
    "email": "admin@solusierp.com"
  },
  "notes": "Approved",
  "actedAt": "2026-06-16T00:00:00Z"
}
```

## Document Link Strategy

ERP should publish relative document path:

```text
/purchasing/purchase-orders/view/5
```

NotificationService owns public base URL:

```properties
ERP_PUBLIC_BASE_URL=http://localhost:8080
```

Email link is assembled in NotificationService:

```text
{ERP_PUBLIC_BASE_URL}{documentPath}
```

Reasoning:

- Avoid environment-specific full URL in event payload.
- Same Kafka event remains valid across local/staging/production.
- NotificationService can render the email link for its environment.

## Data Availability

Generic approval already has:

- `approvalRequestId`
- `referenceType`
- `referenceId`
- `referenceCode`
- `status`
- `currentApproverId`
- histories with `action`, `actorId`, `targetApproverId`, `notes`, `actionDate`

Data to enrich before publishing:

- party/user names
- email addresses
- `documentLabel`
- `documentPath`
- `notificationTarget`

Business-specific fields such as `totalAmount` and `currencyCode` are intentionally out of scope for MVP notification email.

## Implementation Direction

1. Add generic approval integration event payload and factory.
2. Add `documentPath` support to approval request creation, or provide a reference link resolver.
3. Prefer MVP option: module business supplies `documentPath` when requesting approval.
4. Extend approval event publisher to publish internal events for all scoped actions.
5. Persist generic approval event to outbox through `IntegrationEventPublisher`.
6. Change default Kafka topic to `erp.approval.events.v1`.
7. Stop PO-specific Kafka publication for notification use case.
8. Update docs/spec event contract.
9. Update Kafka topic bootstrap script / variables to include `erp.approval.events.v1`.

## Open Design Choice

`documentPath` source:

- MVP recommendation: supplied by business module at approval request time.
- Longer-term option: `ApprovalReferenceLinkResolver` per `referenceType`.

MVP recommendation is simpler and explicit.

## Recommendation

Proceed with generic approval event refactor before building NotificationService consumer. This prevents NotificationService from being tied to Purchase Order and gives a reusable notification foundation for all approval-enabled modules.


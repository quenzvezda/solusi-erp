# Event Contracts

Dokumen ini adalah kontrak manual integration event ERP. POC belum memakai Schema Registry, Avro, Protobuf, atau shared contract library.

## Envelope Standard

Semua integration event memakai envelope berikut:

```json
{
  "eventId": "3f4d8f3a-7f2a-4d37-92f0-111111111111",
  "eventType": "ApprovalActionOccurred",
  "eventVersion": 1,
  "source": "erp-monolith",
  "occurredAt": "2026-06-15T03:30:00Z",
  "correlationId": "55",
  "aggregateType": "ApprovalRequest",
  "aggregateId": "55",
  "payload": {}
}
```

Rules:

- `eventId` dipakai consumer untuk idempotency.
- `eventType` dan `eventVersion` menentukan kontrak payload.
- `occurredAt` adalah ISO-8601 string.
- `aggregateId` dan Kafka message key harus stabil agar event aggregate yang sama masuk partition yang konsisten.
- Field baru di payload harus optional untuk menjaga consumer lama.
- Breaking change wajib menaikkan `eventVersion`.

## ApprovalActionOccurred v1

Topic:

```text
erp.approval.events.v1
```

Kafka message key:

```text
ApprovalRequest aggregate id
```

Payload:

```json
{
  "approvalRequestId": 55,
  "referenceType": "PURCHASE_ORDER",
  "referenceId": 42,
  "referenceCode": "PO-2606-00001",
  "documentLabel": "Purchase Order",
  "documentPath": "/purchasing/purchase-orders/view/42",
  "action": "APPROVE_AND_FINISH",
  "status": "COMPLETED",
  "actorPartyId": 99,
  "actorName": "Approver Party",
  "targetApproverPartyId": null,
  "targetApproverName": null,
  "targetApproverEmail": null,
  "currentApproverPartyId": null,
  "requesterPartyId": 10,
  "requesterName": "Party Requester",
  "requesterEmail": null,
  "notificationTarget": {
    "role": "REQUESTER",
    "partyId": 10,
    "name": "Party Requester",
    "email": null
  },
  "notes": "Approved",
  "actedAt": "2026-06-15T03:30:00Z"
}
```

Business meaning:

- Event menyatakan aksi approval generic terjadi pada dokumen ERP.
- Event bukan command untuk mengirim email; NotificationService hanya salah satu consumer.
- `notificationTarget.email` boleh `null`; consumer harus skip/log jika tidak ada recipient valid.
- `documentPath` adalah path relatif. Consumer merangkai link memakai base URL environment masing-masing.
- Field bisnis spesifik seperti `totalAmount` dan `currencyCode` sengaja tidak masuk kontrak MVP agar event tetap generic.

Known document paths:

| `referenceType` | `documentPath` |
| --- | --- |
| `NEWS` | `/common/news/{id}` |
| `PURCHASE_ORDER` | `/purchasing/purchase-orders/view/{id}` |
| `PURCHASE_REQUISITION` | `/purchasing/purchase-requisitions/view/{id}` |
| `PURCHASE_RETURN` | `/purchasing/purchase-returns/view/{id}` |

Recipient rules:

| Action | `notificationTarget.role` | Target |
| --- | --- | --- |
| `REQUESTED` | `TARGET_APPROVER` | approver pertama |
| `FORWARD` | `TARGET_APPROVER` | approver baru |
| `APPROVE_AND_FORWARD` | `TARGET_APPROVER` | approver baru |
| `APPROVE_AND_FINISH` | `REQUESTER` | pembuat/requester dokumen |
| `REJECTED` | `REQUESTER` | pembuat/requester dokumen |

## Superseded: PurchaseOrderApproved v1

`PurchaseOrderApproved v1` pada topic `erp.procurement.events.v1` adalah kontrak POC awal dan sudah digantikan untuk flow notification oleh `ApprovalActionOccurred v1`.

# Event Contracts

Dokumen ini adalah kontrak manual integration event ERP. POC belum memakai Schema Registry, Avro, Protobuf, atau shared contract library.

## Envelope Standard

Semua integration event memakai envelope berikut:

```json
{
  "eventId": "3f4d8f3a-7f2a-4d37-92f0-111111111111",
  "eventType": "PurchaseOrderApproved",
  "eventVersion": 1,
  "source": "erp-monolith",
  "occurredAt": "2026-06-15T03:30:00Z",
  "correlationId": "42",
  "aggregateType": "PurchaseOrder",
  "aggregateId": "42",
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

## PurchaseOrderApproved v1

Topic:

```text
erp.procurement.events.v1
```

Kafka message key:

```text
PurchaseOrder aggregate id
```

Payload:

```json
{
  "poId": 42,
  "poNumber": "PO-2606-00001",
  "requesterUserId": 7,
  "requesterPartyId": 10,
  "requesterName": "Party Requester",
  "requesterEmail": null,
  "approverPartyId": 99,
  "approverName": "Approver Party",
  "approvedAt": "2026-06-15T03:30:00Z",
  "totalAmount": 15000000.00,
  "currencyCode": "IDR"
}
```

Business meaning:

- Event menyatakan fakta bahwa PO sudah disetujui.
- Event bukan command untuk mengirim email.
- `requesterEmail` boleh `null`; consumer seperti NotificationService harus skip/log jika tidak ada recipient valid.
- `requesterName` dipilih dari Party name, fallback ke UserProfile full name, lalu username.
- `approverName` dipilih dari Party name milik actor approval, fallback ke `Party {id}` atau `Approver`.

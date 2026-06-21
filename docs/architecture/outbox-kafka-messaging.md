# Outbox Kafka Messaging

Dokumen ini menjelaskan foundation messaging keluar dari ERP monolith ke Kafka.

## Tujuan

ERP tetap menjadi monolith utama untuk transaksi bisnis. Kafka dipakai sebagai jalur integration event keluar, bukan sebagai dependency sinkron ke service lain.

Untuk POC generic approval, event yang dipublish adalah `ApprovalActionOccurred v1` ke topic domain:

```text
erp.approval.events.v1
```

## Tanggung Jawab ERP

- ERP menyimpan perubahan bisnis dan integration event dalam transaksi database yang sama.
- ERP hanya menyatakan fakta bisnis, misalnya sebuah aksi approval terjadi pada dokumen ERP.
- ERP tidak tahu NotificationService, email, SMTP, Mailpit, atau consumer lain.
- ERP tidak menghapus row outbox otomatis pada POC agar mudah diaudit.

## Outbox Pattern

Flow teknis:

1. Approval action terjadi (`REQUESTED`, `FORWARD`, `APPROVE_AND_FORWARD`, `APPROVE_AND_FINISH`, atau `REJECTED`).
2. Use case approval menyimpan `ApprovalRequest` dan history dalam transaksi.
3. `ApprovalEventPublisherAdapter` membuat `ApprovalActionOccurred` business event.
4. `IntegrationEventPublisher` menyimpan event ke tabel `outbox_events`.
5. Setelah transaksi commit, scheduled publisher mengambil row `PENDING` atau retryable `FAILED`.
6. Publisher mengirim JSON envelope ke Kafka memakai `KafkaTemplate`.
7. Jika publish sukses, row ditandai `PUBLISHED`.
8. Jika publish gagal, row ditandai `FAILED`, `attempt_count` naik, `last_error` disimpan, dan `next_attempt_at` diisi.

## Enabled / Disabled

Konfigurasi default:

```yaml
erp:
  messaging:
    enabled: false
```

Saat disabled:

- ERP tetap dapat menjalankan flow bisnis normal.
- `IntegrationEventPublisher` menjadi no-op, sehingga tidak menulis event outbox.
- Scheduled publisher langsung return dan tidak mencoba publish ke Kafka.
- Deploy OCI aman walaupun Kafka atau NotificationService belum disiapkan.

Saat enabled:

- Approval use case menyimpan integration event ke outbox.
- Scheduled publisher mem-publish event outbox ke Kafka.
- `spring.kafka.bootstrap-servers` dibaca dari `KAFKA_BOOTSTRAP_SERVERS`, default `localhost:9092`.

## Retry Producer

Retry dikontrol oleh:

```yaml
erp.messaging.outbox.batch-size
erp.messaging.outbox.fixed-delay-ms
erp.messaging.outbox.retry-delay-seconds
erp.messaging.outbox.max-attempts
```

Publisher memakai send sinkron pada POC agar status outbox deterministik.

Jika failure mencapai `max-attempts`, row tetap `FAILED` dan `next_attempt_at` dipindah jauh ke masa depan. Pilihan ini menjaga row tetap audit-able tanpa membuat publisher terus mencoba row yang sama pada polling normal.

## DLQ

DLQ bukan tanggung jawab ERP producer pada POC ini.

Alasannya:

- ERP hanya publish business event ke topic domain.
- Error pemrosesan email adalah tanggung jawab NotificationService sebagai consumer.
- Consumer bisa punya retry, idempotency, dan DLQ sendiri tanpa mengubah ERP.

## Local Kafka

Untuk local manual run, Kafka KRaft single-node akan tersedia lewat Docker Compose profile `messaging`:

```bash
docker compose --profile messaging up -d
```

Kafka broker diekspos ke host di `localhost:9092`. Kafka UI opsional tersedia di:

```text
http://localhost:8085
```

Profile ini tidak menjadi syarat Task 8 ERP verification. Full proof ditunda sampai NotificationService siap:

```text
ERP approval action -> Kafka topic -> NotificationService consumes -> Mailpit email
```

Mailpit adalah bagian fase NotificationService/local full-stack, bukan syarat verifikasi ERP producer.

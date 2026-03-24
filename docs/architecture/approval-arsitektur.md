# Rancangan Arsitektur Generic Approval System

Sistem Approval yang dirancang agar *generic* dan dapat diimplementasikan ke berbagai modul transaksi (seperti Stock Adjustment, Purchase Order, dll) tanpa memerlukan *hard-coding* relasi database antar modul (mencegah *tight-coupling*).

## 1. Desain Database (Entity Model)

Menggunakan pendekatan **Polymorphic Relation** melalui `referenceType` dan `referenceId`.

**A. `appr_requests` (Approval Request)**
Menyimpan status terkini dari proses persetujuan sebuah dokumen.
*   `id`: UUID / Long
*   `referenceType`: String (Contoh: `"STOCK_ADJUSTMENT"`, `"PURCHASE_ORDER"`) -> Kunci generic-nya.
*   `referenceId`: String / Long (ID dari dokumen transaksi terkait).
*   `status`: Enum (`PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`).
*   `currentApproverId`: Relasi ke tabel `Party` (Menandakan giliran siapa yang harus memproses saat ini).
*   *Audit fields dari `BaseModel`* (`createdBy`, `createdDate`, dll).

**B. `appr_histories` (Approval History / Log)**
Menyimpan jejak langkah (log) persetujuan dokumen.
*   `id`: UUID / Long
*   `requestId`: FK ke `appr_requests`.
*   `action`: Enum (`REQUESTED`, `APPROVE_AND_FINISH`, `FORWARD`, `REJECTED`, `APPROVE_AND_FORWARD`).
*   `actorId`: Relasi ke `Party` (Siapa yang melakukan aksi pada log ini).
*   `targetApproverId`: Relasi ke `Party` (Opsional, diisi jika *action*-nya `FORWARD` atau `APPROVE_AND_FORWARD`).
*   `notes`: Text (Alasan *reject* atau catatan *forward*).
*   `actionDate`: LocalDateTime (Sama dengan `createdDate` BaseModel).

## 2. Strategi Integrasi Backend (Event-Driven)

Menggunakan **Spring Application Events (`ApplicationEventPublisher`)** agar modul persetujuan dan modul bisnis dapat berkomunikasi tanpa saling mengetahui detail masing-masing.

1.  **Request:** User klik "Request Approval". Status transaksi berubah menjadi `WAITING_APPROVAL`.
2.  **Approve:** Approver memproses dokumen (Approve & Finish).
3.  **Event Diterbitkan:** `ApprovalService` mencatat history, mengubah status request menjadi `COMPLETED`, lalu mem-publish event:
    ```java
    applicationEventPublisher.publishEvent(new ApprovalCompletedEvent("STOCK_ADJUSTMENT", id));
    ```
4.  **Listener Bereaksi:** Modul transaksi (`StockAdjustmentService`) menangkap event tersebut dan mengeksekusi logika bisnisnya (contoh: memotong/menambah stok):
    ```java
    @EventListener(condition = "#event.referenceType == 'STOCK_ADJUSTMENT'")
    public void onStockAdjustmentApproved(ApprovalCompletedEvent event) {
        stockAdjustmentService.processAdjustmentToInventory(event.getReferenceId());
    }
    ```

## 3. Desain UI/UX Frontend (Generic Fragment)

Karena proyek menggunakan arsitektur Thymeleaf + HTMX, UI approval akan dibuat sebagai **Satu Buah Fragment Universal** yang bisa disematkan di halaman detail modul apapun.

```html
<!-- Fragment: templates/fragments/approval.html -->
<div th:fragment="timeline(refType, refId)">
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Approval Workflow</h3>
        </div>
        
        <!-- Diload asinkron via HTMX agar halaman utama tidak lambat -->
        <div class="card-body" 
             th:attr="hx-get=@{/api/approvals/history(refType=${refType}, refId=${refId})}" 
             hx-trigger="load">
             <div class="spinner-border"></div> Loading history...
        </div>
    </div>
</div>
```

**Cara Implementasi di Halaman Transaksi (contoh: Stock Adjustment):**
```html
<div th:replace="~{fragments/approval :: timeline('STOCK_ADJUSTMENT', ${adjustment.id})}"></div>
```

## 4. Peningkatan Fitur (Future Roadmap / Standard ERP Profesional)

*   **Role/Jabatan vs Person (Party):** Dokumen idealnya di-approve berdasarkan *Jabatan* (Party Role) alih-alih orang spesifik (Party), sehingga jika karyawan cuti, orang dengan jabatan yang sama dapat mengambil alih.
*   **Approval Template / Matrix:** Otomatisasi penentuan approver berdasarkan kriteria transaksi (contoh: Jika nilai Stock Adjustment di atas Rp 10.000.000, maka Approver-nya otomatis Manager Finance).
*   **Notification System:** Penambahan notifikasi (Email atau Lonceng UI) kepada `targetApprover` ketika terjadi *action* `REQUESTED` atau `FORWARD`.
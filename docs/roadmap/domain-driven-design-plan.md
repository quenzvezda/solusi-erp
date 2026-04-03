# Panduan Refactoring Arsitektur: Anemic Domain Model menuju Domain-Driven Design (DDD)

**Konteks Dokumen:**
Dokumen ini berisi rekam jejak diskusi arsitektural untuk merombak sistem ERP (khususnya modul Inventory & Workflow) yang saat ini menggunakan pendekatan *Anemic Domain Model* (CRUD standar Spring Boot) menjadi sistem berbasis *Domain-Driven Design (DDD)* dan *Event-Driven Architecture*.

Dokumen ini bertindak sebagai **Pedoman Implementasi (Guidelines)** bagi AI Agent untuk melakukan refactoring kode.

---

## 📅 Alur Diskusi & Keputusan Arsitektural

### 1. Pertanyaan: Konsep Dasar DDD vs MVC Tradisional
**Q:** Siapa yang boleh memanggil/memodifikasi `OrderItem`? Kenapa tidak boleh langsung diakses dari luar dan harus lewat `Order`? Di mana letak *business logic* sebenarnya?
**Jawaban/Keputusan:**
* Sistem saat ini menggunakan **Anemic Domain Model** (Model hanya berisi *getter/setter*, logic menumpuk di *Service*).
* Target refactoring adalah **Rich Domain Model**.
* `Order` bertindak sebagai **Aggregate Root**. Logic bisnis (seperti `updateItemQuantity`, validasi status, dan hitung total) wajib berada di dalam entitas `Order`.
* *Application Service* berubah peran menjadi murni **Orkestrator** (Ambil data -> Panggil method domain -> Simpan).

### 2. Pertanyaan: Contoh Konkret Implementasi di Java
**Q:** Bagaimana bentuk nyata kode Java untuk pattern DDD pada `Order`?
**Jawaban/Keputusan:**
* Gunakan enkapsulasi ketat. *Setter* publik dilarang keras.
* Gunakan *access modifier* `package-private` pada entitas anak (misal `void changeQuantity()` di `OrderItem`) agar hanya bisa dipanggil oleh Aggregate Root (`Order`).

### 3. Pertanyaan: Kenapa Package "domain" dan Menjadi "Fat Class"?
**Q:** Di Spring Boot biasanya pakai package `model` yang isinya `@Entity` dan `@Getter @Setter`. Kalau logic masuk semua, bukankah class `Order` jadi sangat gemuk?
**Jawaban/Keputusan:**
* Pergantian nama menjadi `domain` menegaskan bahwa class tersebut berisi *Business Behavior*, bukan sekadar pemetaan tabel.
* *Fat Domain Model* adalah hal yang baik dalam DDD demi menjaga **Cohesion**. Ini mencegah terjadinya *God Object* pada layer Service.
* Disepakati pendekatan **Pragmatic DDD**: Menggabungkan anotasi JPA (`@Entity`) dengan Domain Model, tetapi mengunci rapat pembuatan objek (menggunakan *protected constructor* untuk JPA) dan menghapus `@Setter` global.

### 4. Pertanyaan: Studi Kasus Refactoring `StockAdjustment`
**Q:** Saya punya class `StockAdjustment` & `StockAdjustmentLine` beserta `StockAdjustmentServiceImpl`. Ini sangat Anemic. Bagaimana mengubahnya menjadi DDD?
**Jawaban/Keputusan:**
* **Refactor Entitas:** Hapus `@Setter` di level class `StockAdjustment`. Buat method eksplisit seperti `addLine()`, `updateInfo()`, `markAsCompleted()`, dan letakkan `recalculateTotals()` di dalam entitas.
* **Refactor Service:** `StockAdjustmentServiceImpl` dibersihkan dari *for-loop* perhitungan total dan validasi status. Service hanya memanggil `entity.markAsCompleted()`.

### 5. Pertanyaan: Masalah `@Setter` pada `BaseModel` (Audit Fields)
**Q:** Class `BaseModel` saya punya `@Getter` dan `@Setter` untuk id, audit fields, dan version. Apakah ini melanggar DDD?
**Jawaban/Keputusan:**
* **Ya, melanggar enkapsulasi.**
* **Tindakan:** Hapus `@Setter` dari `BaseModel`. Biarkan Spring Data JPA (Hibernate) mengisi *field* seperti `@CreatedBy`, `@CreatedDate`, dan `@Version` secara otomatis menggunakan *Reflection*. Mengizinkan *setter* publik membuka celah manipulasi ID dan merusak *Optimistic Locking*.

### 6. Pertanyaan: Pure DDD (Pemisahan Total)
**Q:** Kalau dibuat Pure DDD, berarti class Model JPA dan Domain dipisah?
**Jawaban/Keputusan:**
* Dalam Pure DDD (*Clean Architecture*), ya. Class di package `domain` murni Java (tanpa anotasi JPA). Class di package `infrastructure` menjadi entitas `@Table`.
* Dibuka opsi untuk tetap menggunakan **Pragmatic DDD** (1 class gabungan tanpa *setter*) jika *overhead* pembuatan *Mapper* dirasa terlalu membebani *development speed*.

### 7. Pertanyaan: Menghubungkan Domain ke External Service (`StockService`)
**Q:** Domain Model tidak boleh pakai `@Autowired`. Bagaimana cara `StockAdjustment` yang murni Java bisa menambah stok secara fisik lewat `StockService`?
**Jawaban/Keputusan:**
* Mengadopsi **Event-Driven Architecture (Pub/Sub)**.
* Alih-alih memanggil service secara langsung, saat method `markAsCompleted()` dijalankan, entitas `StockAdjustment` mempublikasikan **Domain Event** (menggunakan `.registerEvent(new StockAdjustedEvent(...))`).
* Sebuah `@TransactionalEventListener` di modul *Inventory* menangkap event tersebut dan memanggil `StockService.adjust()`.

### 8. Pertanyaan: Desain Event - Generik vs Spesifik?
**Q:** Jika ada modul lain seperti `GoodsIssue` (Delivery Order) yang juga memotong stok, apakah event-nya dijadikan satu (Generic Event)?
**Jawaban/Keputusan:**
* **Tidak.** Event harus spesifik mewakili *Ubiquitous Language* masing-masing domain (`StockAdjustedEvent` dan `GoodsIssuedEvent`).
* **Solusi:** Terapkan pola **"Specific Events, Generic Handler"**. Modul masing-masing menerbitkan event spesifik, lalu *Listener* di modul Inventory bertindak sebagai penerjemah (Translator) untuk mengubah berbagai event tersebut menjadi satu `StockMovementPayload` standar.

### 9. Pertanyaan: Validasi Alur Pub/Sub
**Q:** Recap: SA mengatur dirinya sendiri -> buat Event -> Listener tangkap -> panggil StockService?
**Jawaban/Keputusan:**
* Alur tersebut dikonfirmasi **100% akurat**. Pendekatan ini memutus efek domino, memastikan *Single Responsibility*, dan memudahkan penambahan fitur (misal: kirim email/update jurnal) tanpa menyentuh kode inti.

### 10. Pertanyaan: Menangani Kompleksitas Baru (Fitur Approval)
**Q:** Bagaimana jika ditambahkan fitur Approval (Persetujuan)? Di Anemic tinggal tambah relasi `@ManyToOne`. Apakah DDD lebih mudah?
**Jawaban/Keputusan:**
* Proses persetujuan direpresentasikan sebagai **State Machine** di dalam entitas `StockAdjustment` (memiliki status `DRAFT`, `WAITING_APPROVAL`, `APPROVED`).
* Metode transisi *state* (`requestApproval()`, `approve()`) mengatur kapan data boleh berubah.

### 11. Pertanyaan: Komunikasi Bounded Context (Inventory <-> Workflow)
**Q:** Logikanya saat request approval, SA jadi WAITING_APPROVAL, lalu lempar event ke domain Approvable yang terpisah?
**Jawaban/Keputusan:**
* Dikonfirmasi. Ini adalah pemisahan **Bounded Context** (Zero Foreign Keys).
* **Alur Ping-Pong:** Inventory lempar `ApprovalRequestedEvent` -> Workflow membuat `ApprovalTask` -> Manajer setuju -> Workflow lempar `DocumentApprovedEvent` -> Inventory mengeksekusi `approve()`.

### 12. Pertanyaan: Menghindari "The Fat Listener"
**Q:** Jika banyak dokumen (SA, DO) butuh approval, bukankah Listener-nya jadi gemuk berisi banyak *if-else*? Terlebih aksinya beda (misal DO ditolak harus melepas reservasi stok).
**Jawaban/Keputusan:**
* Untuk mencegah pelanggaran *Open-Closed Principle*, diusulkan 2 solusi arsitektur:
    1.  **Conditional Event Listener:** Menggunakan SpEL `@EventListener(condition="#event.documentType == 'SA'")` untuk memecah listener ke masing-masing modul.
    2.  **Strategy Pattern:** Membuat *Router/Dispatcher* yang menyuntikkan *List* antarmuka `WorkflowResultHandler`.

### 13. Pertanyaan: Pemilihan Pola Terbaik untuk Enterprise
**Q:** Mana yang lebih profesional? Dan bagaimana bentuk kode Strategy Pattern & Router?
**Jawaban/Keputusan:**
* **Strategy Pattern** adalah "Gold Standard" untuk Enterprise ERP yang *scalable*.
* **Desain Implementasi yang Ditetapkan:**
    * Buat antarmuka `WorkflowResultHandler` dengan method `supports()` dan `execute()`.
    * Buat implementasi konkret per dokumen (misal: `SaWorkflowHandler`, `DoWorkflowHandler`).
    * Buat `WorkflowResultRouter` yang menerima `List<WorkflowResultHandler>` via *Dependency Injection* Spring, kemudian me-loop *list* tersebut saat ada event datang untuk mencari *handler* yang tepat.

---
**Instruksi untuk AI Agent:**
Gunakan panduan di atas sebagai landasan utama (*ground truth*) dalam melakukan *refactoring* atau *code generation* pada *repository* proyek ini. Pastikan setiap kode yang dihasilkan mematuhi aturan enkapsulasi DDD, menggunakan pendekatan *Event-Driven* untuk interaksi lintas-modul, dan menghindari pola *Anemic Domain Model*.
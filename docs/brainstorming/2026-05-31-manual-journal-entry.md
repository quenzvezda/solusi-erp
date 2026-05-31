# Brainstorm: Manual Journal Entry

> Created: 2026-05-31
> Sprint: 6 — Accounting Core
> Status: DECISIONS LOCKED — ready for plan
> Plan: docs/plans/2026-05-31-manual-journal-entry.md

## 1. Tujuan & Konteks

Modul `accounting.journal` saat ini **read-only**: hanya menampilkan jurnal hasil auto-posting dari Goods Receipt & Vendor Bill (lewat `PostJournalForEventUseCase`). Fitur ini menambah jalur **input jurnal manual** — "pintu darurat" untuk transaksi keuangan yang tidak punya modul operasional otomatis.

Manual journal adalah fondasi sebelum General Ledger View & Trial Balance (Sprint 6), dan sengaja dikerjakan lebih dulu daripada Purchase Return/Debit Memo agar pemahaman mekanik double-entry matang.

### Use case real (kapan orang input jurnal manual)
| Use case | Contoh |
|---|---|
| Saldo awal saat mulai pakai sistem | DR aset / CR liabilitas+ekuitas |
| Biaya admin bank | DR Beban Admin Bank / CR Bank |
| Depresiasi bulanan | DR Beban Penyusutan / CR Akumulasi Penyusutan |
| Akrual/penyesuaian akhir bulan | DR Beban Gaji / CR Utang Gaji |
| Reklasifikasi / koreksi salah posting | DR akun benar / CR akun salah |
| Setoran modal pemilik | DR Kas / CR Modal |

## 2. Keputusan Terkunci (hasil diskusi)

1. **Lifecycle: DRAFT → POSTED.** Simpan sebagai DRAFT (editable/deletable), lalu post manual. Enum `JournalStatus` **sudah** punya `DRAFT` + `POSTED`.
2. **Tanpa approval.** User dengan permission langsung bisa create + post. Tidak pakai `ApprovalRequest`.
3. **Multi-currency, satu currency per jurnal** (header-level). Semua line ikut currency header. Base amount = `amount × exchangeRate`. Auto-lock rate=1 saat base currency (`isDefault`).
4. **Reversal: MANUAL-only.** Hanya jurnal `source_type=MANUAL` yang bisa dibalik. Jurnal auto-posted (GR/VB) **tidak** boleh dibalik dari layar journal (menghindari desync dgn dokumen sumber).
5. **Balance dicek di transaction currency.** Karena satu rate per jurnal, balance di transaction currency menjamin balance di base.
6. **Reversal**: tanggal user-pick (default hari ini, wajib period open), mewarisi currency+rate jurnal asal, langsung `POSTED`, hanya bisa sekali per jurnal asal.
7. **Kode jurnal tetap id-based** (`JNL-%06d`, mis. `JNL-000123`). TIDAK pakai sequence baru — nomor berbagi dengan auto-journal, list konsisten.
8. **Per-line memo: YA.** Tambah kolom `description` (nullable) di `acc_journal_lines` + field di `JournalLine`. Auto-posting kirim null.
9. **Form meniru layout view** (dua kartu: Detail + Journal Lines), tapi field jadi input.

## 3. Gap Analysis (verified via code exploration)

### Sejalan / sudah ada
- `JournalStatus` = {DRAFT, POSTED}. Aggregate hanya punya `createPosted()` + lines immutable → perlu `createDraft()`, `post()`, jalur edit/delete.
  - ref: src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java:L38-L65
  - ref: src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalStatus.java:L3-L6
- Period Guard reusable: `EnsureOpenPeriodForDateUseCase.execute(LocalDate)` → throw `DomainException("msg.error.period.not.open")`.
  - ref: src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCase.java:L5-L8
- COA "postable" = `isActive=true AND isHeader=false`. Lookup `GET /api/lookup/accounting/coa` (perm `LOOKUP_COA`) sudah memfilter ini; subText = code.
  - ref: src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaJpaRepository.java:L46-L50
  - ref: src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaLookupController.java:L11-L29
- COA prefill: `CoaLookupProvider.resolve(id)` → LookupDto. Currency prefill: `CurrencyLookupProvider.resolve(id)`, payload bawa `isDefault`.
  - ref: src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaLookupProvider.java:L5-L7
  - ref: src/main/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyLookupProviderImpl.java:L25-L42
- Currency base = flag `isDefault` (BUKAN isBase). Lookup `/api/lookup/master/currencies` payload punya `isDefault`. JS `ERP.CurrencyRateLock.init({currencySelectId, rateInputSelector})` (erp-currency-rate-lock.js) siap pakai.
  - ref: docs/spec/currency-exchange-rate.md:L41-L93
- Multi-currency sudah ada **di level line** (V60): `JournalLine` punya `originalCurrencyId, exchangeRate, originalDebitAmount, originalCreditAmount`. Factory `debitWithOriginal()/creditWithOriginal()`.
  - ref: src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalLine.java:L7-L47

### Perlu ditambah / diubah
- **Header currency**: `acc_journal_entries` TIDAK punya currency_id/exchange_rate. Tambah keduanya (keputusan "1 currency/jurnal"). Field per-line `original*` diisi dari header.
  - ref: src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalEntryEntity.java:L18-L41
- **Per-line description**: `JournalLine` (domain) & `acc_journal_lines` (DB) belum punya `description`. Tambah nullable.
- **source_id NOT NULL + UNIQUE(source_type, source_id)**: manual journal tak punya dokumen sumber. Ubah `source_id` jadi **nullable**; manual pakai `source_type='MANUAL'`, `source_id=NULL` (MariaDB izinkan banyak NULL di unique). Idempotency by-source memang tak berlaku untuk manual.
  - ref: src/main/resources/db/migration/V55__Add_Journal_Core.sql:L1-L16
- **Reversal linkage**: tambah kolom `reversal_of_id` (nullable, self-FK). Reversal journal mengisi ini → asal; cek double-reversal via `existsByReversalOfId`.
- **eventType untuk manual**: kolom `event_type` NOT NULL. Manual journal pakai `event_type='MANUAL'` (string; bukan SchemaEventType). Aggregate factory menerimanya.
- **Period Guard belum dipanggil di auto-posting** — TIDAK diretrofit (di luar scope). Hanya diterapkan ke manual POST & reversal.
- **Repository domain** hanya `save()` + `existsBySource()`. Perlu tambah `findById()`, `delete()`, `existsReversalOf()`.
  - ref: src/main/java/com/solusi/erp/accounting/journal/domain/repository/JournalEntryRepository.java:L1-L8
- **Web read-only**: controller hanya list+detail (`JOURNAL-ENTRY_READ`). Perlu route create/edit/post/delete/reverse + permission baru.
- **Detail template** belum menampilkan currency/exchange rate/original amount (walau DTO line sudah punya). Perlu diperluas + reversal badge + action buttons.
  - ref: src/main/resources/templates/accounting/journal/journal-entry-detail.html:L25-L101
- Catatan: DTO detail pakai field `desc` (bukan `description`) — verifikasi saat menyentuh mapper.
  - ref: src/main/java/com/solusi/erp/accounting/journal/web/dto/JournalEntryDetailResponse.java:L1-L26

## 4. Model Data (target)

### Header (`acc_journal_entries`) — tambahan
| Kolom | Tipe | Catatan |
|---|---|---|
| `currency_id` | BIGINT NULL | FK master_currencies; diisi untuk semua jurnal baru |
| `exchange_rate` | DECIMAL(19,6) NULL | kurs ke base; 1 jika base currency |
| `reversal_of_id` | BIGINT NULL | self-FK; diisi pada jurnal reversal |
| `source_id` | BIGINT **NULL** (ALTER) | NULL untuk manual |

### Line (`acc_journal_lines`) — tambahan
| Kolom | Tipe | Catatan |
|---|---|---|
| `description` | VARCHAR(255) NULL | memo per baris |

### Identitas jurnal
- Manual: `event_type='MANUAL'`, `source_type='MANUAL'`, `source_id=NULL`, `source_code=NULL`.
- Reversal: sama + `reversal_of_id = {asal}`.
- Kode tampil: `JNL-%06d` dari id (existing mapper).

## 5. Aturan Validasi (manual journal)

Backend (domain + use case):
1. Minimal 2 line.
2. Tiap line: debit **XOR** credit (sudah dijaga `JournalLine` compact constructor: tak boleh dua-duanya >0 / dua-duanya 0; negatif ditolak).
3. `Σ debit = Σ credit` di transaction currency (`validateBalanced()` existing, cek base; karena 1 rate, ekuivalen).
4. Posting date → period harus OPEN (`EnsureOpenPeriodForDateUseCase`) saat **POST** & **REVERSE** (bukan saat simpan draft).
5. Akun harus valid postable: aktif & non-header. (UI sudah filter; backend percaya lookup, opsional re-check.)
6. Currency wajib; exchangeRate > 0 (1 jika base).
7. Edit/Delete hanya saat status DRAFT.
8. Reverse hanya saat POSTED **dan** source_type=MANUAL **dan** belum pernah direverse.

Frontend:
- Form submit diblokir jika unbalanced atau <2 line (warning modal standar).
- Balance indicator real-time (Σdebit vs Σcredit).

## 6. UI/UX

### Form (create/edit) — dua kartu, meniru detail view
- **Kartu Detail**: Reference No (opsional, teks bebas), Posting Date (flatpickr `data-picker="date"`), Currency (autocomplete + payload isDefault), Exchange Rate (AutoNumeric, auto-lock=1 via ERP.CurrencyRateLock), Description (textarea). Status tak diinput (DRAFT otomatis).
- **Kartu Journal Lines**: tabel dinamis (`ErpLineManager`, `#row-template-source`) — COA autocomplete (subText kode), Debit (erp-number-decimal), Credit (erp-number-decimal), Memo. Tombol add/remove. Baris Total real-time + badge balanced/unbalanced.

### Detail view — perluasan
- Source Type tampil "Manual" (tanpa link source code).
- Baris Currency + Exchange Rate; kolom/total base amount jika multi-currency.
- Badge "Reversed by JNL-xxxxx" (asal) / "Reversal of JNL-xxxxx" (reversal).
- Action buttons sesuai status: DRAFT → Edit/Delete/Post; POSTED+MANUAL belum direverse → Reverse.

## 7. Permission (baru)
`JOURNAL-ENTRY_CREATE`, `JOURNAL-ENTRY_UPDATE`, `JOURNAL-ENTRY_DELETE`, `JOURNAL-ENTRY_POST`, `JOURNAL-ENTRY_REVERSE`. Grant ke `ROLE_ADMIN`. Pola seeder ikut V55/V58. Migrasi berikutnya: **V64**.

## 8. Out of Scope (deferred)
- Approval flow.
- Currency berbeda per line (FX lanjutan).
- Reversal untuk jurnal auto-posted.
- Retrofit Period Guard ke auto-posting GR/VB.
- General Ledger View & Trial Balance (task Sprint 6 berikutnya).
- Reversal-of-reversal chain UI (cukup blok double reverse).

## 9. Referensi
- docs/modules/accounting/journal-entry.md
- docs/spec/header-lines-form.md, numeric-standards.md, autocomplete-generic.md, currency-exchange-rate.md, datetime-standards.md, action-buttons.md, form-submission.md
- Reference module pola: `accountspayable.vendorbill` (header-lines + currency), `purchasing.purchaseorder` (interactive form).

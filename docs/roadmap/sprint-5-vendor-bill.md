# Sprint 5 — Vendor Bill (Accounts Payable)

> Status: **PLANNED** — belum diimplementasi  
> Diskusi awal: 2026-05-09

---

## Konteks Bisnis

Setelah GR selesai (barang fisik diterima), tim AP membuat **Vendor Bill** secara manual
ketika invoice fisik dari vendor tiba. Satu Vendor Bill bisa mereferensi **satu atau lebih GR**
dari vendor yang sama — sesuai standar three-way matching (PO + GR + Invoice).

---

## Alur Journal AP Cycle (Lengkap)

```
PO (approved)        → tidak ada journal
GR (completed)  ✅   → DR Inventory + DR Input VAT → CR GR/IR Clearing
Vendor Bill          → DR GR/IR Clearing            → CR Accounts Payable
Vendor Payment       → DR Accounts Payable           → CR Main Bank Account
```

### Detail Journal per Event

**Goods Receipt** *(sudah live)*
```
DR  Merchandise Inventory (1310)      = GR_INVENTORY_AMT
DR  Tax Receivable / Input VAT (1230) = GR_TAX_AMT
CR  GR/IR Clearing (2120)             = GR_GRAND_TOTAL
```

**Vendor Bill** *(Sprint 5)*
```
DR  GR/IR Clearing (2120)        = VB_GRIR_CLEARING_AMT  (= gross GR yang di-match)
CR  Accounts Payable (2110)      = VB_AP_TOTAL

Catatan: VB_TAX_AMT = 0 dalam flow GR→Bill
karena Input VAT sudah diklaim saat GR.
VB_TAX_AMT hanya dipakai jika ada Invoice tanpa GR sebelumnya.
```

**Vendor Payment** *(Sprint 5)*
```
DR  Accounts Payable (2110)      = VP_AP_AMT
CR  Main Bank Account (1120)     = VP_BANK_OUT_AMT
```

---

## Skema Database (Rancangan Awal)

```
vendor_bills
  id, bill_number (auto-generate), vendor_id, bill_date,
  due_date, status (DRAFT/CONFIRMED/PAID/CANCELLED),
  notes, total_amount, version, audit...

vendor_bill_lines
  id, bill_id, gr_line_id (FK → gr_lines),
  description, qty_billed, unit_price, tax_amount, line_total

vendor_bill_gr_refs        ← bridge many-to-many
  bill_id, gr_id           ← 1 invoice bisa ref banyak GR
```

**Constraint bisnis:**
- Semua GR yang direferensi harus dari **vendor yang sama**
- `qty_billed` tidak boleh melebihi sisa qty GR yang belum di-bill
- GR line yang sudah fully billed tidak bisa di-pick ulang
- Auto-journal `VENDOR_BILL` hanya trigger saat status → `CONFIRMED`

---

## User Flow yang Disarankan

```
1. User klik "Create Vendor Bill"
2. Pilih Vendor
3. Sistem tampilkan semua GR dari vendor tersebut
   yang statusnya COMPLETED dan belum/partial di-bill
4. User centang GR mana yang di-include
5. Lines otomatis ter-populate dari GR lines (qty & harga)
6. User bisa adjust qty jika vendor kirim partial invoice
7. User klik Confirm → auto-journal VENDOR_BILL ter-posting
```

---

## Referensi Perbandingan ERP

| ERP | Behavior |
|---|---|
| **SAP (MIRO)** | Multi-GR dalam 1 Invoice, line-level 3-way match |
| **Odoo** | Default 1 PO = 1 Bill, bisa merge dari multi-PO |
| **Oracle AP** | Multi-GR per Invoice dengan line-level matching |

---

## Scope Sprint 5 (Kasar)

- [ ] Modul `Vendor Bill` (CRUD: Draft → Confirm → Cancel)
- [ ] GR reference picker (multi-select, filter by vendor)
- [ ] Three-way match validation (qty guard)
- [ ] Auto-journal `VENDOR_BILL` on Confirm
- [ ] Vendor Payment (basic: full payment)
- [ ] Auto-journal `VENDOR_PAYMENT` on Confirm
- [ ] Purchase Return & Debit Memo *(opsional, bisa Sprint 5+)*

---

## Pertanyaan Terbuka (Perlu Diputuskan Sebelum Implementasi)

- [ ] Apakah Vendor Bill punya nomor sendiri atau pakai nomor dari vendor?
- [ ] Apakah partial payment didukung di Sprint 5 atau cukup full payment dulu?
- [ ] Bagaimana handling price variance antara PO price vs Invoice price?
- [ ] Apakah Purchase Return masuk Sprint 5 atau defer ke Sprint 5+?

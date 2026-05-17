# Action Buttons Standard

Dokumen ini mendefinisikan standar aksi tombol dokumen (contoh: Submit, Send, Complete, Cancel) agar perilaku UX konsisten di seluruh modul.

## 1. Prinsip Utama

1. Aksi yang mengubah status dokumen wajib memakai konfirmasi **modal yes/no** (`ErpModal.confirm`), bukan `window.confirm()` bawaan browser.
2. Aksi harus non-blocking (AJAX) jika tidak membutuhkan payload form besar, menggunakan helper `ErpForm.postAction`.
3. Error harus terlihat jelas ke user (`ErpModal.showError`) dan tombol harus kembali aktif.

## 2. Kontrak HTML Tombol

Contoh minimal:

```html
<button type="button"
        data-complete-url="/inventory/goods-receipts/123/complete"
        data-confirm-message="Complete this goods receipt?"
        data-redirect-url="/inventory/goods-receipts/123"
        onclick="ErpForm.postAction(this)">
    Complete
</button>
```

### Atribut yang didukung
- `data-*-url` (wajib): endpoint POST untuk aksi.
- `data-confirm-message` (opsional): jika ada, tampilkan modal konfirmasi sebelum request.
- `data-redirect-url` (opsional): redirect sukses ke URL target; jika tidak diisi, fallback ke toast + reload.

## 3. Alur Runtime yang Diwajibkan

1. Disable tombol untuk mencegah double-click.
2. Kirim request POST dengan CSRF header.
3. Saat sukses:
   - simpan pesan sukses ke `sessionStorage` (`erp_pending_success`) jika redirect,
   - lakukan redirect/reload sesuai kontrak atribut.
4. Saat gagal:
   - tampilkan error modal,
   - re-enable tombol,
   - reset state suppress beforeunload jika sempat diaktifkan.

## 4. Beforeunload Guard

Jika halaman memakai dirty-form warning (`beforeunload`), aksi tombol yang memang berniat pindah halaman harus mengaktifkan suppress flag sementara agar browser tidak memunculkan prompt palsu saat aksi sukses.

## 5. Checklist Implementasi

- Endpoint backend memakai `ApiResponse` dengan pesan sukses yang jelas.
- Permission tetap diverifikasi di backend (jangan hanya sembunyikan tombol di frontend).
- Tambahkan test kontrak template untuk memastikan atribut tombol (`data-*-url`, `data-confirm-message`, `data-redirect-url`) tidak regress.
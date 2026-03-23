# ERP Form Development Guide

Panduan ini menjelaskan standar pengembangan form di aplikasi ERP, khususnya untuk form yang memiliki **Header-Lines** (contoh: Stock Adjustment, PO, SO).

## 1. Pola Form "Single File" (Add & Edit)

Untuk menjaga konsistensi, gunakan satu file HTML untuk mode *Add* dan *Edit*. 

### A. Deteksi Mode (Thymeleaf)
Gunakan variabel `isLocked` (biasanya jika status sudah COMPLETED) dan cek ID untuk menentukan judul.
```html
<div class="page-body" th:with="isLocked=${stockAdjustment.status != null && stockAdjustment.status.name() == 'COMPLETED'}">
    <h2 th:text="${stockAdjustment.id == null ? 'Add New' : 'Edit Document'}"></h2>
```

### B. Proteksi Input (Readonly)
Gunakan atribut `th:disabled` atau `isReadonly` pada fragment input agar form tidak bisa diubah jika sudah diproses.
```html
<div th:replace="~{fragments/inputs :: date(..., isReadonly=${isLocked})}"></div>
```

---

## 2. Dynamic Line Manager (`ErpLineManager`)

Jangan menulis logika penambahan baris secara manual. Gunakan `ErpLineManager` yang tersedia di `erp-common-handler.js`.

### A. Template Baris (HTML)
Simpan template baris di dalam tabel yang tersembunyi. Gunakan placeholder `INDEX` untuk atribut `name`.
```html
<table style="display:none">
    <tbody id="row-template-source">
        <tr class="line-row">
            <td><input name="lines[INDEX].productId"></td>
            ...
        </tr>
    </tbody>
</table>
```

### B. Inisialisasi JavaScript
```javascript
// 1. Inisialisasi Manager
const lineManager = new ErpLineManager('line-container', 'row-template-source', { 
    onUpdate: calculateTotals // Opsional: fungsi yang dipanggil tiap ada perubahan baris
});

// 2. Fungsi Setup Logika per Baris
function setupRowLogic(row) {
    // Inisialisasi lookup, event listener, dll khusus untuk baris ini
    initLookup(row.querySelector('.select-product'), 'path/to/api');
    
    // Tombol hapus
    row.querySelector('.btn-remove-line').onclick = () => lineManager.removeRow(row);
}

// 3. Tombol Tambah Baris
btnAddLine.onclick = () => lineManager.addRow(setupRowLogic);

// 4. Inisialisasi Baris Eksisting (saat Edit)
lineContainer.querySelectorAll('.line-row').forEach(setupRowLogic);
lineManager.updateIndexes();
```

---

## 3. Inventory & UoM Engine (`ErpInventory`)

Untuk form inventory, gunakan `ErpInventory.setupUomLogic` untuk menangani pemilihan satuan (UoM) dan Serial Number via Drawer.

```javascript
row.querySelector('.btn-edit-detail').onclick = () => {
    const isSerialized = row.querySelector('.input-serialized').value === 'true';
    const drawerId = isSerialized ? 'drawer-serial' : 'drawer-non-serial';
    
    ErpInventory.setupUomLogic(
        document.getElementById(drawerId), 
        row, 
        isSerialized, 
        calculateTotals // Callback setelah save di drawer
    );
    
    ErpDrawer.open(drawerId);
};
```

---

## 4. Helper Angka (`ErpNumeric`)

Gunakan `ErpNumeric` untuk mengambil atau mengisi nilai pada input yang menggunakan **AutoNumeric**.

*   `ErpNumeric.get(el)` : Mengambil angka murni (float), otomatis menghilangkan koma ribuan.
*   `ErpNumeric.set(el, value)` : Mengisi angka ke input, otomatis memformat ulang (contoh: `1000` jadi `1,000.00`).

---

## 5. Standar Modal (4 Variasi)

Aplikasi memiliki 4 modal standar yang bisa dipanggil:

| Tipe | Helper / Fragment | Kegunaan |
| :--- | :--- | :--- |
| **Error** | `ErpModal.showError(msg)` | Menampilkan pesan kesalahan fatal (Merah). |
| **Warning** | `ErpModal.showWarning(msg)` | Peringatan validasi (Kuning). |
| **Confirm (JS)** | `ErpModal.confirm(msg, callback)` | Konfirmasi aksi (Hapus baris, ganti gudang). |
| **Delete (HTMX)** | `delete-confirm` (Fragment) | Konfirmasi hapus data via HTMX (List page). |

---

## 6. Validasi Line Item

Validasi harus dilakukan di dua sisi:
1.  **Frontend (UI)**: Gunakan `onBefore` pada `ErpAction.confirmAndSubmit` atau cek saat tombol Save diklik.
    ```javascript
    if (lineContainer.children.length === 0) {
        ErpModal.showWarning("Tambahkan minimal 1 item!");
        return false;
    }
    ```
2.  **Backend (Java)**: Gunakan `@Valid` pada controller dan pastikan object lines divalidasi dengan `List<@Valid LineItemDto>`.

---

## 7. Drawer (Side Panels)

Drawer digunakan untuk penginputan detail yang tidak muat di tabel (seperti konversi UoM yang kompleks atau list Serial Number).

*   **HTML**: Letakkan fragment drawer di akhir file: `<div th:replace="~{path/to/drawer-fragments :: adjustment-drawers}"></div>`.
*   **JS**: Gunakan `ErpDrawer.open(id)` dan `ErpDrawer.close(id)`.

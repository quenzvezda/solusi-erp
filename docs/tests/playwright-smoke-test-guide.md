# Playwright Smoke Test Guide — Solusi ERP

> Panduan lengkap untuk AI Agent dan Developer menjalankan smoke test end-to-end pada Solusi ERP menggunakan Playwright, mencakup teknik interaksi dengan TomSelect, Flatpickr, AutoNumeric, Line Items, dan komponen UI lainnya.

---

## Daftar Isi

1. [Setup & Prerequisites](#1-setup--prerequisites)
2. [Strategi Smoke Test](#2-strategi-smoke-test)
3. [Login & Navigasi](#3-login--navigasi)
4. [Kamus Interaksi Komponen](#4-kamus-interaksi-komponen)
   - [4.1 TomSelect (Autocomplete Lookup)](#41-tomselect-autocomplete-lookup)
   - [4.2 TomSelect dalam Line Item (Table)](#42-tomselect-dalam-line-item-table)
   - [4.3 Flatpickr (Date/Time Picker)](#43-flatpickr-datetime-picker)
   - [4.4 AutoNumeric (Numeric Input)](#44-autonumeric-numeric-input)
   - [4.5 Standard Form Elements](#45-standard-form-elements)
   - [4.6 Line Item / Dynamic Rows](#46-line-item--dynamic-rows)
   - [4.7 AJAX Form Submission](#47-ajax-form-submission)
   - [4.8 HTMX Content (Search/Filter/Pagination)](#48-htmx-content-searchfilterpagination)
5. [Fallback: Node.js Playwright Script](#5-fallback-nodejs-playwright-script)
6. [Troubleshooting](#6-troubleshooting)
7. [Checklist Smoke Test per Modul](#7-checklist-smoke-test-per-modul)

---

## 1. Setup & Prerequisites

### 1.1 MCP Playwright (Primary — untuk AI Agent)

MCP Playwright tersedia sebagai tool bawaan di session Copilot CLI / Claude. Tidak perlu install apapun.

**Tools yang tersedia:**
- `browser_navigate` — navigasi ke URL
- `browser_snapshot` — baca accessibility tree (lebih reliable dari screenshot)
- `browser_click` — klik elemen berdasarkan ref
- `browser_type` — ketik teks ke elemen
- `browser_fill_form` — isi form fields sekaligus
- `browser_press_key` — tekan keyboard key
- `browser_evaluate` — jalankan JavaScript di browser
- `browser_take_screenshot` — tangkapan layar visual
- `browser_console_messages` — lihat error JS
- `browser_network_requests` — inspect AJAX/HTMX requests
- `browser_wait_for` — tunggu teks muncul/hilang

### 1.2 Fallback: Global Playwright via Node.js

Jika MCP Playwright error dalam session (sering terjadi setelah banyak interaksi), gunakan Playwright yang ter-install secara global.

**Install sekali (global, jangan di repo):**
```bash
npm install -g playwright @playwright/test
npx playwright install chromium
```

**Cek versi:**
```bash
npx playwright --version
# Output: Version 1.59.1
```

> **PENTING:** DILARANG install Playwright secara lokal di repository (`npm install playwright` di folder project). Ini akan membuat `node_modules/` di repo. Selalu gunakan global installation.

**Jalankan script:**
```bash
npx playwright test tests/smoke-test.spec.js --headed
# atau headless:
npx playwright test tests/smoke-test.spec.js
```

### 1.3 Spring Boot Server

Server **WAJIB** berjalan sebelum test. AI agent **WAJIB** start server secara mandiri — jangan minta user menjalankannya.

**Cek apakah server sudah berjalan:**
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login
# Response 200 atau 302 → sudah aktif, lewati langkah start
```

**Start server (detached background process):**

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=18080"
```

```powershell
# Windows PowerShell (gunakan semicolon bukan &&, wajib ENV_FILE)
$env:ENV_FILE=".env.dev"; .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080"
```

Tunggu hingga log menampilkan `Started ... in ... seconds` sebelum membuka browser. Port wajib **18080** untuk dev.

**Jika port sudah dipakai:**
```bash
# Linux/macOS
lsof -ti:18080 | xargs kill -9

# Windows PowerShell
Stop-Process -Id (Get-NetTCPConnection -LocalPort 18080).OwningProcess -Force
```

> **Cold Start:** Saat pertama kali aplikasi dijalankan, `SystemInitializer` otomatis mengubah placeholder `INITIAL_PASSWORD_SETUP` di database menjadi hash BCrypt untuk password `admin123`. Semua user baru memiliki flag `password_change_required = true`. Login pertama mungkin diredirect ke halaman ganti password — gunakan password lama `admin123` dan set password baru (boleh sama).

### 1.4 Kredensial

| Field    | Value                        |
|----------|------------------------------|
| URL      | `http://localhost:18080`     |
| Username | `admin`                      |
| Password | `admin123`                   |

### 1.5 Database Access (untuk verifikasi data)

```bash
# Via Docker (container name: mariadb-local, DB: erp-test)
docker exec mariadb-local mariadb -uroot -proot erp-test

# Atau langsung (jika MariaDB lokal)
mariadb -h localhost -P 3307 -uroot -proot erp-test
```

---

## 2. Strategi Smoke Test

### Prinsip

1. **Tujuan utama:** Verifikasi data tersimpan dengan benar dan UI tidak error (bukan exhaustive testing)
2. **Efisien:** Langsung isi data minimum required, skip field opsional
3. **Verifikasi ganda:** Cek response sukses DI browser + query database
4. **Satu flow per modul:** Create → verify di list → Edit → verify perubahan

### Urutan Test yang Direkomendasikan

Ikuti dependency data (master dulu, transaksi kemudian):

1. **Login** → pastikan dashboard muncul
2. **Master: Geographic** → buat data wilayah
3. **Master: Party** → buat supplier/customer (depends: Geographic)
4. **Inventory: Brand** → buat brand sederhana
5. **Inventory: Product Category** → buat kategori
6. **Inventory: UoM** → pastikan satuan tersedia
7. **Inventory: Facility** → buat gudang
8. **Inventory: Product** → buat produk (depends: Category, Brand, UoM)
9. **Inventory: Stock Adjustment** → test transaksi dengan line items (depends: Product, Facility)

---

## 3. Login & Navigasi

### MCP Playwright

```
1. browser_navigate → http://localhost:18080/login
2. browser_snapshot → ambil ref elemen
3. browser_fill_form → username: admin, password: admin123
4. browser_click → tombol "Sign in"
5. browser_wait_for → text: "Dashboard" atau URL berubah
6. browser_snapshot → verifikasi dashboard loaded
```

**Contoh step-by-step:**

```
browser_navigate(url: "http://localhost:18080/login")
browser_snapshot()
# Dari snapshot, cari ref untuk username, password, submit button
browser_fill_form(fields: [
  { name: "username", type: "textbox", ref: "<ref_username>", value: "admin" },
  { name: "password", type: "textbox", ref: "<ref_password>", value: "admin123" }
])
browser_click(ref: "<ref_submit_btn>")
browser_wait_for(text: "Dashboard", time: 5)
```

### Node.js Script

```javascript
const { chromium } = require('playwright');

async function login(page) {
  await page.goto('http://localhost:18080/login');
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/dashboard**', { timeout: 10000 });
}
```

---

## 4. Kamus Interaksi Komponen

### 4.1 TomSelect (Autocomplete Lookup)

TomSelect me-wrap elemen `<select>` asli menjadi custom widget. Elemen asli di-hide dan diganti dengan div wrapper. Interaksi langsung ke `<select>` asli **TIDAK AKAN BEKERJA**.

#### Anatomi DOM TomSelect

```
<div class="ts-wrapper single ...">          ← wrapper
  <div class="ts-control">                    ← control (area yang diklik user)
    <input type="text" class="..." />          ← search input (tersembunyi sampai fokus)
    <div class="item" data-value="123">        ← selected item display
      Product A <small>PRD-001</small>
    </div>
  </div>
  <div class="ts-dropdown">                   ← dropdown (muncul saat fokus)
    <div class="ts-dropdown-content">
      <div class="option" data-value="123">    ← pilihan
        Product A
        <small>PRD-001</small>
      </div>
    </div>
  </div>
</div>
<select class="erp-input-ts" style="display:none"> ← elemen asli (hidden)
```

#### Teknik: MCP Playwright

**Prinsip kunci:** Cara PALING SEDERHANA dan PALING RELIABLE untuk TomSelect di MCP Playwright adalah **klik langsung** pada elemen combobox yang visible, lalu pilih option dari listbox yang muncul. Ini bekerja karena MCP Playwright menggunakan accessibility tree (bukan raw DOM) sehingga bisa berinteraksi dengan widget TomSelect secara natural.

**Cara utama — klik combobox + klik option (DIREKOMENDASIKAN):**

```
# 1. Ambil snapshot untuk lihat ref elemen
browser_snapshot()
# → Akan tampil: combobox "-- Select --" [ref=e99] di dalam cell/field yang dituju

# 2. Klik combobox untuk buka dropdown
browser_click(ref: "e99", element: "Category TomSelect dropdown")
# → Dropdown terbuka, muncul listbox dengan option-option

# 3. Ambil snapshot lagi untuk lihat ref option
browser_snapshot()
# → Akan tampil listbox dengan option: option "Electronics PRD-CAT-001" [ref=e105]

# 4. Klik option yang diinginkan
browser_click(ref: "e105", element: "Electronics option")
# → Option terpilih, combobox menampilkan teks yang dipilih
```

**Catatan penting untuk klik approach:**
- Option yang muncul di listbox sudah ter-load otomatis (tidak perlu query API manual)
- Cascade TomSelect (misal: Facility → Grid → Container) bekerja otomatis karena klik trigger semua native events
- Untuk form dengan TomSelect di header (bukan line item), dropdown biasanya muncul langsung tanpa perlu search

**Cara fallback — via JavaScript API (gunakan jika klik tidak berhasil):**

```
browser_evaluate(function: "() => {
  const el = document.querySelector('#category-select');
  if (!el || !el.tomselect) throw new Error('TomSelect not initialized');
  const ts = el.tomselect;
  
  // Trigger load dengan keyword kosong untuk memunculkan semua data
  return new Promise((resolve) => {
    ts.load('', (options) => {
      if (options.length > 0) {
        options.forEach(opt => ts.addOption(opt));
        ts.setValue(options[0].id);
        resolve({ selected: options[0].name, id: options[0].id });
      } else {
        resolve({ error: 'No options loaded' });
      }
    });
  });
}")
```

**Fallback alternatif — jika sudah tahu ID value:**

```
browser_evaluate(function: "() => {
  const el = document.querySelector('#brand-select');
  const ts = el.tomselect;
  return new Promise((resolve) => {
    ts.load('', (options) => {
      options.forEach(opt => ts.addOption(opt));
      ts.setValue('1'); // Set ke value ID = 1
      resolve('done');
    });
  });
}")
```

#### Teknik: Node.js Playwright

```javascript
/**
 * Mengisi TomSelect field via JavaScript API.
 * Ini adalah cara PALING RELIABLE untuk berinteraksi dengan TomSelect.
 * 
 * @param {Page} page - Playwright page object
 * @param {string} selector - CSS selector untuk elemen <select> asli (bukan wrapper)
 * @param {string} searchQuery - keyword pencarian (kosong = load semua)
 * @param {number} optionIndex - index option yang mau dipilih (0 = pertama)
 */
async function selectTomSelect(page, selector, searchQuery = '', optionIndex = 0) {
  const result = await page.evaluate(({ sel, query, idx }) => {
    return new Promise((resolve, reject) => {
      const el = document.querySelector(sel);
      if (!el || !el.tomselect) {
        reject(new Error(`TomSelect not found on ${sel}`));
        return;
      }
      const ts = el.tomselect;
      ts.load(query, (options) => {
        if (options.length === 0) {
          resolve({ error: 'No options', query });
          return;
        }
        options.forEach(opt => ts.addOption(opt));
        const target = options[Math.min(idx, options.length - 1)];
        ts.setValue(target.id);
        resolve({ id: target.id, name: target.name });
      });
    });
  }, { sel: selector, query: searchQuery, idx: optionIndex });
  
  return result;
}

// Contoh penggunaan:
await selectTomSelect(page, '#category-select', '', 0);       // Pilih opsi pertama
await selectTomSelect(page, '#brand-select', 'samsung', 0);   // Search "samsung", pilih pertama
await selectTomSelect(page, '#header-facility', '', 0);        // Pilih facility pertama
```

**Helper untuk set TomSelect dengan specific value ID:**

```javascript
async function setTomSelectValue(page, selector, valueId) {
  await page.evaluate(({ sel, val }) => {
    return new Promise((resolve) => {
      const el = document.querySelector(sel);
      const ts = el.tomselect;
      ts.load('', (options) => {
        options.forEach(opt => ts.addOption(opt));
        ts.setValue(val);
        resolve();
      });
    });
  }, { sel: selector, val: String(valueId) });
}
```

### 4.2 TomSelect dalam Line Item (Table)

Line item TomSelect menggunakan class `erp-input-ts-sm` dan di-initialize secara dinamis saat row ditambahkan. Cascade berlaku: pilih Product → Grid opsi muncul → pilih Grid → Container opsi muncul.

#### Teknik: MCP Playwright (DIREKOMENDASIKAN — klik langsung)

Sama seperti TomSelect di header form, cara terbaik adalah klik combobox lalu klik option:

```
# Setelah klik "Add Line", row baru muncul

# 1. Ambil snapshot untuk lihat combobox di row baru
browser_snapshot()
# → Contoh: combobox "-- Select --" [ref=e308] di cell Product baris ke-2

# 2. Klik combobox Product di row baru
browser_click(ref: "e308", element: "Product TomSelect row 2")
# → Dropdown muncul dengan semua produk

# 3. Snapshot untuk lihat options
browser_snapshot()
# → option "Samsung Galaxy S24 Ultra PRD-DEMO-0001" [ref=e336]
# → option "IKEA Billy Bookcase White PRD-DEMO-0002" [ref=e339]

# 4. Klik produk yang diinginkan
browser_click(ref: "e339", element: "IKEA Billy Bookcase White option")

# 5. Klik Grid TomSelect (cascade: otomatis ter-update setelah produk dipilih)
browser_click(ref: "e314", element: "Grid TomSelect row 2")
browser_snapshot()
# → pilih option dari listbox
browser_click(ref: "<ref_grid_option>", element: "Grid option")

# 6. Klik Container TomSelect (cascade dari Grid)
browser_click(ref: "e320", element: "Container TomSelect row 2")
browser_snapshot()
browser_click(ref: "<ref_container_option>", element: "Container option")
```

**Penting:** Cascade bekerja sempurna dengan klik approach. Tidak perlu trigger manual event.

#### Teknik: Node.js Playwright (via JS API)

```javascript
/**
 * Mengisi TomSelect di dalam line item row tertentu.
 * 
 * @param {Page} page
 * @param {number} rowIndex - 0-based index dari row
 * @param {string} fieldClass - CSS class selector: 'select-product', 'select-grid', 'select-container'
 * @param {string} query - search keyword
 */
async function selectLineItemTomSelect(page, rowIndex, fieldClass, query = '') {
  return await page.evaluate(({ idx, cls, q }) => {
    return new Promise((resolve, reject) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      if (idx >= rows.length) {
        reject(new Error(`Row ${idx} not found. Total rows: ${rows.length}`));
        return;
      }
      const el = rows[idx].querySelector(`.${cls}`);
      if (!el || !el.tomselect) {
        reject(new Error(`TomSelect .${cls} not initialized in row ${idx}`));
        return;
      }
      const ts = el.tomselect;
      ts.load(q, (options) => {
        options.forEach(opt => ts.addOption(opt));
        if (options.length > 0) {
          ts.setValue(options[0].id);
          resolve({ id: options[0].id, name: options[0].name });
        } else {
          resolve({ error: 'No options' });
        }
      });
    });
  }, { idx: rowIndex, cls: fieldClass, q: query });
}

// Contoh:
await selectLineItemTomSelect(page, 0, 'select-product', '');
await selectLineItemTomSelect(page, 0, 'select-grid', '');
await selectLineItemTomSelect(page, 0, 'select-container', '');
```

### 4.3 Flatpickr (Date/Time Picker)

Flatpickr juga me-wrap `<input>` asli. Input asli di-hide dan diganti dengan `altInput` yang menampilkan format user-friendly ("12 Apr 2026"). Value sebenarnya tetap di input asli dalam format ISO ("2026-04-12").

#### Anatomi DOM Flatpickr

```
<input type="date" data-picker="date" name="transactionDate" 
       class="form-control ... flatpickr-input"
       style="display:none"                    ← input asli (hidden)
       value="2026-04-12">                     ← value ISO format
<input class="form-control ... form-input"
       type="text" 
       readonly="readonly"                     ← altInput (visible, readonly)
       value="12 Apr 2026">
```

#### Teknik: Set Value Langsung (PALING EFISIEN)

Untuk smoke test, **JANGAN** simulasi klik kalender. Set value langsung via Flatpickr API.

**MCP Playwright:**

```
browser_evaluate(function: "() => {
  const input = document.querySelector('input[name=\"transactionDate\"]');
  if (input._flatpickr) {
    input._flatpickr.setDate('2026-04-12', true);
    return 'date set';
  }
  // Fallback: set value langsung
  input.value = '2026-04-12';
  input.dispatchEvent(new Event('change', { bubbles: true }));
  return 'date set via fallback';
}")
```

**Node.js Playwright:**

```javascript
/**
 * Set tanggal pada Flatpickr input.
 * 
 * @param {Page} page
 * @param {string} selector - CSS selector input asli (bukan altInput)
 * @param {string} dateStr - format ISO: 'YYYY-MM-DD', 'YYYY-MM-DDTHH:mm', atau 'HH:mm'
 */
async function setFlatpickrDate(page, selector, dateStr) {
  await page.evaluate(({ sel, date }) => {
    const input = document.querySelector(sel);
    if (input && input._flatpickr) {
      input._flatpickr.setDate(date, true); // true = triggerChange
    } else if (input) {
      input.value = date;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }, { sel: selector, date: dateStr });
}

// Contoh:
await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-04-12');
await setFlatpickrDate(page, 'input[name="startDateTime"]', '2026-04-12T09:00');
await setFlatpickrDate(page, 'input[name="startTime"]', '14:30');
```

#### Teknik: Flatpickr di dalam Line Item / Dynamic Row

```javascript
async function setLineItemDate(page, rowIndex, fieldName, dateStr) {
  await page.evaluate(({ idx, name, date }) => {
    const rows = document.querySelectorAll('.id-row, .address-row, .contact-row, .line-row');
    const input = rows[idx]?.querySelector(`input[name*="${name}"]`);
    if (input && input._flatpickr) {
      input._flatpickr.setDate(date, true);
    } else if (input) {
      input.value = date;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }, { idx: rowIndex, name: fieldName, date: dateStr });
}

// Contoh: set expiry date di identification row ke-0
await setLineItemDate(page, 0, 'expiryDate', '2027-12-31');
```

### 4.4 AutoNumeric (Numeric Input)

AutoNumeric mengformat angka dengan separator (1,000.00) dan menyimpan raw value internal. JANGAN fill langsung karena formatting bisa break.

#### Teknik: Set via API

**MCP Playwright:**

```
browser_evaluate(function: "() => {
  const el = document.querySelector('#header-rate');
  if (typeof AutoNumeric !== 'undefined') {
    const instance = AutoNumeric.getAutoNumericElement(el);
    if (instance) {
      instance.set(15000);
      return 'set via AutoNumeric';
    }
  }
  // Fallback
  el.value = '15000';
  el.dispatchEvent(new Event('input', { bubbles: true }));
  return 'set via fallback';
}")
```

**Node.js Playwright:**

```javascript
/**
 * Set value pada AutoNumeric input.
 */
async function setAutoNumeric(page, selector, value) {
  await page.evaluate(({ sel, val }) => {
    const el = document.querySelector(sel);
    if (typeof AutoNumeric !== 'undefined') {
      const instance = AutoNumeric.getAutoNumericElement(el);
      if (instance) { instance.set(val); return; }
    }
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { sel: selector, val: value });
}

// Contoh:
await setAutoNumeric(page, 'input[name="exchangeRate"]', 15000);
await setAutoNumeric(page, '.input-price', 25000.50);
```

#### AutoNumeric di Line Item

```javascript
async function setLineItemNumeric(page, rowIndex, inputClass, value) {
  await page.evaluate(({ idx, cls, val }) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const el = rows[idx]?.querySelector(`.${cls}`);
    if (!el) return;
    if (typeof AutoNumeric !== 'undefined') {
      const instance = AutoNumeric.getAutoNumericElement(el);
      if (instance) { instance.set(val); return; }
    }
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { idx: rowIndex, cls: inputClass, val: value });
}

// Contoh: set price di row ke-0
await setLineItemNumeric(page, 0, 'input-price', 50000);
```

### 4.5 Standard Form Elements

Elemen form standard (text input, select, checkbox, radio, textarea) bisa di-handle normal.

**MCP Playwright:**

```
# Text input
browser_type(ref: "<ref>", text: "Product Name ABC")

# Standard select (bukan TomSelect)
browser_select_option(ref: "<ref>", values: ["value1"])

# Checkbox
browser_click(ref: "<ref_checkbox>")

# Radio button
browser_click(ref: "<ref_radio>")

# Textarea
browser_type(ref: "<ref>", text: "Some notes here...")
```

**Node.js Playwright:**

```javascript
// Text input
await page.fill('input[name="name"]', 'Product Name ABC');

// Standard select (bukan TomSelect!)
await page.selectOption('select[name="uomId"]', { value: '1' });

// Checkbox
await page.check('input[name="isActive"]');
// atau uncheck:
await page.uncheck('input[name="isActive"]');

// Radio button
await page.click('input[name="type"][value="COMPANY"]');

// Textarea
await page.fill('textarea[name="note"]', 'Test notes');
```

### 4.6 Line Item / Dynamic Rows

Sistem line item menggunakan `ErpLineManager` class. Tombol "Add Line" membuat row baru dari template HTML tersembunyi, lalu auto-initialize TomSelect dan AutoNumeric.

#### Menambah Line Item

**MCP Playwright:**

```
# 1. Klik tombol Add Line
browser_click(ref: "<ref_btn_add_line>")

# 2. Tunggu row muncul
browser_wait_for(time: 1)

# 3. Isi TomSelect di row baru via evaluate
browser_evaluate(function: "() => { ... }") // lihat section 4.2
```

**Node.js Playwright:**

```javascript
/**
 * Tambah satu line item dan isi field-fieldnya.
 */
async function addLineItem(page, data) {
  // Klik Add Line
  await page.click('#btn-add-line');
  await page.waitForTimeout(500); // tunggu TomSelect init
  
  // Hitung index row baru
  const rowCount = await page.evaluate(() => 
    document.querySelectorAll('#line-container .line-row').length
  );
  const newIndex = rowCount - 1;
  
  // Isi product (TomSelect)
  if (data.product) {
    await selectLineItemTomSelect(page, newIndex, 'select-product', data.product);
    await page.waitForTimeout(300); // tunggu cascade: uom, price auto-fill
  }
  
  // Isi grid (TomSelect)
  if (data.grid) {
    await selectLineItemTomSelect(page, newIndex, 'select-grid', data.grid);
  }
  
  // Isi container/bin (TomSelect)
  if (data.container) {
    await selectLineItemTomSelect(page, newIndex, 'select-container', data.container);
  }
  
  // Isi price (AutoNumeric)
  if (data.price) {
    await setLineItemNumeric(page, newIndex, 'input-price', data.price);
  }
  
  return newIndex;
}

// Contoh penggunaan:
await addLineItem(page, {
  product: '',        // kosong = pilih produk pertama yang tersedia
  grid: '',
  container: '',
  price: 50000
});
```

#### Menghapus Line Item

```javascript
async function removeLineItem(page, rowIndex) {
  await page.evaluate((idx) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const btn = rows[idx]?.querySelector('.btn-remove-line');
    if (btn) btn.click();
  }, rowIndex);
}
```

#### Line Item dengan Drawer (Stock Adjustment — Qty, UoM, Serial Numbers)

Stock Adjustment menggunakan drawer (offcanvas/dialog) untuk edit qty dan serial numbers per line item. Qty di table bersifat **readonly** — HARUS diisi via drawer.

**Ada dua jenis drawer:**
- **Serial drawer** (`drawer-serial`): untuk produk dengan `is_serialized=true`. Memiliki tabel serial number yang muncul setelah qty diisi.
- **Non-serial drawer** (`drawer-non-serial`): untuk produk biasa. Hanya input qty + UoM.

**Workflow Serial Drawer (MCP Playwright — TESTED):**

```
# 1. Klik pencil icon pada row
browser_click(ref: "<ref_pencil_btn>", element: "Edit qty pencil button row 1")
# → Dialog "Item Detail (Serialized)" terbuka

# 2. Set qty via AutoNumeric API
browser_evaluate(function: "() => {
  const dialog = document.querySelector('.offcanvas.show, dialog[open], .modal.show');
  const qtyInput = dialog.querySelector('.input-qty-target');
  const an = AutoNumeric.getAutoNumericElement(qtyInput);
  if (an) {
    an.set(2); // set qty = 2
    qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
    return 'set via AutoNumeric: ' + qtyInput.value;
  }
  return 'AutoNumeric not found';
}")
# → Serial number rows otomatis muncul di tabel (1 row per unit)

# 3. Isi serial number di tiap row
browser_snapshot()
# → textbox [ref=e285] row 1, textbox [ref=e290] row 2
browser_type(ref: "e285", text: "SN-SAMSUNG-001")
browser_type(ref: "e290", text: "SN-SAMSUNG-002")

# 4. Klik Apply
browser_click(ref: "<ref_apply_btn>", element: "Apply button")
# → Drawer tutup, qty di table ter-update (e.g. "2.00")
```

**Workflow Non-Serial Drawer (MCP Playwright — TESTED):**

```
# 1. Klik pencil icon pada row
browser_click(ref: "<ref_pencil_btn>", element: "Edit qty pencil button row 2")
# → Dialog "Item Detail (Standard)" terbuka

# 2. Set qty via AutoNumeric API
browser_evaluate(function: "() => {
  const dialog = document.querySelector('.offcanvas.show, dialog[open], .modal.show');
  const qtyInput = dialog.querySelector('.input-qty-target');
  const an = AutoNumeric.getAutoNumericElement(qtyInput);
  if (an) {
    an.set(5); // set qty = 5
    qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
    return 'set: ' + qtyInput.value;
  }
}")

# 3. Klik Apply (tidak perlu isi serial number)
browser_click(ref: "<ref_apply_btn>", element: "Apply button")
```

**Set Unit Cost (Price) setelah drawer ditutup:**

```
# Price input menggunakan AutoNumeric, class: input-price
browser_evaluate(function: "() => {
  const priceInputs = document.querySelectorAll('input.input-price');
  // Index 0 = row pertama, index 1 = row kedua, dst.
  const p = priceInputs[0];
  const an = AutoNumeric.getAutoNumericElement(p);
  if (an) {
    an.set(15000000);
    p.dispatchEvent(new Event('change', { bubbles: true }));
    return 'price set: ' + p.value;
  }
}")

# Untuk trigger recalculation total jika tidak otomatis:
browser_evaluate(function: "() => {
  document.querySelectorAll('input.input-price').forEach(p => {
    p.dispatchEvent(new Event('input', { bubbles: true }));
    p.dispatchEvent(new Event('change', { bubbles: true }));
  });
}")
```

**Node.js Playwright (bypass drawer, langsung set value):**

```javascript
/**
 * Set quantity pada line item Stock Adjustment tanpa membuka drawer.
 * Langsung set hidden inputs + visible qty field.
 */
async function setLineItemQty(page, rowIndex, qty, uomId = null, factor = 1) {
  await page.evaluate(({ idx, q, uom, f }) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const row = rows[idx];
    if (!row) return;
    
    // Set qty (AutoNumeric aware)
    const qtyEl = row.querySelector('.input-qty');
    if (typeof AutoNumeric !== 'undefined') {
      const instance = AutoNumeric.getAutoNumericElement(qtyEl);
      if (instance) instance.set(q);
      else qtyEl.value = q;
    } else {
      qtyEl.value = q;
    }
    
    // Set UoM jika diperlukan
    if (uom) {
      row.querySelector('.input-uom-id').value = uom;
      row.querySelector('.input-uom-factor').value = f;
    }
    
    // Trigger recalculation
    qtyEl.dispatchEvent(new Event('input', { bubbles: true }));
  }, { idx: rowIndex, q: qty, uom: uomId, f: factor });
}
```

#### Line Item pada Party Form (Contacts/Addresses/Identifications)

Party form menggunakan tabel inline (bukan ErpLineManager) dengan tombol "Tambah" per section.

```javascript
// Tambah contact row
await page.click('button:has-text("Tambah Kontak")');
await page.waitForTimeout(300);

// Isi contact fields di row terakhir
const contactIndex = await page.evaluate(() => 
  document.querySelectorAll('#table-contacts tbody tr').length - 1
);
await page.fill(`input[name="contacts[${contactIndex}].contactValue"]`, '08123456789');
await page.selectOption(`select[name="contacts[${contactIndex}].typeId"]`, { index: 1 });
```

### 4.7 AJAX Form Submission

Form ERP menggunakan AJAX submission (`data-ajax-form="true"`). Setelah submit:
- Jika **sukses**: redirect ke list page dengan success message di sessionStorage
- Jika **validation error**: field-field invalid ditandai merah dengan pesan error

**MCP Playwright — Submit dan verifikasi:**

```
# Klik tombol submit
browser_click(ref: "<ref_submit_btn>")

# Tunggu response — dua kemungkinan:
# 1. Redirect ke list page (sukses)
browser_wait_for(text: "berhasil", time: 5)

# 2. Error muncul di form
browser_snapshot() → cek apakah ada class "is-invalid" atau alert-danger
```

**Node.js Playwright — Submit dan verifikasi:**

```javascript
async function submitFormAndVerify(page, expectedRedirectUrl) {
  // Intercept AJAX response
  const responsePromise = page.waitForResponse(
    resp => resp.request().method() === 'POST' && resp.headers()['content-type']?.includes('json'),
    { timeout: 10000 }
  );
  
  await page.click('button[type="submit"]');
  
  const response = await responsePromise;
  const body = await response.json();
  
  if (body.success) {
    // Tunggu redirect
    await page.waitForURL(`**${expectedRedirectUrl}**`, { timeout: 5000 });
    return { success: true, message: body.message };
  } else {
    return { success: false, errors: body.validationErrors, message: body.message };
  }
}

// Contoh:
const result = await submitFormAndVerify(page, '/inventory/products');
console.log(result.success ? '✅ Saved' : '❌ Failed: ' + JSON.stringify(result.errors));
```

### 4.8 HTMX Content (Search/Filter/Pagination)

Search dan pagination menggunakan HTMX untuk swap konten secara partial.

**Node.js Playwright:**

```javascript
// Search
await page.fill('input[name="keyword"]', 'test product');
await page.waitForTimeout(500); // debounce
await page.press('input[name="keyword"]', 'Enter');
await page.waitForResponse(resp => resp.url().includes('keyword='));

// Pagination — klik halaman 2
await page.click('.pagination a:has-text("2")');
await page.waitForLoadState('networkidle');

// Sorting — klik header kolom
await page.click('th:has-text("Name") a');
await page.waitForLoadState('networkidle');
```

---

## 5. Fallback: Node.js Playwright Script

Template lengkap smoke test script yang bisa dijalankan jika MCP Playwright error:

```javascript
// tests/smoke-test.spec.js
// Jalankan: npx playwright test tests/smoke-test.spec.js
// JANGAN taruh di node_modules lokal. Gunakan global playwright.

const { test, expect } = require('@playwright/test');

const BASE_URL = 'http://localhost:18080';
const CREDS = { username: 'admin', password: 'admin123' };

// ═══════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════

async function login(page) {
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[name="username"]', CREDS.username);
  await page.fill('input[name="password"]', CREDS.password);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/*', { timeout: 10000 });
}

async function selectTomSelect(page, selector, query = '', index = 0) {
  return await page.evaluate(({ sel, q, idx }) => {
    return new Promise((resolve, reject) => {
      const el = document.querySelector(sel);
      if (!el?.tomselect) { reject(`TomSelect not found: ${sel}`); return; }
      const ts = el.tomselect;
      ts.load(q, (opts) => {
        if (!opts.length) { resolve({ error: 'empty' }); return; }
        opts.forEach(o => ts.addOption(o));
        const pick = opts[Math.min(idx, opts.length - 1)];
        ts.setValue(pick.id);
        resolve({ id: pick.id, name: pick.name });
      });
    });
  }, { sel: selector, q: query, idx: index });
}

async function setFlatpickrDate(page, selector, dateStr) {
  await page.evaluate(({ sel, d }) => {
    const input = document.querySelector(sel);
    if (input?._flatpickr) input._flatpickr.setDate(d, true);
    else if (input) { input.value = d; input.dispatchEvent(new Event('change', { bubbles: true })); }
  }, { sel: selector, d: dateStr });
}

async function setAutoNumeric(page, selector, value) {
  await page.evaluate(({ sel, v }) => {
    const el = document.querySelector(sel);
    if (typeof AutoNumeric !== 'undefined') {
      const inst = AutoNumeric.getAutoNumericElement(el);
      if (inst) { inst.set(v); return; }
    }
    el.value = v;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { sel: selector, v: value });
}

async function submitAndWait(page, listUrl) {
  const respPromise = page.waitForResponse(
    r => r.request().method() === 'POST' && r.headers()['content-type']?.includes('json'),
    { timeout: 15000 }
  );
  await page.click('button[type="submit"]');
  const resp = await respPromise;
  const body = await resp.json();
  if (body.success && listUrl) {
    await page.waitForURL(`**${listUrl}**`, { timeout: 5000 });
  }
  return body;
}

// ═══════════════════════════════════════════════
// TESTS
// ═══════════════════════════════════════════════

test.describe('ERP Smoke Test', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('Login succeeds', async ({ page }) => {
    // Login sudah dilakukan di beforeEach
    const url = page.url();
    expect(url).not.toContain('/login');
  });

  test('Create Brand', async ({ page }) => {
    await page.goto(`${BASE_URL}/inventory/brands/create`);
    await page.waitForLoadState('networkidle');
    
    await page.fill('input[name="name"]', 'Test Brand Smoke');
    
    const result = await submitAndWait(page, '/inventory/brands');
    expect(result.success).toBeTruthy();
  });

  test('Create Product Category', async ({ page }) => {
    await page.goto(`${BASE_URL}/inventory/product-categories/create`);
    await page.waitForLoadState('networkidle');
    
    await page.fill('input[name="name"]', 'Test Category Smoke');
    // Select type jika ada
    const typeSelect = await page.$('select[name="type"]');
    if (typeSelect) await page.selectOption('select[name="type"]', { index: 1 });
    
    const result = await submitAndWait(page, '/inventory/product-categories');
    expect(result.success).toBeTruthy();
  });

  test('Create Product with TomSelect fields', async ({ page }) => {
    await page.goto(`${BASE_URL}/inventory/products/create`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(500); // wait for TomSelect init
    
    // Required fields
    await page.fill('input[name="name"]', 'Test Product Smoke');
    
    // TomSelect: Category
    await selectTomSelect(page, '#category-select', '', 0);
    await page.waitForTimeout(300);
    
    // Standard select: UoM
    await page.selectOption('#uom-select', { index: 1 });
    
    // TomSelect: Brand (optional tapi test aja)
    await selectTomSelect(page, '#brand-select', '', 0);
    
    const result = await submitAndWait(page, '/inventory/products');
    expect(result.success).toBeTruthy();
  });

  // Tambah test cases lain sesuai kebutuhan...
});
```

**Konfigurasi `playwright.config.js` (taruh di root project):**

```javascript
// playwright.config.js
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  timeout: 60000,
  retries: 0,
  use: {
    baseURL: 'http://localhost:18080',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
});
```

> **Catatan:** File `playwright.config.js` dan folder `tests/` boleh ada di repo karena ringan. Yang DILARANG adalah `node_modules/`.

---

## 6. Troubleshooting

### 6.1 MCP Playwright Error Mid-Session

**Gejala:** Tool `browser_navigate` atau `browser_snapshot` return error setelah beberapa kali pakai.

**Solusi bertahap:**
1. **Coba `browser_close` lalu `browser_navigate` ulang** — kadang cukup restart browser page
2. **Jika masih error:** switch ke Node.js Playwright script mode (lihat Section 5)
3. **Jangan install Playwright lokal di repo** — selalu gunakan `npx playwright` (global)

### 6.2 TomSelect Tidak Ter-initialize

**Gejala:** `el.tomselect` is `undefined` saat evaluate.

**Penyebab:** Script belum selesai load (karena `defer` attribute).

**Solusi:**
```javascript
// Tunggu sampai TomSelect ready
await page.waitForFunction(() => {
  const el = document.querySelector('#category-select');
  return el && el.tomselect;
}, { timeout: 5000 });
```

**MCP Playwright:**
```
browser_wait_for(time: 2)
// atau
browser_evaluate(function: "() => {
  return new Promise((resolve) => {
    const check = () => {
      const el = document.querySelector('#category-select');
      if (el && el.tomselect) resolve(true);
      else setTimeout(check, 200);
    };
    check();
  });
}")
```

### 6.3 AJAX Submit Tidak Dapat Response

**Gejala:** `waitForResponse` timeout.

**Penyebab:** CSRF token tidak valid (session expired).

**Solusi:** Pastikan login sudah berhasil sebelum navigasi ke form. Cek CSRF token ada:
```javascript
const hasCsrf = await page.evaluate(() => 
  !!document.querySelector('input[name="_csrf"]')?.value
);
console.log('CSRF present:', hasCsrf);
```

### 6.4 Flatpickr Input Tidak Mau Di-fill

**Gejala:** `page.fill()` gagal karena input `readonly`.

**Penyebab:** Flatpickr altInput bersifat readonly. Input asli di-hide.

**Solusi:** Selalu gunakan `setFlatpickrDate()` helper via `page.evaluate()`. Jangan pernah `page.fill()` langsung ke Flatpickr input.

### 6.5 AutoNumeric Mengirim Value Formatted

**Gejala:** Server error `NumberFormatException` — value diterima sebagai "1,000.00" bukan 1000.

**Penyebab:** `page.fill()` bypass AutoNumeric formatting.

**Solusi:** Selalu gunakan `setAutoNumeric()` helper. AutoNumeric akan handle unformat saat form serialize.

### 6.6 Line Item TomSelect Cascade Tidak Jalan

**Gejala:** Grid/Container TomSelect tidak load options setelah Product/Facility dipilih.

**Penyebab:** Cascade TomSelect bergantung pada event `change` + parent value. Saat set via JS API (`ts.setValue()`), event mungkin tidak ter-trigger ke listener cascade.

**Solusi — Pakai klik approach (DIREKOMENDASIKAN):**

Klik combobox + klik option secara berurutan menggunakan MCP Playwright. Klik di UI memicu semua native events secara otomatis termasuk cascade trigger.

```
browser_click(ref: "<ref_facility_combobox>", element: "Facility dropdown")
browser_snapshot()
browser_click(ref: "<ref_gudang_utama>", element: "Gudang Utama option")
# → Grid TomSelect otomatis ter-populate

browser_click(ref: "<ref_grid_combobox>", element: "Grid dropdown")
browser_snapshot()
browser_click(ref: "<ref_grid_a>", element: "GRD-DEMO-A option")
# → Container TomSelect otomatis ter-populate

browser_click(ref: "<ref_container_combobox>", element: "Container dropdown")
browser_snapshot()
browser_click(ref: "<ref_container>", element: "Container option")
```

**Solusi fallback — Node.js (jika klik tidak memungkinkan):**

```javascript
await selectLineItemTomSelect(page, 0, 'select-product', '');
await page.waitForTimeout(500); // tunggu cascade effect
await selectLineItemTomSelect(page, 0, 'select-grid', '');
await page.waitForTimeout(300);
await selectLineItemTomSelect(page, 0, 'select-container', '');
```

### 6.7 Blank/White Page atau Timeout pada Form Load (Template Error) ⭐ PENTING

**Gejala:** 
- Playwright MCP menunjukkan timeout saat load form
- Browser menampilkan halaman putih/blank
- "Internal Server Error" muncul di browser

**Penyebab (SERING TERJADI):**
- **Template/Thymeleaf error** — property tidak exist pada object yang di-render
- Contoh: `${curr.code}` ketika Currency object hanya punya `symbol` dan `alias` property
- Migration failure yang tidak sepenuhnya ter-apply
- Backend method tidak mengirim object yang expected ke template

**Debugging Steps (HARUS DIIKUTI SETIAP KALI TERJADI):**

#### Step 1: Check Server Logs LANGSUNG (Prioritas #1)
Jangan tunggu timeout selesai. Segera check server logs dari process yang running:

```powershell
# Jika server sedang berjalan di async shell
read_powershell(shellId: "spring_boot_server", delay: 5)

# Cari error pattern:
# - "Exception processing template"
# - "SpelEvaluationException"
# - "Property or field X cannot be found"
# - "Failed to initialize dependency"
```

**Contoh error yang terlihat di server logs:**
```
org.springframework.expression.spel.SpelEvaluationException: 
EL1008E: Property or field 'code' cannot be found on object of type 
'com.solusi.erp.master.currency.domain.model.Currency'
	at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.evaluate(...)
```

#### Step 2: Check Browser Console Messages
Jika server logs tidak menunjukkan error, check browser console:

```
browser_console_messages(level: "error")
```

#### Step 3: Inspect Network Requests
Jika response status adalah 500, lihat apa yang di-return:

```
browser_network_requests(filter: "/purchasing.*", requestBody: true)
```

#### Step 4: Take Visual Screenshot (Terakhir)
Screenshot hanya informatif jika server logs kosong. Gunakan untuk melihat error message yang di-render:

```
browser_take_screenshot(fullPage: true)
```

**PENTING:** **Server logs SELALU lebih informatif daripada browser UI karena full stack trace ada di server.**

#### Contoh: Currency Property Error Fix

**Error yang terlihat:**
```
Exception evaluating SpringEL expression: "curr.code"
Property or field 'code' cannot be found on object of type Currency
```

**Solusi:**
1. Check Currency domain model → lihat apa saja properties yang public
2. Ganti template: `${curr.code}` → `${curr.alias}` (misalnya)
3. Restart server
4. Re-test form load

**Prevention:**
- Selalu check domain model properties sebelum menggunakan di template
- Di Thymeleaf, gunakan `th:object` untuk auto-detect typo:
  ```html
  <div th:object="${currency}">
    <!-- Thymeleaf akan warn jika property tidak exist -->
    <span th:text="*{alias}"></span>  
  </div>
  ```
- Validasi dengan TDD: buat test untuk template rendering sebelum manual testing

---

## 7. Checklist Smoke Test per Modul

### Minimum Viable Smoke Test

Untuk setiap modul CRUD, lakukan:

| Step | Aksi | Verifikasi |
|------|------|------------|
| 1 | Navigate ke list page | Halaman load tanpa error JS |
| 2 | Klik "Add New" / "Create" | Form tampil dengan semua field |
| 3 | Isi required fields minimum | Tidak ada error saat isi |
| 4 | Submit form | Response `{ success: true }` + redirect ke list |
| 5 | Cari data yang baru dibuat di list | Data muncul dengan nilai yang benar |
| 6 | Klik edit pada data tersebut | Form edit tampil dengan data pre-filled |
| 7 | Ubah 1 field, submit | Update berhasil |

### Verifikasi Database

```sql
-- Contoh: verifikasi product tersimpan
SELECT id, code, name, category_id, brand_id, uom_id, is_active 
FROM products 
ORDER BY created_date DESC 
LIMIT 5;

-- Contoh: verifikasi stock adjustment + lines
-- (PENTING: prefix tabel adalah inv_, bukan tanpa prefix)
SELECT sa.code, sa.status, sal.product_id, sal.quantity, sal.unit_cost
FROM inv_stock_adjustments sa
JOIN inv_stock_adjustment_lines sal ON sal.stock_adjustment_id = sa.id
ORDER BY sa.created_date DESC
LIMIT 10;

-- Verifikasi movements dan stock balance setelah SA diproses
SELECT * FROM inv_movements ORDER BY id DESC LIMIT 10;
SELECT * FROM inv_stock_balances ORDER BY id DESC LIMIT 10;
```

### Quick Validation Snippet (MCP Playwright)

Setelah submit form, cek apakah data muncul di list:

```
browser_snapshot()
# Dari snapshot, cari teks nama data yang baru dibuat
# Jika ada → success
# Jika ada alert merah → ada error, baca pesannya
```

---

## Ringkasan — Kapan Pakai Apa

| Komponen | ❌ Jangan | ✅ Gunakan |
|----------|-----------|-----------|
| TomSelect (MCP) | `browser_fill_form`, `browser_select_option` | **Klik combobox → klik option** (primary); `browser_evaluate` → `ts.load()+setValue()` (fallback) |
| TomSelect (Node.js) | `page.fill()`, `page.selectOption()` | `page.evaluate()` → `el.tomselect.load()` + `setValue()` |
| Flatpickr | `page.fill()` (readonly), klik kalender | `page.evaluate()` → `input._flatpickr.setDate()` |
| AutoNumeric | `page.fill()` (bypass format) | `page.evaluate()` → `AutoNumeric.getAutoNumericElement().set()` |
| SA Drawer Qty | Isi langsung ke readonly field | Klik pencil icon → set AutoNumeric di drawer → klik Apply |
| SA Serial Number | - | Set qty → tunggu serial rows muncul → isi `.input-sn-item` |
| Standard Input | - | `page.fill()` / `browser_type` / `browser_fill_form` |
| Standard Select | - | `page.selectOption()` / `browser_select_option` |
| Checkbox/Radio | - | `page.check()` / `page.click()` / `browser_click` |
| Line Item Add | - | Klik tombol "Add Line" + delay + fill fields |
| AJAX Form Submit | `page.click()` tanpa wait | `page.click()` + `waitForResponse` JSON |
| JS Confirm Dialog | - | `browser_handle_dialog(accept: true)` |

# Implementation Plan: Autocomplete Access/Error Feedback (SOL-55)

> Source: Linear SOL-55 — "Improvement - Kalau Autocomplete tidak punya access (forbidden/tidak ada akses pada permission granular) munculkan popup warning"
> URL: https://linear.app/solusi-program/issue/SOL-55
> Created: 2026-05-30
> Sprint: minor-fix-bug-batch-1
> Status: IN_PROGRESS

## Summary

Saat ini, jika user membuka form yang memiliki field autocomplete (TomSelect) tetapi tidak punya permission granular `LOOKUP_*`, dropdown hanya tampil kosong tanpa feedback apa pun. Plan ini memperbaiki jalur mayoritas global (shared `initLookup`) dan menyelaraskan loader manual yang teridentifikasi agar autocomplete di seluruh aplikasi menampilkan pesan feedback saat query lookup gagal (403 Forbidden atau error lain).

## Root Cause (verified)

**Akar masalah inti ada di FRONTEND.** Status HTTP backend sudah benar.

1. **Frontend (akar masalah)** — `initLookup()` di `erp-common-handler.js` (load handler) memanggil `fetch(url).then(r => r.json()).then(callback).catch(() => callback([]))`. Tidak ada cek `r.ok`; semua kegagalan (403, 500, parse error) ditelan menjadi list kosong → dropdown kosong senyap. Fix utama = **cek `r.ok`/`r.status` sebelum `r.json()`**.
      ref: src/main/resources/static/js/shared/erp-common-handler.js:L352-L360 — load handler tanpa cek r.ok
2. **Backend (status SUDAH benar — bukan bug)** — `@ExceptionHandler(AccessDeniedException)` sudah beranotasi `@ResponseStatus(HttpStatus.FORBIDDEN)`, jadi saat `@PreAuthorize` menolak respons **sudah 403** (dengan atau tanpa header `Accept`). Jadi `r.status === 403` sudah tersedia di frontend tanpa perubahan backend apa pun.
      ref: src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java:L159-L171 — handleAccessDeniedException + @ResponseStatus(FORBIDDEN)
3. **Content negotiation (perbaikan rapi, opsional)** — `fetch` polos mengirim `Accept: */*`. Karena `isAjaxRequest()` hanya mendeteksi `application/json`/`X-Requested-With`/`HX-Request`, body 403 dikembalikan sebagai **HTML `error/403` penuh** (bukan JSON). Menambah header `Accept: application/json` pada fetch lookup membuat body 403 jadi JSON kecil — best practice content-negotiation, BUKAN yang memperbaiki status code. Frontend tetap aman walau body bukan JSON karena keputusan feedback hanya bergantung pada `r.status`/`r.ok` (lihat Task 2).
      ref: src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java:L164-L168 — cabang isAjaxRequest → JSON 403
4. **Loader manual (cakupan tambahan terverifikasi)** — shared `initLookup()` bukan satu-satunya pembuat TomSelect. Stock Adjustment memiliki copy privat, navbar menu search memiliki loader inline, dan form Party memiliki tiga loader geografis inline. Ketiganya perlu diselaraskan di Task 3. Khusus Party edit, hierarchy fetch yang gagal juga dapat membuat `cityId` submit kosong jika canonical value tidak dipertahankan terpisah dari UI TomSelect.
      ref: src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L28-L70
      ref: src/main/resources/templates/layout/master.html:L95-L119
      ref: src/main/resources/templates/master/parties/form.html:L494-L677

## Design Decision (untuk direview)

Issue memberi kebebasan: popup warning **atau** 1 elemen list di dropdown yang menampilkan pesan. Plan ini memilih **opsi dropdown row** sebagai feedback utama karena:

- **Paling tidak mengganggu** — autocomplete memicu `load` saat focus + tiap ketik (debounce). Popup/modal di tiap keystroke akan spam. Pesan inline di dropdown muncul tepat di tempat user melihat.
- **Mayoritas global & terpusat** — override `render.no_results` di shared `initLookup` otomatis berlaku untuk fragment autocomplete standar. Loader manual yang tidak memakai shared helper tetap harus diselaraskan secara eksplisit di Task 3.
- **Reuse TomSelect** — tidak perlu komponen baru, hanya hook render bawaan.

**Tidak ada perubahan backend.** Status 403 sudah benar (lihat Root Cause #2). Header `Accept: application/json` ditambahkan **di sisi frontend** (pada fetch) supaya body 403 balik sebagai JSON kecil, bukan halaman HTML penuh — murni perbaikan content-negotiation. Keputusan feedback hanya bergantung pada `r.status`/`r.ok`, jadi frontend tetap aman apa pun bentuk body-nya.

**Risiko teknis #1 (dropdown-row) — diuji empiris saat implementasi:** TomSelect umumnya hanya merender `no_results` saat input punya teks query. Karena lookup pakai `preload: 'focus'` (memuat dengan `q=''` saat difokus), ada kemungkinan baris pesan TIDAK muncul kalau user hanya fokus tanpa mengetik. Ini diverifikasi tepat di kriteria validasi Task 2. **Fallback (sudah disetujui):** jika `no_results` tidak reliable, ganti ke `ErpModal.showWarning(...)` dengan throttle 1x per buka-dropdown (lihat Task 2 langkah fallback).

**Risiko teknis #2 (Party edit cityId preservation) — wajib ditangani di Task 3:** form Party memiliki tiga TomSelect geografis manual (`country`, `province`, `city`) dan fetch hierarchy terpisah. Saat edit, `cityId` awal hanya ada di hidden helper lalu dipindahkan ke `<select name="addresses[i].cityId">` setelah hierarchy fetch sukses. Jika user punya `PARTY_UPDATE` tetapi tidak punya `LOOKUP_GEOGRAPHIC`, hierarchy fetch mendapat 403 dan select submit tetap kosong. Save unrelated field berisiko menghapus `cityId` lama. Fix: jadikan hidden input `addresses[i].cityId` sebagai canonical submitted value yang diinisialisasi SSR; TomSelect city menjadi UI-only dan hanya menyinkronkan hidden value saat user benar-benar mengubah pilihan.

## Tasks

### Task 1: i18n keys + expose ke `window.ErpI18n` [x]
Tambah pesan error lookup (forbidden + generic + no-results) di kedua file properties dan expose ke JS global.

**Depends on:** (none)
**Reference module:** master.html global i18n block

Steps:
- [x] Tambah key di `messages.properties` (EN) di blok "AJAX / JS Global Messages":
      - `msg.error.lookup.forbidden=You don't have access to this list.`
      - `msg.error.lookup.failed=Failed to load. Please try again.`
      - `label.lookup.no-results=No results found`
      ref: src/main/resources/messages.properties:L298-L301 — blok msg.error.ajax.* (gunakan replace tool, JANGAN echo)
- [x] Tambah key padanan Bahasa Indonesia di `messages_id.properties`:
      - `msg.error.lookup.forbidden=Anda tidak punya akses ke daftar ini.`
      - `msg.error.lookup.failed=Gagal memuat. Silakan coba lagi.`
      - `label.lookup.no-results=Tidak ada hasil`
      ref: src/main/resources/messages_id.properties:L299-L301 — blok msg.error.ajax.* (ikuti protokol i18n di docs/spec/i18n-guide.md §7)
- [x] Expose 3 key tersebut ke `window.ErpI18n` di master.html:
      ```js
      lookupForbidden: /*[[#{msg.error.lookup.forbidden}]]*/ "You don't have access to this list.",
      lookupError:     /*[[#{msg.error.lookup.failed}]]*/ 'Failed to load. Please try again.',
      lookupNoResults: /*[[#{label.lookup.no-results}]]*/ 'No results found'
      ```
      ref: src/main/resources/templates/layout/master.html:L61-L68 — window.ErpI18n block

**Validation criteria:**
- Aplikasi compile & start; tidak ada `NoSuchMessageException` saat render master.html.
- `window.ErpI18n.lookupForbidden` terisi di browser console (EN & ID sesuai cookie `lang`).

---

### Task 2: Fix shared `initLookup` — cek `r.ok` + tampilkan feedback row [x]
Perbaiki load handler agar mendeteksi kegagalan dan menampilkan pesan di dropdown via `render.no_results`. Ini perbaikan inti yang berlaku global.

**Depends on:** Task 1
**Reference module:** erp-common-handler.js initLookup

Steps:
- [x] Di `initLookup`, deklarasikan closure var dan fallback string sebelum `new TomSelect(...)` (closure aman dipakai arrow fn di `render`):
      ```js
      let loadError = null;
      const lookupI18n = window.ErpI18n || {};
      const lookupForbidden = lookupI18n.lookupForbidden || "You don't have access to this list.";
      const lookupFailed = lookupI18n.lookupError || 'Failed to load. Please try again.';
      ```
      ref: src/main/resources/static/js/shared/erp-common-handler.js:L321-L325 — awal fungsi initLookup
- [x] Ganti body `load`:
      - reset `loadError = null;` di awal
      - `fetch(url, { headers: { 'Accept': 'application/json' } })`
      - cek `if (!r.ok) { loadError = r.status === 403 ? lookupForbidden : lookupFailed; callback([]); return; }`
      - jika ok: `return r.json().then(callback);`
      - `.catch(() => { loadError = lookupFailed; callback([]); });`
      ref: src/main/resources/static/js/shared/erp-common-handler.js:L352-L360 — load handler lama
- [x] Tambah `no_results` ke object `render`:
      ```js
      no_results: (data, escape) => loadError
        ? `<div class="no-results text-danger px-2 py-1"><i class="ti ti-alert-triangle me-1"></i>${escape(loadError)}</div>`
        : `<div class="no-results px-2 py-1">${escape((window.ErpI18n && window.ErpI18n.lookupNoResults) || 'No results found')}</div>`
      ```
      ref: src/main/resources/static/js/shared/erp-common-handler.js:L361-L371 — object render (option/item)
- [x] Pastikan akses `window.ErpI18n` defensif (fallback string) agar tidak error bila i18n block belum termuat.
- [x] **VERIFIKASI EMPIRIS (titik keputusan #1):** jalankan skenario forbidden, BUKA dropdown TANPA mengetik (karena `preload: 'focus'` memuat `q=''`). Konfirmasi baris merah muncul.
      - JIKA `no_results` tidak ter-render saat query kosong → aktifkan **FALLBACK POPUP**: di handler `load`, saat `loadError` ter-set, panggil `ErpModal.showWarning(loadError)` dengan guard throttle (mis. flag `errorShownForThisOpen`, di-reset pada event `dropdown_open`) agar tidak spam tiap keystroke. Catat keputusan ini di report.
      ref: src/main/resources/static/js/shared/erp-common-handler.js:L377-L397 — event dropdown_open/close (tempat reset throttle flag)

**Validation criteria:**
- User TANPA `LOOKUP_PARTY` membuka form yang punya autocomplete Party → buka dropdown → muncul feedback (baris merah ATAU popup fallback) bertuliskan pesan forbidden.
- User DENGAN izin → dropdown normal, hasil tampil seperti biasa, "No results" muncul saat query tak match.
- Network tab: request lookup mengirim `Accept: application/json`; respon 403 ber-body JSON (bukan halaman HTML `error/403`).
      ref: src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java:L164-L168 — cabang isAjaxRequest → JSON 403

---

### Task 3: Selaraskan loader manual (stock-adjustment + menu search + Party geographic) [x]
Beberapa autocomplete tidak memakai shared `initLookup` melainkan punya `TomSelect`/`load` sendiri. Terapkan perbaikan yang sama agar feedback benar-benar global.

**Depends on:** Task 2
**Reference module:** stock-adjustment-form.js (local initLookup copy), master.html (menu search inline), master/parties/form.html (geographic TomSelect inline)

Steps:
- [x] **Stock Adjustment** — file ini punya SALINAN PRIVAT fungsi `initLookup` sendiri (bukan yang shared), jadi fix Task 2 tidak otomatis berlaku. Terapkan pola sama (Accept header + cek r.ok + `loadError` + render.no_results, plus fallback popup bila dipilih di Task 2) pada salinan lokal tsb.
      ref: src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L28-L70 — salinan privat initLookup (load di L46)
- [x] **Menu search global (navbar)** — `TomSelect` inline di master.html dengan `load` sendiri. Terapkan `Accept: application/json`, cek `r.ok`, `loadError`, dan `render.no_results` generic error agar kegagalan tidak kembali menjadi dropdown kosong senyap.
      - CATATAN: controllernya `@PreAuthorize("isAuthenticated()")` (BUKAN permission granular), jadi untuk user login kasus **403 praktis tidak terpicu**. Feedback yang realistis di sini = **error backend (500)** atau sesi habis. Tetap diberi perlakuan sama demi konsistensi, tapi jangan harapkan skenario "forbidden" muncul di sini.
      ref: src/main/resources/templates/layout/master.html:L101-L106 — menu search load handler
      ref: src/main/java/com/solusi/erp/security/menusearch/web/controller/MenuSearchController.java:L28-L29 — @PreAuthorize isAuthenticated()
- [x] **Party geographic autocomplete** — form Party punya tiga `TomSelect` inline untuk `countries`, `provinces`, dan `cities`; ketiganya tidak memakai shared `initLookup`. Terapkan `Accept: application/json`, cek `r.ok`, `loadError`, dan `render.no_results` pada masing-masing loader. Gunakan helper lokal kecil agar pemetaan 403 → `lookupForbidden`, error lain → `lookupError`, dan markup no-results tidak diduplikasi tiga kali.
      ref: src/main/resources/templates/master/parties/form.html:L494-L650 — `GEO_API`, city/province/country TomSelect manual
      ref: src/main/java/com/solusi/erp/master/geographic/web/controller/GeographicLookupController.java:L17-L21 — seluruh endpoint lookup geografis dilindungi `LOOKUP_GEOGRAPHIC`
- [x] **Party edit cityId preservation** — ubah field kota Party agar hidden input menjadi canonical submitted value:
      - existing row: tambah `<input type="hidden" th:field="*{addresses[__${stat.index}__].cityId}" class="city-id-value">`
      - dynamic row template: tambah `<input type="hidden" name="addresses[INDEX].cityId" class="city-id-value" value="">`
      - hapus `name="addresses[...].cityId"` dari `<select class="city-ts">`; select hanya UI TomSelect
      - saat city berubah atau dihapus, sinkronkan `.city-id-value`
      - saat province/country berubah dan city dibersihkan secara silent, kosongkan `.city-id-value` secara eksplisit
      - pertahankan hidden helper `.initial-city-id` untuk hierarchy prefill edit
      ref: src/main/resources/templates/master/parties/form.html:L317-L338 — existing city select + initial helper
      ref: src/main/resources/templates/master/parties/form.html:L468-L473 — dynamic city select
- [x] **Party hierarchy fetch** — semua fetch ke `GEO_HIER` juga tambahkan `Accept: application/json` + cek `r.ok`. Jika hierarchy prefill gagal, tampilkan warning paling banyak 1x per page load dan JANGAN kosongkan hidden canonical `cityId`; user tetap dapat menyimpan perubahan unrelated tanpa kehilangan kota lama.
      ref: src/main/resources/templates/master/parties/form.html:L555-L569,L607-L615,L652-L677 — hierarchy fetch manual
      ref: src/main/java/com/solusi/erp/master/geographic/web/controller/GeographicController.java:L165-L182 — hierarchy endpoint dilindungi `LOOKUP_GEOGRAPHIC`
- [x] Konfirmasi loader dropdown pada `goods-receipt-form.js`, `purchase-order-form.js`, `purchase-requisition-form.js`, dan `signature-capture.js` memakai shared `initLookup` (sudah tercakup Task 2). Fetch detail/payload pendukung bukan loader dropdown diaudit pada langkah berikutnya.
      ref: src/main/resources/static/js/inventory/goods-receipt-form.js:L485-L486 — pakai initLookup shared
- [x] Audit fetch detail/payload pendukung secara terpisah. Jangan perluas patch ini untuk fetch yang bukan loader dropdown kecuali fetch hierarchy Party di atas, karena hierarchy tersebut memengaruhi preservasi nilai submit edit.
      ref: src/main/resources/static/js/purchasing/purchase-order-form.js:L204-L209 — product detail payload
      ref: src/main/resources/static/js/purchasing/purchase-requisition-form.js:L90-L102 — product detail payload
- [x] (Out of scope — catat di report saja) Refactor agar stock-adjustment memakai shared `initLookup` daripada menyalin. JANGAN refactor sekarang.

**Validation criteria:**
- Audit bersih: seluruh `TomSelect.load` handler yang memanggil endpoint lookup sudah mengirim `Accept: application/json`, mengecek `r.ok`, dan menampilkan feedback; cakup `erp-common-handler.js`, `stock-adjustment-form.js`, `layout/master.html`, dan `master/parties/form.html`.
- Stock Adjustment form: lookup product tanpa izin menampilkan feedback (baris/popup sesuai keputusan Task 2).
- Party create form: user dengan `PARTY_CREATE` tetapi tanpa `LOOKUP_GEOGRAPHIC` membuka country/city dropdown → feedback forbidden tampil.
- Party edit form: user dengan `PARTY_UPDATE` tetapi tanpa `LOOKUP_GEOGRAPHIC` membuka Party yang sudah punya alamat, mengubah field unrelated, lalu submit → payload tetap membawa `cityId` lama; warning hierarchy maksimal muncul 1x per page load.

---

### Task 4: Regression gate — seluruh E2E existing harus pass
Bug fix ini menyentuh jalur autocomplete global (shared `initLookup`) yang dipakai banyak form, jadi risiko utamanya adalah **regresi** pada lookup yang sudah jalan. Spec E2E khusus "forbidden" DI-SKIP untuk patch ini (sesuai keputusan); gantinya, gate finalisasi = seluruh suite E2E yang ada tetap hijau.

**Depends on:** Task 3

> WAJIB baca dulu: docs/tests/playwright-pitfalls.md + docs/tests/playwright-e2e-guide.md. Catatan: server dijalankan agen sendiri (profile `e2e`), JANGAN minta user start server.

Steps:
- [ ] Build dulu agar perubahan JS/i18n ikut ter-package: `mvn -q -o -DskipTests package` (atau mekanisme build E2E sesuai guide).
      ref: docs/tests/playwright-e2e-guide.md — cara start server profile e2e + pemilihan JAR
- [ ] Start server profile `e2e` secara mandiri sesuai guide, tunggu sampai siap.
- [ ] Jalankan SELURUH suite: `cd e2e-tests && npx playwright test`. Bukan hanya `--list` / `tsc`.
- [ ] Jika ada yang gagal: pastikan kegagalan BUKAN akibat perubahan ini (regresi lookup). Tangkap screenshot/trace, diagnosa, perbaiki sebelum lanjut.
- [ ] Smoke manual cepat (di luar E2E): satu form ber-autocomplete yang user-nya punya izin → pastikan dropdown masih memuat hasil normal (mengonfirmasi `r.ok` path tidak merusak happy path).
- [ ] Smoke manual Party edit: buka Party yang memiliki alamat sebagai user berizin lookup, pastikan country/province/city prefill tetap tampil dan save unrelated field mempertahankan `cityId`.

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean (jika ada perubahan TS).
- `cd e2e-tests && npx playwright test` → seluruh spec existing PASS (hijau).
- Tidak ada regresi pada autocomplete happy-path (hasil tetap tampil untuk user berizin).
- Tidak perlu entri known-issues baru di docs/tests/playwright-pitfalls.md.

---

### Task 5: Versioning (SemVer PATCH)
Naikkan versi pom sesuai protokol AGENTS.md §9.A (bug fix → PATCH).

**Depends on:** Task 4
Steps:
- [ ] Bump `<version>` proyek dari `1.8.0` → `1.8.1` di pom.xml (PATCH: bug fix, tanpa fitur baru).
      ref: pom.xml:L13 — project version (BUKAN versi Spring Boot di L8)
- [ ] Lakukan SETELAH user manual verification lolos (bug fix, sesuai §9.A.2).

**Validation criteria:**
- `mvn -q -o validate` sukses; versi terbaca 1.8.1.

## Coverage Check vs Issue

- "autocomplete tidak punya akses → tidak terjadi apa-apa" → diperbaiki Task 2 (feedback row) + Task 1 (pesan).
- "tampilkan error/forbidden ke user" → Task 1 pesan 403 vs generic.
- "1 element list itemnya yang menampilkan message" → Task 2 `render.no_results`.
- "aman secara global" → Task 2 di shared `initLookup` + Task 3 audit loader manual (stock-adjustment + menu search + Party geographic) dan preservasi `cityId` edit.
- "pilih paling mudah implementasi" → dropdown row (tanpa komponen backend baru), TANPA perubahan backend (status 403 sudah benar); tambah header `Accept`, cek `r.ok`, render feedback frontend, dan guard preservasi `cityId` Party edit.

## Out of Scope (untuk diskusi bila perlu)

- Refactor stock-adjustment agar memakai shared `initLookup` (hanya dicatat di report).
- Mengubah `isAjaxRequest` backend (tidak perlu — header `Accept` dari frontend sudah cukup).
- Permission/menu seeder changes (tidak ada permission baru ditambahkan).
- Hardening seluruh fetch detail/payload pendukung yang bukan loader dropdown dan tidak memengaruhi preservasi submit Party.

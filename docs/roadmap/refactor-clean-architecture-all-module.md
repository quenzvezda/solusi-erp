# Roadmap Refactor: Clean Architecture untuk Semua Modul

Dokumen ini adalah peta jalan refactor dari sistem yang awalnya banyak memakai pola anemic domain model menuju **Clean Architecture + DDD** dengan pendekatan **vertical slicing per feature**.

Tujuan roadmap ini adalah memberi arah yang jelas untuk seluruh modul yang masih saling bergantung, supaya refactor bisa dilanjutkan bertahap tanpa kehilangan stabilitas build, test, dan perilaku aplikasi.

---

## 1. Latar Belakang Masalah

Saat ini proyek sudah mulai dipindahkan ke arsitektur clean arch, tetapi perubahannya belum selesai penuh.

Masalah utamanya:

* Banyak fitur masih memakai struktur lama yang bercampur antara `model`, `dto`, `mapper`, `service`, dan `repository` secara horizontal.
* Beberapa modul masih saling bergantung langsung pada entity modul lain.
* Sebagian logic lookup dan presentation masih tersebar, bukan lewat use case/port yang jelas.
* Ada package legacy yang masih tertinggal walaupun sebagian besar alur sudah dipindah ke fitur masing-masing.
* Di sisi UI, beberapa halaman form/list sebelumnya sempat error karena kontrak data belum seragam setelah refactor.

Secara garis besar, proyek ini sedang bergerak dari:

* **anemic CRUD layer**

menjadi:

* **feature-based vertical slicing**
* **domain/application/infrastructure boundary yang jelas**
* **shared package hanya untuk kontrak yang benar-benar dipakai bersama**

---

## 2. Tujuan Akhir

Target akhirnya adalah:

* setiap fitur punya slice sendiri dari domain sampai web layer;
* dependensi lintas modul diputus atau dipindahkan ke port/use case/reference id;
* shared package hanya berisi hal yang benar-benar lintas fitur, seperti value object atau enum yang memang dipakai bersama;
* tidak ada lagi coupling langsung antar entity dari modul berbeda;
* lookup untuk autocomplete, dropdown, dan edit form memakai kontrak yang konsisten;
* test coverage tidak hanya unit test, tetapi juga integration/smoke test untuk form dan list view;
* modul `master`, `inventory`, `core`, dan `security` sama-sama mengikuti pola yang seragam.

---

## 3. Status Saat Ini

### [x] Yang sudah dikerjakan

* [x] Memindahkan banyak logika feature ke struktur clean architecture.
* [x] Memisahkan repository boundary ke package feature-local pada beberapa slice master.
* [x] Menyatukan enum bersama ke shared package saat memang dipakai lintas fitur.
* [x] Menghapus beberapa package legacy yang sudah tidak terpakai.
* [x] Memperbaiki issue runtime pada halaman master yang sebelumnya error di form/list.
* [x] Menyelesaikan sorting dan autocomplete pada beberapa modul yang sempat rusak setelah refactor.
* [x] Memutus coupling langsung `User -> Party` dengan reference id dan lookup port.
* [x] Memutus coupling langsung `Address -> Geographic` dengan reference id.
* [x] Memutus coupling entity currency pada inventory monetary embeddable dan menggantinya dengan reference id.
* [x] Menjaga `mvn clean test` tetap hijau setelah setiap fase perubahan besar.

### [x] Yang sudah selesai (update akhir)

* [x] Menghabiskan sisa legacy package pada modul `master` sampai vertical slicing benar-benar bersih.
* [x] Menuntaskan pemisahan `master.model` lama — diterima sebagai transitional JPA layer intra-master; semua consumer lintas modul sudah memakai reference id.
* [x] Menyelaraskan pola lookup/response untuk autocomplete agar contract data sama di form pre-add dan form pre-edit.
* [x] Menambahkan integration test yang konsisten untuk list view, form view, sorting, dan autocomplete di tiap fitur penting.
* [x] Menyapu sisa coupling lintas modul yang masih tersisa di `security`, `core`, dan `inventory`.
* [x] Menstandarkan package shared agar hanya berisi contract yang memang universal.

---

## 4. Roadmap Bertahap

### Phase 1 - Stabilization dan Boundary Cleanup

Fokus phase ini adalah memastikan boundary antar modul sudah aman dan tidak ada dependency liar.

* [x] Mengidentifikasi package legacy yang masih tertinggal.
* [x] Memindahkan repository boundary ke feature slice masing-masing.
* [x] Memindahkan shared enum dan shared contract yang benar-benar lintas fitur.
* [x] Menjaga build tetap hijau setelah perubahan besar.
* [x] Audit ulang semua import lintas modul yang masih langsung mengarah ke entity modul lain.
* [x] Pastikan tidak ada package horizontal lama yang masih dipakai kecuali memang transitional.

### Phase 2 - Master Module Full Vertical Slicing

Fokus phase ini adalah merapikan modul `master` supaya pola clean arch-nya konsisten end-to-end.

* [x] Memindahkan slice tertentu ke package fitur masing-masing.
* [x] Memisahkan beberapa lookup use case untuk kebutuhan form dan autocomplete.
* [x] Hapus atau migrasikan sisa package lama yang masih ada di root `master` (controller, service, dto, form, mapper dihapus; master.model tetap sebagai JPA persistence layer intra-master).
* [x] Pastikan setiap fitur master punya struktur yang seragam:
  * domain
  * application/usecase
  * infrastructure/persistence
  * web/controller
  * web/mapper
  * web/template
* [x] Pastikan form edit dan form create memakai sumber lookup yang sama.
* [x] Pastikan list view, sorting, dan pagination memakai query/use case yang sama.
* [x] Tambahkan integration test untuk form dan list pada tiap fitur master.

### Phase 3 - Standardisasi Autocomplete dan Lookup Contract

Fokus phase ini adalah membuat autocomplete konsisten lintas modul.

* [x] Definisikan kontrak lookup standar untuk autocomplete (`LookupDto` di core.dto).
* [x] Pastikan data untuk autocomplete dan data yang dipakai form pre-edit berasal dari sumber yang sama.
* [x] Jadikan `id`, `name`, dan `subText` sebagai kontrak utama jika dibutuhkan UI.
* [x] Hindari mapping ganda yang membuat label di list berbeda dengan label di form.
* [x] Buat smoke test Playwright untuk halaman lookup penting.
* [x] Dokumentasikan pola path lookup baru agar frontend tetap stabil walau backend berubah.

### Phase 4 - Inventory Decoupling Lanjutan

Fokus phase ini adalah membersihkan sisa coupling yang masih tersisa di inventory.

* [x] Memindahkan beberapa alur stock adjustment ke port/use case yang lebih jelas.
* [x] Memutus referensi entity currency langsung dari embeddable inventory.
* [x] Audit sisa entity inventory yang masih bergantung ke model lintas modul.
* [x] Rapikan mapper agar tidak menyimpan concern lookup yang tidak semestinya.
* [x] Putus coupling `inventory.model.Facility` → `master.model.Party` (ownerId Long sebagai pengganti @ManyToOne).
* [x] Pastikan stock adjustment, stock card, dan valuation tetap konsisten setelah refactor.
* [x] Tambahkan integration test pada flow form penting inventory.

### Phase 5 - Security Module Decoupling

Fokus phase ini adalah membuat modul security lebih independen dari master.

* [x] Memutus direct relation `User -> Party`.
* [x] Audit sisa referensi security yang masih bergantung ke entity master.
* [x] Ganti import `master.party.domain.model.Party` di UserServiceImpl dengan `PartyReference` DTO via `GetPartyReferenceUseCase`.
* [x] Pastikan mapping profile dan user lookup tetap berjalan tanpa coupling entity.
* [x] Tambahkan test untuk skenario user lookup dan edit profile.

### Phase 6 - Core Module Simplification

Fokus phase ini adalah menjaga `core` hanya berisi primitive reusable contract dan value object.

* [x] Memutus direct relation `Address -> Geographic`.
* [x] Tinjau kembali value object core — tidak ada import dari master/inventory di core.
* [x] Pastikan `core` hanya menyimpan kontrak universal.
* [x] Hindari referensi langsung dari core ke slice fitur tertentu.

### Phase 7 - Hardening dan Regression Coverage

Fokus phase ini adalah mencegah regresi setelah refactor besar selesai.

* [x] Tambahkan test coverage untuk form create/edit di fitur yang masih belum punya integration test.
* [x] Tambahkan smoke test untuk halaman list yang paling kritikal.
* [x] Tambahkan smoke test untuk autocomplete yang paling sering dipakai.
* [x] Pastikan sorting, pagination, dan lookup tetap stabil setelah setiap cleanup.
* [x] Jalankan `mvn clean test` sebagai gate utama sebelum menutup fase.

---

## 5. Prinsip Implementasi

Selama roadmap ini berjalan, prinsip yang harus dijaga:

* jangan memindahkan coupling lama ke tempat baru secara diam-diam;
* jangan membuat shared package terlalu gemuk;
* jangan menambah layer yang tidak perlu;
* kalau data dipakai bersama, buat kontraknya jelas;
* kalau hanya dipakai satu fitur, tetap tinggal di slice fitur tersebut;
* kalau ada perubahan besar di form/list/autocomplete, sertakan integration test atau smoke test;
* jaga supaya build tetap hijau di setiap milestone.

---

## 6. Definisi Selesai

Roadmap ini dianggap selesai kalau:

* seluruh modul utama sudah mengikuti vertical slicing;
* root package legacy sudah bersih atau hanya tersisa untuk compatibility yang benar-benar terpaksa;
* lookup/autocomplete/form/list memakai contract yang seragam;
* coupling antar modul sudah turun drastis dan sebagian besar sudah lewat port/reference id;
* test suite unit + integration + smoke test sudah cukup untuk menangkap regresi paling umum;
* codebase mudah dilanjutkan oleh AI agent maupun developer tanpa perlu menebak-nebak boundary fitur.

---

## 7. Catatan Akhir

Roadmap ini sengaja dibuat bertahap supaya refactor besar tidak dilakukan sekaligus.
Pendekatan terbaik adalah menyelesaikan satu boundary atau satu feature slice penuh, lalu mengunci perilaku lewat test sebelum pindah ke fase berikutnya.

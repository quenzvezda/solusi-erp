# Internationalization (i18n) Naming Convention Guide

Dokumen ini mendefinisikan standar penamaan kunci pesan (message keys) untuk memastikan konsistensi di seluruh aplikasi ERP.

## 1. File Structure
*   `src/main/resources/messages.properties`: Default (English).
*   `src/main/resources/messages_id.properties`: Bahasa Indonesia.

## 2. Naming Categories

### 2.1. Labels (`label.*`)
Digunakan untuk teks statis yang berfungsi sebagai judul, label form, atau nama kolom tabel.
*   **Format:** `label.[feature].[element]`
*   **Contoh:** 
    *   `label.dashboard=Dashboard`
    *   `label.user.username=Username`
    *   `label.common.actions=Actions`

### 2.2. Messages (`msg.*`)
Digunakan untuk pesan umpan balik (feedback) kepada pengguna.
*   **Format:** `msg.[type].[action]`
*   **Contoh:**
    *   `msg.success.create=Data added successfully.`
    *   `msg.error.notfound=Data not found.`
    *   `msg.confirm.delete=Are you sure you want to delete this?`

### 2.3. Placeholders (`placeholder.*`)
Digunakan khusus untuk teks bantuan di dalam input form.
*   **Format:** `placeholder.[feature].[element]`
*   **Contoh:**
    *   `placeholder.login.username=Enter your username`
    *   `placeholder.common.search=Search here...`

### 2.4. Page Specific (`[page].*`)
Digunakan untuk teks yang sangat spesifik pada satu halaman dan tidak akan digunakan di tempat lain (seperti judul Hero, subtitle unik).
*   **Format:** `[page-name].[element]`
*   **Contoh:**
    *   `landing.hero.title=ERP Program Solutions`
    *   `profile.subtitle=Your account information and system preferences.`

### 2.5. Validations (`validation.*`)
Digunakan untuk pesan error pada form (Jakarta Validation). Gunakan pola **Suffix** untuk mempermudah penggabungan dengan label field.
*   **Format Suffix:** `validation.[constraint].suffix`
*   **Contoh Keys:**
    *   `validation.notblank.suffix=tidak boleh kosong`
    *   `validation.notnull.suffix=harus dipilih`
    *   `validation.size.suffix=panjang harus antara {min} dan {max} karakter`
    *   `validation.email.suffix=format email tidak valid`

---

## 3. Implementation in Java (DTO)
Untuk pesan validasi yang dinamis dan ter-lokalisasi, gunakan interpolasi pesan di anotasi DTO:

```java
@NotBlank(message = "{label.user.username} {validation.notblank.suffix}")
private String username;

@Size(max = 100, message = "{label.product.name} {validation.size.suffix}")
private String name;
```

## 4. Implementation in Thymeleaf
Selalu gunakan operator `#{...}`.
*   **Text:** `<span th:text="#{label.dashboard}">Dashboard</span>`
*   **Placeholder:** `<input th:placeholder="#{placeholder.login.username}" ...>`
*   **With Arguments:** `<h2 th:text="#{msg.welcome(${username})}">Welcome</h2>`

## 5. Implementation in Controller
Gunakan `MessageSource` jika perlu mengirim pesan dari backend.
```java
String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
redirectAttributes.addFlashAttribute("successMessage", message);
```

## 6. Configuration Architecture (For AI & Developers)
**PENTING:** Arsitektur i18n pada project ini sudah final dan terbagi menjadi dua bagian utama:
1.  **`I18nConfig.java`**: Bertanggung jawab penuh atas manajemen `LocaleResolver` (menggunakan `CookieLocaleResolver`) dan `LocaleChangeInterceptor`. **DILARANG** menambahkan bean `localeResolver` di tempat/file config lain.
2.  **`WebMvcConfig.java`**: Bertugas me-register interceptor dari *I18nConfig* dan mendefinisikan `LocalValidatorFactoryBean` agar anomali/pesan error dari `@Valid` DTO tersinkronisasi murni dengan file `messages_id.properties`.

## 7. AI Guidelines for Updating i18n Files (CRITICAL)
Untuk AI Assistant, ikuti protokol berikut saat menambah atau memperbarui file `.properties`:

1. **DILARANG KERAS menggunakan `echo` atau `run_shell_command("echo ... >> ...")`**: Teknik ini seringkali gagal menangani karakter Unicode/encoding dengan benar pada file `.properties` dan merusak format file.
2. **Gunakan `replace` tool**: Selalu gunakan tool `replace` dengan strategi pencarian yang spesifik:
   - **Cari Section Header**: Cari baris yang dimulai dengan `#` (contoh: `# Profile`, `# Stock Adjustment`) untuk menyisipkan entry baru di bawah pengelompokan tersebut.
   - **Cari Prefix**: Jika header tidak ditemukan, cari key dengan prefix yang sama (contoh: `profile.*`) untuk menemukan lokasi baris yang relevan.
   - **Append di Akhir**: Jika membuat fitur/modul baru yang belum ada pengelompokannya, tambahkan section header baru di baris paling bawah file.
3. **Validasi Encoding**: Pastikan hasil akhir tetap menggunakan format `.properties` yang valid tanpa merusak karakter non-ASCII.

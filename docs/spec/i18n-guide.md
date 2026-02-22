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

## 3. Implementation in Thymeleaf
Selalu gunakan operator `#{...}`.
*   **Text:** `<span th:text="#{label.dashboard}">Dashboard</span>`
*   **Placeholder:** `<input th:placeholder="#{placeholder.login.username}" ...>`
*   **With Arguments:** `<h2 th:text="#{msg.welcome(${username})}">Welcome</h2>`

## 4. Implementation in Controller
Gunakan `MessageSource` jika perlu mengirim pesan dari backend.
```java
String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
redirectAttributes.addFlashAttribute("successMessage", message);
```

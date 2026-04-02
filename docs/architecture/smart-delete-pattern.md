# Smart Delete Pattern

> **Kapan dipakai:** Ketika entity bisa dihapus dari sistem, tapi juga bisa direferensikan
> oleh entity lain. Jika masih direferensikan → nonaktifkan (soft-delete), jika tidak → hapus permanen (hard-delete).
>
> **Prinsip:** Sistem yang memutuskan, bukan user. User cukup klik "Hapus" — sistem cek otomatis.
>
> **Referensi implementasi:** `master/tax`, `master/currency`, `master/partyroletype`,
> `master/geographic`, `master/party`, `master/bankaccount`

---

## Kapan Menggunakan Pattern Ini

✅ **Gunakan Smart Delete jika:**
- Entity punya field `isActive` (soft-delete capable)
- Entity berpotensi direferensikan oleh entity lain via FK
- User tidak boleh menghapus data yang sedang digunakan

❌ **Jangan gunakan jika:**
- Entity tidak punya `isActive` — gunakan "Reject Delete" pattern (throw error jika inUse, seperti di `inventory/brand`)
- Entity tidak mungkin direferensikan oleh entity lain
- Hard-delete selalu aman (misal: draft dokumen)

---

## Anatomi Pattern

### 1. Domain Layer (Pure Java)

#### `DeleteResult` enum
Lokasi: `core/domain/model/DeleteResult.java`

```java
public enum DeleteResult {
    HARD_DELETED,   // Entity tidak dipakai, dihapus permanen
    SOFT_DELETED    // Entity masih dipakai, dinonaktifkan (isActive=false)
}
```

#### InUse Port Interface
Lokasi: `<feature>/domain/port/<Feature>InUseChecker.java`

```java
public interface TaxInUseChecker {
    boolean isInUse(Long taxId);
}
```

**Aturan:**
- Pure Java interface, tidak ada import framework
- Satu method: `boolean isInUse(Long id)`
- Nama class: `<Feature>InUseChecker`

#### Domain Model — `softDelete()` method
Setiap domain model yang support Smart Delete harus punya:
```java
public void softDelete() {
    this.isActive = false;
}
```

**Catatan khusus untuk domain model dengan field `final`** (seperti `Party`):
Jika field `isActive` adalah `final`, tambahkan `softDelete(Long id)` di domain repository interface dan
implementasikan di adapter layer (langsung set pada JPA entity).

### 2. Application Layer

#### Delete Use Case Interface
Lokasi: `<feature>/application/usecase/command/Delete<Feature>UseCase.java`

```java
@FunctionalInterface
public interface DeleteTaxUseCase {
    DeleteResult execute(Long id);  // ← Return DeleteResult, bukan void
}
```

#### Delete Use Case Implementation
Lokasi: `<feature>/application/usecase/command/Delete<Feature>UseCaseImpl.java`

```java
public class DeleteTaxUseCaseImpl implements DeleteTaxUseCase {

    private final TaxRepository repository;
    private final TaxInUseChecker inUseChecker;

    public DeleteTaxUseCaseImpl(TaxRepository repository, TaxInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        Tax tax = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.tax.notfound"));

        if (inUseChecker.isInUse(id)) {
            tax.softDelete();
            repository.save(tax);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
```

**Flow:**
1. Load entity — throw jika tidak ditemukan
2. Cek `inUseChecker.isInUse(id)`
3. Jika `true` → `softDelete()` + `save()` → return `SOFT_DELETED`
4. Jika `false` → `delete()` → return `HARD_DELETED`

### 3. Infrastructure Layer

#### InUse Checker Implementation
Lokasi: `<feature>/infrastructure/adapter/<Feature>InUseCheckerImpl.java`

```java
public class GeographicInUseCheckerImpl implements GeographicInUseChecker {

    private final GeographicJpaRepository geographicJpaRepository;
    private final BankAccountJpaRepository bankAccountJpaRepository;

    // Constructor injection

    @Override
    public boolean isInUse(Long geographicId) {
        if (geographicJpaRepository.existsByParentId(geographicId)) return true;
        return bankAccountJpaRepository.existsByCityId(geographicId);
    }
}
```

**Aturan:**
- Inject JPA repositories dari module yang mereferensikan entity ini
- Gunakan `existsBy*()` derived queries (Spring Data) — efisien, hanya `SELECT 1`
- Return `true` jika SALAH SATU referensi ditemukan
- Untuk composite checks (banyak checker), gunakan pola `*InUseCheckerComposite` (lihat `inventory/uom`)

**Placeholder pattern** (jika belum ada consumer):
```java
public class BankAccountInUseCheckerImpl implements BankAccountInUseChecker {
    @Override
    public boolean isInUse(Long bankAccountId) {
        return false;  // No known consumers yet
    }
}
```

#### Composition Root (Config)
Lokasi: `<feature>/infrastructure/config/<Feature>Config.java`

```java
@Bean
public TaxInUseChecker taxInUseChecker() {
    return new TaxInUseCheckerImpl();
}

@Bean
public DeleteTaxUseCase deleteTaxUseCase(
        TaxRepository taxDomainRepository,
        TaxInUseChecker taxInUseChecker,
        PlatformTransactionManager txManager) {
    DeleteTaxUseCase pure = new DeleteTaxUseCaseImpl(taxDomainRepository, taxInUseChecker);
    TransactionTemplate tx = new TransactionTemplate(txManager);
    return (id) -> tx.execute(status -> pure.execute(id));
}
```

**Penting:** Gunakan `tx.execute()` (bukan `tx.executeWithoutResult()`) karena use case sekarang return `DeleteResult`.

### 4. Web Layer (Controller)

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('TAX_DELETE')")
@ResponseBody
public ResponseEntity<Void> delete(@PathVariable Long id) {
    DeleteResult result = deleteTaxUseCase.execute(id);
    if (result == DeleteResult.SOFT_DELETED) {
        String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
    }
    String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
    return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
}
```

**Dua response path:**
- `HARD_DELETED` → toast hijau via `erp:show-success` event
- `SOFT_DELETED` → toast kuning/amber via `erp:show-warning` event

### 5. Frontend (JavaScript)

Listener di `erp-form-handler.js` sudah menangkap `erp:show-warning` event:
```javascript
document.body.addEventListener('erp:show-warning', function(evt) {
    const message = evt.detail.value || evt.detail.message || "Action completed with warning";
    ErpFormHandler.showWarning(message);
});
```

Tidak perlu perubahan di template — ini sudah global.

### 6. i18n Messages

Tambahkan di `messages.properties` dan `messages_id.properties`:

```properties
# Generic (sudah ada, shared oleh semua fitur)
msg.success.delete=Data deleted successfully.
msg.success.deactivated=Data has been deactivated because it is still referenced by other records.
```

---

## Checklist Implementasi

Gunakan checklist ini saat menambahkan Smart Delete ke fitur baru:

- [ ] Domain model punya field `Boolean isActive` dan method `softDelete()`
- [ ] Buat `domain/port/<Feature>InUseChecker.java` (pure Java interface)
- [ ] Buat `infrastructure/adapter/<Feature>InUseCheckerImpl.java`
- [ ] Edit `Delete<Feature>UseCase.java` — return type `DeleteResult`
- [ ] Edit `Delete<Feature>UseCaseImpl.java` — smart delete logic
- [ ] Edit `<Feature>Config.java` — wire checker bean + `tx.execute()` (bukan `executeWithoutResult`)
- [ ] Edit `<Feature>Controller.java` — branch on `DeleteResult`
- [ ] JPA repository: tambah `existsBy*()` derived query jika belum ada
- [ ] Pastikan `msg.success.deactivated` ada di i18n (sudah global, tidak perlu per-fitur)

---

## Perbandingan dengan "Reject Delete" Pattern

| Aspek | Smart Delete | Reject Delete |
|-------|-------------|---------------|
| **Contoh** | `master/tax`, `master/party` | `inventory/brand`, `inventory/uom` |
| **Jika inUse** | Soft-delete (isActive=false) | Throw `DomainException` → error modal |
| **Return type** | `DeleteResult` | `void` |
| **UX** | Toast kuning "Data dinonaktifkan" | Modal error merah "Tidak bisa dihapus" |
| **Kapan pakai** | Entity punya `isActive`, boleh dinonaktifkan | Entity HARUS ada atau HARUS dihapus, tidak ada middle ground |
| **Controller** | Branch `SOFT_DELETED` / `HARD_DELETED` | Tidak perlu branch (exception ditangkap GlobalExceptionHandler) |

---

## Diagram Alur

```
User klik "Hapus"
    │
    ▼
Bootstrap Modal Konfirmasi
    │
    ▼ (user konfirmasi)
    │
DELETE /resource/{id}  ← HTMX request
    │
    ▼
Controller.delete(id)
    │
    ▼
DeleteUseCase.execute(id)
    ├── findById() → tidak ada? → throw DomainException → Error Modal (merah)
    │
    ├── inUseChecker.isInUse(id)
    │       │
    │       ├── TRUE  → softDelete() + save() → return SOFT_DELETED
    │       │                                        │
    │       │                                        ▼
    │       │                              Controller: okWithRefreshTableAndWarning()
    │       │                                        │
    │       │                                        ▼
    │       │                              Toast KUNING: "Data telah dinonaktifkan"
    │       │
    │       └── FALSE → delete() → return HARD_DELETED
    │                                    │
    │                                    ▼
    │                          Controller: okWithRefreshTableAndSuccess()
    │                                    │
    │                                    ▼
    │                          Toast HIJAU: "Data berhasil dihapus"
```

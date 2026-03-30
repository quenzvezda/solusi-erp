# Modul: Party Role Type (Master)

## Ringkasan

Modul `master.partyroletype` mengelola master data **jenis peran** yang dapat dimiliki oleh sebuah Party (entitas bisnis). Satu Party bisa memiliki banyak peran (misal: sebuah perusahaan bisa menjadi SUPPLIER sekaligus CUSTOMER).

**Package Path**: `com.solusi.erp.master.partyroletype`

---

## 1. Struktur Clean Architecture

```
master.partyroletype
├── domain
│   ├── model
│   │   └── PartyRoleType.java
│   └── repository
│       └── PartyRoleTypeRepository.java
├── application
│   └── usecase
│       ├── command
│       │   ├── CreatePartyRoleTypeUseCase.java
│       │   └── UpdatePartyRoleTypeUseCase.java
│       └── query
│           ├── GetPartyRoleTypeUseCase.java
│           └── GetPartyRoleTypeListUseCase.java
├── infrastructure
│   ├── persistence
│   │   ├── PartyRoleTypeEntity.java
│   │   ├── PartyRoleTypeJpaRepository.java
│   │   └── PartyRoleTypePersistenceMapper.java
│   ├── adapter
│   │   └── PartyRoleTypeRepositoryAdapter.java
│   └── config
│       └── PartyRoleTypeConfig.java
└── web
    ├── controller
    │   └── PartyRoleTypeController.java
    ├── dto
    │   └── ...
    └── mapper
        └── PartyRoleTypeWebMapper.java
```

---

## 2. Domain Model (`PartyRoleType.java`)

```java
public class PartyRoleType {
    private AuditMetadata metadata;
    private String code;     // Kode unik, umumnya huruf kapital (e.g. SUPPLIER)
    private String name;     // Nama tampilan (e.g. Supplier)
    private String note;     // Keterangan opsional
    private boolean isActive;

    public static PartyRoleType createNew(String code, String name, String note) { ... }
    public void update(String name, String note) { ... }
    public void softDelete() { this.isActive = false; }
}
```

---

## 3. Data Seed Default

Data berikut di-seed otomatis oleh `DataInitializer` saat aplikasi pertama kali jalan:

| Kode | Nama | Keterangan |
|------|------|-----------|
| `INTERNAL` | Internal | Entitas internal perusahaan |
| `CUSTOMER` | Customer | Pelanggan |
| `SUPPLIER` | Supplier | Pemasok |
| `EMPLOYEE` | Employee | Karyawan |
| `COURIER` | Courier | Jasa pengiriman |

---

## 4. Permissions

| Kode Permission | Deskripsi |
|-----------------|-----------|
| `PARTY_ROLE_TYPE_VIEW` | Melihat daftar & detail |
| `PARTY_ROLE_TYPE_CREATE` | Membuat tipe baru |
| `PARTY_ROLE_TYPE_EDIT` | Mengedit tipe yang sudah ada |

---

## 5. Business Rules

1. **Kode unik**: `code` tidak boleh duplikat. Divalidasi di use case sebelum persist.
2. **Kode tidak bisa diubah setelah dibuat**: Karena `code` digunakan sebagai referensi di tabel `party_roles`, field ini bersifat immutable.
3. **Soft delete via `isActive`**: Tipe yang tidak aktif tidak muncul di dropdown pemilihan peran Party. Data tidak dihapus dari database.
4. **Data seed dilindungi**: Kode bawaan (`INTERNAL`, `CUSTOMER`, dll.) tidak boleh dihapus atau di-deactivate melalui UI standar.

---

## 6. Keterkaitan dengan Modul Party

`PartyRoleType` adalah tabel master yang direferensikan oleh tabel `party_roles`:

```
parties (1) ──< party_roles (n) >── party_role_types (1)
```

Satu Party bisa memiliki lebih dari satu peran, masing-masing mengacu pada `PartyRoleType` yang berbeda. Ini memungkinkan satu vendor sekaligus menjadi customer tanpa duplikasi data Party.

---

## 7. Endpoint API

| Method | Endpoint | Permission | Deskripsi |
|--------|----------|-----------|-----------|
| GET | `/master/party-role-types` | `PARTY_ROLE_TYPE_VIEW` | List (paginasi) |
| GET | `/master/party-role-types/{id}` | `PARTY_ROLE_TYPE_VIEW` | Detail |
| POST | `/master/party-role-types` | `PARTY_ROLE_TYPE_CREATE` | Buat baru |
| PUT | `/master/party-role-types/{id}` | `PARTY_ROLE_TYPE_EDIT` | Update |

---

## 8. Lihat Juga

- [`docs/modules/master/party.md`](./party.md) — Modul Party (referensi utama)
- [`docs/database/master-erd.md`](../../database/master-erd.md) — ERD tabel master

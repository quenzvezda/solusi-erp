# Autocomplete Generic Implementation Guide

Dokumen ini menjelaskan spesifikasi dan panduan untuk mengimplementasikan komponen Autocomplete yang _generic_ dan _hierarchical_, yang telah terstandarisasi melalui pengerjaan modul Stock Adjustment.

Implementasi ini menggunakan library frontend **TomSelect** yang dihubungkan dengan **Spring Boot REST API** di backend, mendukung debouncing, cascading (filtering bertingkat), dan auto-filling.

---

## 1. Komponen Backend

### A. Data Transfer Object (`LookupDto` vs `InventoryLookupDto`)

1. **`LookupDto` (Core)**: Digunakan untuk pencarian sederhana satu tingkat (misal: Brand, Currency).
   ```java
   public record LookupDto(Long id, String name, String subText) {}
   ```

2. **`InventoryLookupDto` (Module Specific)**: Digunakan untuk entitas hirarkis (Container -> Grid -> Facility). Menambahkan context parent agar UI bisa melakukan auto-filling field terkait tanpa request tambahan.
   ```java
   public class InventoryLookupDto {
       private Long id;
       private String name;
       private String subText;
       private Long parentId;   // misal: gridId
       private String parentName; // misal: gridName
   }
   ```

### B. Controller (Standard Endpoint)

Setiap entitas yang mendukung autocomplete harus memiliki dua jenis endpoint:
1.  **Search Endpoint**: `/api/lookup/[entities]?q=[keyword]&limit=10`
2.  **Detail Endpoint**: `/api/lookup/[entities]/{id}` (PENTING untuk mekanisme cascading yang stabil).

```java
@GetMapping("/containers/{id}")
public InventoryLookupDto getLookupContainer(@PathVariable Long id) {
    return containerService.getLookupContainer(id);
}
```

### C. Security Prefix
Gunakan prefix **`LOOKUP_`** pada permission (contoh: `LOOKUP_INVENTORY`) agar izin pencarian terkelompok rapi di UI Role Management.

---

## 2. Komponen Frontend (UI)

### A. Inisialisasi Standard

```javascript
function initLookup(el, type, parentProvider = null) {
    return new TomSelect(el, {
        valueField: 'id',
        labelField: 'name',
        searchField: ['name'],
        placeholder: '-- Select --',
        preload: 'focus', // Load data saat fokus/klik tanpa ketik
        load: debounce(function(q, callback) {
            let url = `/api/lookup/${type}?q=${encodeURIComponent(q)}&limit=10`;
            if (parentProvider) {
                const parent = parentProvider();
                if (parent.id) url += `&${parent.key}=${parent.id}`;
            }
            fetch(url).then(r => r.json()).then(callback);
        }, 150),
        render: {
            option: (data, escape) => {
                if (!data.id) return ''; // Sembunyikan empty anchor
                return `<div class="py-1"><div>${escape(data.name)}</div><small class="text-muted">${escape(data.subText || '')}</small></div>`;
            }
        }
    });
}
```

### B. Pola Cascading Hirarkis (Parent -> Child)

Untuk memastikan data yang muncul di dropdown anak selalu relevan dengan pilihan di induk, gunakan event `dropdown_open` untuk membersihkan cache pilihan lama.

```javascript
// Contoh: Inisialisasi Grid yang difilter oleh Facility
const tsGrid = initLookup(elGrid, 'grids', () => ({ key: 'facilityId', id: headerFacility.value }));

tsGrid.on('dropdown_open', () => {
    tsGrid.clearOptions(); // Paksa fetch ulang setiap kali dibuka
});
```

### C. Pola Auto-filling (Child -> Parent)

Gunakan **Fetch API** pada event `change` untuk mengambil metadata lengkap dari Detail Endpoint. Ini lebih stabil daripada mengandalkan cache `options` di TomSelect. Gunakan **Flag Guard** untuk mencegah *Circular Reset* (induk berubah -> anak ke-reset -> loop).

```javascript
let isAutoSetting = false;

tsChild.on('change', function(val) {
    if (val && !isAutoSetting) {
        fetch(`/api/lookup/children/${val}`)
            .then(r => r.json())
            .then(data => {
                if (data.parentId) {
                    isAutoSetting = true; 
                    tsParent.addOption({id: data.parentId, name: data.parentName});
                    tsParent.setValue(data.parentId);
                    setTimeout(() => { isAutoSetting = false; }, 100);
                }
            });
    }
});
```

### D. CSS Standar untuk Tabel
Agar tabel dengan banyak kolom autocomplete tetap rapi dan tidak memiliki scrollbar:
1.  Gunakan `table-layout: fixed; width: 100%;`.
2.  Gunakan `.ts-control { width: 100% !important; }`.
3.  Persempit padding sel tabel (misal: `padding: 0.4rem 0.2rem`).

---

## Ringkasan Ketentuan
1.  **Selalu** gunakan debouncing minimal 150ms.
2.  **Selalu** sediakan Detail Endpoint untuk setiap Lookup Entity.
3.  **Gunakan Flag Guard** saat melakukan set nilai antar field dependen.
4.  **Sembunyikan Opsi Kosong** (`!data.id`) agar tidak merusak visual dropdown.

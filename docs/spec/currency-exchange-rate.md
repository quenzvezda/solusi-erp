# Currency Exchange Rate Auto-Lock

## Overview

Pola reusable untuk mengunci exchange rate ke 1 (readonly) saat default/base currency dipilih, dan membuka kembali saat non-default currency dipilih.

## Kapan Digunakan

Setiap form yang memiliki:
- Field currency (autocomplete TomSelect)
- Field exchange rate (AutoNumeric decimal input)

Contoh: Vendor Payment, Purchase Order, Vendor Bill.

## Prasyarat Backend

### 1. Currency Lookup Payload harus mengandung `isDefault`

`CurrencyLookupProviderImpl` dan `CurrencyLookupController` harus menyertakan:

```java
payload.put("isDefault", Boolean.TRUE.equals(c.getIsDefault()));
```

Ini membuat TomSelect item data memiliki `payload.isDefault` yang bisa dibaca oleh JS.

### 2. Controller Preadd (opsional tapi recommended)

Saat form create, pre-populate default currency agar user tidak perlu pilih manual:

```java
currencyJpaRepository.findByIsDefaultTrue().stream().findFirst().ifPresent(currency -> {
    request.setCurrencyId(currency.getId());
    request.setExchangeRate(BigDecimal.ONE);
    // Build vpUI map with currencyText + currencySubtext
});
```

## Setup Frontend

### 1. Include Script

Tambahkan di `page-specific-scripts` **sebelum** form JS:

```html
<script th:src="@{/js/shared/erp-currency-rate-lock.js}" defer></script>
```

### 2. Panggil Init di Page JS

```javascript
if (window.ERP && ERP.CurrencyRateLock) {
    ERP.CurrencyRateLock.init({
        currencySelectId: "vp-currency",       // ID elemen select currency
        rateInputSelector: '[name="exchangeRate"]'  // Selector input exchange rate
    });
}
```

### 3. Options API

| Option | Type | Required | Default | Description |
|--------|------|----------|---------|-------------|
| `currencySelectId` | string | Yes | — | ID elemen `<select>` currency (TomSelect) |
| `rateInputSelector` | string | Yes | — | CSS selector untuk input exchange rate |
| `defaultRate` | string | No | `"1"` | Nilai rate saat di-lock |

## Behavior

1. **Page load**: Jika currency sudah terpilih dan `payload.isDefault === true`, rate langsung di-lock ke 1.
2. **Currency change ke default**: Rate di-set ke 1, input jadi readonly (termasuk AutoNumeric instance).
3. **Currency change ke non-default**: Input rate jadi editable, value tidak diubah (user isi manual).
4. **Currency di-clear**: Rate di-unlock.

## Cara Kerja Internal

- Utility menunggu TomSelect init via event `erp:lookup-initialized`
- Membaca `ts.options[value].payload.isDefault` dari item data TomSelect
- Untuk initial rendered option, fallback membaca `data-payload-is-default="true"` dari DOM option
- Menggunakan `ErpNumeric.set(input, value)` saat lock agar sinkron dengan AutoNumeric
- Toggle native `readonly` property dan class `bg-body-tertiary` untuk tampilan readonly

## Gotcha: Initial Rendered TomSelect Payload

Payload dari hasil AJAX lookup masuk ke `ts.options[value].payload`, tetapi option yang sudah dirender server-side saat page load bisa tidak membawa payload lengkap di TomSelect. Jika behavior bergantung pada metadata initial option, render juga metadata itu sebagai `data-payload-*` di `<option>` dan pastikan JS punya fallback ke DOM attribute.

Contoh currency default:

```html
<option value="5" data-payload-is-default="true" selected>Indonesian Rupiah</option>
```

Tanpa fallback ini, field bisa sudah benar readonly dari Thymeleaf, lalu JS salah membaca `payload.isDefault` sebagai `undefined` dan meng-unlock field saat TomSelect selesai init.

## Adoption Guide

### Purchase Order

```javascript
// di purchase-order-form.js
ERP.CurrencyRateLock.init({
    currencySelectId: "header-currency",
    rateInputSelector: '[name="exchangeRate"]'
});
```

### Vendor Bill

```javascript
// di vendor-bills/form.js (jika currency editable)
ERP.CurrencyRateLock.init({
    currencySelectId: "vb-currency",
    rateInputSelector: '[name="exchangeRate"]'
});
```

## File Terkait

| File | Fungsi |
|------|--------|
| `static/js/shared/erp-currency-rate-lock.js` | Reusable utility |
| `CurrencyLookupProviderImpl.java` | Backend payload `isDefault` |
| `CurrencyLookupController.java` | Search endpoint payload `isDefault` |

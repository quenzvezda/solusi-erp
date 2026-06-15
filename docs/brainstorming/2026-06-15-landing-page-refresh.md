# Landing Page Refresh Brainstorm

## Executive Summary

Landing page Solusi ERP sebaiknya diposisikan sebagai hybrid portfolio dan ERP case study. Konten tidak hanya menonjolkan tech stack, tetapi juga memperlihatkan alur bisnis nyata yang sudah dibangun: master data, procurement, inventory, accounts payable, approval, dan accounting journal.

Rekomendasi utama: jadikan landing page sebagai "ERP portfolio case study" dengan screenshot produk sebagai bukti visual, lalu tempatkan tech stack sebagai engineering credibility di bagian bawah.

## Recommended Narrative

Solusi ERP adalah aplikasi ERP monolitik berbasis Spring Boot dan Thymeleaf yang dibangun untuk pembelajaran, portofolio, dan eksplorasi proses bisnis SME. Aplikasi ini mendemonstrasikan alur Procure-to-Pay dari purchase order sampai pencatatan jurnal, dengan RBAC, approval, audit trail, i18n, dan laporan inventory.

## Recommended Landing Structure

1. Hero
   - Headline: "Solusi ERP"
   - Subheadline: "ERP learning project and portfolio case study for procurement, inventory, accounts payable, and accounting workflows."
   - CTA: Login Demo / View Modules

2. Product Preview
   - Gunakan screenshot utama `purchase-order.png` atau komposisi 2-3 screenshot.
   - Tujuan: langsung menunjukkan bahwa aplikasi ini nyata dan bukan sekadar template.

3. Business Flow
   - Procure-to-Pay: Purchase Order -> Goods Receipt -> Stock Movement -> Vendor Bill -> Vendor Payment -> Journal Entry.
   - Gunakan cards atau horizontal timeline.

4. Screenshot Showcase
   - Prioritas utama:
     - `purchase-order.png`
     - `goods-receipt.png`
     - `on-hand-quantity.png`
     - `vendor-bill.png`
     - `vendor-payment.png`
     - `journal-entry.png`
   - Screenshot pendukung:
     - `manage-approval.png`
     - `chart-of-account.png`
     - `stock-card.png`
     - `party.png`
     - `product.png`
     - `goods-issue.png`

5. Feature Highlights
   - Procurement workflow
   - Inventory control
   - Accounts payable
   - Accounting journal
   - Approval and RBAC
   - Audit trail and multilingual UI

6. Engineering Highlights
   - Java 21, Spring Boot 4, Maven
   - Thymeleaf SSR, Bootstrap 5, Tabler
   - MariaDB, Flyway migration
   - Clean Architecture, DDD, CQRS
   - Spring Security RBAC
   - HTMX/AJAX hybrid forms

## Screenshot Selection Rationale

`purchase-order.png` is the strongest business entry point because it shows supplier, tax, totals, line items, and approval history.

`goods-receipt.png` connects procurement to inventory and shows serial numbers, billing status, tax amount, and journal integration.

`on-hand-quantity.png` is easy for non-technical visitors to understand because it shows real-time stock availability.

`vendor-bill.png` and `vendor-payment.png` prove the accounts payable flow and settlement concept.

`journal-entry.png` proves accounting integration through generated debit and credit lines.

`manage-approval.png` is useful as a trust feature, but better as supporting content rather than the first visual.

## Recommendation

Use a hybrid landing page with business-first storytelling and developer credibility second. Recruiters can understand the engineering stack, while business-oriented viewers can understand the ERP scope and workflow.

Next implementation step: redesign `src/main/resources/templates/home.html` and add i18n keys in `messages.properties` and `messages_id.properties` for the new content.

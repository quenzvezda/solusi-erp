-- Development seeder master taxes.

DELETE FROM taxes
WHERE code IN ('PPN11-EX', 'PPN11-IN', 'PPH23-OUT', 'NON-TAX');

INSERT INTO taxes (
    code,
    name,
    rate,
    calculation_mode,
    note,
    is_subtract,
    is_active,
    version,
    created_by_user_id,
    created_date,
    updated_by_user_id,
    updated_date
)
VALUES
    ('PPN11-EX', 'PPN 11% Exclusive', 11.0000, 'EXCLUSIVE', 'PPN keluaran/masukan standar untuk harga belum termasuk pajak.', 0, 1, 1, 1, NOW(), 1, NOW()),
    ('PPN11-IN', 'PPN 11% Inclusive', 11.0000, 'INCLUSIVE', 'PPN untuk skenario harga jual sudah termasuk pajak.', 0, 1, 1, 1, NOW(), 1, NOW()),
    ('PPH23-OUT', 'PPh 23 Vendor', 2.0000, 'EXCLUSIVE', 'Potongan PPh 23 untuk pembayaran vendor jasa.', 1, 1, 1, 1, NOW(), 1, NOW()),
    ('NON-TAX', 'Non Tax', 0.0000, 'EXCLUSIVE', 'Transaksi tanpa pajak untuk kebutuhan dev/testing.', 0, 1, 1, 1, NOW(), 1, NOW());

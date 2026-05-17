-- V15: Master Module - Complete Indonesia Geographic Data
-- 38 Provinsi + 514 Kabupaten/Kota (Data Resmi Kemendagri)
-- Mandate: AGENTS.md Section 4 & 5
-- Note: Menghapus data legacy V12 dan melengkapi seluruh data wilayah Indonesia

-- ============================================================
-- 0. Hapus data kota legacy dari V12 (format kode tidak konsisten)
-- ============================================================
-- Sever foreign key links in party_addresses first to avoid constraint errors
UPDATE party_addresses 
SET city_id = NULL 
WHERE city_id IN (
    SELECT id FROM geographics WHERE code IN ('ID-CITY-JKT', 'ID-CITY-BDO', 'ID-CITY-SUB')
);

DELETE FROM geographics WHERE code IN ('ID-CITY-JKT', 'ID-CITY-BDO', 'ID-CITY-SUB');

-- ============================================================
-- 1. Provinsi yang belum ada (V12 sudah memasukkan: ID-JK, ID-JB, ID-JT, ID-JI)
-- ============================================================
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-AC', 'Aceh', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SU', 'Sumatera Utara', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SB', 'Sumatera Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-RI', 'Riau', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KR', 'Kepulauan Riau', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-JA', 'Jambi', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SS', 'Sumatera Selatan', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-BB', 'Kepulauan Bangka Belitung', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-BE', 'Bengkulu', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-LA', 'Lampung', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-BT', 'Banten', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-YO', 'DI Yogyakarta', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-BA', 'Bali', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-NB', 'Nusa Tenggara Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-NT', 'Nusa Tenggara Timur', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KB', 'Kalimantan Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KT', 'Kalimantan Tengah', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KS', 'Kalimantan Selatan', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KI', 'Kalimantan Timur', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-KU', 'Kalimantan Utara', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SA', 'Sulawesi Utara', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-GO', 'Gorontalo', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-ST', 'Sulawesi Tengah', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SR', 'Sulawesi Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SN', 'Sulawesi Selatan', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-SG', 'Sulawesi Tenggara', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-MA', 'Maluku', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-MU', 'Maluku Utara', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PA', 'Papua', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PB', 'Papua Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PS', 'Papua Selatan', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PT', 'Papua Tengah', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PP', 'Papua Pegunungan', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date)
SELECT 'ID-PD', 'Papua Barat Daya', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';

-- ============================================================
-- 2. Kota & Kabupaten per Provinsi (514 total, data resmi Kemendagri)
-- ============================================================

-- ----------------------------------------------------------
-- ACEH — 5 Kota, 18 Kabupaten (23)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-AC');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-AC-BandaAceh',      'Kota Banda Aceh',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Sabang',         'Kota Sabang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Lhokseumawe',    'Kota Lhokseumawe',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Langsa',         'Kota Langsa',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Subulussalam',   'Kota Subulussalam',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehBesar',      'Kab. Aceh Besar',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Pidie',          'Kab. Pidie',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-PidieJaya',      'Kab. Pidie Jaya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Bireuen',        'Kab. Bireuen',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehUtara',      'Kab. Aceh Utara',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehTimur',      'Kab. Aceh Timur',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehTamiang',    'Kab. Aceh Tamiang',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-GayoLues',       'Kab. Gayo Lues',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehTengah',     'Kab. Aceh Tengah',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-BenerMeriah',    'Kab. Bener Meriah',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehTenggara',   'Kab. Aceh Tenggara',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehSelatan',    'Kab. Aceh Selatan',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehBaratDaya',  'Kab. Aceh Barat Daya', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehBarat',      'Kab. Aceh Barat',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-NaganRaya',      'Kab. Nagan Raya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehJaya',       'Kab. Aceh Jaya',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-AcehSingkil',    'Kab. Aceh Singkil',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-AC-Simeulue',       'Kab. Simeulue',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SUMATERA UTARA — 8 Kota, 25 Kabupaten (33)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SU');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SU-Medan',             'Kota Medan',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Binjai',            'Kota Binjai',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Pematangsiantar',   'Kota Pematangsiantar',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Tanjungbalai',      'Kota Tanjungbalai',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-TebingTinggi',      'Kota Tebing Tinggi',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Sibolga',           'Kota Sibolga',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Padangsidimpuan',   'Kota Padangsidimpuan',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Gunungsitoli',      'Kota Gunungsitoli',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-DeliSerdang',       'Kab. Deli Serdang',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-SerdangBedagai',    'Kab. Serdang Bedagai',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Langkat',           'Kab. Langkat',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Karo',              'Kab. Karo',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Simalungun',        'Kab. Simalungun',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Asahan',            'Kab. Asahan',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-BatuBara',          'Kab. Batu Bara',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Labuhanbatu',       'Kab. Labuhanbatu',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-LabuhanbBatuUtara', 'Kab. Labuhanbatu Utara', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-LabuhanbatuSelatan','Kab. Labuhanbatu Selatan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-TapanuliUtara',     'Kab. Tapanuli Utara',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-TapanuliTengah',    'Kab. Tapanuli Tengah',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-TapanuliSelatan',   'Kab. Tapanuli Selatan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-PadangLawas',       'Kab. Padang Lawas',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-PadangLawasUtara',  'Kab. Padang Lawas Utara','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-MandailingNatal',   'Kab. Mandailing Natal',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Toba',              'Kab. Toba',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Samosir',           'Kab. Samosir',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-HumbangHasundutan', 'Kab. Humbang Hasundutan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-PakpakBharat',      'Kab. Pakpak Bharat',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Dairi',             'Kab. Dairi',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-Nias',              'Kab. Nias',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-NiasUtara',         'Kab. Nias Utara',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-NiasBarat',         'Kab. Nias Barat',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SU-NiasSelatan',       'Kab. Nias Selatan',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SUMATERA BARAT — 7 Kota, 12 Kabupaten (19)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SB-Padang',         'Kota Padang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Bukittinggi',    'Kota Bukittinggi',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Payakumbuh',     'Kota Payakumbuh',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Padangpanjang',  'Kota Padangpanjang',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Solok',          'Kota Solok',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Sawahlunto',     'Kota Sawahlunto',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Pariaman',       'Kota Pariaman',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-PesisirSelatan', 'Kab. Pesisir Selatan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-KabSolok',       'Kab. Solok',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-SolokSelatan',   'Kab. Solok Selatan',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Dharmasraya',    'Kab. Dharmasraya',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Sijunjung',      'Kab. Sijunjung',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-TanahDatar',     'Kab. Tanah Datar',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-PadangPariaman', 'Kab. Padang Pariaman',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Agam',           'Kab. Agam',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-LimaPuluhKota',  'Kab. Lima Puluh Kota',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-Pasaman',        'Kab. Pasaman',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-PasamanBarat',   'Kab. Pasaman Barat',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SB-KepMentawai',    'Kab. Kepulauan Mentawai','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- RIAU — 2 Kota, 10 Kabupaten (12)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-RI');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-RI-Pekanbaru',        'Kota Pekanbaru',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-Dumai',            'Kota Dumai',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-Kampar',           'Kab. Kampar',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-RokanHulu',        'Kab. Rokan Hulu',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-RokanHilir',       'Kab. Rokan Hilir',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-Bengkalis',        'Kab. Bengkalis',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-Siak',             'Kab. Siak',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-KepMeranti',       'Kab. Kepulauan Meranti', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-Pelalawan',        'Kab. Pelalawan',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-IndragiriHulu',    'Kab. Indragiri Hulu',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-IndragiriHilir',   'Kab. Indragiri Hilir',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-RI-KuantanSingingi',  'Kab. Kuantan Singingi',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KEPULAUAN RIAU — 2 Kota, 5 Kabupaten (7)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KR');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KR-Batam',            'Kota Batam',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-Tanjungpinang',    'Kota Tanjungpinang',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-Bintan',           'Kab. Bintan',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-Karimun',          'Kab. Karimun',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-Natuna',           'Kab. Natuna',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-Lingga',           'Kab. Lingga',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KR-KepAnambas',       'Kab. Kepulauan Anambas', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- JAMBI — 2 Kota, 9 Kabupaten (11)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-JA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-JA-Jambi',            'Kota Jambi',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-SungaiPenuh',      'Kota Sungai Penuh',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Batanghari',       'Kab. Batanghari',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-MuaroJambi',       'Kab. Muaro Jambi',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-TanjabBarat',      'Kab. Tanjung Jabung Barat','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-TanjabTimur',      'Kab. Tanjung Jabung Timur','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Tebo',             'Kab. Tebo',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Bungo',            'Kab. Bungo',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Merangin',         'Kab. Merangin',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Sarolangun',       'Kab. Sarolangun',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JA-Kerinci',          'Kab. Kerinci',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SUMATERA SELATAN — 4 Kota, 13 Kabupaten (17)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SS');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SS-Palembang',        'Kota Palembang',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-Prabumulih',       'Kota Prabumulih',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-PagarAlam',        'Kota Pagar Alam',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-Lubuklinggau',     'Kota Lubuklinggau',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-OKU',              'Kab. Ogan Komering Ulu', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-OKUSelatan',       'Kab. OKU Selatan',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-OKUTimur',         'Kab. OKU Timur',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-OKI',              'Kab. Ogan Komering Ilir', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-OganIlir',         'Kab. Ogan Ilir',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-MuaraEnim',        'Kab. Muara Enim',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-Lahat',            'Kab. Lahat',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-EmpatLawang',      'Kab. Empat Lawang',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-MusiRawas',        'Kab. Musi Rawas',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-MusiRawasUtara',   'Kab. Musi Rawas Utara',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-MusiBanyuasin',    'Kab. Musi Banyuasin',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-Banyuasin',        'Kab. Banyuasin',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SS-PALI',             'Kab. Penukal Abab Lematang Ilir','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- BENGKULU — 1 Kota, 9 Kabupaten (10)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-BE');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-BE-Bengkulu',         'Kota Bengkulu',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-BengkuluUtara',    'Kab. Bengkulu Utara',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-BengkuluTengah',   'Kab. Bengkulu Tengah',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-BengkuluSelatan',  'Kab. Bengkulu Selatan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-RejangLebong',     'Kab. Rejang Lebong',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-Lebong',           'Kab. Lebong',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-Kepahiang',        'Kab. Kepahiang',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-MukoMuko',         'Kab. Mukomuko',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-Seluma',           'Kab. Seluma',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BE-Kaur',             'Kab. Kaur',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- LAMPUNG — 2 Kota, 13 Kabupaten (15)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-LA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-LA-BandarLampung',    'Kota Bandar Lampung',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-Metro',            'Kota Metro',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-LampungUtara',     'Kab. Lampung Utara',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-LampungSelatan',   'Kab. Lampung Selatan',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-LampungTengah',    'Kab. Lampung Tengah',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-LampungTimur',     'Kab. Lampung Timur',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-LampungBarat',     'Kab. Lampung Barat',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-PesisirBarat',     'Kab. Pesisir Barat',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-Mesuji',           'Kab. Mesuji',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-TulangBawang',     'Kab. Tulang Bawang',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-TulangBawangBarat','Kab. Tulang Bawang Barat','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-WayKanan',         'Kab. Way Kanan',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-Tanggamus',        'Kab. Tanggamus',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-Pringsewu',        'Kab. Pringsewu',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-LA-Pesawaran',        'Kab. Pesawaran',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KEPULAUAN BANGKA BELITUNG — 1 Kota, 6 Kabupaten (7)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-BB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-BB-Pangkalpinang',    'Kota Pangkalpinang',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-Bangka',           'Kab. Bangka',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-BangkaBarat',      'Kab. Bangka Barat',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-BangkaTengah',     'Kab. Bangka Tengah',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-BangkaSelatan',    'Kab. Bangka Selatan',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-Belitung',         'Kab. Belitung',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BB-BelitungTimur',    'Kab. Belitung Timur',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- DKI JAKARTA — 5 Kota Admin, 1 Kabupaten Admin (6)
-- (Menggantikan ID-CITY-JKT yang dihapus di langkah 0)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-JK');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-JK-JakartaPusat',     'Kota Adm. Jakarta Pusat',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JK-JakartaUtara',     'Kota Adm. Jakarta Utara',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JK-JakartaBarat',     'Kota Adm. Jakarta Barat',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JK-JakartaSelatan',   'Kota Adm. Jakarta Selatan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JK-JakartaTimur',     'Kota Adm. Jakarta Timur',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JK-KepulauanSeribu',  'Kab. Adm. Kepulauan Seribu','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- JAWA BARAT — 9 Kota, 18 Kabupaten (27)
-- (Bandung menggantikan ID-CITY-BDO yang dihapus di langkah 0)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-JB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-JB-Bandung',          'Kota Bandung',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Bogor',            'Kota Bogor',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Bekasi',           'Kota Bekasi',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Depok',            'Kota Depok',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Cimahi',           'Kota Cimahi',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Tasikmalaya',      'Kota Tasikmalaya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Sukabumi',         'Kota Sukabumi',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Banjar',           'Kota Banjar',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Cirebon',          'Kota Cirebon',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabBogor',         'Kab. Bogor',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabSukabumi',      'Kab. Sukabumi',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Cianjur',          'Kab. Cianjur',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabBandung',       'Kab. Bandung',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-BandungBarat',     'Kab. Bandung Barat',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabBekasi',        'Kab. Bekasi',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Karawang',         'Kab. Karawang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Purwakarta',       'Kab. Purwakarta',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Subang',           'Kab. Subang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Sumedang',         'Kab. Sumedang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Garut',            'Kab. Garut',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabTasikmalaya',   'Kab. Tasikmalaya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Ciamis',           'Kab. Ciamis',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Pangandaran',      'Kab. Pangandaran',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Kuningan',         'Kab. Kuningan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-KabCirebon',       'Kab. Cirebon',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Majalengka',       'Kab. Majalengka',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JB-Indramayu',        'Kab. Indramayu',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- BANTEN — 4 Kota, 4 Kabupaten (8)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-BT');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-BT-Tangerang',        'Kota Tangerang',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-TangerangSelatan', 'Kota Tangerang Selatan', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-Serang',           'Kota Serang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-Cilegon',          'Kota Cilegon',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-KabTangerang',     'Kab. Tangerang',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-KabSerang',        'Kab. Serang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-Lebak',            'Kab. Lebak',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BT-Pandeglang',       'Kab. Pandeglang',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- JAWA TENGAH — 6 Kota, 29 Kabupaten (35)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-JT');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-JT-Semarang',         'Kota Semarang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Surakarta',        'Kota Surakarta',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Magelang',         'Kota Magelang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Pekalongan',       'Kota Pekalongan',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Salatiga',         'Kota Salatiga',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Tegal',            'Kota Tegal',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Banyumas',         'Kab. Banyumas',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Cilacap',          'Kab. Cilacap',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Purbalingga',      'Kab. Purbalingga',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Banjarnegara',     'Kab. Banjarnegara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Kebumen',          'Kab. Kebumen',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Purworejo',        'Kab. Purworejo',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Wonosobo',         'Kab. Wonosobo',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-KabMagelang',      'Kab. Magelang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Temanggung',       'Kab. Temanggung',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Boyolali',         'Kab. Boyolali',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Klaten',           'Kab. Klaten',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Sukoharjo',        'Kab. Sukoharjo',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Wonogiri',         'Kab. Wonogiri',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Karanganyar',      'Kab. Karanganyar',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Sragen',           'Kab. Sragen',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-KabSemarang',      'Kab. Semarang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Kendal',           'Kab. Kendal',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Demak',            'Kab. Demak',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Grobogan',         'Kab. Grobogan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Blora',            'Kab. Blora',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Rembang',          'Kab. Rembang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Pati',             'Kab. Pati',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Kudus',            'Kab. Kudus',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Jepara',           'Kab. Jepara',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Batang',           'Kab. Batang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-KabPekalongan',    'Kab. Pekalongan',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Pemalang',         'Kab. Pemalang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-KabTegal',         'Kab. Tegal',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JT-Brebes',           'Kab. Brebes',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- DI YOGYAKARTA — 1 Kota, 4 Kabupaten (5)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-YO');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-YO-Yogyakarta',       'Kota Yogyakarta',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-YO-Sleman',           'Kab. Sleman',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-YO-Bantul',           'Kab. Bantul',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-YO-KulonProgo',       'Kab. Kulon Progo',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-YO-Gunungkidul',      'Kab. Gunungkidul',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- JAWA TIMUR — 9 Kota, 29 Kabupaten (38)
-- (Surabaya menggantikan ID-CITY-SUB yang dihapus di langkah 0)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-JI');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-JI-Surabaya',         'Kota Surabaya',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Malang',           'Kota Malang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Blitar',           'Kota Blitar',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Kediri',           'Kota Kediri',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Probolinggo',      'Kota Probolinggo',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Pasuruan',         'Kota Pasuruan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Mojokerto',        'Kota Mojokerto',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Madiun',           'Kota Madiun',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Batu',             'Kota Batu',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Gresik',           'Kab. Gresik',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Sidoarjo',         'Kab. Sidoarjo',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabMojokerto',     'Kab. Mojokerto',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Jombang',          'Kab. Jombang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Lamongan',         'Kab. Lamongan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Bojonegoro',       'Kab. Bojonegoro',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Tuban',            'Kab. Tuban',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Ngawi',            'Kab. Ngawi',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Magetan',          'Kab. Magetan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabMadiun',        'Kab. Madiun',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Ponorogo',         'Kab. Ponorogo',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Pacitan',          'Kab. Pacitan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Trenggalek',       'Kab. Trenggalek',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Tulungagung',      'Kab. Tulungagung',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabBlitar',        'Kab. Blitar',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabKediri',        'Kab. Kediri',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Nganjuk',          'Kab. Nganjuk',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabMalang',        'Kab. Malang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Lumajang',         'Kab. Lumajang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabProbolinggo',   'Kab. Probolinggo',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-KabPasuruan',      'Kab. Pasuruan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Bangkalan',        'Kab. Bangkalan',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Sampang',          'Kab. Sampang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Pamekasan',        'Kab. Pamekasan',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Sumenep',          'Kab. Sumenep',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Jember',           'Kab. Jember',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Banyuwangi',       'Kab. Banyuwangi',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Bondowoso',        'Kab. Bondowoso',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-JI-Situbondo',        'Kab. Situbondo',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- BALI — 1 Kota, 8 Kabupaten (9)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-BA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-BA-Denpasar',         'Kota Denpasar',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Badung',           'Kab. Badung',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Gianyar',          'Kab. Gianyar',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Tabanan',          'Kab. Tabanan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Buleleng',         'Kab. Buleleng',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Jembrana',         'Kab. Jembrana',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Klungkung',        'Kab. Klungkung',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Bangli',           'Kab. Bangli',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-BA-Karangasem',       'Kab. Karangasem',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- NUSA TENGGARA BARAT — 2 Kota, 8 Kabupaten (10)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-NB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-NB-Mataram',          'Kota Mataram',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-Bima',             'Kota Bima',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-LombokBarat',      'Kab. Lombok Barat',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-LombokTengah',     'Kab. Lombok Tengah',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-LombokTimur',      'Kab. Lombok Timur',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-LombokUtara',      'Kab. Lombok Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-Sumbawa',          'Kab. Sumbawa',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-SumbawaBarat',     'Kab. Sumbawa Barat',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-Dompu',            'Kab. Dompu',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NB-KabBima',          'Kab. Bima',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- NUSA TENGGARA TIMUR — 1 Kota, 21 Kabupaten (22)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-NT');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-NT-Kupang',           'Kota Kupang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-KabKupang',        'Kab. Kupang',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-TTS',              'Kab. Timor Tengah Selatan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-TTU',              'Kab. Timor Tengah Utara', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Belu',             'Kab. Belu',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Malaka',           'Kab. Malaka',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Alor',             'Kab. Alor',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Lembata',          'Kab. Lembata',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-FloresTimur',      'Kab. Flores Timur',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Sikka',            'Kab. Sikka',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Ende',             'Kab. Ende',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Ngada',            'Kab. Ngada',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Nagekeo',          'Kab. Nagekeo',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-Manggarai',        'Kab. Manggarai',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-ManggaraiTimur',   'Kab. Manggarai Timur',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-ManggaraiBarat',   'Kab. Manggarai Barat',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-SumbaTimur',       'Kab. Sumba Timur',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-SumbaTengah',      'Kab. Sumba Tengah',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-SumbaBarat',       'Kab. Sumba Barat',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-SumbaBaratDaya',   'Kab. Sumba Barat Daya',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-RoteNdao',         'Kab. Rote Ndao',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-NT-SabuRaijua',       'Kab. Sabu Raijua',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KALIMANTAN BARAT — 2 Kota, 12 Kabupaten (14)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KB-Pontianak',        'Kota Pontianak',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Singkawang',       'Kota Singkawang',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-KubuRaya',         'Kab. Kubu Raya',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Mempawah',         'Kab. Mempawah',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Sambas',           'Kab. Sambas',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Bengkayang',       'Kab. Bengkayang',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Landak',           'Kab. Landak',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Sanggau',          'Kab. Sanggau',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Sekadau',          'Kab. Sekadau',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Melawi',           'Kab. Melawi',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Sintang',          'Kab. Sintang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-KapuasHulu',       'Kab. Kapuas Hulu',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-Ketapang',         'Kab. Ketapang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KB-KayongUtara',      'Kab. Kayong Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KALIMANTAN TENGAH — 1 Kota, 13 Kabupaten (14)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KT');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KT-PalangkaRaya',     'Kota Palangka Raya',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-KotawaringinBarat','Kab. Kotawaringin Barat','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-KotawaringinTimur','Kab. Kotawaringin Timur','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-Kapuas',           'Kab. Kapuas',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-BaritoSelatan',    'Kab. Barito Selatan',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-BaritoUtara',      'Kab. Barito Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-BaritoTimur',      'Kab. Barito Timur',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-MurungRaya',       'Kab. Murung Raya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-GunungMas',        'Kab. Gunung Mas',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-PulangPisau',      'Kab. Pulang Pisau',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-Katingan',         'Kab. Katingan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-Seruyan',          'Kab. Seruyan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-Sukamara',         'Kab. Sukamara',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KT-Lamandau',         'Kab. Lamandau',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KALIMANTAN SELATAN — 2 Kota, 11 Kabupaten (13)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KS');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KS-Banjarmasin',      'Kota Banjarmasin',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Banjarbaru',       'Kota Banjarbaru',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Banjar',           'Kab. Banjar',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-BaritoKuala',      'Kab. Barito Kuala',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Tapin',            'Kab. Tapin',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-HSS',              'Kab. Hulu Sungai Selatan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-HST',              'Kab. Hulu Sungai Tengah','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-HSU',              'Kab. Hulu Sungai Utara', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Balangan',         'Kab. Balangan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Tabalong',         'Kab. Tabalong',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-Kotabaru',         'Kab. Kotabaru',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-TanahLaut',        'Kab. Tanah Laut',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KS-TanahBumbu',       'Kab. Tanah Bumbu',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KALIMANTAN TIMUR — 3 Kota, 7 Kabupaten (10)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KI');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KI-Samarinda',        'Kota Samarinda',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-Balikpapan',       'Kota Balikpapan',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-Bontang',          'Kota Bontang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-KutaiKartanegara', 'Kab. Kutai Kartanegara', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-KutaiTimur',       'Kab. Kutai Timur',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-KutaiBarat',       'Kab. Kutai Barat',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-Berau',            'Kab. Berau',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-MahakamUlu',       'Kab. Mahakam Ulu',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-Paser',            'Kab. Paser',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KI-PPU',              'Kab. Penajam Paser Utara','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- KALIMANTAN UTARA — 1 Kota, 4 Kabupaten (5)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-KU');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-KU-Tarakan',          'Kota Tarakan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KU-Bulungan',         'Kab. Bulungan',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KU-Malinau',          'Kab. Malinau',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KU-Nunukan',          'Kab. Nunukan',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-KU-TanaTidung',       'Kab. Tana Tidung',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SULAWESI UTARA — 4 Kota, 11 Kabupaten (15)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SA-Manado',           'Kota Manado',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-Bitung',           'Kota Bitung',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-Tomohon',          'Kota Tomohon',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-Kotamobagu',       'Kota Kotamobagu',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-Minahasa',         'Kab. Minahasa',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-MinahasaUtara',    'Kab. Minahasa Utara',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-MinahasaSelatan',  'Kab. Minahasa Selatan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-MinahasaTenggara', 'Kab. Minahasa Tenggara', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-BolaangMongondow', 'Kab. Bolaang Mongondow', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-BMU',              'Kab. Bolaang Mongondow Utara','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-BMT',              'Kab. Bolaang Mongondow Timur','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-BMS',              'Kab. Bolaang Mongondow Selatan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-KepSangihe',       'Kab. Kepulauan Sangihe', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-KepTalaud',        'Kab. Kepulauan Talaud',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SA-Sitaro',           'Kab. Kepulauan Siau Tagulandang Biaro','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- GORONTALO — 1 Kota, 5 Kabupaten (6)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-GO');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-GO-Gorontalo',        'Kota Gorontalo',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-GO-KabGorontalo',     'Kab. Gorontalo',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-GO-GorontaloUtara',   'Kab. Gorontalo Utara',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-GO-Boalemo',          'Kab. Boalemo',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-GO-Pohuwato',         'Kab. Pohuwato',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-GO-BoneBolango',      'Kab. Bone Bolango',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SULAWESI TENGAH — 1 Kota, 12 Kabupaten (13)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-ST');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-ST-Palu',             'Kota Palu',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Banggai',          'Kab. Banggai',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-BanggaiLaut',      'Kab. Banggai Laut',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-BanggaiKep',       'Kab. Banggai Kepulauan', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Morowali',         'Kab. Morowali',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-MorowaliUtara',    'Kab. Morowali Utara',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Poso',             'Kab. Poso',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-ParigiMoutong',    'Kab. Parigi Moutong',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Tolitoli',         'Kab. Tolitoli',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Buol',             'Kab. Buol',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Donggala',         'Kab. Donggala',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-Sigi',             'Kab. Sigi',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-ST-TojoUnaUna',       'Kab. Tojo Una-Una',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SULAWESI BARAT — 0 Kota, 6 Kabupaten (6)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SR');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SR-Mamuju',           'Kab. Mamuju',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SR-MamujuTengah',     'Kab. Mamuju Tengah',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SR-Majene',           'Kab. Majene',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SR-PolewaliMandar',   'Kab. Polewali Mandar',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SR-Mamasa',           'Kab. Mamasa',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SR-Pasangkayu',       'Kab. Pasangkayu',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SULAWESI SELATAN — 3 Kota, 21 Kabupaten (24)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SN');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SN-Makassar',         'Kota Makassar',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Parepare',         'Kota Parepare',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Palopo',           'Kota Palopo',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Gowa',             'Kab. Gowa',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Takalar',          'Kab. Takalar',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Jeneponto',        'Kab. Jeneponto',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Bantaeng',         'Kab. Bantaeng',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Bulukumba',        'Kab. Bulukumba',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-KepSelayar',       'Kab. Kepulauan Selayar', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Sinjai',           'Kab. Sinjai',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Bone',             'Kab. Bone',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Wajo',             'Kab. Wajo',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Soppeng',          'Kab. Soppeng',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Barru',            'Kab. Barru',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Pangkep',          'Kab. Pangkajene dan Kepulauan','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Maros',            'Kab. Maros',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Luwu',             'Kab. Luwu',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-LuwuUtara',        'Kab. Luwu Utara',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-LuwuTimur',        'Kab. Luwu Timur',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-TanaToraja',       'Kab. Tana Toraja',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-TorajaUtara',      'Kab. Toraja Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-SidenrengRappang', 'Kab. Sidenreng Rappang', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Pinrang',          'Kab. Pinrang',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SN-Enrekang',         'Kab. Enrekang',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- SULAWESI TENGGARA — 2 Kota, 15 Kabupaten (17)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-SG');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-SG-Kendari',          'Kota Kendari',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Baubau',           'Kota Baubau',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Konawe',           'Kab. Konawe',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-KonaweSelatan',    'Kab. Konawe Selatan',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-KonaweUtara',      'Kab. Konawe Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-KonaweKep',        'Kab. Konawe Kepulauan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Kolaka',           'Kab. Kolaka',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-KolakaUtara',      'Kab. Kolaka Utara',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-KolakaTimur',      'Kab. Kolaka Timur',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Bombana',          'Kab. Bombana',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Buton',            'Kab. Buton',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-ButonSelatan',     'Kab. Buton Selatan',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-ButonTengah',      'Kab. Buton Tengah',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-ButonUtara',       'Kab. Buton Utara',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Muna',             'Kab. Muna',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-MunaBarat',        'Kab. Muna Barat',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-SG-Wakatobi',         'Kab. Wakatobi',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- MALUKU — 2 Kota, 9 Kabupaten (11)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-MA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-MA-Ambon',            'Kota Ambon',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-Tual',             'Kota Tual',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-MalukuTengah',     'Kab. Maluku Tengah',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-MalukuTenggara',   'Kab. Maluku Tenggara',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-KepAru',           'Kab. Kepulauan Aru',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-SBB',              'Kab. Seram Bagian Barat','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-SBT',              'Kab. Seram Bagian Timur','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-Buru',             'Kab. Buru',              'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-BuruSelatan',      'Kab. Buru Selatan',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-MalukuBaratDaya',  'Kab. Maluku Barat Daya', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MA-KepTanimbar',      'Kab. Kepulauan Tanimbar','CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- MALUKU UTARA — 2 Kota, 8 Kabupaten (10)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-MU');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-MU-Ternate',          'Kota Ternate',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-TidoreKep',        'Kota Tidore Kepulauan',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-HalmaheraBarat',   'Kab. Halmahera Barat',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-HalmaheraUtara',   'Kab. Halmahera Utara',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-HalmaheraTimur',   'Kab. Halmahera Timur',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-HalmaheraSelatan', 'Kab. Halmahera Selatan', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-HalmaheraTengah',  'Kab. Halmahera Tengah',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-KepSula',          'Kab. Kepulauan Sula',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-PulauTaliabu',     'Kab. Pulau Taliabu',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-MU-PulauMorotai',     'Kab. Pulau Morotai',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA — 1 Kota, 8 Kabupaten (9)
-- (Pasca pemekaran 2022: Papua Selatan, Tengah, Pegunungan sudah pisah)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PA');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PA-Jayapura',         'Kota Jayapura',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-KabJayapura',      'Kab. Jayapura',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-Keerom',           'Kab. Keerom',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-Sarmi',            'Kab. Sarmi',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-MamberamoRaya',    'Kab. Mamberamo Raya',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-Waropen',          'Kab. Waropen',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-BiakNumfor',       'Kab. Biak Numfor',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-KepYapen',         'Kab. Kepulauan Yapen',   'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PA-Supiori',          'Kab. Supiori',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA BARAT — 0 Kota, 6 Kabupaten (6)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PB');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PB-Manokwari',        'Kab. Manokwari',         'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-ManokwariSelatan', 'Kab. Manokwari Selatan', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-TelukBintuni',     'Kab. Teluk Bintuni',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-TelukWondama',     'Kab. Teluk Wondama',     'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-Kaimana',          'Kab. Kaimana',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-Fakfak',           'Kab. Fakfak',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PB-PegununganArfak',  'Kab. Pegunungan Arfak',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA SELATAN — 0 Kota, 4 Kabupaten (4)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PS');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PS-Merauke',          'Kab. Merauke',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PS-BovenDigoel',      'Kab. Boven Digoel',      'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PS-Mappi',            'Kab. Mappi',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PS-Asmat',            'Kab. Asmat',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA TENGAH — 0 Kota, 7 Kabupaten (7)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PT');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PT-Nabire',           'Kab. Nabire',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-Paniai',           'Kab. Paniai',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-PuncakJaya',       'Kab. Puncak Jaya',       'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-Puncak',           'Kab. Puncak',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-Dogiyai',          'Kab. Dogiyai',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-IntanJaya',        'Kab. Intan Jaya',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-Deiyai',           'Kab. Deiyai',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PT-Mimika',           'Kab. Mimika',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA PEGUNUNGAN — 0 Kota, 8 Kabupaten (8)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PP');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PP-Jayawijaya',       'Kab. Jayawijaya',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-PegununganBintang','Kab. Pegunungan Bintang', 'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-Yahukimo',         'Kab. Yahukimo',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-Tolikara',         'Kab. Tolikara',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-MamberamoTengah',  'Kab. Mamberamo Tengah',  'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-Nduga',            'Kab. Nduga',             'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-LannyJaya',        'Kab. Lanny Jaya',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PP-Yalimo',           'Kab. Yalimo',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

-- ----------------------------------------------------------
-- PAPUA BARAT DAYA — 1 Kota, 5 Kabupaten (6)
-- ----------------------------------------------------------
SET @p = (SELECT id FROM geographics WHERE code = 'ID-PD');
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES
('ID-PD-Sorong',           'Kota Sorong',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PD-KabSorong',        'Kab. Sorong',            'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PD-RajaAmpat',        'Kab. Raja Ampat',        'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PD-Tambrauw',         'Kab. Tambrauw',          'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PD-Maybrat',          'Kab. Maybrat',           'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW()),
('ID-PD-SorongSelatan',    'Kab. Sorong Selatan',    'CITY_MUNICIPALITY', @p, 'SYSTEM', NOW());

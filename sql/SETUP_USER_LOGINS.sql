-- ============================================
-- Tanishq Event Management - User Login Setup
-- ============================================
-- This script creates sample ABM, RBM, and CEE logins
-- and maps them to stores across India

USE tanishq;

-- ============================================
-- STEP 1: Create ABM Logins (Area Business Managers)
-- ============================================
-- ABMs manage stores at a regional/zonal level

INSERT INTO abm_login (abm_user_id, abm_name, password, email, region) VALUES
-- North Region
('abm_north1', 'Rajesh Kumar Singh', 'Tanishq@2026', 'rajesh.kumar@tanishq.com', 'North'),
('abm_north2', 'Priyanka Sharma', 'Tanishq@2026', 'priyanka.sharma@tanishq.com', 'North'),

-- South Region
('abm_south1', 'Lakshmi Venkataraman', 'Tanishq@2026', 'lakshmi.v@tanishq.com', 'South'),
('abm_south2', 'Krishna Reddy', 'Tanishq@2026', 'krishna.reddy@tanishq.com', 'South'),

-- East Region
('abm_east1', 'Amit Kumar Bose', 'Tanishq@2026', 'amit.bose@tanishq.com', 'East'),
('abm_east2', 'Soumya Das', 'Tanishq@2026', 'soumya.das@tanishq.com', 'East'),

-- West Region
('abm_west1', 'Sneha Patel', 'Tanishq@2026', 'sneha.patel@tanishq.com', 'West'),
('abm_west2', 'Kiran Mehta', 'Tanishq@2026', 'kiran.mehta@tanishq.com', 'West'),

-- Central Region
('abm_central1', 'Vijay Malhotra', 'Tanishq@2026', 'vijay.malhotra@tanishq.com', 'Central'),

-- Northeast Region
('abm_northeast1', 'Rina Sharma', 'Tanishq@2026', 'rina.sharma@tanishq.com', 'Northeast');

SELECT 'ABM Logins Created' AS Status, COUNT(*) AS Count FROM abm_login;

-- ============================================
-- STEP 2: Create RBM Logins (Regional Business Managers)
-- ============================================
-- RBMs oversee multiple ABMs and broader regions

INSERT INTO rbm_login (rbm_user_id, rbm_name, password, email) VALUES
-- Major Regions
('rbm_north', 'Vikram Singh Rathore', 'Tanishq@2026', 'vikram.singh@tanishq.com'),
('rbm_south', 'Meenakshi Sundaram', 'Tanishq@2026', 'meenakshi.s@tanishq.com'),
('rbm_east', 'Debashish Chatterjee', 'Tanishq@2026', 'debashish.c@tanishq.com'),
('rbm_west', 'Sanjay Deshmukh', 'Tanishq@2026', 'sanjay.d@tanishq.com'),
('rbm_central', 'Arun Kumar Jain', 'Tanishq@2026', 'arun.jain@tanishq.com'),
('rbm_northeast', 'Rajat Barua', 'Tanishq@2026', 'rajat.barua@tanishq.com');

SELECT 'RBM Logins Created' AS Status, COUNT(*) AS Count FROM rbm_login;

-- ============================================
-- STEP 3: Create CEE Logins (Customer Experience Executives)
-- ============================================
-- CEEs focus on customer experience at city/cluster level

INSERT INTO cee_login (cee_user_id, cee_name, password, email, region) VALUES
-- Metro Cities
('cee_delhi', 'Neha Gupta', 'Tanishq@2026', 'neha.gupta@tanishq.com', 'Delhi NCR'),
('cee_mumbai', 'Sanjay Desai', 'Tanishq@2026', 'sanjay.desai@tanishq.com', 'Mumbai'),
('cee_bangalore', 'Anjali Reddy', 'Tanishq@2026', 'anjali.reddy@tanishq.com', 'Bangalore'),
('cee_kolkata', 'Ravi Banerjee', 'Tanishq@2026', 'ravi.banerjee@tanishq.com', 'Kolkata'),
('cee_chennai', 'Priya Iyer', 'Tanishq@2026', 'priya.iyer@tanishq.com', 'Chennai'),
('cee_hyderabad', 'Ramesh Naidu', 'Tanishq@2026', 'ramesh.naidu@tanishq.com', 'Hyderabad'),
('cee_pune', 'Pooja Kulkarni', 'Tanishq@2026', 'pooja.kulkarni@tanishq.com', 'Pune'),
('cee_ahmedabad', 'Ketan Shah', 'Tanishq@2026', 'ketan.shah@tanishq.com', 'Ahmedabad'),

-- Tier 2 Cities
('cee_jaipur', 'Mohit Agarwal', 'Tanishq@2026', 'mohit.agarwal@tanishq.com', 'Jaipur'),
('cee_lucknow', 'Shikha Verma', 'Tanishq@2026', 'shikha.verma@tanishq.com', 'Lucknow'),
('cee_chandigarh', 'Simran Kaur', 'Tanishq@2026', 'simran.kaur@tanishq.com', 'Chandigarh'),
('cee_coimbatore', 'Suresh Kumar', 'Tanishq@2026', 'suresh.kumar@tanishq.com', 'Coimbatore'),
('cee_kochi', 'Maya Nair', 'Tanishq@2026', 'maya.nair@tanishq.com', 'Kochi'),
('cee_indore', 'Arjun Rao', 'Tanishq@2026', 'arjun.rao@tanishq.com', 'Indore'),
('cee_bhopal', 'Kavita Mishra', 'Tanishq@2026', 'kavita.mishra@tanishq.com', 'Bhopal'),
('cee_nagpur', 'Sachin Patil', 'Tanishq@2026', 'sachin.patil@tanishq.com', 'Nagpur'),
('cee_surat', 'Divya Parikh', 'Tanishq@2026', 'divya.parikh@tanishq.com', 'Surat'),
('cee_visakhapatnam', 'Venkat Rao', 'Tanishq@2026', 'venkat.rao@tanishq.com', 'Visakhapatnam'),
('cee_guwahati', 'Ananya Bora', 'Tanishq@2026', 'ananya.bora@tanishq.com', 'Guwahati'),
('cee_patna', 'Rahul Kumar', 'Tanishq@2026', 'rahul.kumar@tanishq.com', 'Patna');

SELECT 'CEE Logins Created' AS Status, COUNT(*) AS Count FROM cee_login;

-- ============================================
-- STEP 4: Map Stores to ABM/RBM/CEE
-- ============================================

-- First, set region for stores based on state/city
UPDATE stores SET region = 'North' WHERE store_state IN ('Delhi', 'Punjab', 'Haryana', 'Himachal Pradesh', 'Jammu and Kashmir', 'Uttarakhand', 'Uttar Pradesh');
UPDATE stores SET region = 'South' WHERE store_state IN ('Karnataka', 'Tamil Nadu', 'Kerala', 'Andhra Pradesh', 'Telangana');
UPDATE stores SET region = 'East' WHERE store_state IN ('West Bengal', 'Bihar', 'Jharkhand', 'Odisha', 'Assam');
UPDATE stores SET region = 'West' WHERE store_state IN ('Maharashtra', 'Gujarat', 'Rajasthan', 'Goa');
UPDATE stores SET region = 'Central' WHERE store_state IN ('Madhya Pradesh', 'Chhattisgarh');
UPDATE stores SET region = 'Northeast' WHERE store_state IN ('Manipur', 'Meghalaya', 'Mizoram', 'Nagaland', 'Tripura', 'Arunachal Pradesh', 'Sikkim');

-- ============================================
-- NORTH REGION MAPPING
-- ============================================

-- Delhi NCR stores → abm_north1, rbm_north, cee_delhi
UPDATE stores
SET abm_username = 'abm_north1', rbm_username = 'rbm_north', cee_username = 'cee_delhi'
WHERE store_city IN ('Delhi', 'New Delhi', 'Noida', 'Gurgaon', 'Gurugram', 'Faridabad', 'Ghaziabad')
   OR store_code LIKE 'DEL%' OR store_code LIKE 'NCR%';

-- Chandigarh/Punjab stores → abm_north1, rbm_north, cee_chandigarh
UPDATE stores
SET abm_username = 'abm_north1', rbm_username = 'rbm_north', cee_username = 'cee_chandigarh'
WHERE store_city IN ('Chandigarh', 'Mohali', 'Panchkula', 'Ludhiana', 'Amritsar', 'Jalandhar')
   OR store_code LIKE 'CHD%' OR store_code LIKE 'PUN%';

-- Jaipur/Rajasthan stores → abm_north2, rbm_north, cee_jaipur
UPDATE stores
SET abm_username = 'abm_north2', rbm_username = 'rbm_north', cee_username = 'cee_jaipur'
WHERE store_city IN ('Jaipur', 'Jodhpur', 'Udaipur', 'Kota', 'Ajmer')
   OR store_code LIKE 'JAI%' OR store_code LIKE 'RAJ%';

-- Lucknow/UP stores → abm_north2, rbm_north, cee_lucknow
UPDATE stores
SET abm_username = 'abm_north2', rbm_username = 'rbm_north', cee_username = 'cee_lucknow'
WHERE store_city IN ('Lucknow', 'Kanpur', 'Agra', 'Varanasi', 'Meerut', 'Allahabad')
   OR store_code LIKE 'LKO%' OR store_code LIKE 'KAN%';

-- ============================================
-- SOUTH REGION MAPPING
-- ============================================

-- Bangalore stores → abm_south1, rbm_south, cee_bangalore
UPDATE stores
SET abm_username = 'abm_south1', rbm_username = 'rbm_south', cee_username = 'cee_bangalore'
WHERE store_city IN ('Bangalore', 'Bengaluru', 'Mysore', 'Mangalore')
   OR store_code LIKE 'BLR%' OR store_code LIKE 'BNG%';

-- Chennai stores → abm_south1, rbm_south, cee_chennai
UPDATE stores
SET abm_username = 'abm_south1', rbm_username = 'rbm_south', cee_username = 'cee_chennai'
WHERE store_city IN ('Chennai', 'Madurai', 'Trichy', 'Salem', 'Tiruppur')
   OR store_code LIKE 'CHE%' OR store_code LIKE 'MAD%';

-- Hyderabad stores → abm_south2, rbm_south, cee_hyderabad
UPDATE stores
SET abm_username = 'abm_south2', rbm_username = 'rbm_south', cee_username = 'cee_hyderabad'
WHERE store_city IN ('Hyderabad', 'Secunderabad', 'Vijayawada', 'Guntur')
   OR store_code LIKE 'HYD%' OR store_code LIKE 'VIJ%';

-- Kochi/Kerala stores → abm_south2, rbm_south, cee_kochi
UPDATE stores
SET abm_username = 'abm_south2', rbm_username = 'rbm_south', cee_username = 'cee_kochi'
WHERE store_city IN ('Kochi', 'Cochin', 'Trivandrum', 'Kozhikode', 'Thrissur', 'Kannur')
   OR store_code LIKE 'KOC%' OR store_code LIKE 'KER%';

-- Coimbatore stores → abm_south1, rbm_south, cee_coimbatore
UPDATE stores
SET abm_username = 'abm_south1', rbm_username = 'rbm_south', cee_username = 'cee_coimbatore'
WHERE store_city IN ('Coimbatore', 'Erode', 'Tiruppur')
   OR store_code LIKE 'COI%';

-- Visakhapatnam stores → abm_south2, rbm_south, cee_visakhapatnam
UPDATE stores
SET abm_username = 'abm_south2', rbm_username = 'rbm_south', cee_username = 'cee_visakhapatnam'
WHERE store_city IN ('Visakhapatnam', 'Vizag', 'Rajahmundry', 'Kakinada')
   OR store_code LIKE 'VIS%';

-- ============================================
-- EAST REGION MAPPING
-- ============================================

-- Kolkata stores → abm_east1, rbm_east, cee_kolkata
UPDATE stores
SET abm_username = 'abm_east1', rbm_username = 'rbm_east', cee_username = 'cee_kolkata'
WHERE store_city IN ('Kolkata', 'Calcutta', 'Howrah', 'Siliguri', 'Durgapur')
   OR store_code LIKE 'KOL%' OR store_code LIKE 'CAL%';

-- Patna/Bihar stores → abm_east2, rbm_east, cee_patna
UPDATE stores
SET abm_username = 'abm_east2', rbm_username = 'rbm_east', cee_username = 'cee_patna'
WHERE store_city IN ('Patna', 'Gaya', 'Bhagalpur', 'Muzaffarpur')
   OR store_code LIKE 'PAT%';

-- Guwahati/Northeast stores → abm_northeast1, rbm_northeast, cee_guwahati
UPDATE stores
SET abm_username = 'abm_northeast1', rbm_username = 'rbm_northeast', cee_username = 'cee_guwahati'
WHERE store_city IN ('Guwahati', 'Shillong', 'Imphal', 'Agartala', 'Dibrugarh')
   OR store_code LIKE 'GUW%' OR store_code LIKE 'ASM%';

-- ============================================
-- WEST REGION MAPPING
-- ============================================

-- Mumbai stores → abm_west1, rbm_west, cee_mumbai
UPDATE stores
SET abm_username = 'abm_west1', rbm_username = 'rbm_west', cee_username = 'cee_mumbai'
WHERE store_city IN ('Mumbai', 'Bombay', 'Thane', 'Navi Mumbai', 'Kalyan')
   OR store_code LIKE 'MUM%' OR store_code LIKE 'BOM%';

-- Pune stores → abm_west1, rbm_west, cee_pune
UPDATE stores
SET abm_username = 'abm_west1', rbm_username = 'rbm_west', cee_username = 'cee_pune'
WHERE store_city IN ('Pune', 'Pimpri-Chinchwad', 'Nashik')
   OR store_code LIKE 'PUN%';

-- Ahmedabad/Gujarat stores → abm_west2, rbm_west, cee_ahmedabad
UPDATE stores
SET abm_username = 'abm_west2', rbm_username = 'rbm_west', cee_username = 'cee_ahmedabad'
WHERE store_city IN ('Ahmedabad', 'Gandhinagar', 'Vadodara', 'Baroda', 'Rajkot')
   OR store_code LIKE 'AHM%' OR store_code LIKE 'GUJ%';

-- Surat stores → abm_west2, rbm_west, cee_surat
UPDATE stores
SET abm_username = 'abm_west2', rbm_username = 'rbm_west', cee_username = 'cee_surat'
WHERE store_city IN ('Surat', 'Valsad', 'Navsari')
   OR store_code LIKE 'SUR%';

-- Nagpur stores → abm_west1, rbm_west, cee_nagpur
UPDATE stores
SET abm_username = 'abm_west1', rbm_username = 'rbm_west', cee_username = 'cee_nagpur'
WHERE store_city IN ('Nagpur', 'Amravati', 'Akola')
   OR store_code LIKE 'NAG%';

-- ============================================
-- CENTRAL REGION MAPPING
-- ============================================

-- Indore stores → abm_central1, rbm_central, cee_indore
UPDATE stores
SET abm_username = 'abm_central1', rbm_username = 'rbm_central', cee_username = 'cee_indore'
WHERE store_city IN ('Indore', 'Ujjain', 'Dewas')
   OR store_code LIKE 'IND%';

-- Bhopal stores → abm_central1, rbm_central, cee_bhopal
UPDATE stores
SET abm_username = 'abm_central1', rbm_username = 'rbm_central', cee_username = 'cee_bhopal'
WHERE store_city IN ('Bhopal', 'Jabalpur', 'Gwalior')
   OR store_code LIKE 'BHO%';

-- ============================================
-- STEP 5: Verify Mappings
-- ============================================

SELECT '=== MAPPING VERIFICATION ===' AS Info;

-- Count stores by ABM
SELECT
    a.abm_name,
    a.abm_user_id,
    a.region,
    COUNT(s.store_code) as store_count
FROM abm_login a
LEFT JOIN stores s ON s.abm_username = a.abm_user_id
GROUP BY a.abm_name, a.abm_user_id, a.region
ORDER BY a.region, a.abm_user_id;

-- Count stores by RBM
SELECT
    r.rbm_name,
    r.rbm_user_id,
    COUNT(s.store_code) as store_count
FROM rbm_login r
LEFT JOIN stores s ON s.rbm_username = r.rbm_user_id
GROUP BY r.rbm_name, r.rbm_user_id
ORDER BY r.rbm_user_id;

-- Count stores by CEE
SELECT
    c.cee_name,
    c.cee_user_id,
    c.region,
    COUNT(s.store_code) as store_count
FROM cee_login c
LEFT JOIN stores s ON s.cee_username = c.cee_user_id
GROUP BY c.cee_name, c.cee_user_id, c.region
ORDER BY c.region, c.cee_user_id;

-- Stores without any mapping (need attention)
SELECT
    store_code,
    store_name,
    store_city,
    store_state,
    region
FROM stores
WHERE abm_username IS NULL
   AND rbm_username IS NULL
   AND cee_username IS NULL
LIMIT 50;

-- Sample mapped stores
SELECT
    store_code,
    store_name,
    store_city,
    region,
    abm_username,
    rbm_username,
    cee_username
FROM stores
WHERE abm_username IS NOT NULL
LIMIT 20;

SELECT '=== SETUP COMPLETE ===' AS Status;


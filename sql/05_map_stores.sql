-- Map Stores to Users
USE tanishq;
-- Example: Map stores to ABM users
UPDATE stores SET abm_username = 'abm_north1' WHERE region = 'North' AND store_code LIKE 'DEL%';
UPDATE stores SET abm_username = 'abm_south1' WHERE region = 'South' AND store_code LIKE 'BLR%';
UPDATE stores SET abm_username = 'abm_east1' WHERE region = 'East' AND store_code LIKE 'KOL%';
UPDATE stores SET abm_username = 'abm_west1' WHERE region = 'West' AND store_code LIKE 'MUM%';
-- Example: Map stores to RBM users
UPDATE stores SET rbm_username = 'rbm_north1' WHERE region IN ('North', 'NorthEast');
UPDATE stores SET rbm_username = 'rbm_south1' WHERE region IN ('South', 'SouthEast');
-- Example: Map stores to CEE users
UPDATE stores SET cee_username = 'cee_mumbai1' WHERE store_city = 'Mumbai';
UPDATE stores SET cee_username = 'cee_delhi1' WHERE store_city = 'Delhi';
UPDATE stores SET cee_username = 'cee_bangalore1' WHERE store_city = 'Bangalore';
-- Verify mappings
SELECT store_code, store_name, store_city, region, abm_username, rbm_username, cee_username
FROM stores
WHERE abm_username IS NOT NULL OR rbm_username IS NOT NULL OR cee_username IS NOT NULL
LIMIT 20;

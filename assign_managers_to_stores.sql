-- =====================================================
-- ASSIGN MANAGERS TO STORES BY REGION
-- =====================================================
-- This assigns appropriate RBM, CEE, and ABM to each store
-- based on their region
-- =====================================================

-- EAST 1 Region (CSB already done)
-- CSB is already assigned to EAST1, EAST1-CEE-01, EAST1-ABM-01

-- EAST 2 Region
UPDATE stores 
SET rbm_username = 'EAST2',
    cee_username = 'EAST2-CEE-01',
    abm_username = 'EAST2-ABM-01'
WHERE store_code = 'TPT';

-- NORTH 1 Region
UPDATE stores 
SET rbm_username = 'NORTH1',
    cee_username = 'NORTH1-CEE-01',
    abm_username = 'NORTH1-ABM-01'
WHERE store_code = 'PRA';

-- NORTH 2 Region
UPDATE stores 
SET rbm_username = 'NORTH2',
    cee_username = 'NORTH2-CEE-01',
    abm_username = 'NORTH2-ABM-01'
WHERE store_code = 'LKO';

-- NORTH 3 Region
UPDATE stores 
SET rbm_username = 'NORTH3',
    cee_username = 'NORTH3-CEE-01',
    abm_username = 'NORTH3-ABM-01'
WHERE store_code = 'YNR';

-- NORTH 4 Region
UPDATE stores 
SET rbm_username = 'NORTH4',
    cee_username = 'NORTH4-CEE-01',
    abm_username = 'NORTH4-ABM-01'
WHERE store_code = 'FRD';

-- SOUTH 1 Region
UPDATE stores 
SET rbm_username = 'SOUTH1',
    cee_username = 'SOUTH1-CEE-01',
    abm_username = 'SOUTH1-ABM-01'
WHERE store_code = 'CRB';

-- SOUTH 2 Region
UPDATE stores 
SET rbm_username = 'SOUTH2',
    cee_username = 'SOUTH2-CEE-01',
    abm_username = 'SOUTH2-ABM-01'
WHERE store_code = 'BGR';

-- SOUTH 3 Region
UPDATE stores 
SET rbm_username = 'SOUTH3',
    cee_username = 'SOUTH3-CEE-01',
    abm_username = 'SOUTH3-ABM-01'
WHERE store_code = 'HNR';

-- WEST 1 Region (ADH already has managers, update RPT)
UPDATE stores 
SET rbm_username = 'WEST1',
    cee_username = 'WEST1-CEE-01',
    abm_username = 'WEST1-ABM-01'
WHERE store_code = 'RPT';

-- WEST 2 Region
UPDATE stores 
SET rbm_username = 'WEST2',
    cee_username = 'WEST2-CEE-01',
    abm_username = 'WEST2-ABM-01'
WHERE store_code = 'AUN';

-- WEST 3 Region
UPDATE stores 
SET rbm_username = 'WEST3',
    cee_username = 'WEST3-CEE-01',
    abm_username = 'WEST3-ABM-01'
WHERE store_code = 'JAB';

-- Verification
SELECT '=== MANAGER ASSIGNMENTS COMPLETED ===' as status;
SELECT '' as blank;
SELECT 'Stores with Manager Assignments:' as info;
SELECT 
    store_code, 
    store_name,
    region,
    rbm_username, 
    cee_username, 
    abm_username 
FROM stores 
WHERE region IS NOT NULL
ORDER BY region, store_code;

SELECT '' as blank;
SELECT 'Stores WITHOUT Manager Assignments:' as info;
SELECT 
    store_code, 
    store_name,
    region
FROM stores 
WHERE rbm_username IS NULL OR cee_username IS NULL OR abm_username IS NULL;

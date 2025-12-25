# Database Structure - Manager and Store Logins

## Overview
The system now has **separate tables** for each type of login:
- **`rbm_login`** - Regional Business Managers
- **`cee_login`** - Customer Experience Executives  
- **`abm_login`** - Area Business Managers
- **`users`** - Store logins ONLY

## Table Structures

### 1. rbm_login (Regional Business Managers)
```sql
CREATE TABLE rbm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rbm_user_id VARCHAR(255) UNIQUE NOT NULL,  -- e.g., "EAST1", "NORTH1"
    rbm_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Current Data:** 13 RBM accounts
- EAST1, EAST2
- NORTH1, NORTH2, NORTH3, NORTH4
- SOUTH1, SOUTH2, SOUTH3
- WEST1, WEST2, WEST3

**Login Example:**
- Username: `EAST1`
- Password: `east1@123`

---

### 2. cee_login (Customer Experience Executives)
```sql
CREATE TABLE cee_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cee_user_id VARCHAR(255) UNIQUE NOT NULL,  -- e.g., "EAST1-CEE-01"
    cee_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    region VARCHAR(50),                         -- e.g., "EAST1", "NORTH1"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Current Data:** 17 CEE accounts
- EAST1-CEE-01, EAST1-CEE-02, EAST2-CEE-01
- NORTH1-CEE-01, NORTH2-CEE-01, NORTH3-CEE-01, NORTH4-CEE-01
- SOUTH1-CEE-01, SOUTH1-CEE-02, SOUTH2-CEE-01, SOUTH3-CEE-01
- WEST1-CEE-01, WEST1-CEE-02, WEST2-CEE-01, WEST3-CEE-01, WEST3-CEE-02

**Login Example:**
- Username: `EAST1-CEE-01`
- Password: `Tanishq@cee`

---

### 3. abm_login (Area Business Managers)
```sql
CREATE TABLE abm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abm_user_id VARCHAR(255) UNIQUE NOT NULL,  -- e.g., "EAST1-ABM-01"
    abm_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    region VARCHAR(50),                         -- e.g., "EAST1", "NORTH1"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Current Data:** 89 ABM accounts
- EAST1-ABM-01 to EAST1-ABM-10 (10 ABMs)
- EAST2-ABM-01 to EAST2-ABM-09 (9 ABMs)
- NORTH1-ABM-01 to NORTH1-ABM-06 (6 ABMs)
- NORTH2-ABM-01 to NORTH2-ABM-08 (8 ABMs)
- NORTH3-ABM-01 to NORTH3-ABM-05 (5 ABMs)
- NORTH4-ABM-01 to NORTH4-ABM-05 (5 ABMs)
- SOUTH1-ABM-01 to SOUTH1-ABM-10 (10 ABMs)
- SOUTH2-ABM-01 to SOUTH2-ABM-07 (7 ABMs)
- SOUTH3-ABM-01 to SOUTH3-ABM-07 (7 ABMs)
- WEST1-ABM-01 to WEST1-ABM-09 (9 ABMs)
- WEST2-ABM-01 to WEST2-ABM-05 (5 ABMs)
- WEST3-ABM-01 to WEST3-ABM-07 (7 ABMs)

**Login Example:**
- Username: `EAST1-ABM-01`
- Password: `Tanishq@amb`

---

### 4. users (Store Logins ONLY)
```sql
-- Existing structure (unchanged)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),      -- Store code (e.g., "PRA", "LKO")
    password VARCHAR(255),
    role VARCHAR(50),           -- Always "Store" now
    name VARCHAR(255),
    email VARCHAR(255)
);
```

**Current Data:** 16 store accounts
- PRA, LKO, YNR, FRD (NORTH stores)
- CRB, BGR, HNR (SOUTH stores)
- CSB, TPT (EAST stores)
- RPT, AUN, JAB (WEST stores)
- TEST, ABH, ADH (Test stores)

**Login Example:**
- Username: `PRA`
- Password: `Tanishq@123`

---

## Relationship with Stores

### stores Table References
The `stores` table has columns that link to managers:
- `abm_username` → Links to `abm_login.abm_user_id`
- `rbm_username` → Links to `rbm_login.rbm_user_id`
- `cee_username` → Links to `cee_login.cee_user_id`
- `region` → Categorizes store location

**Example Store Record:**
```
store_code: PRA
region: NORTH 1
abm_username: NORTH1-ABM-01 (links to abm_login)
rbm_username: NORTH1 (links to rbm_login)
cee_username: NORTH1-CEE-01 (links to cee_login)
```

---

## Auto-Linking New Stores

When you add a new store, you need to:

### Option 1: Manual Assignment
```sql
INSERT INTO stores (
    store_code, region, store_name, store_address, ...,
    abm_username, rbm_username, cee_username
) VALUES (
    'NEW', 'NORTH 1', 'New Store', 'Address...',
    'NORTH1-ABM-01',  -- Assign to ABM
    'NORTH1',         -- Assign to RBM
    'NORTH1-CEE-01'   -- Assign to CEE
);

-- Create store login
INSERT INTO users (username, password, role, name, email)
VALUES ('NEW', 'Tanishq@123', 'Store', 'New Store', 'new@titan.co.in');
```

### Option 2: Trigger for Auto-Creation (Recommended)
Create a database trigger to automatically create store login when store is added:

```sql
DELIMITER $$

CREATE TRIGGER after_store_insert
AFTER INSERT ON stores
FOR EACH ROW
BEGIN
    -- Auto-create user login for new store
    INSERT IGNORE INTO users (username, password, role, name, email)
    VALUES (
        NEW.store_code,
        'Tanishq@123',
        'Store',
        NEW.store_name,
        NEW.store_email_id
    );
END$$

DELIMITER ;
```

---

## Database Queries

### Get All Stores for a Specific RBM
```sql
SELECT s.store_code, s.store_name, s.store_city, s.region
FROM stores s
JOIN rbm_login r ON s.rbm_username = r.rbm_user_id
WHERE r.rbm_user_id = 'NORTH1';
```

### Get All Stores for a Specific CEE
```sql
SELECT s.store_code, s.store_name, s.store_city, s.region
FROM stores s
JOIN cee_login c ON s.cee_username = c.cee_user_id
WHERE c.cee_user_id = 'NORTH1-CEE-01';
```

### Get All Stores for a Specific ABM
```sql
SELECT s.store_code, s.store_name, s.store_city, s.region
FROM stores s
JOIN abm_login a ON s.abm_username = a.abm_user_id
WHERE a.abm_user_id = 'NORTH1-ABM-01';
```

### Get All Managers for a Region
```sql
-- RBM for region
SELECT * FROM rbm_login WHERE rbm_user_id LIKE 'NORTH%';

-- CEEs for region
SELECT * FROM cee_login WHERE region = 'NORTH1';

-- ABMs for region
SELECT * FROM abm_login WHERE region = 'NORTH1';
```

---

## Statistics

| Table | Records | Purpose |
|-------|---------|---------|
| `rbm_login` | 13 | Regional Business Manager logins |
| `cee_login` | 17 | Customer Experience Executive logins |
| `abm_login` | 89 | Area Business Manager logins |
| `users` | 16 | Store logins ONLY |
| `stores` | 15 | Store information with manager assignments |

**Total Accounts:** 135 (13 RBM + 17 CEE + 89 ABM + 16 Stores)

---

## Login Endpoints Required

You'll need to update your Java code to query these new tables:

### RBM Login
```java
// TanishqPageService.java
public Optional<LoginResponseDTO> authenticateRbm(String username, String password) {
    // Query rbm_login table instead of users
    RbmLogin rbm = rbmLoginRepository.findByRbmUserIdAndPassword(username, password);
    if (rbm != null) {
        // Get stores for this RBM
        List<Store> stores = storeRepository.findByRbmUsername(username);
        return Optional.of(new LoginResponseDTO(rbm, stores));
    }
    return Optional.empty();
}
```

### CEE Login
```java
public Optional<LoginResponseDTO> authenticateCee(String username, String password) {
    CeeLogin cee = ceeLoginRepository.findByCeeUserIdAndPassword(username, password);
    if (cee != null) {
        List<Store> stores = storeRepository.findByCeeUsername(username);
        return Optional.of(new LoginResponseDTO(cee, stores));
    }
    return Optional.empty();
}
```

### ABM Login
```java
public Optional<LoginResponseDTO> authenticateAbm(String username, String password) {
    AbmLogin abm = abmLoginRepository.findByAbmUserIdAndPassword(username, password);
    if (abm != null) {
        List<Store> stores = storeRepository.findByAbmUsername(username);
        return Optional.of(new LoginResponseDTO(abm, stores));
    }
    return Optional.empty();
}
```

---

## Migration Summary

✅ **Completed:**
1. Created `rbm_login` table (13 records)
2. Created `cee_login` table (17 records)
3. Created `abm_login` table (89 records)
4. Migrated all manager accounts from `users` table
5. Deleted manager records from `users` table
6. `users` table now contains ONLY store logins (16 records)

✅ **Benefits:**
- Clear separation of concerns
- Easier to manage different login types
- Better performance with targeted queries
- Simpler to add new managers without affecting stores
- Each table has appropriate fields for its role type

---

## Next Steps

1. **Update Java Entities:** Create new entities for RbmLogin, CeeLogin, AbmLogin
2. **Create Repositories:** RbmLoginRepository, CeeLoginRepository, AbmLoginRepository
3. **Update Service Layer:** Modify authentication methods to query new tables
4. **Test Logins:** Verify all manager logins work with new tables
5. **Create Trigger:** Implement auto-creation of store users (optional)

---

**Last Updated:** November 26, 2025
**Database:** tanishq_app (MySQL/MariaDB)

-- ============================================
-- Tanishq Event Management - Database Schema
-- ============================================
-- This script creates all necessary tables for the event management system

USE tanishq;

-- ============================================
-- 1. ABM Login Table
-- ============================================
CREATE TABLE IF NOT EXISTS abm_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    abm_user_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Unique username for ABM',
    abm_name VARCHAR(255) COMMENT 'Full name of ABM',
    password VARCHAR(255) NOT NULL COMMENT 'Password (plain text - consider hashing!)',
    email VARCHAR(255) COMMENT 'Email address',
    region VARCHAR(255) COMMENT 'Region managed by ABM (e.g., North, South, East, West)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_abm_user_id (abm_user_id),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores Area Business Manager login credentials';

-- ============================================
-- 2. RBM Login Table
-- ============================================
CREATE TABLE IF NOT EXISTS rbm_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rbm_user_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Unique username for RBM',
    rbm_name VARCHAR(255) COMMENT 'Full name of RBM',
    password VARCHAR(255) NOT NULL COMMENT 'Password (plain text - consider hashing!)',
    email VARCHAR(255) COMMENT 'Email address',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rbm_user_id (rbm_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores Regional Business Manager login credentials';

-- ============================================
-- 3. CEE Login Table
-- ============================================
CREATE TABLE IF NOT EXISTS cee_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cee_user_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Unique username for CEE',
    cee_name VARCHAR(255) COMMENT 'Full name of CEE',
    password VARCHAR(255) NOT NULL COMMENT 'Password (plain text - consider hashing!)',
    email VARCHAR(255) COMMENT 'Email address',
    region VARCHAR(255) COMMENT 'Region managed by CEE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cee_user_id (cee_user_id),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores Customer Experience Executive login credentials';

-- ============================================
-- 4. Stores Table (if not exists)
-- ============================================
CREATE TABLE IF NOT EXISTS stores (
    store_code VARCHAR(255) PRIMARY KEY,
    store_name VARCHAR(255),
    store_address VARCHAR(500),
    store_city VARCHAR(255),
    store_state VARCHAR(255),
    store_country VARCHAR(255),
    store_zip_code VARCHAR(20),
    store_phone_no_one VARCHAR(20),
    store_phone_no_two VARCHAR(20),
    store_email_id VARCHAR(255),
    store_latitude VARCHAR(50),
    store_longitude VARCHAR(50),
    store_date_of_opening VARCHAR(50),
    store_type VARCHAR(100),
    store_opening_time VARCHAR(20),
    store_closing_time VARCHAR(20),
    store_manager_name VARCHAR(255),
    store_manager_no VARCHAR(20),
    store_manager_email VARCHAR(255),
    store_location_link TEXT,
    languages VARCHAR(500),
    parking VARCHAR(255),
    payment VARCHAR(255),
    kakatiya_store VARCHAR(10),
    celeste_store VARCHAR(10),
    rating VARCHAR(10),
    number_of_ratings VARCHAR(50),
    is_collection VARCHAR(10),

    -- Regional Management Mappings
    region VARCHAR(100) COMMENT 'Region/Zone (e.g., North1, South2)',
    level VARCHAR(50) COMMENT 'Store tier/level',
    abm_username VARCHAR(255) COMMENT 'Mapped ABM username',
    rbm_username VARCHAR(255) COMMENT 'Mapped RBM username',
    cee_username VARCHAR(255) COMMENT 'Mapped CEE username',

    INDEX idx_abm_username (abm_username),
    INDEX idx_rbm_username (rbm_username),
    INDEX idx_cee_username (cee_username),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores information about Tanishq retail locations';

-- ============================================
-- Verification Queries
-- ============================================
-- Run these to verify tables were created:

-- SHOW TABLES;
-- DESCRIBE abm_login;
-- DESCRIBE rbm_login;
-- DESCRIBE cee_login;
-- DESCRIBE stores;

-- Check if any data exists:
-- SELECT COUNT(*) as abm_count FROM abm_login;
-- SELECT COUNT(*) as rbm_count FROM rbm_login;
-- SELECT COUNT(*) as cee_count FROM cee_login;
-- SELECT COUNT(*) as store_count FROM stores;


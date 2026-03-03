-- ============================================
-- Complete Setup Script - Sample Data
-- ============================================
-- This script creates sample ABM, RBM, and CEE logins
-- Run this after creating tables with 01_create_tables.sql
USE tanishq;
-- ============================================
-- 1. Create ABM (Area Business Manager) Logins
-- ============================================
INSERT INTO abm_login (abm_user_id, abm_name, password, email, region) VALUES
('abm.north', 'Rajesh Kumar Singh', 'Welcome@123', 'rajesh.kumar@titan.co.in', 'North'),
('abm.south', 'Priya Lakshmi Sharma', 'Welcome@123', 'priya.sharma@titan.co.in', 'South'),
('abm.east', 'Amit Kumar Patel', 'Welcome@123', 'amit.patel@titan.co.in', 'East'),
('abm.west', 'Sneha Mehta Desai', 'Welcome@123', 'sneha.desai@titan.co.in', 'West'),
('abm.central', 'Vikram Agarwal', 'Welcome@123', 'vikram.agarwal@titan.co.in', 'Central');
-- ============================================
-- 2. Create RBM (Regional Business Manager) Logins
-- ============================================
INSERT INTO rbm_login (rbm_user_id, rbm_name, password, email) VALUES
('rbm.north1', 'Suresh Reddy', 'Welcome@123', 'suresh.reddy@titan.co.in'),
('rbm.north2', 'Kavita Malhotra', 'Welcome@123', 'kavita.malhotra@titan.co.in'),
('rbm.south1', 'Lakshmi Iyer', 'Welcome@123', 'lakshmi.iyer@titan.co.in'),
('rbm.south2', 'Arun Menon', 'Welcome@123', 'arun.menon@titan.co.in'),
('rbm.east1', 'Debashish Sen', 'Welcome@123', 'debashish.sen@titan.co.in'),
('rbm.west1', 'Pooja Shah', 'Welcome@123', 'pooja.shah@titan.co.in');
-- ============================================
-- 3. Create CEE (Customer Experience Executive) Logins
-- ============================================
INSERT INTO cee_login (cee_user_id, cee_name, password, email, region) VALUES
('cee.mumbai', 'Anita Verma', 'Welcome@123', 'anita.verma@titan.co.in', 'West'),
('cee.delhi', 'Rahul Singh', 'Welcome@123', 'rahul.singh@titan.co.in', 'North'),
('cee.bangalore', 'Divya Nair', 'Welcome@123', 'divya.nair@titan.co.in', 'South'),
('cee.kolkata', 'Sanjay Das', 'Welcome@123', 'sanjay.das@titan.co.in', 'East'),
('cee.pune', 'Meera Patil', 'Welcome@123', 'meera.patil@titan.co.in', 'West'),
('cee.chennai', 'Karthik Subramanian', 'Welcome@123', 'karthik.s@titan.co.in', 'South'),
('cee.hyderabad', 'Neha Kapoor', 'Welcome@123', 'neha.kapoor@titan.co.in', 'South');
-- ============================================
-- 4. Verify Data Inserted
-- ============================================
SELECT 'ABM Logins Created:' as Info, COUNT(*) as Count FROM abm_login
UNION ALL
SELECT 'RBM Logins Created:', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'CEE Logins Created:', COUNT(*) FROM cee_login;
-- ============================================
-- 5. Display All Created Logins
-- ============================================
SELECT 'ABM' as UserType, abm_user_id as Username, abm_name as Name, email as Email, region as Region FROM abm_login
UNION ALL
SELECT 'RBM', rbm_user_id, rbm_name, email, 'N/A' FROM rbm_login
UNION ALL
SELECT 'CEE', cee_user_id, cee_name, email, region FROM cee_login
ORDER BY UserType, Username;
-- ============================================
-- NOTES:
-- ============================================
-- Default Password for all users: Welcome@123
-- 
-- IMPORTANT: Change passwords immediately after first login!
--
-- To change password:
-- UPDATE abm_login SET password = 'NewPassword' WHERE abm_user_id = 'username';
-- UPDATE rbm_login SET password = 'NewPassword' WHERE rbm_user_id = 'username';
-- UPDATE cee_login SET password = 'NewPassword' WHERE cee_user_id = 'username';

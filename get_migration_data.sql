-- Get all migration statistics
SELECT 'Stores' as Category, COUNT(*) as Count FROM stores
UNION ALL SELECT 'Users (Store Logins)', COUNT(*) FROM users
UNION ALL SELECT 'RBM Accounts', COUNT(*) FROM rbm_login
UNION ALL SELECT 'CEE Accounts', COUNT(*) FROM cee_login
UNION ALL SELECT 'ABM Accounts', COUNT(*) FROM abm_login
UNION ALL SELECT 'Events', COUNT(*) FROM events
UNION ALL SELECT 'Invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'Attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'Password History', COUNT(*) FROM password_history;

-- Regional distribution
SELECT 'Regional Distribution' as Info, '' as Data;
SELECT region as Region, COUNT(*) as Store_Count FROM stores GROUP BY region ORDER BY region;

-- Event details
SELECT 'Event Summary' as Info, '' as Data;
SELECT COUNT(*) as Total_Events, SUM(invitees) as Total_Invitees, SUM(attendees) as Total_Attendees FROM events;

-- User totals
SELECT 'Total User Accounts' as Info,
(SELECT COUNT(*) FROM rbm_login) + (SELECT COUNT(*) FROM cee_login) + (SELECT COUNT(*) FROM abm_login) + (SELECT COUNT(*) FROM users) as Total_Users;


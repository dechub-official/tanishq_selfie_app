# 🔍 Database Quick Reference Guide

## Quick Access Commands

### Connect to Databases

```bash
# Pre-Production
mysql -h localhost -u root -pDechub#2025 selfie_preprod

# Production
mysql -h 10.10.63.97 -u root -pNagaraj@07 selfie_prod
```

---

## 📊 15 Tables at a Glance

| # | Table Name | Primary Key | Records | Purpose |
|---|------------|-------------|---------|---------|
| 1 | `events` | id (VARCHAR) | Events data | Store events management |
| 2 | `stores` | store_code | ~500 stores | Store master data |
| 3 | `attendees` | id (BIGINT) | Customer data | Event participants |
| 4 | `invitees` | id (BIGINT) | Invitation list | Event invitations |
| 5 | `users` | id (BIGINT) | User accounts | General authentication |
| 6 | `user_details` | id (BIGINT) | Extended info | User additional data |
| 7 | `bride_details` | id (BIGINT) | Bride records | Wedding data |
| 8 | `greetings` | id (BIGINT) | Greeting cards | QR code greetings |
| 9 | `abm_login` | id (BIGINT) | ABM accounts | Area manager login |
| 10 | `rbm_login` | id (BIGINT) | RBM accounts | Regional manager login |
| 11 | `cee_login` | id (BIGINT) | CEE accounts | Executive login |
| 12 | `password_history` | btq_code | Password audit | Password changes |
| 13 | `rivaah` | id (BIGINT) | Rivaah collections | Bridal collections |
| 14 | `rivaah_users` | id (BIGINT) | Rivaah customers | Collection users |
| 15 | `product_details` | id (BIGINT) | Product catalog | Rivaah products |

---

## 🔗 Key Relationships

```
STORES (1) ──→ (Many) EVENTS
EVENTS (1) ──→ (Many) ATTENDEES
EVENTS (1) ──→ (Many) INVITEES
RIVAAH (1) ──→ (Many) PRODUCT_DETAILS
RIVAAH (1) ──→ (Many) RIVAAH_USERS
```

---

## 📝 Useful SQL Queries

### 1. View All Tables
```sql
SHOW TABLES;
```

### 2. Describe Table Structure
```sql
DESCRIBE events;
DESCRIBE stores;
DESCRIBE attendees;
```

### 3. Count Records in Each Table
```sql
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'abm_login', COUNT(*) FROM abm_login
UNION ALL
SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'cee_login', COUNT(*) FROM cee_login;
```

### 4. Latest Events
```sql
SELECT id, event_name, store_code, start_date, created_at 
FROM events 
ORDER BY created_at DESC 
LIMIT 10;
```

### 5. Event with Most Attendees
```sql
SELECT e.id, e.event_name, s.store_name, COUNT(a.id) as attendee_count
FROM events e
LEFT JOIN attendees a ON e.id = a.event_id
LEFT JOIN stores s ON e.store_code = s.store_code
GROUP BY e.id
ORDER BY attendee_count DESC
LIMIT 10;
```

### 6. Store Performance
```sql
SELECT 
    s.store_code,
    s.store_name,
    s.store_city,
    COUNT(e.id) as total_events,
    SUM(e.sale) as total_sales,
    SUM(e.attendees) as total_attendees
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
GROUP BY s.store_code
ORDER BY total_sales DESC;
```

### 7. Region-wise Analysis
```sql
SELECT 
    region,
    COUNT(DISTINCT store_code) as stores_count,
    COUNT(id) as events_count,
    SUM(sale) as total_sales,
    AVG(sale) as avg_sale_per_event
FROM events
GROUP BY region
ORDER BY total_sales DESC;
```

### 8. Recent Attendee Registrations
```sql
SELECT 
    a.name,
    a.phone,
    a.created_at,
    e.event_name,
    s.store_name
FROM attendees a
JOIN events e ON a.event_id = e.id
JOIN stores s ON e.store_code = s.store_code
ORDER BY a.created_at DESC
LIMIT 20;
```

### 9. Events by Type
```sql
SELECT 
    event_type,
    COUNT(*) as event_count,
    SUM(sale) as total_sales,
    AVG(attendees) as avg_attendees
FROM events
GROUP BY event_type
ORDER BY event_count DESC;
```

### 10. All Active Managers
```sql
SELECT 'ABM' as role, abm_user_id as username, abm_name as name, email, region FROM abm_login
UNION ALL
SELECT 'RBM', rbm_user_id, rbm_name, email, NULL FROM rbm_login
UNION ALL
SELECT 'CEE', cee_user_id, cee_name, email, region FROM cee_login;
```

---

## 🔍 Database Health Checks

### Check Table Sizes
```sql
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'selfie_preprod'
ORDER BY (data_length + index_length) DESC;
```

### Check Indexes
```sql
SHOW INDEX FROM events;
SHOW INDEX FROM stores;
SHOW INDEX FROM attendees;
```

### Check Foreign Keys
```sql
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'selfie_preprod'
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## 🛠️ Common Maintenance Tasks

### Export Data
```bash
# Export specific table
mysqldump -u root -p selfie_preprod events > events_backup.sql

# Export entire database
mysqldump -u root -p selfie_preprod > full_backup_$(date +%Y%m%d).sql
```

### Import Data
```bash
mysql -u root -p selfie_preprod < backup.sql
```

### Clear Old Data (Be Careful!)
```sql
-- Delete events older than 1 year
DELETE FROM events WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Delete attendees with no associated event
DELETE FROM attendees WHERE event_id NOT IN (SELECT id FROM events);
```

---

## 📊 Data Integrity Checks

### Check for Orphaned Records
```sql
-- Attendees without events
SELECT COUNT(*) FROM attendees 
WHERE event_id NOT IN (SELECT id FROM events);

-- Events without stores
SELECT COUNT(*) FROM events 
WHERE store_code NOT IN (SELECT store_code FROM stores);
```

### Find Duplicate Phone Numbers
```sql
SELECT phone, COUNT(*) as count
FROM attendees
GROUP BY phone
HAVING count > 1
ORDER BY count DESC;
```

### Check Missing Data
```sql
-- Events without sales data
SELECT COUNT(*) FROM events WHERE sale IS NULL OR sale = 0;

-- Stores without region
SELECT COUNT(*) FROM stores WHERE region IS NULL OR region = '';
```

---

## 🔐 User Management

### Create New User
```sql
INSERT INTO abm_login (abm_user_id, abm_name, password, email, region, created_at)
VALUES ('ABM001', 'John Doe', 'encrypted_password', 'john@example.com', 'North1', NOW());
```

### Update Password
```sql
-- Record old password in history
INSERT INTO password_history (btq_code, old_password, new_password, changed_at)
VALUES ('BTQ123', 'old_hash', 'new_hash', NOW());

-- Update in login table
UPDATE abm_login SET password = 'new_hash', updated_at = NOW() WHERE abm_user_id = 'ABM001';
```

---

## 📈 Business Intelligence Queries

### Monthly Sales Trend
```sql
SELECT 
    DATE_FORMAT(STR_TO_DATE(start_date, '%d-%m-%Y'), '%Y-%m') as month,
    COUNT(*) as events,
    SUM(sale) as total_sales,
    AVG(sale) as avg_sale,
    SUM(attendees) as total_attendees
FROM events
WHERE start_date IS NOT NULL
GROUP BY month
ORDER BY month DESC;
```

### Top Performing Stores
```sql
SELECT 
    s.store_code,
    s.store_name,
    s.store_city,
    s.region,
    COUNT(e.id) as events_conducted,
    SUM(e.attendees) as total_footfall,
    SUM(e.sale) as total_revenue,
    AVG(e.sale) as avg_revenue_per_event
FROM stores s
JOIN events e ON s.store_code = e.store_code
WHERE e.created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
GROUP BY s.store_code
HAVING events_conducted > 0
ORDER BY total_revenue DESC
LIMIT 20;
```

### Conversion Rate (Invitees vs Attendees)
```sql
SELECT 
    e.id,
    e.event_name,
    s.store_name,
    e.invitees,
    e.attendees,
    ROUND((e.attendees * 100.0 / NULLIF(e.invitees, 0)), 2) as conversion_rate
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE e.invitees > 0
ORDER BY conversion_rate DESC;
```

---

## 🎯 Quick Troubleshooting

### Application Can't Connect
```bash
# Check MySQL is running
systemctl status mysqld

# Check port is listening
netstat -tuln | grep 3306

# Test connection
mysql -u root -p -e "SELECT 1;"
```

### Slow Queries
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- Check for missing indexes
EXPLAIN SELECT * FROM attendees WHERE event_id = 'BTQ123_uuid';
```

### Database Size Growing Too Fast
```sql
-- Check largest tables
SELECT 
    table_name,
    table_rows,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'selfie_preprod'
ORDER BY size_mb DESC;
```

---

## 📱 Application Configuration

Database configuration is in:
- `src/main/resources/application-preprod.properties`
- `src/main/resources/application-prod.properties`

Key settings:
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

---

**Quick Reference Version:** 1.0  
**For detailed schema, see:** DATABASE_SCHEMA_DOCUMENTATION.md


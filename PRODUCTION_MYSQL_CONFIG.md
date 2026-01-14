# 🗄️ Production MySQL Configuration Guide

## Server Information

**Production Server**: `10.10.63.97`  
**MySQL Version**: MySQL 8.4.7 Enterprise (Commercial)  
**Status**: ✅ RUNNING

---

## MySQL Configuration Details

### Service Information
- **Service Name**: `mysqld`
- **Status**: RUNNING
- **Auto-start**: Enabled

### Paths
| Purpose | Path |
|---------|------|
| MySQL Binary | `/usr/bin/mysql` |
| MySQL Daemon | `/usr/sbin/mysqld` |
| Data Directory | `/var/lib/mysql` |
| Config File | `/etc/my.cnf` |
| Socket File | `/var/lib/mysql/mysql.sock` |
| Error Log | `/var/log/mysqld.log` |
| Service File | `/usr/lib/systemd/system/mysqld.service` |

### Network Configuration
- **Host**: `localhost` (for application on same server)
- **Port**: `3306`
- **Protocol**: TCP/IP

### Database
- **Database Name**: `selfie_prod`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`

### Authentication
- **User**: `root` (production)
- **Password**: Set and working (not documented here for security)

---

## Spring Boot Configuration

In `application-prod.properties`:

```properties
# MySQL Configuration for Production Server (10.10.63.97)
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=root
spring.datasource.password=YOUR_PRODUCTION_MYSQL_PASSWORD_HERE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

**⚠️ IMPORTANT**: Replace `YOUR_PRODUCTION_MYSQL_PASSWORD_HERE` with actual password before deploying!

---

## Useful MySQL Commands

### Check MySQL Status
```bash
# Service status
systemctl status mysqld

# Check if running
ps aux | grep mysql

# Check port
ss -tlnp | grep 3306
```

### Connect to MySQL
```bash
# Connect as root
mysql -u root -p

# Connect and run command
mysql -u root -p -e "SELECT VERSION();"
```

### Database Operations
```sql
-- Show all databases
SHOW DATABASES;

-- Use production database
USE selfie_prod;

-- Show tables
SHOW TABLES;

-- Check database size
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'selfie_prod'
GROUP BY table_schema;

-- Show data directory
SELECT @@datadir;

-- Show version
SELECT VERSION();
```

### Backup Database
```bash
# Manual backup
mysqldump -u root -p selfie_prod > /opt/tanishq/backups/selfie_prod_backup_$(date +%Y%m%d).sql

# Automated backup (use the provided script)
/opt/tanishq/backup_production_database.sh
```

### Restore Database
```bash
# Restore from backup
mysql -u root -p selfie_prod < /opt/tanishq/backups/selfie_prod_backup_20260113.sql
```

### Performance Monitoring
```sql
-- Show running queries
SHOW PROCESSLIST;

-- Show status
SHOW STATUS;

-- Show variables
SHOW VARIABLES LIKE 'max_connections';
SHOW VARIABLES LIKE 'max_allowed_packet';
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

-- Show table status
SHOW TABLE STATUS FROM selfie_prod;
```

---

## Recommended MySQL Settings for Production

Edit `/etc/my.cnf` and add/modify under `[mysqld]` section:

```ini
[mysqld]
# Connection settings
max_connections=200
connect_timeout=10
wait_timeout=600
max_allowed_packet=100M

# Character set
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# InnoDB settings (adjust based on available RAM)
innodb_buffer_pool_size=2G
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=1
innodb_flush_method=O_DIRECT

# Query cache (if needed)
query_cache_size=0
query_cache_type=OFF

# Logging
log_error=/var/log/mysqld.log
slow_query_log=1
slow_query_log_file=/var/log/mysql-slow.log
long_query_time=2

# Binary logging (for backup/replication)
log_bin=/var/lib/mysql/mysql-bin
binlog_format=ROW
expire_logs_days=7
```

After changes:
```bash
systemctl restart mysqld
```

---

## Security Best Practices

### 1. Create Dedicated Application User (Recommended)

Instead of using root, create a dedicated user:

```sql
-- Create application user
CREATE USER 'tanishq_prod'@'localhost' IDENTIFIED BY 'StrongPassword#2026';

-- Grant privileges
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'tanishq_prod'@'localhost';

-- Verify
SHOW GRANTS FOR 'tanishq_prod'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;
```

Then update `application-prod.properties`:
```properties
spring.datasource.username=tanishq_prod
spring.datasource.password=StrongPassword#2026
```

### 2. Secure MySQL Installation
```bash
mysql_secure_installation
```

### 3. Regular Backups
Set up automated backups:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /opt/tanishq/backup_production_database.sh >> /opt/tanishq/logs/backup.log 2>&1
```

### 4. Monitor Disk Space
```bash
# Check MySQL data directory size
du -sh /var/lib/mysql/

# Check disk usage
df -h /var/lib/mysql
```

---

## Troubleshooting

### MySQL Won't Start
```bash
# Check logs
tail -100 /var/log/mysqld.log

# Check if port is in use
ss -tlnp | grep 3306

# Check service status
systemctl status mysqld

# Try to start
systemctl start mysqld
```

### Connection Refused
```bash
# Check if MySQL is listening
ss -tlnp | grep 3306

# Check firewall
firewall-cmd --list-ports

# Test connection
mysql -u root -p -e "SELECT 1;"
```

### Database Too Large
```sql
-- Check table sizes
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'selfie_prod'
ORDER BY (data_length + index_length) DESC;

-- Optimize tables
OPTIMIZE TABLE table_name;

-- Clean up old data (if applicable)
DELETE FROM selfie_images WHERE created_date < DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

### Performance Issues
```sql
-- Check slow queries
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;

-- Show processlist
SHOW FULL PROCESSLIST;

-- Kill long-running query
KILL <process_id>;
```

---

## Monitoring Checklist

Daily checks:
- [ ] MySQL service is running
- [ ] Disk space available (> 20% free)
- [ ] No connection errors in logs
- [ ] Database backup completed

Weekly checks:
- [ ] Review slow query log
- [ ] Check database size growth
- [ ] Verify backup restoration works
- [ ] Review error logs

Monthly checks:
- [ ] Optimize tables
- [ ] Review and clean old backups
- [ ] Check MySQL updates available
- [ ] Performance tuning if needed

---

## Quick Reference Commands

```bash
# Service management
systemctl start mysqld
systemctl stop mysqld
systemctl restart mysqld
systemctl status mysqld

# Connect to MySQL
mysql -u root -p

# Backup
mysqldump -u root -p selfie_prod > backup.sql

# Restore
mysql -u root -p selfie_prod < backup.sql

# Check logs
tail -f /var/log/mysqld.log

# Monitor disk space
df -h /var/lib/mysql
du -sh /var/lib/mysql/selfie_prod/

# Check port
ss -tlnp | grep 3306
```

---

## Important Notes

1. **Data Directory**: All databases stored in `/var/lib/mysql`
2. **Backups**: Store backups in `/opt/tanishq/backups`
3. **Logs**: Error logs in `/var/log/mysqld.log`
4. **Version**: MySQL 8.4.7 Enterprise - check compatibility before upgrades
5. **Character Set**: Always use utf8mb4 for full Unicode support

---

**Last Updated**: January 13, 2026  
**MySQL Version**: 8.4.7 Enterprise  
**Production Server**: 10.10.63.97


# 📚 Database Documentation Index

Complete database documentation for the Tanishq Selfie App

---

## 📖 Available Documentation

### 1. **DATABASE_SCHEMA_DOCUMENTATION.md** ⭐ MAIN REFERENCE
**Complete Database Schema Documentation**

Contains:
- Overview of all 15 tables
- Detailed column descriptions for each table
- Data types, constraints, and indexes
- Entity Relationship Diagrams (ERD)
- Foreign key relationships
- Sample SQL queries
- Database connection strings
- Backup and maintenance commands

**When to use:** When you need detailed information about any table, column, or relationship.

---

### 2. **DATABASE_QUICK_REFERENCE.md** 🚀 QUICK START
**Quick Reference Guide with SQL Queries**

Contains:
- Common SQL queries (ready to copy-paste)
- Database health checks
- Performance monitoring queries
- Data integrity checks
- Maintenance tasks
- Troubleshooting tips
- Business intelligence queries

**When to use:** When you need to quickly query the database or troubleshoot issues.

---

### 3. **DATABASE_VISUAL_SCHEMA.md** 🎨 VISUAL GUIDE
**Visual Database Diagrams**

Contains:
- ASCII art ERD diagrams
- Visual table structures
- Data flow diagrams
- User journey flows
- Relationship visualizations
- Index strategies
- Storage considerations

**When to use:** When you want to understand the database structure visually or explain it to others.

---

## 🗄️ Your Database at a Glance

### Database Information
- **Type:** MySQL 8.x
- **ORM:** Hibernate JPA (Spring Boot)
- **Total Tables:** 15
- **Environments:** 
  - Local: `tanishq`
  - Pre-Prod: `selfie_preprod`
  - Production: `selfie_prod`

### Table Breakdown

#### 📁 Core Event Management (5 tables)
1. **events** - Main event records with sales and metrics
2. **stores** - Store master data (~500 stores)
3. **attendees** - Event participants/customers
4. **invitees** - Event invitation lists
5. **greetings** - QR code greeting cards

#### 👥 User Management (4 tables)
6. **users** - General user accounts
7. **user_details** - Extended user information
8. **bride_details** - Bride-specific data for weddings
9. **password_history** - Password change audit trail

#### 🔐 Authentication (3 tables)
10. **abm_login** - Area Business Manager authentication
11. **rbm_login** - Regional Business Manager authentication
12. **cee_login** - Customer Engagement Executive authentication

#### 💍 Product/Rivaah System (3 tables)
13. **rivaah** - Rivaah bridal collection data
14. **rivaah_users** - Rivaah system users
15. **product_details** - Product catalog for Rivaah

---

## 🔗 Key Relationships

```
STORES → EVENTS → ATTENDEES
              ↓
         INVITEES

RIVAAH → PRODUCT_DETAILS
      ↓
   RIVAAH_USERS
```

---

## 🚀 Quick Start

### Connect to Database

**Pre-Production:**
```bash
mysql -h localhost -u root -pDechub#2025 selfie_preprod
```

**Production:**
```bash
mysql -h 10.10.63.97 -u root -pNagaraj@07 selfie_prod
```

### View All Tables
```sql
SHOW TABLES;
```

### View Table Structure
```sql
DESCRIBE events;
DESCRIBE stores;
```

### Count Records
```sql
SELECT COUNT(*) FROM events;
SELECT COUNT(*) FROM attendees;
```

---

## 📊 Common Use Cases

### 1. View Recent Events
```sql
SELECT * FROM events ORDER BY created_at DESC LIMIT 10;
```
👉 **See more queries in:** DATABASE_QUICK_REFERENCE.md

### 2. Understand Table Structure
👉 **See full schema in:** DATABASE_SCHEMA_DOCUMENTATION.md

### 3. See Visual Diagrams
👉 **See diagrams in:** DATABASE_VISUAL_SCHEMA.md

---

## 🔍 How to Find Information

### "I need to know what columns are in the EVENTS table"
→ Open **DATABASE_SCHEMA_DOCUMENTATION.md** → Section: "EVENTS Table"

### "I want to query all attendees for an event"
→ Open **DATABASE_QUICK_REFERENCE.md** → Section: "Useful SQL Queries"

### "I want to see how tables are related"
→ Open **DATABASE_VISUAL_SCHEMA.md** → Section: "Entity Relationship Diagram"

### "I need to backup the database"
→ Open **DATABASE_SCHEMA_DOCUMENTATION.md** → Section: "Database Maintenance"

### "I want to check database performance"
→ Open **DATABASE_QUICK_REFERENCE.md** → Section: "Database Health Checks"

---

## 📁 Application Configuration

Database settings are configured in:
- `src/main/resources/application-local.properties`
- `src/main/resources/application-preprod.properties`
- `src/main/resources/application-prod.properties`

Entity classes (JPA) are located in:
- `src/main/java/com/dechub/tanishq/entity/`

---

## 🛠️ Tools & Access

### MySQL Command Line
```bash
# Pre-prod
mysql -u root -p selfie_preprod

# Production
mysql -u root -p selfie_prod
```

### JDBC Connection String
```
jdbc:mysql://localhost:3306/selfie_preprod
```

### MySQL Workbench
- Host: localhost (or 10.10.63.97 for prod)
- Port: 3306
- Database: selfie_preprod / selfie_prod

---

## 🎯 Documentation Structure

```
📚 Database Documentation
├── 📄 DATABASE_SCHEMA_DOCUMENTATION.md
│   ├── All 15 tables detailed
│   ├── Column definitions
│   ├── Relationships
│   ├── ERD diagrams
│   └── SQL examples
│
├── 📄 DATABASE_QUICK_REFERENCE.md
│   ├── SQL query library
│   ├── Maintenance commands
│   ├── Health checks
│   └── Troubleshooting
│
├── 📄 DATABASE_VISUAL_SCHEMA.md
│   ├── ASCII ERD diagrams
│   ├── Data flow charts
│   └── Visual guides
│
└── 📄 DATABASE_INDEX.md (This file)
    └── Navigation guide
```

---

## 💡 Tips

1. **Start with DATABASE_VISUAL_SCHEMA.md** if you're new to the database
2. **Use DATABASE_QUICK_REFERENCE.md** for day-to-day queries
3. **Refer to DATABASE_SCHEMA_DOCUMENTATION.md** for detailed specifications
4. **Bookmark this index** for quick navigation

---

## 📞 Need Help?

### For Schema Questions
- Check: DATABASE_SCHEMA_DOCUMENTATION.md
- Look for: Table definitions, column types, relationships

### For Query Examples
- Check: DATABASE_QUICK_REFERENCE.md
- Look for: SQL examples, common queries

### For Visual Understanding
- Check: DATABASE_VISUAL_SCHEMA.md
- Look for: Diagrams, flow charts

---

## 🔄 Keeping Documentation Updated

This documentation is based on JPA entities in the codebase. When entities change:
1. Entity classes are in: `src/main/java/com/dechub/tanishq/entity/`
2. Hibernate auto-generates tables from these entities
3. Documentation should be updated to reflect entity changes

---

## ✅ Quick Checklist

Before working with the database:

- [ ] Know which environment (local/preprod/prod)
- [ ] Have correct credentials
- [ ] Understand table relationships
- [ ] Have backup plan if modifying data
- [ ] Test queries on preprod first
- [ ] Check documentation for examples

---

**Documentation Version:** 1.0  
**Last Updated:** January 24, 2026  
**Database Version:** MySQL 8.x  
**Application:** Tanishq Selfie App (Spring Boot 2.7.18)

---

**Happy Querying! 🚀**


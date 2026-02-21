# Tanishq Selfie App

A Spring Boot application for managing Tanishq store events and selfie uploads.

## 📋 Project Overview

This is a Java Spring Boot web application that allows:
- Store managers to create and manage events
- Users to upload selfies at events
- Track event attendance and engagement
- Store-wise event management and reporting

## 🚀 Quick Start

### Prerequisites
- Java 11
- Maven 3.6+
- MySQL 8.0+
- SSH access to production server (10.10.63.97)

### Build Project
```bash
mvn clean package -P production
```

### Deploy to Production
```bash
# Upload WAR file
scp target/tanishq-preprod-21-01-2026-1-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/

# SSH to server
ssh root@10.10.63.97

# Run deployment script
cd /opt/tanishq
./deploy_production.sh
```

## 📚 Documentation

### Deployment Documentation
**[DEPLOYMENT_README.md](DEPLOYMENT_README.md)** - Complete deployment guide
- Server configuration details
- Database setup instructions
- Deployment procedures
- Troubleshooting guide
- CSV data import steps
- Quick command reference

### Database Documentation
**[DATABASE_SCHEMA_DOCUMENTATION.md](DATABASE_SCHEMA_DOCUMENTATION.md)** - Complete database schema
- All 15 tables with columns and data types
- Table relationships and foreign keys
- Entity relationship diagrams
- SQL queries and examples

**[DATABASE_QUICK_REFERENCE.md](DATABASE_QUICK_REFERENCE.md)** - Quick reference guide
- Common SQL queries
- Database maintenance commands
- Troubleshooting tips
- Connection strings

**[DATABASE_VISUAL_SCHEMA.md](DATABASE_VISUAL_SCHEMA.md)** - Visual database diagrams
- ASCII art ERD diagrams
- Data flow diagrams
- Table relationships visualization

## 🗂️ Project Structure

```
tanishq_selfie_app/
├── src/main/
│   ├── java/          # Java source code
│   └── resources/     # Configuration files
├── target/            # Build output
├── storage/           # Uploaded files
├── database_backup/   # Database backups
├── pom.xml           # Maven configuration
└── README.md         # This file
```

## 🛠️ Technology Stack

- **Backend:** Spring Boot 2.7.18
- **Database:** MySQL 8.4.7
- **Build Tool:** Maven
- **Java Version:** 11

## 📊 Database

- **Production DB:** selfie_prod
- **Pre-Prod DB:** selfie_preprod
- **Host:** localhost (10.10.63.97)
- **Port:** 3306
- **Total Tables:** 15

### Table Categories
- **Core Event Management:** 5 tables (events, stores, attendees, invitees, greetings)
- **User Management:** 4 tables (users, user_details, bride_details, password_history)
- **Authentication:** 3 tables (abm_login, rbm_login, cee_login)
- **Product/Rivaah:** 3 tables (rivaah, rivaah_users, product_details)

For complete database schema, see [DATABASE_SCHEMA_DOCUMENTATION.md](DATABASE_SCHEMA_DOCUMENTATION.md)

## 🔐 Security

- Database credentials stored in `application-production.properties`
- File upload restrictions configured
- CORS enabled for allowed origins
- SQL injection protection via JPA

## 📝 Notes

- Project cleaned up on January 24, 2026
- All unnecessary documentation files removed
- Only essential deployment files retained
- See DELETED_FILES_LIST.txt for list of removed files

## 📞 Support

Check logs at: `/opt/tanishq/logs/application.log`

---

**Last Updated:** January 24, 2026


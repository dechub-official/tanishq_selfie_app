# 📚 Greeting Module - Documentation Index

## 🎯 Quick Navigation

Choose the document that fits your needs:

### 🚀 **Start Here** (New to the module)
→ **[GREETING_MODULE_QUICK_REFERENCE.md](./GREETING_MODULE_QUICK_REFERENCE.md)**
- One-page overview
- Quick facts
- API endpoints
- Test commands

### 📖 **Complete Guide** (Need full details)
→ **[GREETING_MODULE_DOCUMENTATION.md](./GREETING_MODULE_DOCUMENTATION.md)**
- Full architecture
- All layers explained
- Data flow diagrams
- Configuration details
- Comparison with Events module

### 💾 **Database Reference** (SQL & Schema)
→ **[GREETING_DATABASE_SCHEMA.sql](./GREETING_DATABASE_SCHEMA.sql)**
- Table structure
- Sample queries
- Maintenance scripts
- Migration notes

### 🔄 **Migration Guide** (Upgrading from Google Sheets)
→ **[GREETING_MIGRATION_GUIDE.md](./GREETING_MIGRATION_GUIDE.md)**
- Old vs New comparison
- Why MySQL is better
- Step-by-step migration
- Data transformation
- Testing checklist

---

## 📋 What is the Greeting Module?

A **standalone mini-project** for video greeting cards:
- Users generate unique QR codes
- Upload video messages with personal greetings
- Recipients view videos via QR code scan

**Key Facts:**
- ✅ **Database:** MySQL (table: `greetings`)
- ✅ **Storage:** AWS S3 (prod) or Local (dev)
- ✅ **Technology:** Spring Boot + JPA + Hibernate
- ✅ **Separate:** Independent from Events module
- ✅ **Auto-Setup:** Table auto-created by Hibernate

---

## 🗂️ File Structure

```
tanishq_selfie_app/
├── GREETING_MODULE_QUICK_REFERENCE.md    ← Start here (1-page)
├── GREETING_MODULE_DOCUMENTATION.md      ← Full technical guide
├── GREETING_DATABASE_SCHEMA.sql          ← SQL reference
├── GREETING_MIGRATION_GUIDE.md           ← Upgrade guide
└── GREETING_INDEX.md                     ← This file

src/main/java/com/dechub/tanishq/
├── entity/
│   └── Greeting.java                     ← JPA Entity (database model)
├── repository/
│   └── GreetingRepository.java           ← Database queries (JPA)
├── service/
│   ├── GreetingService.java              ← Business logic
│   └── storage/
│       ├── StorageService.java           ← Interface
│       ├── LocalFileStorageService.java  ← Local storage (@Profile("local"))
│       └── AwsS3StorageService.java      ← S3 storage (@Profile({"preprod","prod"}))
└── controller/
    └── GreetingController.java           ← REST API endpoints

src/main/resources/
├── application-local.properties          ← Local dev config
├── application-preprod.properties        ← Preprod config
└── application-prod.properties           ← Production config
```

---

## 🎓 Learning Path

### Beginner → Start Here
1. Read **Quick Reference** (5 minutes)
2. Test API endpoints with curl/Postman
3. Check database table with SQL

### Intermediate → Go Deeper
1. Read **Full Documentation** (15 minutes)
2. Understand data flow diagrams
3. Review entity/repository/service code
4. Configure local environment

### Advanced → Migration & Optimization
1. Read **Migration Guide** (10 minutes)
2. Export data from Google Sheets (if applicable)
3. Optimize database queries
4. Configure AWS S3 properly

---

## 🔍 Quick Answers

### How does it store data?
**MySQL database** (`greetings` table)
- Auto-created by Hibernate JPA
- No manual SQL needed

### Where are videos stored?
**Profile-based:**
- Local dev: `./storage/greetings/{id}/`
- Preprod/Prod: AWS S3 `s3://bucket/greetings/{id}/`

### Is it related to Events module?
**No** - Completely separate:
- Own table: `greetings`
- Own API endpoints: `/greetings/*`
- Own storage: `greetings/` folder

### How does the database work?
**Spring Data JPA + Hibernate:**
```java
@Entity
public class Greeting { ... }  // Entity maps to table

public interface GreetingRepository extends JpaRepository<Greeting, Long> {
    Optional<Greeting> findByUniqueId(String uniqueId);
}

// Hibernate auto-creates table on app startup
// spring.jpa.hibernate.ddl-auto=update
```

### Does it use Excel sheets?
**No** (unlike Events module):
- Events module: Excel → MySQL
- Greeting module: Direct MySQL (no Excel)

### How do I verify it's working?
```bash
# 1. Check table exists
mysql -u root -p
USE selfie_preprod;
SHOW TABLES LIKE 'greetings';

# 2. Test API
curl -X POST http://localhost:3000/greetings/generate

# 3. Check logs
tail -f logs/application.log | grep Greeting
```

---

## 📊 Database Quick View

### Table: `greetings`
```sql
CREATE TABLE greetings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unique_id VARCHAR(255),              -- "GREETING_1738318234567"
    greeting_text VARCHAR(255),          -- Sender name
    message TEXT,                        -- Personal message
    qr_code_data LONGTEXT,               -- Base64 QR PNG
    drive_file_id VARCHAR(255),          -- Video URL (S3/local)
    created_at DATETIME,                 -- Creation time
    uploaded BOOLEAN DEFAULT 0           -- Upload status
);
```

### Quick Queries
```sql
-- View all greetings
SELECT unique_id, greeting_text, uploaded, created_at 
FROM greetings ORDER BY created_at DESC LIMIT 10;

-- Count by status
SELECT 
    CASE WHEN uploaded THEN 'Uploaded' ELSE 'Pending' END as status,
    COUNT(*) as count
FROM greetings GROUP BY uploaded;
```

---

## 🌐 API Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/greetings/generate` | Create new greeting |
| GET | `/greetings/{id}/qr` | Get QR code (PNG) |
| POST | `/greetings/{id}/upload` | Upload video + metadata |
| GET | `/greetings/{id}/view` | Get greeting info (JSON) |
| GET | `/greetings/{id}/status` | Check upload status |
| DELETE | `/greetings/{id}` | Delete greeting |

**Base URL:**
- Local: `http://localhost:3000`
- Preprod: `https://celebrationsite-preprod.tanishq.co.in`

---

## ⚙️ Configuration

### application-local.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tanishq
local.storage.base.path=./storage
greeting.qr.base.url=http://localhost:3000/greetings/
```

### application-preprod.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
aws.s3.bucket.name={bucket}
aws.s3.region={region}
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/
```

---

## 🧪 Testing Workflow

```bash
# 1. Generate greeting
curl -X POST http://localhost:3000/greetings/generate
# Response: GREETING_1738318234567

# 2. Get QR code
curl http://localhost:3000/greetings/GREETING_1738318234567/qr -o qr.png

# 3. Upload video (use Postman)
POST http://localhost:3000/greetings/GREETING_1738318234567/upload
Body: multipart/form-data
  - video: [file]
  - name: "John Doe"
  - message: "Happy Birthday!"

# 4. View greeting
curl http://localhost:3000/greetings/GREETING_1738318234567/view

# 5. Check in database
mysql -u root -p
USE tanishq;
SELECT * FROM greetings WHERE unique_id = 'GREETING_1738318234567';
```

---

## 🔧 Troubleshooting

### Table not created?
- Check: `spring.jpa.hibernate.ddl-auto=update`
- Verify: Database connection in logs
- Manual: Run `GREETING_DATABASE_SCHEMA.sql`

### Videos not uploading?
- Local: Check `./storage/greetings/` exists
- S3: Verify IAM role permissions
- Logs: Check for exceptions

### QR codes not working?
- Verify: `greeting.qr.base.url` matches your domain
- Test: Scan QR with phone camera
- Check: URL format in database

---

## 📦 Dependencies

```xml
<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>

<!-- AWS S3 (preprod/prod) -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
</dependency>

<!-- QR Code -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
</dependency>
```

---

## 🎯 Summary

**Your Greeting Module:**
- ✅ MySQL database (not Excel/Sheets)
- ✅ S3 or local storage (profile-based)
- ✅ Completely separate from Events
- ✅ Auto-schema creation (JPA)
- ✅ REST API for all operations
- ✅ Production-ready

**Key Insight:**
It was working fine because everything is automated:
- Hibernate creates the table
- JPA handles database operations
- Storage service abstracts S3/local
- No manual setup required!

---

## 📞 Support

Need help? Check the documentation files:

1. **Quick Questions** → Quick Reference
2. **Technical Details** → Full Documentation
3. **Database Issues** → Database Schema
4. **Migration** → Migration Guide

---

## 🗃️ Version History

- **v2.0** (Current Branch) - MySQL + JPA + S3
- **v1.0** (Main Branch) - Google Sheets + Drive

**Recommendation:** Your current implementation (v2.0) is superior! ✅

---

Generated: January 31, 2026
Last Updated: January 31, 2026

**Next Steps:**
1. Read Quick Reference (5 min)
2. Test endpoints (10 min)
3. Check database (5 min)
4. Push to main when ready! 🚀


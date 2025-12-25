# 📊 TANISHQ CELEBRATION APP - QUICK ANALYSIS SUMMARY

**Analysis Date:** December 20, 2025

---

## 🎯 WHAT IS THIS PROJECT?

A **multi-store event management system** for Tanishq jewelry stores across India that enables:
- Store managers to create celebration events
- Generate QR codes for customer registration
- Collect attendee information and photos
- Send video greeting cards to customers
- Track event metrics and analytics

---

## 🏗️ ARCHITECTURE AT A GLANCE

```
┌──────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                       │
│  - Events Dashboard  - QR Scanner Pages  - Forms             │
│  Location: src/main/resources/static/                        │
│  ⚠️ SOURCE CODE NOT IN THIS REPO (Compiled only)            │
└────────────────────┬─────────────────────────────────────────┘
                     │ REST APIs (JSON)
┌────────────────────▼─────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                         │
│                                                               │
│  Controllers:                                                 │
│    • EventsController      → Event management APIs           │
│    • GreetingController    → Video greeting cards           │
│    • TanishqPageController → Selfie & bride features        │
│                                                               │
│  Services:                                                    │
│    • TanishqPageService     → Core business logic           │
│    • GreetingService        → Greeting cards                │
│    • S3Service              → AWS file storage              │
│    • EventQrCodeService     → QR code generation            │
│                                                               │
│  Data Access (JPA Repositories):                             │
│    • 15 Entity classes                                       │
│    • 15 Repository interfaces                                │
│    • MySQL database (selfie_preprod)                        │
└───────────┬─────────────────────────────┬───────────────────┘
            │                             │
            ▼                             ▼
    ┌───────────────┐           ┌─────────────────┐
    │  MySQL 8.0    │           │   AWS S3        │
    │  (localhost)  │           │  (Event Images) │
    │               │           │  (Videos)       │
    └───────────────┘           └─────────────────┘
```

---

## 📦 TECH STACK

### Backend
- **Framework:** Spring Boot 2.7.18
- **Language:** Java 11
- **Database:** MySQL 8.0
- **ORM:** Hibernate/JPA
- **Security:** Spring Security 5.7.12
- **Server:** Tomcat 9.0.98

### Key Libraries
- AWS SDK S3 (1.12.529) - File storage
- ZXing (3.5.1) - QR code generation
- Apache POI (5.2.3) - Excel processing
- Lombok (1.18.30) - Code generation
- OpenCSV (5.7.1) - CSV export

### Frontend
- React (compiled - source not in repo)
- Vite (build tool)
- Location: `src/main/resources/static/`

### Cloud & Infrastructure
- AWS S3 - File storage
- AWS EC2 - Application server
- Office365 SMTP - Email notifications
- IAM Roles - Security

---

## 🗂️ PROJECT STRUCTURE

```
tanishq_selfie_app/
├── src/main/
│   ├── java/com/dechub/tanishq/
│   │   ├── TanishqSelfieApplication.java    # Main entry point
│   │   ├── config/           # 9 config classes
│   │   ├── controller/       # 3 REST controllers
│   │   ├── dto/              # 25+ DTOs
│   │   ├── entity/           # 15 database entities
│   │   ├── repository/       # 15 JPA repositories
│   │   ├── service/          # 7+ service classes
│   │   ├── mail/             # Email service
│   │   └── util/             # Utilities
│   └── resources/
│       ├── application.properties            # Main config
│       ├── application-preprod.properties    # Preprod config
│       ├── application-prod.properties       # Prod config
│       ├── application-uat.properties        # UAT config
│       ├── application-test.properties       # Test config
│       └── static/           # React frontend (compiled)
├── pom.xml                   # Maven dependencies
├── target/                   # Build output
└── [200+ markdown docs]      # Extensive documentation
```

---

## 🎯 CORE FEATURES

### 1️⃣ Event Management
- Create events (weddings, engagements, etc.)
- Upload invitee lists (Excel)
- Generate unique event IDs
- Track invitees vs attendees

### 2️⃣ QR Code System
- **Event QR Codes** → Customer registration form
- **Greeting QR Codes** → Video message recording
- Download as PNG (Base64 encoded)
- Mobile-friendly landing pages

### 3️⃣ Customer Registration
- Scan QR code → Fill form
- Upload selfie/photo
- Record preferences
- First-time visitor tracking

### 4️⃣ Video Greeting Cards
- Generate unique greeting links
- QR code for video recording
- Upload videos up to 100MB
- Store in AWS S3

### 5️⃣ Multi-Store Support
- 100+ stores across India
- Regional hierarchy (North, South, East, West)
- Manager roles (ABM, RBM, CEE)
- Store-specific dashboards

### 6️⃣ Reporting & Analytics
- Event completion tracking
- Attendee reports (CSV export)
- Store-wise summaries
- Date range filtering

---

## 🗄️ DATABASE SCHEMA

### Main Tables (15 total)

```sql
events              # Event master data
  ├── attendees     # Registered customers
  └── invitees      # Pre-event invitations

stores              # Store master data
  └── events        # One-to-many relationship

greetings           # Video greeting cards

users               # Customer profiles
  └── user_details  # Detailed customer info

bride_details       # Wedding customer data

rivaah              # Wedding jewelry collection
  └── rivaah_user   # Collection preferences

abm_login           # Area manager logins
rbm_login           # Regional manager logins
cee_login           # Store executive logins

password_history    # Password tracking
```

---

## 🔌 KEY API ENDPOINTS

### Events APIs
```
POST   /events/login               # Store manager login
POST   /events/upload              # Create event
POST   /events/attendees           # Register attendee
GET    /events/dowload-qr/{id}     # Download QR code
POST   /events/getevents           # Get completed events
GET    /events/customer/{eventId}  # Attendee form page
```

### Greeting APIs
```
POST   /greetings/generate         # Create greeting
GET    /greetings/{id}/qr          # Get QR code
POST   /greetings/{id}/upload      # Upload video
GET    /greetings/{id}/view        # View greeting
GET    /greetings/{id}/status      # Check status
```

### Selfie APIs
```
POST   /tanishq/selfie/save        # Save customer details
POST   /tanishq/selfie/upload      # Upload selfie
POST   /tanishq/selfie/brideImage  # Upload bride photo
```

---

## ⚙️ ENVIRONMENTS

| Environment | Domain | Database | Status |
|-------------|--------|----------|--------|
| **Preprod** | celebrationsite-preprod.tanishq.co.in | selfie_preprod | ✅ Active |
| **UAT** | [TBD] | selfie_uat | ⚠️ Configured |
| **Production** | celebrationsite.tanishq.co.in (likely) | selfie_production | ⚠️ Ready |
| **Test/Local** | localhost:3000 | selfie_test | ✅ Dev |

### Build Commands
```bash
# Preprod
mvn clean package -Ppreprod

# Production
mvn clean package -Pprod

# UAT
mvn clean package -Puat
```

---

## 📁 KEY FILES TO KNOW

### Backend Core
| File | Lines | Purpose |
|------|-------|---------|
| `TanishqSelfieApplication.java` | 17 | Application entry point |
| `TanishqPageService.java` | 1268 | Core business logic (large!) |
| `EventsController.java` | 793 | Event management APIs |
| `GreetingController.java` | 211 | Greeting card APIs |
| `GreetingService.java` | 314 | Greeting business logic |
| `S3Service.java` | 192 | AWS S3 integration |

### Configuration
| File | Purpose |
|------|---------|
| `pom.xml` | Dependencies & build config |
| `application-preprod.properties` | Preprod settings |
| `SecurityConfig.java` | Security rules |
| `WebConfig.java` | MVC configuration |

### Database Entities
| File | Table | Purpose |
|------|-------|---------|
| `Event.java` | events | Event master |
| `Attendee.java` | attendees | Customer registration |
| `Greeting.java` | greetings | Video greetings |
| `Store.java` | stores | Store master |
| `User.java` | users | Customer profiles |

---

## 🚀 HOW TO BUILD & DEPLOY

### 1. Build for Preprod
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod
```

### 2. Output
```
target/tanishq-preprod-[date]-0.0.1-SNAPSHOT.war
```

### 3. Deploy to Server
```bash
# Copy WAR to server
scp target/*.war user@10.160.128.94:/opt/tanishq/applications_preprod/

# SSH to server
ssh user@10.160.128.94

# Stop app
./stop-app.sh

# Start app
./start-app.sh
```

### 4. Verify
```bash
# Check if running
curl http://localhost:3000/events

# Check logs
tail -f /opt/tanishq/logs/application.log
```

---

## ⚠️ KNOWN ISSUES

### 🔴 Critical Issues

#### 1. Frontend URL Redirection (PENDING)
- **Problem:** "Create Event" button redirects to production
- **Cause:** React app compiled with production URLs
- **Fix:** Need React source code to rebuild with preprod config
- **Workaround:** Manually type preprod URL

#### 2. React Source Code Missing
- **Problem:** Only compiled JavaScript in `static/` folder
- **Impact:** Cannot modify frontend behavior
- **Need:** Original React project with `package.json` and `src/` folder

### ✅ Fixed Issues

#### 1. QR Code Internal IP (FIXED)
- **Was:** QR codes had `http://10.160.128.94:3000/...`
- **Now:** `https://celebrationsite-preprod.tanishq.co.in/...`
- **Status:** Fixed in config, needs rebuild + redeploy

---

## 📊 PROJECT STATISTICS

### Code Metrics
- **Total Java Files:** 90+
- **Total Lines of Code:** ~15,000+
- **Entities:** 15 database tables
- **Controllers:** 3 REST controllers
- **Services:** 7+ service classes
- **API Endpoints:** 30+ REST APIs
- **Dependencies:** 30+ Maven dependencies

### Documentation
- **Markdown Files:** 200+ (!!)
- **Covers:** Deployment, database, fixes, issues, testing
- **Categories:**
  - Deployment guides: 20+
  - Database migration: 15+
  - QR code fixes: 25+
  - Frontend issues: 10+
  - Testing: 8+

### Complexity Assessment
- **Backend Complexity:** Medium-High
- **Database Schema:** Medium
- **API Design:** RESTful, well-structured
- **Security:** Basic Spring Security
- **Cloud Integration:** AWS S3 (proper IAM roles)

---

## 🎓 FOR NEW DEVELOPERS

### Understanding the Flow

#### Creating an Event
```
1. Manager logs in → /events/login
2. Fills event form → /events/upload
3. Uploads invitee Excel (optional)
4. System creates Event ID: "STORECODE_UUID"
5. Generates QR code → /events/dowload-qr/{id}
6. Manager prints/shares QR code
```

#### Customer Registration
```
1. Customer scans QR code
2. Redirected to: /events/customer/{eventId}
3. Fills form (name, phone, preferences)
4. Uploads selfie photo
5. Data saved to attendees table
6. Photo uploaded to S3
7. Shows success message
```

#### Video Greeting
```
1. Generate greeting → /greetings/generate
2. Get QR code → /greetings/{id}/qr
3. Customer scans → /greetings/{id}/upload
4. Records video (phone camera)
5. Uploads to S3 (max 100MB)
6. Recipient views → /greetings/{id}/view
```

### Key Concepts

#### Entity Relationships
```java
Store (1) ─── (many) Event
Event (1) ─── (many) Attendee
Event (1) ─── (many) Invitee
```

#### Service Layer Pattern
```java
Controller → Service → Repository → Database

EventsController
  ↓ calls
TanishqPageService
  ↓ uses
EventRepository, AttendeeRepository
  ↓ queries
MySQL Database
```

#### Configuration Pattern
```properties
# Different file per environment
application-preprod.properties  → Preprod settings
application-prod.properties     → Production settings
application-uat.properties      → UAT settings

# Spring activates based on profile
mvn package -Ppreprod → Uses preprod config
```

---

## 🔍 QUICK REFERENCE

### Common Tasks

| Task | Command |
|------|---------|
| **Build preprod** | `mvn clean package -Ppreprod` |
| **Build production** | `mvn clean package -Pprod` |
| **Run locally** | `mvn spring-boot:run` |
| **Check errors** | Check IntelliJ Problems tab |
| **View logs** | `tail -f logs/application.log` |
| **Test API** | Use Postman collection |

### Important URLs (Preprod)
| URL | Purpose |
|-----|---------|
| http://10.160.128.94:3000 | Direct server access |
| https://celebrationsite-preprod.tanishq.co.in | Public domain |
| /events | Events dashboard |
| /events/customer/{id} | Attendee form |
| /greetings/{id}/view | View greeting |

### Configuration Files
| File | Location |
|------|----------|
| Main config | `src/main/resources/application.properties` |
| Preprod config | `src/main/resources/application-preprod.properties` |
| Dependencies | `pom.xml` |
| Security | `src/main/java/.../config/SecurityConfig.java` |

---

## 📞 NEED HELP?

### Documentation
- **Full Analysis:** `COMPREHENSIVE_PROJECT_ANALYSIS.md`
- **Current Status:** `PROJECT_STATUS_REPORT.md`
- **Issues:** `COMPLETE_ANALYSIS_REPORT.md`
- **Database:** `DATABASE_STRUCTURE.md`
- **Deployment:** `DEPLOYMENT_CHECKLIST_TODAY.md`

### Quick Troubleshooting
| Issue | Check |
|-------|-------|
| Build fails | `pom.xml` dependencies |
| App won't start | Port 3000 in use? |
| Database error | MySQL running? Credentials correct? |
| QR code wrong URL | `events.qr.base.url` in config |
| S3 upload fails | IAM role attached to EC2? |

---

## ✅ PROJECT HEALTH CHECK

| Component | Status | Notes |
|-----------|--------|-------|
| **Backend Code** | ✅ Good | Well-structured Spring Boot |
| **Database** | ✅ Good | MySQL migration complete |
| **AWS S3** | ✅ Good | IAM role-based access |
| **Security** | ⚠️ Basic | CSRF disabled, credentials in config |
| **Frontend** | ⚠️ Issue | Source code missing |
| **Documentation** | ⚠️ Excessive | 200+ markdown files (!!) |
| **Testing** | ⚠️ Limited | No unit tests found |
| **Deployment** | ✅ Good | Multi-environment support |

### Overall Rating: **7.5/10**

**Strengths:**
- Clean architecture
- Multi-environment support
- AWS integration
- Comprehensive features

**Needs Improvement:**
- Frontend source control
- Reduce documentation files
- Add automated testing
- Secrets management
- Enable CSRF protection

---

## 🎯 NEXT STEPS

1. ✅ **Fix QR URLs** - Rebuild with corrected config
2. ⚠️ **Find React source** - Locate frontend project
3. ✅ **Add unit tests** - JUnit + Mockito
4. ✅ **Consolidate docs** - 200+ files → wiki
5. ✅ **Secrets manager** - Move credentials to AWS
6. ✅ **CI/CD pipeline** - Automated builds
7. ✅ **API documentation** - Swagger/OpenAPI

---

**📖 For detailed analysis, see: `COMPREHENSIVE_PROJECT_ANALYSIS.md`**

---

*Generated: December 20, 2025*  
*Analyzed by: GitHub Copilot*


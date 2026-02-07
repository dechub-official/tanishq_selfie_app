# 🎨 Greeting Module - Visual Architecture Diagram

## 🏗️ System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER / CLIENT                                │
│  (Mobile App, Web Browser, QR Scanner)                              │
└────────────────┬────────────────────────────────────────────────────┘
                 │
                 │ HTTP REST API
                 ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                                  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │          GreetingController.java                              │  │
│  │  @RestController @RequestMapping("/greetings")                │  │
│  │                                                                │  │
│  │  • POST   /generate          → Create greeting                │  │
│  │  • GET    /{id}/qr           → Get QR code PNG                │  │
│  │  • POST   /{id}/upload       → Upload video                   │  │
│  │  • GET    /{id}/view         → View greeting info             │  │
│  │  • GET    /{id}/status       → Check upload status            │  │
│  │  • DELETE /{id}              → Delete greeting                │  │
│  └───────────────────────────────────────────────────────────────┘  │
└────────────────┬────────────────────────────────────────────────────┘
                 │
                 │ @Autowired
                 ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                                   │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │          GreetingService.java                                 │  │
│  │  @Service                                                     │  │
│  │                                                                │  │
│  │  • createGreeting()      → Generate unique ID                 │  │
│  │  • generateQrCode()      → Create QR PNG                      │  │
│  │  • uploadVideo()         → Save video + metadata              │  │
│  │  • getGreeting()         → Retrieve by ID                     │  │
│  │  • deleteGreeting()      → Remove record                      │  │
│  │  • hasVideoUploaded()    → Check status                       │  │
│  └───────────────────────────────────────────────────────────────┘  │
└────────────────┬────────────────────────────────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
    ↓                         ↓
┌─────────────┐      ┌─────────────────┐
│  JPA Repo   │      │  Storage        │
│             │      │  Service        │
└──────┬──────┘      └────────┬────────┘
       │                      │
       ↓                      ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                                 │
│                                                                      │
│  ┌───────────────────────┐    ┌──────────────────────────────────┐ │
│  │  GreetingRepository   │    │    StorageService (Interface)    │ │
│  │  @Repository          │    │                                  │ │
│  │                       │    │  ┌────────────────────────────┐  │ │
│  │  JpaRepository        │    │  │ LocalFileStorageService   │  │ │
│  │  <Greeting, Long>     │    │  │ @Profile("local")          │  │ │
│  │                       │    │  │ • ./storage/greetings/     │  │ │
│  │  findByUniqueId()     │    │  └────────────────────────────┘  │ │
│  └───────────────────────┘    │                                  │ │
│                                │  ┌────────────────────────────┐  │ │
│                                │  │ AwsS3StorageService        │  │ │
│                                │  │ @Profile({"preprod","prod"})│ │ │
│                                │  │ • AWS S3 bucket/greetings/ │  │ │
│                                │  └────────────────────────────┘  │ │
│                                └──────────────────────────────────┘ │
└────────────────┬───────────────────────────────────┬────────────────┘
                 │                                   │
                 ↓                                   ↓
┌─────────────────────────────────┐   ┌─────────────────────────────┐
│       MySQL DATABASE             │   │    FILE STORAGE             │
│                                  │   │                             │
│  Database: selfie_preprod        │   │  Local:                     │
│                                  │   │  ./storage/greetings/{id}/  │
│  Table: greetings                │   │                             │
│  ┌─────────────────────────────┐ │   │  AWS S3:                    │
│  │ id (PK)                     │ │   │  s3://bucket/greetings/{id}/│
│  │ unique_id                   │ │   │                             │
│  │ greeting_text               │ │   │  Videos: .mp4, .mov, etc.   │
│  │ message                     │ │   │  Max size: 100MB            │
│  │ qr_code_data (Base64)       │ │   └─────────────────────────────┘
│  │ drive_file_id (Video URL)   │ │
│  │ created_at                  │ │
│  │ uploaded (Boolean)          │ │
│  └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

## 🔄 Data Flow Diagrams

### Flow 1: Generate Greeting + QR Code

```
┌────────┐                                                    ┌──────────┐
│  USER  │                                                    │ DATABASE │
└───┬────┘                                                    └────┬─────┘
    │                                                              │
    │ 1. POST /greetings/generate                                 │
    ├──────────────────────────────────►                          │
    │                                  GreetingController          │
    │                                        │                     │
    │                                        │ 2. createGreeting() │
    │                                        ├─────────────────────►
    │                                  GreetingService             │
    │                                        │                     │
    │                                        │ 3. Generate         │
    │                                        │    GREETING_XXX     │
    │                                        │                     │
    │                                        │ 4. INSERT INTO      │
    │                                        │    greetings        │
    │                                        ├─────────────────────►
    │                                        │                     │
    │                                        │ 5. Return ID        │
    │                                        ◄─────────────────────┤
    │                                        │                     │
    │ 6. Return: "GREETING_1738318234567"   │                     │
    ◄──────────────────────────────────────┤                     │
    │                                                              │
    │ 7. GET /greetings/{id}/qr                                   │
    ├──────────────────────────────────►                          │
    │                                  GreetingController          │
    │                                        │                     │
    │                                        │ 8. generateQrCode() │
    │                                        ├─────────────────────►
    │                                  QrCodeService               │
    │                                        │                     │
    │                                        │ 9. SELECT *         │
    │                                        │    WHERE unique_id  │
    │                                        ├─────────────────────►
    │                                        │                     │
    │                                        │ 10. Return record   │
    │                                        ◄─────────────────────┤
    │                                        │                     │
    │                                        │ 11. Generate        │
    │                                        │     QR PNG          │
    │                                        │                     │
    │                                        │ 12. UPDATE          │
    │                                        │     qr_code_data    │
    │                                        ├─────────────────────►
    │                                        │                     │
    │ 13. Return PNG image (bytes)          │                     │
    ◄──────────────────────────────────────┤                     │
    │                                                              │
```

### Flow 2: Upload Video

```
┌────────┐                                          ┌──────────┐  ┌─────────┐
│  USER  │                                          │ DATABASE │  │ STORAGE │
└───┬────┘                                          └────┬─────┘  └────┬────┘
    │                                                    │             │
    │ 1. POST /greetings/{id}/upload                    │             │
    │    FormData:                                      │             │
    │    - video: file.mp4                              │             │
    │    - name: "John Doe"                             │             │
    │    - message: "Happy Birthday!"                   │             │
    ├─────────────────────────────────►                 │             │
    │                                 GreetingController │             │
    │                                       │            │             │
    │                                       │ 2. uploadVideo()         │
    │                                       ├────────────►             │
    │                                 GreetingService    │             │
    │                                       │            │             │
    │                                       │ 3. SELECT  │             │
    │                                       │    greeting│             │
    │                                       ├────────────►             │
    │                                       │            │             │
    │                                       │ 4. Validate│             │
    │                                       │    - Size  │             │
    │                                       │    - Type  │             │
    │                                       │            │             │
    │                                       │ 5. Upload video          │
    │                                       ├─────────────────────────►
    │                                       │            │ StorageService
    │                                       │            │             │
    │                                       │            │ 6. Save to │
    │                                       │            │    S3/local│
    │                                       │            │             │
    │                                       │ 7. Return URL            │
    │                                       ◄─────────────────────────┤
    │                                       │            │             │
    │                                       │ 8. UPDATE  │             │
    │                                       │    greetings│            │
    │                                       │    SET:    │             │
    │                                       │    - greeting_text       │
    │                                       │    - message│            │
    │                                       │    - drive_file_id       │
    │                                       │    - uploaded=true       │
    │                                       ├────────────►             │
    │                                       │            │             │
    │ 9. Return: "Video uploaded successfully"          │             │
    ◄─────────────────────────────────────┤            │             │
    │                                                    │             │
```

### Flow 3: View Greeting

```
┌────────┐                                                    ┌──────────┐
│  USER  │                                                    │ DATABASE │
└───┬────┘                                                    └────┬─────┘
    │                                                              │
    │ 1. GET /greetings/{id}/view                                 │
    ├──────────────────────────────────►                          │
    │                                  GreetingController          │
    │                                        │                     │
    │                                        │ 2. getGreetingInfo()│
    │                                        ├─────────────────────►
    │                                  GreetingService             │
    │                                        │                     │
    │                                        │ 3. SELECT *         │
    │                                        │    FROM greetings   │
    │                                        │    WHERE unique_id  │
    │                                        ├─────────────────────►
    │                                        │                     │
    │                                        │ 4. Return record    │
    │                                        ◄─────────────────────┤
    │                                        │                     │
    │                                        │ 5. Check uploaded   │
    │                                        │    flag             │
    │                                        │                     │
    │ 6. Return JSON:                       │                     │
    │    {                                  │                     │
    │      "hasVideo": true,                │                     │
    │      "status": "completed",           │                     │
    │      "videoPlaybackUrl": "https://...",                     │
    │      "greetingText": "John Doe",      │                     │
    │      "message": "Happy Birthday!",    │                     │
    │      "submissionTimestamp": "..."     │                     │
    │    }                                  │                     │
    ◄──────────────────────────────────────┤                     │
    │                                                              │
```

---

## 🗄️ Database Entity Mapping

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Java Entity                                  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  @Entity                                                      │  │
│  │  @Table(name = "greetings")                                  │  │
│  │  public class Greeting {                                      │  │
│  │      @Id                                                      │  │
│  │      @GeneratedValue(strategy = GenerationType.IDENTITY)     │  │
│  │      private Long id;                                         │  │
│  │                                                                │  │
│  │      private String uniqueId;                                 │  │
│  │      private String greetingText;                             │  │
│  │      private String phone;                                    │  │
│  │      private String message;                                  │  │
│  │                                                                │  │
│  │      @Lob                                                     │  │
│  │      @Column(columnDefinition = "LONGTEXT")                  │  │
│  │      private String qrCodeData;                               │  │
│  │                                                                │  │
│  │      private String driveFileId;  // Video URL                │  │
│  │      private LocalDateTime createdAt;                         │  │
│  │      private Boolean uploaded;                                │  │
│  │  }                                                            │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────────────┘
                  │
                  │ Hibernate ORM
                  │ (spring.jpa.hibernate.ddl-auto=update)
                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         MySQL Table                                  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CREATE TABLE `greetings` (                                   │  │
│  │      `id` BIGINT NOT NULL AUTO_INCREMENT,                     │  │
│  │      `unique_id` VARCHAR(255) DEFAULT NULL,                   │  │
│  │      `greeting_text` VARCHAR(255) DEFAULT NULL,               │  │
│  │      `phone` VARCHAR(255) DEFAULT NULL,                       │  │
│  │      `message` TEXT DEFAULT NULL,                             │  │
│  │      `qr_code_data` LONGTEXT DEFAULT NULL,                    │  │
│  │      `drive_file_id` VARCHAR(255) DEFAULT NULL,               │  │
│  │      `created_at` DATETIME DEFAULT NULL,                      │  │
│  │      `uploaded` TINYINT(1) DEFAULT 0,                         │  │
│  │      PRIMARY KEY (`id`),                                      │  │
│  │      UNIQUE KEY `uk_unique_id` (`unique_id`)                  │  │
│  │  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;                     │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🌍 Environment-Based Storage Selection

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Spring Profile Selection                          │
└────────────────┬────────────────────────────────────────────────────┘
                 │
      ┌──────────┴──────────┐
      │                     │
      ↓                     ↓
┌────────────┐        ┌────────────┐
│ @Profile   │        │ @Profile   │
│ ("local")  │        │ ({"preprod"│
│            │        │  ,"prod"}) │
└─────┬──────┘        └─────┬──────┘
      │                     │
      ↓                     ↓
┌──────────────────┐  ┌──────────────────┐
│ LocalFile        │  │ AwsS3            │
│ StorageService   │  │ StorageService   │
│                  │  │                  │
│ Storage:         │  │ Storage:         │
│ ./storage/       │  │ s3://bucket/     │
│ greetings/       │  │ greetings/       │
│                  │  │                  │
│ URL:             │  │ URL:             │
│ http://localhost │  │ https://bucket.  │
│ :3000/storage/   │  │ s3.region.       │
│                  │  │ amazonaws.com/   │
└──────────────────┘  └──────────────────┘
```

---

## 📊 System Component Interaction

```
         ┌─────────────────┐
         │   QR Scanner    │
         │   (Mobile App)  │
         └────────┬────────┘
                  │ Scan QR Code
                  │ URL: /qr?id=GREETING_XXX
                  ↓
         ┌─────────────────┐
         │  Web Browser    │
         │  Upload Page    │
         └────────┬────────┘
                  │ Upload Form
                  │ POST /greetings/{id}/upload
                  ↓
    ┌─────────────────────────────────┐
    │    GreetingController           │
    │    (REST API Layer)             │
    └────────┬────────────────────────┘
             │
    ┌────────┴────────────────────┐
    │                             │
    ↓                             ↓
┌────────────┐           ┌────────────────┐
│ Greeting   │           │   Storage      │
│ Service    │           │   Service      │
│            │           │                │
│ Business   │           │ File Upload    │
│ Logic      │           │ S3 or Local    │
└─────┬──────┘           └────────┬───────┘
      │                           │
      │                           │
      ↓                           ↓
┌─────────────┐          ┌────────────────┐
│   MySQL     │          │  File Storage  │
│  Database   │          │                │
│             │          │  Videos:       │
│  greetings  │          │  • S3 Bucket   │
│  table      │          │  • Local Dir   │
└─────────────┘          └────────────────┘
```

---

## 🔐 Authentication & Authorization Flow

```
Currently: No authentication required
Future: Add security layer

┌─────────────────────────────────────────────────────────────────────┐
│  TODO: Add Security                                                  │
│                                                                      │
│  @PreAuthorize("hasRole('ADMIN')")                                  │
│  public ResponseEntity<String> deleteGreeting(...)                   │
│                                                                      │
│  Consider:                                                           │
│  - JWT tokens for API access                                        │
│  - Rate limiting for /generate endpoint                             │
│  - Upload validation (virus scanning)                               │
│  - Admin-only delete operations                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Deployment Architecture

```
┌───────────────────────────────────────────────────────────────────────┐
│                        PRODUCTION DEPLOYMENT                          │
│                                                                       │
│  ┌──────────────┐         ┌──────────────┐         ┌──────────────┐ │
│  │   Nginx      │         │   Tomcat     │         │   MySQL      │ │
│  │  (Reverse    │────────►│   Server     │────────►│  Database    │ │
│  │   Proxy)     │         │              │         │              │ │
│  │  Port 80/443 │         │  Port 8080   │         │  Port 3306   │ │
│  └──────────────┘         └──────┬───────┘         └──────────────┘ │
│                                   │                                   │
│                                   │ IAM Role                          │
│                                   │ Credentials                       │
│                                   ↓                                   │
│                          ┌──────────────┐                            │
│                          │   AWS S3     │                            │
│                          │   Bucket     │                            │
│                          │  /greetings/ │                            │
│                          └──────────────┘                            │
└───────────────────────────────────────────────────────────────────────┘
```

---

Generated: January 31, 2026
Version: MySQL Implementation (Current Branch)


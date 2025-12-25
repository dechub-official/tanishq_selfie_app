# Single Frontend Build for All Environments

## ✅ Confirmed: One Build Works for Pre-Prod and Production

The frontend code does **NOT** require separate builds for different environments. A single build can be deployed to both Pre-Prod and Production servers.

---

## How It Works

### Frontend (JavaScript/HTML/CSS)
The frontend uses **relative URLs** for all API calls:

```javascript
// From static/js/common.js
function loadEvents() {
    $.ajax({ url: './selfie_app', ... });
}

// All API endpoints use relative paths like:
// - ./selfie_app
// - ./eventsList
// - ./storeList
// - ./uploadFrontendFile
```

These relative URLs automatically resolve to the current server's domain, meaning:
- On **Pre-Prod** (`http://preprod.example.com`) → API calls go to `http://preprod.example.com/selfie_app`
- On **Production** (`http://prod.example.com`) → API calls go to `http://prod.example.com/selfie_app`

### Backend (Spring Boot)
The environment-specific configuration is handled by `application.properties`:

| Setting | Configured In | Purpose |
|---------|--------------|---------|
| Database URL | `application.properties` | Points to Pre-Prod or Prod database |
| S3 Bucket | `application.properties` | Points to correct AWS S3 bucket |
| Server Port | `application.properties` | Server port configuration |

---

## Build Once, Deploy Anywhere

### Step 1: Build the Application (Once)
```bash
# Standard Maven build
mvn clean package -DskipTests
```

This creates: `target/tanishq-selfie-app-0.0.1-SNAPSHOT.jar`

### Step 2: Deploy to Pre-Prod
```bash
# Copy the JAR to Pre-Prod server
scp target/*.jar user@preprod-server:/app/

# On Pre-Prod server, ensure application.properties has Pre-Prod settings
# Then start the application
java -jar tanishq-selfie-app-0.0.1-SNAPSHOT.jar
```

### Step 3: Deploy to Production (Same JAR)
```bash
# Copy the SAME JAR to Production server
scp target/*.jar user@prod-server:/app/

# On Production server, ensure application.properties has Production settings
# Then start the application
java -jar tanishq-selfie-app-0.0.1-SNAPSHOT.jar
```

---

## Environment Configuration

The only difference between environments is the `application.properties` file:

### Pre-Prod `application.properties`
```properties
spring.datasource.url=jdbc:mysql://preprod-db-server:3306/tanishq_selfie_preprod
spring.datasource.username=preprod_user
aws.s3.bucket=tanishq-selfie-preprod
```

### Production `application.properties`
```properties
spring.datasource.url=jdbc:mysql://prod-db-server:3306/tanishq_selfie_prod
spring.datasource.username=prod_user
aws.s3.bucket=tanishq-selfie-prod
```

---

## Why This Works

1. **Relative URLs**: Frontend uses `./endpoint` instead of `http://domain.com/endpoint`
2. **Same Origin Policy**: All API calls go to the same domain the page was loaded from
3. **Spring Boot Packaging**: Static resources are bundled inside the JAR
4. **External Configuration**: Environment settings are read from `application.properties` at runtime

---

## Summary

| Component | Build Required | Environment-Specific |
|-----------|---------------|---------------------|
| Frontend (JS/HTML/CSS) | Once | ❌ No |
| Backend (Java) | Once | ❌ No |
| Configuration (application.properties) | N/A | ✅ Yes |

**One JAR file → Deploy to any environment → Configuration determines behavior**

---

## Deprecated Build Scripts

The following batch files that suggest separate builds are **no longer needed**:
- `BUILD_PREPROD.bat`
- `BUILD_PROD.bat`
- `build-preprod.bat`
- `BUILD_ENVIRONMENT_SWITCHER.bat`

Use a single standard build instead:
```bash
mvn clean package -DskipTests
```


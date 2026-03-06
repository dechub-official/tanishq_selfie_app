# Rate Limiting Vulnerability Fix - Implementation Plan

## Vulnerability Details

- **CWSS Score:** 8.2 (High)
- **CVSS Score:** 8.1 (High)
- **Cyber Security Exposure Score:** High
- **OWASP Category:** A04:2021 - Insecure Design / A05:2021 - Security Misconfiguration

## Current State

**No rate limiting exists** - The application currently has **ZERO rate limiting** protection on any endpoints. All form submission endpoints can be attacked with unlimited requests.

## What Needs to Change

---

## 📦 1. ADD NEW DEPENDENCIES (pom.xml)

### Location: `pom.xml`

**Add Bucket4j library for rate limiting:**

```xml
<!-- Add after other dependencies, before </dependencies> -->

<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

**Why:** Bucket4j provides token bucket algorithm for rate limiting with in-memory or distributed storage support.

---

## 🛡️ 2. CREATE NEW RATE LIMITER SERVICE

### New File: `src/main/java/com/dechub/tanishq/security/RateLimiterService.java`

**Purpose:** Centralized service to manage rate limiting across all endpoints

**Key Features:**
- **Per-IP rate limiting** - Limit requests per IP address
- **Per-user rate limiting** - Limit requests per authenticated user
- **Per-endpoint rate limiting** - Different limits for different endpoint types
- **In-memory storage** - Uses ConcurrentHashMap with automatic cleanup
- **Configurable limits** - Different rules for login, form submission, and data endpoints

**Rate Limit Configurations Needed:**

| Endpoint Type | Rate Limit | Window | Reasoning |
|---------------|------------|--------|-----------|
| **Login endpoints** | 5 attempts | 15 minutes | Prevent brute force attacks |
| **Form submission** | 20 requests | 1 minute | Prevent spam/abuse |
| **Data query endpoints** | 30 requests | 1 minute | Normal usage protection |
| **File upload** | 10 uploads | 5 minutes | Heavy operation protection |
| **Password change** | 3 attempts | 30 minutes | Sensitive operation |

**Methods to implement:**

```java
public class RateLimiterService {
    // Check if request is allowed
    public boolean allowRequest(String identifier, RateLimitType type);
    
    // Check and consume token (atomic operation)
    public boolean tryConsume(String identifier, RateLimitType type);
    
    // Get remaining quota
    public long getRemainingTokens(String identifier, RateLimitType type);
    
    // Reset specific user's limit (admin function)
    public void resetLimit(String identifier, RateLimitType type);
    
    // Cleanup expired buckets (scheduled task)
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupExpiredBuckets();
}

enum RateLimitType {
    LOGIN,              // 5 requests per 15 minutes
    FORM_SUBMISSION,    // 20 requests per 1 minute
    DATA_QUERY,         // 30 requests per 1 minute
    FILE_UPLOAD,        // 10 requests per 5 minutes
    PASSWORD_CHANGE     // 3 requests per 30 minutes
}
```

---

## 🔌 3. CREATE RATE LIMITING FILTER/INTERCEPTOR

### New File: `src/main/java/com/dechub/tanishq/security/RateLimitingFilter.java`

**Purpose:** Intercept all HTTP requests and apply rate limiting before reaching controllers

**Implementation Strategy:**
- Extend `OncePerRequestFilter` (Spring Web filter)
- Extract IP address from request (handle X-Forwarded-For for load balancers)
- Extract authenticated user from session if available
- Determine rate limit type based on endpoint pattern
- Call RateLimiterService to check if request is allowed
- Return 429 Too Many Requests if limit exceeded
- Add rate limit headers to response

**Response Headers to Add:**
```
X-RateLimit-Limit: 20          // Total allowed requests
X-RateLimit-Remaining: 15      // Remaining requests
X-RateLimit-Reset: 1735987200  // Unix timestamp when limit resets
Retry-After: 45                // Seconds until retry allowed
```

**Error Response Format (429 Status):**
```json
{
    "status": false,
    "message": "Too many requests. Please try again after 45 seconds.",
    "errorCode": "RATE_LIMIT_EXCEEDED",
    "retryAfter": 45
}
```

**IP Address Extraction Logic:**
```java
// Must handle proxy/load balancer scenarios
String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
    }
    String xRealIP = request.getHeader("X-Real-IP");
    if (xRealIP != null && !xRealIP.isEmpty()) {
        return xRealIP;
    }
    return request.getRemoteAddr();
}
```

---

## ⚙️ 4. REGISTER FILTER IN SECURITY CONFIG

### File: `src/main/java/com/dechub/tanishq/config/SecurityConfig.java`

**Changes needed:**

1. **Add RateLimitingFilter bean:**
```java
@Autowired
private RateLimitingFilter rateLimitingFilter;
```

2. **Register filter in HTTP security chain:**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        // ADD THIS LINE - Apply rate limiting before authentication
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        
        .authorizeRequests()
        // ... existing configuration
}
```

**Alternative approach:** Register as global filter in `WebMvcConfig`:
```java
@Bean
public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter() {
    FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new RateLimitingFilter(rateLimiterService));
    registrationBean.addUrlPatterns("/events/*"); // Apply to all event endpoints
    registrationBean.setOrder(1); // Execute early in filter chain
    return registrationBean;
}
```

---

## 🎯 5. CONFIGURE RATE LIMITS PER ENDPOINT

### Endpoint Classification and Rate Limits

#### **A. Login Endpoints (CRITICAL - Prevent Brute Force)**
Rate Limit: **5 attempts per 15 minutes per IP + per username**

Endpoints:
- `POST /events/login` 
- `POST /events/abm_login`
- `POST /events/rbm_login`
- `POST /events/cee_login`
- `POST /events/corporate_login`

**Dual Rate Limiting Strategy:**
- IP-based: Block after 5 failed attempts from same IP
- Username-based: Block after 5 failed attempts for same username (even from different IPs)

#### **B. Form Submission Endpoints (HIGH RISK)**
Rate Limit: **20 requests per 1 minute per IP/user**

Endpoints:
- `POST /events/upload` - Create event (with file upload)
- `POST /events/attendees` - Register attendee (customer facing)
- `POST /events/updateSaleOfAnEvent`
- `POST /events/updateAdvanceOfAnEvent`
- `POST /events/updateGhsRgaOfAnEvent`
- `POST /events/updateGmbOfAnEvent`
- `POST /events/changePassword` (MORE RESTRICTIVE: 3 per 30 min)

#### **C. File Upload Endpoints (RESOURCE INTENSIVE)**
Rate Limit: **10 uploads per 5 minutes per user**

Endpoints:
- `POST /events/uploadCompletedEvents` (multiple files)

#### **D. Data Query Endpoints (MEDIUM RISK)**
Rate Limit: **30 requests per 1 minute per IP/user**

Endpoints:
- `POST /events/getevents`
- `POST /events/getinvitedmember`
- `GET /events/getStoresByRegion/{region}`
- `GET /events/rbmStores`
- `GET /events/abmStores`
- `GET /events/ceeStores`
- `GET /events/corporate/events`

#### **E. Download/Export Endpoints (CSV Generation)**
Rate Limit: **5 downloads per 10 minutes per user**

Endpoints:
- `GET /events/store/events/download`
- `GET /events/abm/events/download`
- `GET /events/rbm/events/download`
- `GET /events/cee/events/download`
- `GET /events/corporate/events/download`
- `GET /events/download-event-report`
- `GET /events/download-invitees-sample`

---

## 🚨 6. ENHANCED LOGGING FOR RATE LIMIT VIOLATIONS

### File: `RateLimiterService.java` and `RateLimitingFilter.java`

**Add security audit logging:**

```java
// When rate limit exceeded
log.warn("RATE LIMIT EXCEEDED: IP={}, User={}, Endpoint={}, Attempts={}, Window={}", 
         clientIP, username, endpoint, attemptCount, timeWindow);

// For severe violations (10x over limit)
log.error("SEVERE RATE LIMIT VIOLATION: Possible DDoS attack from IP={}, Endpoint={}, Attempts={}", 
          clientIP, endpoint, attemptCount);
```

**Log entry format:**
```
[RATE_LIMIT] timestamp | level | IP | username | endpoint | attempt_count | limit | window | action
```

---

## 📊 7. ADD CONFIGURATION PROPERTIES

### File: `src/main/resources/application.properties`

**Add rate limiting configuration:**

```properties
# Rate Limiting Configuration
rate.limit.enabled=true

# Login endpoints
rate.limit.login.capacity=5
rate.limit.login.refill.tokens=5
rate.limit.login.refill.minutes=15

# Form submission endpoints
rate.limit.form.capacity=20
rate.limit.form.refill.tokens=20
rate.limit.form.refill.minutes=1

# Data query endpoints
rate.limit.query.capacity=30
rate.limit.query.refill.tokens=30
rate.limit.query.refill.minutes=1

# File upload endpoints
rate.limit.upload.capacity=10
rate.limit.upload.refill.tokens=10
rate.limit.upload.refill.minutes=5

# Password change endpoints
rate.limit.password.capacity=3
rate.limit.password.refill.tokens=3
rate.limit.password.refill.minutes=30

# CSV download endpoints
rate.limit.download.capacity=5
rate.limit.download.refill.tokens=5
rate.limit.download.refill.minutes=10

# Whitelist IPs (comma-separated, optional)
rate.limit.whitelist.ips=127.0.0.1,::1

# Block duration for severe violations (minutes)
rate.limit.block.duration=60
```

**Also add to environment-specific files:**
- `application-local.properties` (more lenient for testing)
- `application-preprod.properties` (same as production)
- `application-prod.properties` (strict limits)
- `application-uat.properties` (moderate limits)

---

## 🎪 8. OPTIONAL: ADD CAPTCHA FOR ABUSE-PRONE FORMS

### Most Critical Endpoints for CAPTCHA:

1. **`POST /events/attendees`** - Public endpoint (QR code customer registration)
2. **`POST /events/login`** - After 3 failed attempts
3. **`POST /events/upload`** - Event creation

### Implementation Options:

#### Option A: Google reCAPTCHA v3 (Invisible)
**Add to pom.xml:**
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

**New Service:** `src/main/java/com/dechub/tanishq/security/CaptchaService.java`
```java
public class CaptchaService {
    public boolean verifyCaptcha(String captchaToken, String clientIP);
}
```

**Frontend Changes Required:**
- Add reCAPTCHA script to HTML
- Include token in form submissions
- Backend validates token before processing

#### Option B: hCaptcha (Privacy-focused alternative)
Similar implementation to reCAPTCHA

**Configuration needed:**
```properties
# CAPTCHA Configuration (if implementing)
captcha.enabled=true
captcha.provider=recaptcha  # or hcaptcha
captcha.secret.key=YOUR_SECRET_KEY
captcha.site.key=YOUR_SITE_KEY
captcha.verify.url=https://www.google.com/recaptcha/api/siteverify
```

---

## 🔄 9. ENDPOINT-SPECIFIC CHANGES

### A. EventsController.java - Add Rate Limit Annotations

**Option 1: Using Custom Annotation**

Create annotation:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    RateLimitType value();
    boolean perUser() default true;
    boolean perIP() default true;
}
```

Apply to endpoints:
```java
@PostMapping("/login")
@RateLimited(value = RateLimitType.LOGIN, perIP = true, perUser = true)
public ResponseEntity<?> eventsLogin(@RequestBody LoginDTO loginDTO, HttpSession session) {
    // existing code
}

@PostMapping("/attendees")
@RateLimited(value = RateLimitType.FORM_SUBMISSION, perIP = true)
public ResponseDataDTO storeAttendeesData(...) {
    // existing code
}

@PostMapping("/upload")
@RateLimited(value = RateLimitType.FORM_SUBMISSION, perUser = true)
public QrResponseDTO storeEventsDetails(...) {
    // existing code
}
```

**Option 2: Programmatic Check in Filter**

Filter automatically detects endpoint pattern and applies appropriate rate limit:
```java
// In RateLimitingFilter
private RateLimitType determineRateLimitType(String uri) {
    if (uri.matches(".*/login$")) return RateLimitType.LOGIN;
    if (uri.contains("/upload")) return RateLimitType.FILE_UPLOAD;
    if (uri.contains("/attendees")) return RateLimitType.FORM_SUBMISSION;
    if (uri.contains("/changePassword")) return RateLimitType.PASSWORD_CHANGE;
    if (uri.contains("/download")) return RateLimitType.DOWNLOAD;
    // default for other POST endpoints
    return RateLimitType.DATA_QUERY;
}
```

### B. Specific Endpoints That MUST Have Rate Limiting

#### Critical Priority (Implement First):
1. ✅ `POST /events/login` - Prevent credential stuffing
2. ✅ `POST /events/abm_login` - Prevent brute force
3. ✅ `POST /events/rbm_login` - Prevent brute force
4. ✅ `POST /events/cee_login` - Prevent brute force
5. ✅ `POST /events/corporate_login` - Prevent brute force
6. ✅ `POST /events/attendees` - **PUBLIC ENDPOINT** - Most vulnerable to spam
7. ✅ `POST /events/upload` - Prevent event spam creation
8. ✅ `POST /events/changePassword` - Prevent password change attacks

#### High Priority:
9. ✅ `POST /events/updateSaleOfAnEvent` - Financial data manipulation
10. ✅ `POST /events/updateAdvanceOfAnEvent` - Financial data manipulation
11. ✅ `POST /events/updateGhsRgaOfAnEvent` - Data manipulation
12. ✅ `POST /events/updateGmbOfAnEvent` - Data manipulation
13. ✅ `POST /events/uploadCompletedEvents` - File upload spam
14. ✅ `POST /events/getevents` - Database query load
15. ✅ `POST /events/getinvitedmember` - Database query load

#### Medium Priority:
16. ✅ `GET /events/store/events/download` - CSV generation (CPU intensive)
17. ✅ `GET /events/abm/events/download` - CSV generation
18. ✅ `GET /events/rbm/events/download` - CSV generation
19. ✅ `GET /events/cee/events/download` - CSV generation
20. ✅ `GET /events/corporate/events/download` - CSV generation

---

## 🔧 10. INTEGRATION WITH EXISTING SECURITY

### File: `SecurityConfig.java`

**Changes needed:**

1. **Add rate limiting filter to security chain:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private RateLimiterService rateLimiterService;
    
    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(rateLimiterService);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // ADD THIS - Apply rate limiting first
            .addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter.class)
            
            .authorizeRequests()
            // ... existing configuration
    }
}
```

2. **Enable scheduled tasks for cleanup:**
Add to main application class or configuration:
```java
@EnableScheduling
public class TanishqApplication {
    // ...
}
```

---

## 📝 11. CONTROLLER MODIFICATIONS (OPTIONAL)

### File: `EventsController.java`

**Option 1: No changes needed** - Filter handles everything

**Option 2: Add explicit validation** (defense in depth):
```java
@PostMapping("/attendees")
public ResponseDataDTO storeAttendeesData(..., HttpServletRequest request) {
    
    // Explicit rate limit check (in addition to filter)
    String clientIP = getClientIP(request);
    if (!rateLimiterService.tryConsume(clientIP, RateLimitType.FORM_SUBMISSION)) {
        log.warn("RATE LIMIT: Attendee submission blocked for IP: {}", clientIP);
        ResponseDataDTO error = new ResponseDataDTO();
        error.setStatus(false);
        error.setMessage("Too many submissions. Please wait and try again.");
        return error;
    }
    
    // existing validation and processing
    log.info("Received attendee submission - EventId: {}, Name: {}, Phone: {}", eventId, name, phone);
    // ...
}
```

**If choosing Option 2, add to these endpoints:**
- All login endpoints
- `/attendees` (most critical - public facing)
- `/upload` 
- `/changePassword`

---

## 📈 12. MONITORING AND METRICS

### New File: `src/main/java/com/dechub/tanishq/security/RateLimitMetrics.java`

**Track and expose metrics:**

```java
@Component
public class RateLimitMetrics {
    private final AtomicLong totalBlocked = new AtomicLong(0);
    private final AtomicLong totalAllowed = new AtomicLong(0);
    private final Map<String, AtomicLong> blockedByEndpoint = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> blockedByIP = new ConcurrentHashMap<>();
    
    public void recordBlocked(String endpoint, String ip);
    public void recordAllowed(String endpoint);
    
    public Map<String, Object> getMetrics();
    
    // Top offenders
    public List<String> getTopBlockedIPs(int limit);
}
```

**Expose metrics endpoint (admin only):**
```java
@GetMapping("/admin/rate-limit-metrics")
public ResponseEntity<?> getRateLimitMetrics() {
    // Return metrics for security dashboard
    return ResponseEntity.ok(rateLimitMetrics.getMetrics());
}
```

---

## 🧪 13. TESTING REQUIREMENTS

### Unit Tests to Create:

**File:** `src/test/java/com/dechub/tanishq/security/RateLimiterServiceTest.java`
- Test bucket creation
- Test rate limit enforcement
- Test limit reset after time window
- Test concurrent access
- Test different endpoint types

**File:** `src/test/java/com/dechub/tanishq/security/RateLimitingFilterTest.java`
- Test filter applies limits correctly
- Test 429 response format
- Test rate limit headers
- Test IP extraction logic
- Test whitelist functionality

### Integration Tests to Create:

**File:** `src/test/java/com/dechub/tanishq/controller/RateLimitIntegrationTest.java`
- Test login endpoint rate limiting (rapid fire 6 requests)
- Test attendee submission rate limiting (spam 25 requests)
- Test that legitimate traffic is not blocked
- Test rate limit reset after time window

---

## 🔐 14. SECURITY CONSIDERATIONS

### A. IP Spoofing Prevention

**Risk:** Attackers could spoof X-Forwarded-For header

**Mitigation in RateLimitingFilter:**
```java
// Validate IP format
private boolean isValidIP(String ip) {
    return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || 
           ip.matches("^(?:[0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$");
}

// Trust X-Forwarded-For only if behind known proxy
private String getClientIP(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && isTrustedProxy(request.getRemoteAddr())) {
        String[] ips = xff.split(",");
        for (String ip : ips) {
            String trimmed = ip.trim();
            if (isValidIP(trimmed) && !isPrivateIP(trimmed)) {
                return trimmed;
            }
        }
    }
    return request.getRemoteAddr();
}
```

### B. Distributed Environment Support

**Current implementation:** In-memory (single server)

**For clustered deployment:**
```xml
<!-- Add Redis dependency for distributed rate limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>7.6.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### C. Whitelist Management

**Trusted IPs that bypass rate limiting:**
- Internal corporate IPs
- Load balancer health checks
- Monitoring systems

**Implementation:**
```java
// In RateLimitingFilter
private static final Set<String> WHITELISTED_IPS = new HashSet<>(Arrays.asList(
    "10.0.0.0/8",      // Internal network
    "172.16.0.0/12",   // Private network
    "192.168.0.0/16"   // Local network
));
```

---

## ⚡ 15. PERFORMANCE OPTIMIZATION

### Memory Management

**Problem:** Unlimited bucket storage = memory leak

**Solution:** Implement automatic cleanup:
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void cleanupExpiredBuckets() {
    long now = System.currentTimeMillis();
    bucketCache.entrySet().removeIf(entry -> {
        long lastAccess = entry.getValue().getLastAccessTime();
        return (now - lastAccess) > CLEANUP_THRESHOLD;
    });
}
```

### Caching Strategy

**Use LoadingCache (Guava) for automatic eviction:**
```xml
<!-- Add Guava dependency -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>
```

```java
private final LoadingCache<String, Bucket> bucketCache = CacheBuilder.newBuilder()
    .maximumSize(10000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build(new CacheLoader<String, Bucket>() {
        public Bucket load(String key) {
            return createNewBucket(key);
        }
    });
```

---

## 🌐 16. WAF/LOAD BALANCER LEVEL (RECOMMENDED)

### Additional Protection Layer

**If using AWS:**
```yaml
# AWS WAF Rule for Rate Limiting
RateLimitRule:
  Type: AWS::WAFv2::WebACL
  Properties:
    Rules:
      - Name: RateLimitLogin
        Priority: 1
        Statement:
          RateBasedStatement:
            Limit: 100  # per 5 minutes
            AggregateKeyType: IP
            ScopeDownStatement:
              ByteMatchStatement:
                FieldToMatch:
                  UriPath: {}
                PositionalConstraint: CONTAINS
                SearchString: /events/login
```

**If using Nginx:**
```nginx
# /etc/nginx/conf.d/rate-limit.conf
http {
    # Define rate limit zones
    limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;
    limit_req_zone $binary_remote_addr zone=form_limit:10m rate=20r/m;
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/m;
    
    server {
        # Apply to login endpoints
        location ~ /events/.*login$ {
            limit_req zone=login_limit burst=2 nodelay;
            proxy_pass http://backend;
        }
        
        # Apply to form submissions
        location ~ /events/(upload|attendees) {
            limit_req zone=form_limit burst=5 nodelay;
            proxy_pass http://backend;
        }
        
        # Apply to other API endpoints
        location /events/ {
            limit_req zone=api_limit burst=10 nodelay;
            proxy_pass http://backend;
        }
    }
}
```

**If using Apache:**
```apache
# mod_evasive or mod_qos
<Location /events/>
    DOSHashTableSize 3097
    DOSPageCount 5
    DOSSiteCount 50
    DOSPageInterval 1
    DOSSiteInterval 1
    DOSBlockingPeriod 60
</Location>
```

---

## 📋 17. DEPLOYMENT CHECKLIST

### Pre-Deployment:
- [ ] Add Bucket4j dependency to pom.xml
- [ ] Create RateLimiterService class
- [ ] Create RateLimitingFilter class
- [ ] Create RateLimitType enum
- [ ] Add rate limit configuration to application.properties
- [ ] Register filter in SecurityConfig
- [ ] Enable @EnableScheduling for cleanup tasks
- [ ] Create unit tests
- [ ] Create integration tests

### Testing in Pre-Prod:
- [ ] Test login rate limiting (try 6 rapid logins)
- [ ] Test form submission rate limiting (spam attendee endpoint)
- [ ] Test that legitimate users are not impacted
- [ ] Test rate limit reset after time window expires
- [ ] Monitor logs for rate limit violations
- [ ] Verify 429 status code and proper error messages
- [ ] Test with different IP addresses
- [ ] Test with authenticated vs unauthenticated users

### Production Deployment:
- [ ] Deploy to pre-prod first
- [ ] Monitor for 48 hours
- [ ] Analyze rate limit metrics
- [ ] Adjust limits if needed
- [ ] Deploy to production
- [ ] Configure WAF/load balancer rules (if available)
- [ ] Set up alerts for excessive rate limit violations

---

## 🚦 18. RESPONSE HANDLING CHANGES

### HTTP Status Codes to Use:

| Scenario | Status Code | Response Body |
|----------|-------------|---------------|
| Rate limit exceeded | **429 Too Many Requests** | `{"status": false, "message": "Too many requests. Try again in 45s", "retryAfter": 45}` |
| Temporarily blocked | **429 Too Many Requests** | `{"status": false, "message": "Temporarily blocked due to excessive requests", "blockDuration": 3600}` |
| Normal processing | **200 OK** | Normal response |

### Response Headers:

**Always include on rate-limited endpoints:**
```
X-RateLimit-Limit: 20
X-RateLimit-Remaining: 15
X-RateLimit-Reset: 1735987200
```

**When rate limit exceeded:**
```
Retry-After: 45
X-RateLimit-Remaining: 0
```

---

## 📊 19. LOGGING AND MONITORING

### Log Patterns to Add:

**Success (DEBUG level):**
```
[RATE_LIMIT] Request allowed: IP=192.168.1.100, User=STORE001, Endpoint=/events/upload, Remaining=15/20
```

**Rate limit hit (WARN level):**
```
[RATE_LIMIT] Limit exceeded: IP=192.168.1.100, User=STORE001, Endpoint=/events/login, Attempts=6/5, Window=15min
```

**Severe violation (ERROR level):**
```
[RATE_LIMIT] SEVERE VIOLATION: IP=192.168.1.100, Endpoint=/events/attendees, Attempts=500/20, Possible DDoS
```

### Metrics Dashboard (Optional)

Create admin endpoint:
```java
@GetMapping("/admin/security-metrics")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getSecurityMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    metrics.put("rateLimitViolations", rateLimitMetrics.getTotalBlocked());
    metrics.put("topOffendingIPs", rateLimitMetrics.getTopBlockedIPs(10));
    metrics.put("mostTargetedEndpoints", rateLimitMetrics.getMostTargetedEndpoints(10));
    metrics.put("currentActiveLimits", rateLimiterService.getActiveConnectionCount());
    return ResponseEntity.ok(metrics);
}
```

---

## 🎯 20. FRONTEND CHANGES REQUIRED

### A. Handle 429 Responses

**File:** Frontend code (React/JS)

**Add global error handler:**
```javascript
// In API client
async function apiRequest(url, options) {
    const response = await fetch(url, options);
    
    if (response.status === 429) {
        const data = await response.json();
        const retryAfter = data.retryAfter || 60;
        
        // Show user-friendly message
        showError(`Too many requests. Please wait ${retryAfter} seconds and try again.`);
        
        // Optional: Disable submit button temporarily
        disableFormForSeconds(retryAfter);
        
        throw new Error('RATE_LIMIT_EXCEEDED');
    }
    
    return response;
}
```

### B. Add Client-Side Throttling (Good Practice)

**Debounce form submissions:**
```javascript
// Prevent accidental double-clicks
const debouncedSubmit = debounce(async (formData) => {
    await submitForm(formData);
}, 1000, { leading: true, trailing: false });
```

### C. Show Rate Limit Info to Users

**Display remaining quota:**
```javascript
// Parse rate limit headers
const remaining = response.headers.get('X-RateLimit-Remaining');
const limit = response.headers.get('X-RateLimit-Limit');

if (remaining && remaining < 5) {
    showWarning(`You have ${remaining} of ${limit} requests remaining`);
}
```

---

## 🔥 21. CRITICAL ENDPOINTS ANALYSIS

### Most Vulnerable Endpoints (Priority Order):

#### 1. `/events/attendees` - **HIGHEST RISK**
- **Why:** Public endpoint (no authentication required)
- **Attack vector:** Spam attendee registrations
- **Impact:** Database bloat, storage costs, email spam
- **Mitigation:** 
  - Rate limit: 10 per minute per IP
  - Add CAPTCHA after 3 submissions
  - Validate phone number format
  - Check for duplicate entries

#### 2. `/events/login` - **CRITICAL**
- **Why:** Gateway to system access
- **Attack vector:** Credential stuffing, brute force
- **Impact:** Account compromise
- **Mitigation:**
  - Rate limit: 5 per 15 minutes per IP + per username
  - Add CAPTCHA after 3 failed attempts
  - Account lockout after 10 failed attempts
  - Alert security team on 50+ failures from same IP

#### 3. `/events/upload` - **HIGH RISK**
- **Why:** Creates database records and uploads files
- **Attack vector:** Spam event creation
- **Impact:** Storage costs, database bloat
- **Mitigation:**
  - Rate limit: 20 per hour per user
  - File type validation
  - File size limits
  - Validate store authentication

#### 4. `/events/changePassword` - **CRITICAL**
- **Why:** Modifies authentication credentials
- **Attack vector:** Password change abuse
- **Impact:** Account takeover
- **Mitigation:**
  - Rate limit: 3 per 30 minutes per user
  - Require current password
  - Session validation
  - Email notification on change

---

## 🔄 22. IMPLEMENTATION APPROACH

### Recommended Implementation Strategy:

**Phase 1: Core Infrastructure (Week 1)**
1. Add Bucket4j dependency
2. Create RateLimiterService
3. Create RateLimitingFilter
4. Configure basic rate limits
5. Unit test components

**Phase 2: Integration (Week 1)**
6. Register filter in SecurityConfig
7. Add rate limit configuration properties
8. Enable scheduling for cleanup
9. Integration tests

**Phase 3: Critical Endpoints (Week 2)**
10. Apply strict limits to login endpoints
11. Apply limits to public /attendees endpoint
12. Add CAPTCHA to /attendees (optional)
13. Test in pre-prod

**Phase 4: All Endpoints (Week 2)**
14. Apply limits to all form submission endpoints
15. Apply limits to file upload endpoints
16. Apply limits to query endpoints
17. Comprehensive testing

**Phase 5: Monitoring (Week 3)**
18. Add metrics collection
19. Add admin dashboard
20. Set up alerts
21. Deploy to production

---

## 📁 23. FILE STRUCTURE - WHAT TO CREATE

### New Files Needed:

```
src/main/java/com/dechub/tanishq/
├── security/
│   ├── StoreContextValidator.java           [EXISTS]
│   ├── RateLimiterService.java              [NEW - CREATE THIS]
│   ├── RateLimitingFilter.java              [NEW - CREATE THIS]
│   ├── RateLimitType.java                   [NEW - CREATE THIS - Enum]
│   ├── RateLimitConfig.java                 [NEW - CREATE THIS]
│   ├── RateLimitMetrics.java                [NEW - CREATE THIS - Optional]
│   └── CaptchaService.java                  [NEW - CREATE THIS - Optional]
├── exception/
│   └── RateLimitExceededException.java      [NEW - CREATE THIS]
└── dto/
    └── RateLimitResponse.java               [NEW - CREATE THIS]

src/test/java/com/dechub/tanishq/
└── security/
    ├── RateLimiterServiceTest.java          [NEW - CREATE THIS]
    ├── RateLimitingFilterTest.java          [NEW - CREATE THIS]
    └── RateLimitIntegrationTest.java        [NEW - CREATE THIS]
```

### Files to Modify:

```
✏️ pom.xml                                    [ADD Bucket4j dependency]
✏️ SecurityConfig.java                        [REGISTER filter]
✏️ application.properties                     [ADD rate limit configs]
✏️ application-preprod.properties             [ADD rate limit configs]
✏️ application-prod.properties                [ADD rate limit configs]
✏️ EventsController.java                      [OPTIONAL - Add annotations or explicit checks]
```

---

## 🎨 24. CODE STRUCTURE OVERVIEW

### Class Relationships:

```
┌─────────────────────────────────────────────────────────────────┐
│                    RateLimitingFilter                           │
│  (Intercepts ALL requests before reaching controllers)          │
└────────────────────────┬────────────────────────────────────────┘
                         │ uses
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RateLimiterService                           │
│  - Creates/manages Bucket4j buckets                             │
│  - Tracks limits per IP and per user                            │
│  - Configures different limits per endpoint type                │
│  - Cleans up expired buckets                                    │
└────────────────────────┬────────────────────────────────────────┘
                         │ uses
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Bucket4j Library                             │
│  (Token bucket algorithm implementation)                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    RateLimitConfig                              │
│  (Loads configuration from application.properties)              │
└────────────────────────┬────────────────────────────────────────┘
                         │ injected into
                         ▼
                  RateLimiterService
```

---

## 🧩 25. DETAILED COMPONENT SPECIFICATIONS

### A. RateLimitType Enum

**File:** `RateLimitType.java`

**Structure:**
```java
public enum RateLimitType {
    LOGIN(5, 15),               // 5 requests per 15 minutes
    FORM_SUBMISSION(20, 1),     // 20 requests per 1 minute
    DATA_QUERY(30, 1),          // 30 requests per 1 minute
    FILE_UPLOAD(10, 5),         // 10 requests per 5 minutes
    PASSWORD_CHANGE(3, 30),     // 3 requests per 30 minutes
    DOWNLOAD(5, 10);            // 5 requests per 10 minutes
    
    private final int capacity;
    private final int refillMinutes;
    
    // Constructor and getters
}
```

### B. RateLimitConfig Class

**File:** `RateLimitConfig.java`

**Purpose:** Load configuration from properties file

**Structure:**
```java
@Configuration
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitConfig {
    private boolean enabled = true;
    
    private LoginConfig login = new LoginConfig();
    private FormConfig form = new FormConfig();
    private QueryConfig query = new QueryConfig();
    private UploadConfig upload = new UploadConfig();
    private PasswordConfig password = new PasswordConfig();
    private DownloadConfig download = new DownloadConfig();
    
    private List<String> whitelistIps = new ArrayList<>();
    private int blockDuration = 60; // minutes
    
    // Nested configuration classes
    @Data
    public static class LoginConfig {
        private int capacity = 5;
        private int refillTokens = 5;
        private int refillMinutes = 15;
    }
    // ... similar for other types
}
```

### C. RateLimiterService Class

**File:** `RateLimiterService.java`

**Key Components:**

1. **Bucket Storage:**
```java
private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
private final Map<String, Long> lastAccessTime = new ConcurrentHashMap<>();
```

2. **Bucket Creation:**
```java
private Bucket createBucket(RateLimitType type) {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(
            type.getCapacity(), 
            Refill.intervally(
                type.getCapacity(), 
                Duration.ofMinutes(type.getRefillMinutes())
            )
        ))
        .build();
}
```

3. **Main Validation Method:**
```java
public boolean allowRequest(String ip, String username, RateLimitType type) {
    // Check whitelist
    if (isWhitelisted(ip)) return true;
    
    // Check IP-based limit
    String ipKey = ip + ":" + type.name();
    Bucket ipBucket = ipBuckets.computeIfAbsent(ipKey, k -> createBucket(type));
    if (!ipBucket.tryConsume(1)) {
        logRateLimitViolation(ip, username, type, "IP");
        return false;
    }
    
    // Check user-based limit (if authenticated)
    if (username != null) {
        String userKey = username + ":" + type.name();
        Bucket userBucket = userBuckets.computeIfAbsent(userKey, k -> createBucket(type));
        if (!userBucket.tryConsume(1)) {
            logRateLimitViolation(ip, username, type, "USER");
            return false;
        }
    }
    
    updateLastAccessTime(ip, username, type);
    return true;
}
```

### D. RateLimitingFilter Class

**File:** `RateLimitingFilter.java`

**Structure:**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    @Autowired
    private RateLimiterService rateLimiterService;
    
    @Autowired
    private RateLimitConfig config;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        // Skip if disabled
        if (!config.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip for public assets and OPTIONS
        if (shouldSkipRateLimiting(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract IP and user
        String clientIP = extractClientIP(request);
        String username = extractUsername(request);
        
        // Determine rate limit type
        RateLimitType limitType = determineRateLimitType(request);
        
        // Check rate limit
        if (rateLimiterService.allowRequest(clientIP, username, limitType)) {
            // Add rate limit headers
            addRateLimitHeaders(response, clientIP, username, limitType);
            filterChain.doFilter(request, response);
        } else {
            // Block request
            sendRateLimitError(response, clientIP, username, limitType);
        }
    }
    
    private boolean shouldSkipRateLimiting(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip OPTIONS (CORS preflight)
        if ("OPTIONS".equals(method)) return true;
        
        // Skip static resources
        if (uri.startsWith("/static/")) return true;
        if (uri.startsWith("/assets/")) return true;
        if (uri.endsWith(".css") || uri.endsWith(".js") || 
            uri.endsWith(".png") || uri.endsWith(".jpg")) return true;
        
        // Skip health checks
        if (uri.equals("/health") || uri.equals("/actuator/health")) return true;
        
        return false;
    }
}
```

---

## ⚠️ 26. EDGE CASES TO HANDLE

### A. Load Balancer Scenarios

**Problem:** All requests appear to come from load balancer IP

**Solution:**
- Trust X-Forwarded-For header from load balancer
- Configure trusted proxy IPs
- Validate IP format to prevent header injection

### B. NAT/Proxy Environments

**Problem:** Multiple users behind same corporate proxy

**Solution:**
- Use combined IP + session-based limiting
- Increase limits for corporate IP ranges
- Rely more on per-user limits for authenticated requests

### C. Mobile Networks

**Problem:** Users on mobile may have changing IPs

**Solution:**
- Prefer session/user-based limiting over IP
- Use longer time windows
- More lenient limits for authenticated users

### D. API Testing/Development

**Problem:** Developers hit limits during testing

**Solution:**
- Disable rate limiting in local profile
- Whitelist developer IPs
- Provide admin endpoint to reset limits
- More lenient limits in UAT environment

---

## 🛠️ 27. CONFIGURATION PER ENVIRONMENT

### application-local.properties
```properties
# Very lenient for development
rate.limit.enabled=false
rate.limit.login.capacity=1000
rate.limit.form.capacity=1000
```

### application-uat.properties
```properties
# Moderate for testing
rate.limit.enabled=true
rate.limit.login.capacity=10
rate.limit.form.capacity=50
```

### application-preprod.properties
```properties
# Same as production
rate.limit.enabled=true
rate.limit.login.capacity=5
rate.limit.form.capacity=20
```

### application-prod.properties
```properties
# Strict limits for production
rate.limit.enabled=true
rate.limit.login.capacity=5
rate.limit.login.refill.minutes=15
rate.limit.form.capacity=20
rate.limit.form.refill.minutes=1
rate.limit.query.capacity=30
rate.limit.upload.capacity=10
rate.limit.password.capacity=3
rate.limit.download.capacity=5
rate.limit.block.duration=60
rate.limit.whitelist.ips=10.0.0.0/8,172.16.0.0/12
```

---

## 📋 28. BACKWARD COMPATIBILITY

### No Breaking Changes Expected:

✅ **Legitimate users unaffected** - Normal usage patterns won't hit limits
✅ **No API contract changes** - Same request/response formats
✅ **Graceful degradation** - Can disable via config if issues arise
✅ **No database changes** - Purely in-memory rate limiting
✅ **No authentication changes** - Works with existing session management

### Migration Path:

1. **Deploy with monitoring only** (log violations, don't block)
2. **Analyze logs for 48 hours** (identify legitimate high-volume users)
3. **Adjust limits** based on analysis
4. **Enable enforcement** (start blocking violators)
5. **Monitor and tune** over next week

---

## 🎯 29. SUCCESS CRITERIA

### Before Fix:
- ❌ Burp Suite can send 1000 requests in 10 seconds - ALL succeed
- ❌ Automated tools can spam attendee registration
- ❌ Brute force login attacks possible
- ❌ No protection against DDoS

### After Fix:
- ✅ Login attempts limited to 5 per 15 minutes
- ✅ Form submissions limited to 20 per minute
- ✅ Excess requests return 429 status
- ✅ Rate limit headers present in responses
- ✅ Security alerts logged for violations
- ✅ Legitimate users unaffected

---

## 🔍 30. VALIDATION & TESTING

### Manual Testing Steps:

**Test 1: Login Rate Limiting**
```bash
# Use Burp Suite or curl
for i in {1..10}; do
    curl -X POST http://localhost:8080/events/login \
         -H "Content-Type: application/json" \
         -d '{"code":"STORE001","password":"wrong"}' \
         -w "Status: %{http_code}\n"
    sleep 1
done

# Expected: First 5 return 401 (Unauthorized)
#          Next 5 return 429 (Too Many Requests)
```

**Test 2: Form Submission Rate Limiting**
```bash
# Rapid fire attendee submissions
for i in {1..25}; do
    curl -X POST http://localhost:8080/events/attendees \
         -F "eventId=TEST123" \
         -F "name=Test User $i" \
         -F "phone=1234567890" \
         -w "Status: %{http_code}\n"
done

# Expected: First 20 succeed
#          Next 5 return 429
```

**Test 3: Rate Limit Reset**
```bash
# Hit rate limit
curl -X POST http://localhost:8080/events/login ... # (6 times)

# Wait for window expiration
sleep 900 # 15 minutes

# Try again - should work
curl -X POST http://localhost:8080/events/login ...

# Expected: Request succeeds after window reset
```

---

## 🚀 31. DEPLOYMENT INSTRUCTIONS

### Step 1: Update Dependencies
```bash
# Add Bucket4j to pom.xml
# Run: mvn clean install
```

### Step 2: Create New Classes
```bash
# Create all new security classes listed in section 23
# Ensure package structure is correct
```

### Step 3: Update Configuration
```bash
# Add rate limit properties to all application-*.properties files
# Set appropriate limits per environment
```

### Step 4: Register Filter
```bash
# Modify SecurityConfig.java to register RateLimitingFilter
```

### Step 5: Build and Test
```bash
mvn clean package
# Run application
# Execute manual tests from section 30
```

### Step 6: Deploy to Pre-Prod
```bash
# Deploy WAR file to pre-prod Tomcat
# Monitor for 48 hours
# Analyze logs for false positives
```

### Step 7: Adjust and Deploy to Prod
```bash
# Tune limits based on pre-prod data
# Deploy to production
# Monitor closely for first week
```

---

## 💡 32. ALTERNATIVE APPROACHES

### Approach 1: Spring Cloud Gateway (If Using Microservices)
- Use built-in rate limiting
- Configure in gateway layer
- Offload from application servers

### Approach 2: Redis-Based Distributed Rate Limiting
- Share rate limits across cluster
- Requires Redis infrastructure
- More complex but more scalable

### Approach 3: WAF-Only Rate Limiting
- Configure rate limits in AWS WAF, Cloudflare, or Nginx
- No code changes needed
- Less granular control (no per-user limits)

### Approach 4: Resilience4j RateLimiter
- Alternative to Bucket4j
- Part of Spring Cloud ecosystem
- Similar functionality

**Recommended:** Approach with Bucket4j (in this document) for:
- ✅ Fine-grained control per endpoint
- ✅ Per-user AND per-IP limiting
- ✅ No external dependencies
- ✅ Easy to implement and test

---

## 📝 33. SUMMARY OF CHANGES

### New Components (7 files):
1. ✅ `RateLimiterService.java` - Core rate limiting logic
2. ✅ `RateLimitingFilter.java` - Request interceptor
3. ✅ `RateLimitType.java` - Enum for limit types
4. ✅ `RateLimitConfig.java` - Configuration loader
5. ✅ `RateLimitMetrics.java` - Metrics collection (optional)
6. ✅ `RateLimitResponse.java` - DTO for error responses
7. ✅ `RateLimitExceededException.java` - Custom exception

### Modified Components (6 files):
1. ✏️ `pom.xml` - Add Bucket4j dependency
2. ✏️ `SecurityConfig.java` - Register filter
3. ✏️ `application.properties` - Add rate limit configs
4. ✏️ `application-preprod.properties` - Add configs
5. ✏️ `application-prod.properties` - Add configs
6. ✏️ `EventsController.java` - Optional explicit checks

### Configuration Changes:
- 15+ new configuration properties
- Per-environment rate limit tuning
- IP whitelist management

### Infrastructure Changes (Optional but Recommended):
- Nginx/Apache rate limiting rules
- AWS WAF rules
- Load balancer configuration

---

## ⏱️ 34. ESTIMATED EFFORT

| Task | Effort | Dependencies |
|------|--------|--------------|
| Add Bucket4j dependency | 10 min | None |
| Create RateLimiterService | 3 hours | Dependency added |
| Create RateLimitingFilter | 2 hours | RateLimiterService |
| Create supporting classes | 1 hour | None |
| Register filter in SecurityConfig | 30 min | Filter created |
| Add configuration properties | 1 hour | None |
| Unit tests | 4 hours | All components created |
| Integration tests | 3 hours | Unit tests done |
| Manual testing | 2 hours | Deployed to UAT |
| Documentation | 2 hours | Implementation complete |
| **Total** | **~19 hours** | **~3 working days** |

---

## 🎯 35. CRITICAL ENDPOINTS - DETAILED MITIGATION

### Endpoint: POST /events/attendees

**Current State:**
- Public endpoint (no authentication)
- Processes all requests
- Creates database records
- Sends emails/SMS
- Can be spammed infinitely

**What Changes:**
1. **Add rate limiting:**
   - IP-based: 10 submissions per minute
   - Phone-based: 1 submission per phone per event
   - Event-based: Max 100 attendees per event per hour

2. **Add CAPTCHA (recommended):**
   - Show CAPTCHA after 3 submissions from same IP
   - Always show CAPTCHA for suspicious patterns

3. **Add validation:**
   - Check for duplicate phone numbers
   - Validate phone number format
   - Validate email format
   - Check event still active

4. **Add honeypot field:**
   - Hidden field that bots fill but humans don't
   - Reject if honeypot filled

**Code structure:**
```java
@PostMapping("/attendees")
@RateLimited(RateLimitType.FORM_SUBMISSION)
public ResponseDataDTO storeAttendeesData(
    @RequestParam String eventId,
    @RequestParam String name,
    @RequestParam String phone,
    @RequestParam(required = false) String captchaToken,
    HttpServletRequest request
) {
    String clientIP = getClientIP(request);
    
    // Rate limit check (done by filter, but double-check)
    if (!rateLimiterService.allowRequest(clientIP, null, RateLimitType.FORM_SUBMISSION)) {
        return errorResponse("Too many submissions. Please wait.");
    }
    
    // CAPTCHA validation (if enabled)
    if (shouldRequireCaptcha(clientIP, eventId)) {
        if (!captchaService.verify(captchaToken, clientIP)) {
            return errorResponse("CAPTCHA validation failed");
        }
    }
    
    // Check per-event rate limit
    if (rateLimiterService.getEventSubmissionCount(eventId) > 100) {
        log.warn("Event {} receiving excessive submissions", eventId);
        return errorResponse("This event is currently unavailable");
    }
    
    // Existing processing logic
    // ...
}
```

---

## 🔐 36. SECURITY HEADERS TO ADD

### In RateLimitingFilter (when rate limit exceeded):

```java
response.setStatus(429);
response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
response.setHeader("X-RateLimit-Remaining", "0");
response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
response.setHeader("Content-Type", "application/json");
```

### In SecurityConfig (global headers):

**No changes needed** - existing headers are fine

---

## 🧪 37. JUNIT TEST CASES STRUCTURE

### RateLimiterServiceTest.java

**Tests needed:**
```java
@Test
public void testAllowRequest_WithinLimit_ReturnsTrue()

@Test
public void testAllowRequest_ExceedsLimit_ReturnsFalse()

@Test
public void testAllowRequest_AfterRefill_ReturnsTrue()

@Test
public void testAllowRequest_WhitelistedIP_AlwaysReturnsTrue()

@Test
public void testAllowRequest_DifferentEndpointTypes_DifferentLimits()

@Test
public void testConcurrentRequests_ThreadSafe()

@Test
public void testCleanupExpiredBuckets_RemovesOldEntries()

@Test
public void testPerUserLimit_IndependentFromIPLimit()
```

### RateLimitingFilterTest.java

**Tests needed:**
```java
@Test
public void testFilter_AllowsRequestWithinLimit()

@Test
public void testFilter_Blocks429WhenLimitExceeded()

@Test
public void testFilter_AddsRateLimitHeaders()

@Test
public void testFilter_ExtractsIPFromXForwardedFor()

@Test
public void testFilter_SkipsStaticResources()

@Test
public void testFilter_SkipsOptionsRequests()

@Test
public void testFilter_DeterminesCorrectRateLimitType()
```

### RateLimitIntegrationTest.java

**Tests needed:**
```java
@Test
public void testLoginEndpoint_RateLimited()

@Test
public void testAttendeesEndpoint_RateLimited()

@Test
public void testUploadEndpoint_RateLimited()

@Test
public void testLegitimateTraffic_NotImpacted()

@Test
public void testRateLimitReset_AllowsRequestsAfterWindow()
```

---

## 🎪 38. OPTIONAL ENHANCEMENTS

### A. Dynamic Rate Limit Adjustment

**Auto-adjust limits based on traffic:**
```java
@Scheduled(cron = "0 0 * * * *") // Every hour
public void adjustRateLimits() {
    // If system load < 50%, increase limits by 20%
    // If system load > 80%, decrease limits by 30%
    // If under attack (>1000 blocks/min), emergency mode
}
```

### B. Geo-Blocking

**Block requests from suspicious countries:**
```java
// Add GeoIP library
<dependency>
    <groupId>com.maxmind.geoip2</groupId>
    <artifactId>geoip2</artifactId>
    <version>4.0.0</version>
</dependency>

// In filter
if (isFromBlockedCountry(clientIP)) {
    return 403; // Forbidden
}
```

### C. Behavioral Analysis

**Track request patterns:**
- User-Agent analysis
- Request timing patterns
- Mouse movement patterns (frontend)
- Distinguish bots from humans

### D. Admin Dashboard

**Create admin UI to:**
- View current rate limit status
- See blocked IPs
- Manually unblock IPs
- Adjust limits in real-time
- View attack patterns

---

## 📊 39. MONITORING & ALERTING

### Metrics to Track:

1. **Rate limit violations per hour**
2. **Top 10 blocked IP addresses**
3. **Most targeted endpoints**
4. **Average requests per user**
5. **Percentage of blocked vs allowed requests**

### Alerts to Configure:

**Alert 1: High Volume Attack**
```
Trigger: >100 rate limit violations in 5 minutes
Action: Email security team, page on-call
```

**Alert 2: Brute Force Detected**
```
Trigger: >20 login failures for same username
Action: Lock account, email user, alert security
```

**Alert 3: DDoS Pattern**
```
Trigger: >1000 blocked requests from single IP
Action: Auto-ban IP for 24 hours, alert security
```

### Logging Configuration:

**Add to logback-spring.xml:**
```xml
<logger name="com.dechub.tanishq.security.RateLimiter" level="WARN"/>

<!-- Separate log file for rate limiting -->
<appender name="RATE_LIMIT_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/rate-limit.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/rate-limit.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss} [%level] %msg%n</pattern>
    </encoder>
</appender>
```

---

## 🎓 40. BEST PRACTICES IMPLEMENTED

### Token Bucket Algorithm (Bucket4j)
✅ **Smooth rate limiting** - Allows bursts within limits
✅ **Fair distribution** - Tokens refill at steady rate
✅ **Simple to understand** - Clear capacity and refill rules

### Defense in Depth
✅ **Multiple layers** - Filter + Service + Controller validation
✅ **IP + User limiting** - Can't bypass by changing IP or user
✅ **Different limits per endpoint** - Appropriate to risk level

### Graceful Degradation
✅ **Can be disabled via config** - Emergency override
✅ **Whitelist support** - Don't block critical systems
✅ **Informative error messages** - Users know when to retry

### Observability
✅ **Comprehensive logging** - Track all violations
✅ **Metrics collection** - Measure effectiveness
✅ **Rate limit headers** - Clients can adapt behavior

---

## ⚖️ 41. RISK ASSESSMENT

### Implementation Risks:

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Block legitimate users | Medium | High | Conservative limits, whitelist, monitoring |
| Performance degradation | Low | Medium | In-memory, concurrent structures, cleanup |
| False positives (NAT) | Medium | Medium | Session-based limits, corporate IP ranges |
| Memory leak | Low | High | Scheduled cleanup, max bucket count |
| Distributed environment issues | High | Medium | Document clustering requirements, Redis option |

### Benefits vs. Risks:

**Benefits:**
- ✅ Prevents brute force attacks (Critical)
- ✅ Prevents DDoS attacks (Critical)
- ✅ Prevents spam/abuse (High)
- ✅ Reduces infrastructure costs (Medium)
- ✅ Improves system stability (High)

**Risks:**
- ⚠️ May block legitimate users (Low probability with proper limits)
- ⚠️ Adds complexity (Minimal - well-encapsulated)
- ⚠️ Requires monitoring (Worth the security benefit)

**Verdict:** Benefits FAR outweigh risks - IMPLEMENT THIS FIX

---

## 📞 42. STAKEHOLDER COMMUNICATION

### For Security Team:
"Implementing comprehensive rate limiting with Bucket4j token bucket algorithm. Will prevent brute force, DDoS, and spam attacks. Fixes CVSS 8.1 vulnerability."

### For Development Team:
"Adding rate limiting filter. Won't affect existing code - transparent to controllers. Need to handle 429 status codes in frontend."

### For QA Team:
"New 429 error responses for rate limit exceeded. Test cases: rapid login attempts, form spam, legitimate high-volume users. Check rate limit headers."

### For DevOps Team:
"Consider adding WAF-level rate limiting for defense-in-depth. May need Redis if clustering. Monitor new rate-limit.log file."

### For Business Users:
"Adding protection against automated attacks. Normal users won't notice any difference. Abusive behavior will be blocked."

---

## ✅ 43. FINAL CHECKLIST

### Code Changes:
- [ ] Add Bucket4j dependency to pom.xml
- [ ] Create RateLimiterService.java (core logic)
- [ ] Create RateLimitingFilter.java (interceptor)
- [ ] Create RateLimitType.java (enum)
- [ ] Create RateLimitConfig.java (configuration)
- [ ] Create RateLimitResponse.java (DTO)
- [ ] Create RateLimitExceededException.java (exception)
- [ ] Modify SecurityConfig.java (register filter)
- [ ] Add properties to application.properties (all environments)
- [ ] Optional: Add CAPTCHA integration
- [ ] Optional: Add RateLimitMetrics.java

### Testing:
- [ ] Unit tests for RateLimiterService
- [ ] Unit tests for RateLimitingFilter
- [ ] Integration tests for all critical endpoints
- [ ] Manual testing with Burp Suite (replicate POC)
- [ ] Load testing to ensure no performance impact
- [ ] Test in multiple environments (local, UAT, preprod)

### Documentation:
- [ ] Update API documentation with 429 responses
- [ ] Document rate limit headers
- [ ] Create troubleshooting guide
- [ ] Update deployment guide
- [ ] Create monitoring runbook

### Deployment:
- [ ] Build application (mvn clean package)
- [ ] Deploy to UAT
- [ ] Monitor for 48 hours
- [ ] Adjust limits based on metrics
- [ ] Deploy to pre-prod
- [ ] Monitor for 1 week
- [ ] Deploy to production
- [ ] Configure WAF rules (if available)

### Post-Deployment:
- [ ] Monitor rate limit logs daily for 1 week
- [ ] Set up automated alerts
- [ ] Review metrics weekly
- [ ] Tune limits based on usage patterns
- [ ] Document any issues and resolutions

---

## 🎬 44. IMPLEMENTATION ORDER

### Day 1: Foundation
1. Add Bucket4j dependency
2. Create RateLimitType enum
3. Create RateLimitConfig class
4. Add configuration properties
5. Create basic RateLimiterService (without all features)

### Day 2: Core Implementation
6. Complete RateLimiterService with all methods
7. Create RateLimitingFilter
8. Register filter in SecurityConfig
9. Unit tests for service and filter

### Day 3: Testing & Refinement
10. Integration tests
11. Manual testing with Burp Suite
12. Fix any issues discovered
13. Add logging and metrics
14. Deploy to UAT

### Week 2: Validation & Deployment
15. Monitor UAT for false positives
16. Tune rate limits
17. Deploy to pre-prod
18. Monitor for 1 week
19. Deploy to production

### Week 3: Enhancements (Optional)
20. Add CAPTCHA integration
21. Add admin dashboard
22. Configure WAF rules
23. Implement advanced metrics

---

## 🚨 45. CRITICAL NOTES

### IMPORTANT:

1. **DO NOT block legitimate traffic** - Start with lenient limits, tune down
2. **TEST THOROUGHLY** - Rate limiting bugs can lock out all users
3. **MONITOR CLOSELY** - Watch logs after deployment
4. **HAVE ROLLBACK PLAN** - Can disable via config without redeployment
5. **DOCUMENT WHITELIST** - Maintain list of trusted IPs
6. **COMMUNICATE** - Inform users of any expected behavior changes
7. **GRADUAL ROLLOUT** - UAT → Pre-prod → Production (1 week each)

### RED FLAGS TO WATCH:

- ⚠️ Sudden increase in 429 errors from legitimate users
- ⚠️ Customer complaints about "too many requests"
- ⚠️ Increased memory usage (bucket leak)
- ⚠️ Mobile users (changing IPs) getting blocked
- ⚠️ Corporate users behind NAT getting blocked

### EMERGENCY PROCEDURES:

**If rate limiting causes issues:**
1. Set `rate.limit.enabled=false` in properties
2. Restart application (or just reload properties if using Spring Cloud Config)
3. Investigate root cause
4. Fix and redeploy with adjusted limits

**Quick disable without redeployment:**
- Use Spring Boot Admin to update property
- Or use environment variable override: `RATE_LIMIT_ENABLED=false`

---

## 📖 46. REFERENCES & STANDARDS

### Standards Addressed:
- **OWASP API Security Top 10** - API4:2023 Unrestricted Resource Consumption
- **OWASP Top 10** - A05:2021 Security Misconfiguration
- **NIST SP 800-53** - SC-5 Denial of Service Protection
- **CWE-770** - Allocation of Resources Without Limits or Throttling
- **PCI DSS 6.5.10** - Broken Authentication and Session Management

### Libraries Used:
- **Bucket4j 7.6.0** - https://github.com/vladimir-bukhtoyarov/bucket4j
- **Spring Security** - Filter integration
- **Google Guava** - Cache management (optional)

### Similar Implementations:
- Spring Cloud Gateway rate limiting
- Netflix Zuul rate limiting
- Kong API Gateway rate limiting
- Express Rate Limit (Node.js equivalent)

---

## 🎉 47. EXPECTED OUTCOMES

### Before Implementation:
- ❌ **POC 1:** Burp Suite can send 1000 login requests - all processed
- ❌ **POC 2:** Browser shows all requests successfully completed
- ❌ **POC 3:** Attendee endpoint can be spammed infinitely
- ❌ **POC 4:** No protection against automated tools

### After Implementation:
- ✅ **POC 1:** Burp Suite can send only 5 login requests per 15 min
- ✅ **POC 2:** 6th request returns 429 Too Many Requests
- ✅ **POC 3:** Attendee spam limited to 20 per minute
- ✅ **POC 4:** Automated tools blocked with 429 status
- ✅ **Bonus:** Security logs show attack attempts
- ✅ **Bonus:** Metrics track attack patterns

---

## 🏁 CONCLUSION

### Implementation Summary:

**What this fix does:**
- ✅ Adds comprehensive rate limiting to ALL endpoints
- ✅ Prevents brute force login attacks
- ✅ Prevents form submission spam
- ✅ Prevents DDoS attacks
- ✅ Protects against automated abuse
- ✅ Adds security logging and metrics
- ✅ Compatible with existing authentication fix

**What needs to be created:**
- 7 new Java classes (RateLimiterService, Filter, Config, DTOs)
- Configuration properties for all environments
- Unit and integration tests
- Optional: CAPTCHA integration
- Optional: WAF/load balancer rules

**What needs to be modified:**
- 1 line in pom.xml (add dependency)
- ~10 lines in SecurityConfig.java (register filter)
- ~50 lines in application.properties (add configs)
- 0 lines in EventsController.java (optional improvements)

**Effort:** ~3 days development + 1 week testing/deployment

**Impact:** Fixes CVSS 8.1 High severity vulnerability

---

**Last Updated:** March 4, 2026  
**Status:** Planning Complete - Ready for Implementation  
**Next Step:** Begin Day 1 tasks (Foundation)


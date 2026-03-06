# 🛠️ Hands-On Learning - Code Examples & Exercises

**Practical guide with runnable code examples from your implementations**

---

## 📑 SECTIONS

1. [Session Management - Code Deep Dive](#session-management)
2. [Input Validation - Practice Examples](#input-validation)
3. [Rate Limiting - Algorithm Walkthrough](#rate-limiting)
4. [Exception Handling - Real Scenarios](#exception-handling)
5. [Password Hashing - Step-by-Step](#password-hashing)
6. [Filters - Build Your Own](#filters)
7. [Testing Security - Practical Tests](#testing)
8. [Complete Mini Projects](#mini-projects)

---

## 1. SESSION MANAGEMENT

### 📖 Concept: HttpSession API

**From your code (EventsController):**
```java
@PostMapping("/login")
public ResponseEntity<?> eventsLogin(@Valid @RequestBody LoginDTO loginDTO, 
                                      HttpSession session) {
    // Authenticate user
    EventsLoginResponseDTO response = tanishqPageService.eventsLogin(
        loginDTO.getCode(), 
        loginDTO.getPassword()
    );
    
    if (response.isStatus()) {
        // SUCCESS: Store in session
        String userType = determineUserType(loginDTO.getCode());
        storeContextValidator.setAuthenticatedUser(session, code, userType);
        
        log.info("User {} logged in with type {}", code, userType);
        return ResponseEntity.ok(Map.of("status", true));
    } else {
        // FAILURE: Don't create session
        return ResponseEntity.status(401)
            .body(Map.of("status", false, "message", "Invalid credentials"));
    }
}
```

### 🎯 Exercise 1: Session Tracking

**Task:** Create an endpoint that shows session information

```java
@GetMapping("/session-info")
public ResponseEntity<?> getSessionInfo(HttpSession session) {
    Map<String, Object> info = new HashMap<>();
    
    // Session basic info
    info.put("sessionId", session.getId());
    info.put("creationTime", new Date(session.getCreationTime()));
    info.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
    info.put("maxInactiveInterval", session.getMaxInactiveInterval());
    
    // Calculate remaining time
    long now = System.currentTimeMillis();
    long lastAccessed = session.getLastAccessedTime();
    long timeout = session.getMaxInactiveInterval() * 1000;
    long remaining = timeout - (now - lastAccessed);
    
    info.put("remainingSeconds", remaining / 1000);
    info.put("willExpireAt", new Date(lastAccessed + timeout));
    
    // Session attributes
    Enumeration<String> attributeNames = session.getAttributeNames();
    Map<String, Object> attributes = new HashMap<>();
    while (attributeNames.hasMoreElements()) {
        String name = attributeNames.nextElement();
        // Don't expose sensitive data
        if (!name.equals("password")) {
            attributes.put(name, session.getAttribute(name));
        }
    }
    info.put("attributes", attributes);
    
    return ResponseEntity.ok(info);
}
```

**Test it:**
1. Login to create session
2. Call `/session-info` to see details
3. Wait and call again to see time remaining
4. Logout and call again (should fail)

---

### 🎯 Exercise 2: Session Timeout Warning

**Task:** Add endpoint to check if session is about to expire

```java
@GetMapping("/session-status")
public ResponseEntity<?> checkSessionStatus(HttpSession session) {
    if (session == null || !storeContextValidator.isAuthenticated(session)) {
        return ResponseEntity.status(401)
            .body(Map.of("authenticated", false));
    }
    
    long now = System.currentTimeMillis();
    long lastAccessed = session.getLastAccessedTime();
    long timeoutMs = session.getMaxInactiveInterval() * 1000;
    long elapsedMs = now - lastAccessed;
    long remainingMs = timeoutMs - elapsedMs;
    
    boolean aboutToExpire = remainingMs < (5 * 60 * 1000); // Less than 5 minutes
    
    Map<String, Object> status = new HashMap<>();
    status.put("authenticated", true);
    status.put("remainingSeconds", remainingMs / 1000);
    status.put("aboutToExpire", aboutToExpire);
    status.put("username", session.getAttribute("authenticatedUser"));
    
    return ResponseEntity.ok(status);
}

@PostMapping("/extend-session")
public ResponseEntity<?> extendSession(HttpSession session) {
    if (session != null && storeContextValidator.isAuthenticated(session)) {
        // Touch session to extend timeout
        session.setAttribute("lastActivity", System.currentTimeMillis());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Session extended"
        ));
    }
    return ResponseEntity.status(401).body(Map.of("success", false));
}
```

**Frontend Integration:**
```javascript
// Check session every minute
setInterval(async () => {
    const response = await fetch('/events/session-status');
    const status = await response.json();
    
    if (status.aboutToExpire) {
        // Show warning dialog
        showSessionWarning(status.remainingSeconds);
    }
}, 60000); // Every 60 seconds

function showSessionWarning(remainingSeconds) {
    const minutes = Math.floor(remainingSeconds / 60);
    const shouldExtend = confirm(
        `Your session will expire in ${minutes} minutes. Extend session?`
    );
    
    if (shouldExtend) {
        fetch('/events/extend-session', { method: 'POST' });
    }
}
```

---

### 🎯 Exercise 3: Session Activity Tracking

**Task:** Track user actions in session

```java
@Component
public class SessionActivityTracker {
    
    private static final Logger log = LoggerFactory.getLogger(SessionActivityTracker.class);
    
    public void trackActivity(HttpSession session, String action, String details) {
        if (session == null) return;
        
        List<ActivityLog> activities = (List<ActivityLog>) session.getAttribute("activities");
        if (activities == null) {
            activities = new ArrayList<>();
            session.setAttribute("activities", activities);
        }
        
        activities.add(new ActivityLog(
            action,
            details,
            LocalDateTime.now()
        ));
        
        // Keep only last 50 activities
        if (activities.size() > 50) {
            activities.remove(0);
        }
        
        log.debug("Activity tracked: {} - {}", action, details);
    }
    
    public List<ActivityLog> getRecentActivities(HttpSession session) {
        if (session == null) return Collections.emptyList();
        
        List<ActivityLog> activities = (List<ActivityLog>) session.getAttribute("activities");
        return activities != null ? activities : Collections.emptyList();
    }
    
    public static class ActivityLog {
        private String action;
        private String details;
        private LocalDateTime timestamp;
        
        // Constructor, getters, setters
    }
}

// Usage in controller
@PostMapping("/upload")
public ResponseEntity<?> upload(..., HttpSession session) {
    sessionActivityTracker.trackActivity(session, "FILE_UPLOAD", "Uploaded: " + filename);
    // ... rest of upload logic
}
```

---

## 2. INPUT VALIDATION

### 📖 Concept: Bean Validation Lifecycle

**From your code (LoginDTO):**
```java
public class LoginDTO {
    @NotBlank(message = "Code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;
    
    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    private String password;
    
    // Getters and setters
}

// In controller
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
    // If validation fails, MethodArgumentNotValidException is thrown
    // GlobalExceptionHandler catches it and returns 400 with field errors
}
```

### 🎯 Exercise 4: Create Custom Validator

**Task:** Create @ValidIndianPAN annotation

**Step 1: Create annotation**
```java
package com.dechub.tanishq.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndianPanValidator.class)
@Documented
public @interface ValidIndianPAN {
    
    String message() default "Invalid PAN format. Must be like ABCDE1234F";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
```

**Step 2: Create validator class**
```java
package com.dechub.tanishq.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IndianPanValidator implements ConstraintValidator<ValidIndianPAN, String> {
    
    // PAN format: 5 letters + 4 digits + 1 letter (e.g., ABCDE1234F)
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    
    @Override
    public void initialize(ValidIndianPAN constraintAnnotation) {
        // Initialization if needed
    }
    
    @Override
    public boolean isValid(String pan, ConstraintValidatorContext context) {
        // Null is considered valid (use @NotNull separately if required)
        if (pan == null || pan.isEmpty()) {
            return true;
        }
        
        // Validate format
        if (!PAN_PATTERN.matcher(pan.toUpperCase()).matches()) {
            return false;
        }
        
        // Validate check digit (4th character must be 'P' for individual)
        // This is domain-specific validation
        char fourthChar = pan.charAt(3);
        if (fourthChar != 'P' && fourthChar != 'C') {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Invalid PAN type. 4th character must be 'P' (person) or 'C' (company)"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
```

**Step 3: Use in DTO**
```java
public class TaxDetailsDTO {
    
    @NotBlank(message = "PAN is required for tax purposes")
    @ValidIndianPAN
    private String panNumber;
    
    // Getters and setters
}
```

**Step 4: Test it**
```java
@Test
public void testValidPAN() {
    TaxDetailsDTO dto = new TaxDetailsDTO();
    dto.setPanNumber("ABCDE1234F");  // Valid
    
    Set<ConstraintViolation<TaxDetailsDTO>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty());
}

@Test
public void testInvalidPAN() {
    TaxDetailsDTO dto = new TaxDetailsDTO();
    dto.setPanNumber("INVALID");  // Invalid
    
    Set<ConstraintViolation<TaxDetailsDTO>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
}
```

---

### 🎯 Exercise 5: Cross-Field Validation

**Task:** Validate password confirmation matches

**Step 1: Create annotation**
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    String passwordField() default "password";
    String confirmPasswordField() default "confirmPassword";
}
```

**Step 2: Create validator**
```java
public class PasswordMatchesValidator 
    implements ConstraintValidator<PasswordMatches, Object> {
    
    private String passwordField;
    private String confirmPasswordField;
    
    @Override
    public void initialize(PasswordMatches annotation) {
        this.passwordField = annotation.passwordField();
        this.confirmPasswordField = annotation.confirmPasswordField();
    }
    
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            // Use reflection to get field values
            Field passwordFieldObj = object.getClass().getDeclaredField(passwordField);
            Field confirmPasswordFieldObj = object.getClass().getDeclaredField(confirmPasswordField);
            
            passwordFieldObj.setAccessible(true);
            confirmPasswordFieldObj.setAccessible(true);
            
            String password = (String) passwordFieldObj.get(object);
            String confirmPassword = (String) confirmPasswordFieldObj.get(object);
            
            return password != null && password.equals(confirmPassword);
            
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Step 3: Use in DTO**
```java
@PasswordMatches(
    passwordField = "newPassword",
    confirmPasswordField = "confirmPassword"
)
public class ChangePasswordDTO {
    @NotBlank
    private String oldPassword;
    
    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
    
    // Getters and setters
}
```

---

### 🎯 Exercise 6: Complex Validation Rules

**Task:** Validate event date is in future and within 1 year

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureEventDateValidator.class)
public @interface ValidEventDate {
    String message() default "Event date must be in future and within 1 year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int maxMonthsInFuture() default 12;
}

public class FutureEventDateValidator 
    implements ConstraintValidator<ValidEventDate, LocalDate> {
    
    private int maxMonthsInFuture;
    
    @Override
    public void initialize(ValidEventDate annotation) {
        this.maxMonthsInFuture = annotation.maxMonthsInFuture();
    }
    
    @Override
    public boolean isValid(LocalDate eventDate, ConstraintValidatorContext context) {
        if (eventDate == null) {
            return true;  // Use @NotNull separately
        }
        
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(maxMonthsInFuture);
        
        // Must be in future
        if (eventDate.isBefore(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Event date must be in the future"
            ).addConstraintViolation();
            return false;
        }
        
        // Must be within max months
        if (eventDate.isAfter(maxDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Event date cannot be more than " + maxMonthsInFuture + " months in future"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}

// Usage
public class CreateEventDTO {
    @NotNull
    @ValidEventDate(maxMonthsInFuture = 12)
    private LocalDate eventDate;
}
```

---

## 3. RATE LIMITING

### 📖 Concept: Token Bucket Algorithm

**Visual Representation:**
```
Time: 0s                      Bucket: [🪙🪙🪙🪙🪙🪙🪙🪙🪙🪙] (10 tokens)
Request 1 arrives             Bucket: [🪙🪙🪙🪙🪙🪙🪙🪙🪙  ] (9 tokens) ✅ Allow
Request 2 arrives             Bucket: [🪙🪙🪙🪙🪙🪙🪙🪙    ] (8 tokens) ✅ Allow
...
Request 10 arrives            Bucket: [                  ] (0 tokens) ✅ Allow
Request 11 arrives            Bucket: [                  ] (0 tokens) ❌ DENY (429)

Time: 6s (10% of 60s)         Bucket: [🪙                ] (1 token refilled)
Request 12 arrives            Bucket: [                  ] (0 tokens) ✅ Allow

Time: 60s                     Bucket: [🪙🪙🪙🪙🪙🪙🪙🪙🪙🪙] (10 tokens refilled)
```

### 🎯 Exercise 7: Implement Simple Rate Limiter

**Task:** Build rate limiter from scratch (without Bucket4j)

```java
public class SimpleRateLimiter {
    
    private final int maxRequests;
    private final long windowMs;
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();
    
    public SimpleRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        RequestWindow window = windows.computeIfAbsent(clientId, 
            k -> new RequestWindow(now, 0));
        
        synchronized (window) {
            // Check if window has expired
            if (now - window.startTime >= windowMs) {
                // Reset window
                window.startTime = now;
                window.requestCount = 0;
            }
            
            // Check if under limit
            if (window.requestCount < maxRequests) {
                window.requestCount++;
                return true;
            } else {
                return false;  // Rate limit exceeded
            }
        }
    }
    
    private static class RequestWindow {
        long startTime;
        int requestCount;
        
        RequestWindow(long startTime, int requestCount) {
            this.startTime = startTime;
            this.requestCount = requestCount;
        }
    }
    
    // Cleanup old windows periodically
    public void cleanup() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> 
            now - entry.getValue().startTime > windowMs * 2
        );
    }
}

// Usage
SimpleRateLimiter limiter = new SimpleRateLimiter(10, 60_000); // 10 req/min

if (limiter.allowRequest(clientIP)) {
    // Allow request
} else {
    // Deny - rate limit exceeded
}
```

**Compare with Bucket4j:**
| Feature | Simple (Your code) | Bucket4j (Production) |
|---------|--------------------|-----------------------|
| Algorithm | Fixed window | Token bucket |
| Bursts | Not handled well | Handles smoothly |
| Refill | All at once | Gradual |
| Memory | Manual cleanup | Automatic |
| Thread safety | Manual sync | Built-in |
| Redis support | Manual | Built-in |

---

### 🎯 Exercise 8: Endpoint-Specific Limits

**Task:** Different limits for different endpoints

```java
@Component
public class AdvancedRateLimitingFilter implements Filter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Endpoint-specific configurations
    private static final Map<String, RateLimitConfig> ENDPOINT_LIMITS = Map.of(
        "/events/login",           new RateLimitConfig(5, 1),    // 5 req/min
        "/events/upload",          new RateLimitConfig(10, 1),   // 10 req/min
        "/events/attendees",       new RateLimitConfig(20, 1),   // 20 req/min
        "/events/getevents",       new RateLimitConfig(100, 1)   // 100 req/min
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String endpoint = req.getRequestURI();
        RateLimitConfig config = getConfigForEndpoint(endpoint);
        
        if (config != null) {
            String clientIP = getClientIP(req);
            String bucketKey = clientIP + ":" + endpoint;  // Separate bucket per endpoint
            
            Bucket bucket = buckets.computeIfAbsent(bucketKey, 
                k -> createBucket(config));
            
            if (!bucket.tryConsume(1)) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write(String.format(
                    "{\"success\": false, \"message\": \"Rate limit exceeded. Max %d requests per minute.\"}",
                    config.maxRequests
                ));
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private RateLimitConfig getConfigForEndpoint(String endpoint) {
        return ENDPOINT_LIMITS.getOrDefault(endpoint, null);
    }
    
    private Bucket createBucket(RateLimitConfig config) {
        Bandwidth limit = Bandwidth.classic(
            config.maxRequests,
            Refill.greedy(config.maxRequests, Duration.ofMinutes(config.durationMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }
    
    private static class RateLimitConfig {
        int maxRequests;
        int durationMinutes;
        
        RateLimitConfig(int maxRequests, int durationMinutes) {
            this.maxRequests = maxRequests;
            this.durationMinutes = durationMinutes;
        }
    }
}
```

---

### 🎯 Exercise 9: User-Based Rate Limiting

**Task:** Rate limit by authenticated user instead of IP

```java
@Component
public class UserRateLimitingFilter implements Filter {
    
    @Autowired
    private StoreContextValidator storeContextValidator;
    
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        // Rate limit authenticated users
        if (session != null && storeContextValidator.isAuthenticated(session)) {
            String username = storeContextValidator.getAuthenticatedUser(session);
            Bucket bucket = userBuckets.computeIfAbsent(username, k -> createBucket());
            
            if (!bucket.tryConsume(1)) {
                res.setStatus(429);
                res.getWriter().write(
                    "{\"success\": false, \"message\": \"You're making too many requests. Please slow down.\"}"
                );
                return;
            }
        } else {
            // Rate limit by IP for unauthenticated requests
            String clientIP = getClientIP(req);
            Bucket bucket = userBuckets.computeIfAbsent("IP:" + clientIP, k -> createBucket());
            
            if (!bucket.tryConsume(1)) {
                res.setStatus(429);
                res.getWriter().write(
                    "{\"success\": false, \"message\": \"Too many requests. Please login.\"}"
                );
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
```

**Benefits:**
- Authenticated users get higher limits
- Separate tracking for logged-in vs anonymous
- Can implement tiered limits (premium users get more)

---

## 4. EXCEPTION HANDLING

### 📖 Concept: Exception Handler Precedence

**From your code (GlobalExceptionHandler):**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Most specific - handles validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDataDTO> handleValidation(...) {
        // Returns 400 with field-level errors
    }
    
    // Specific - handles database errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDataDTO> handleDatabaseError(...) {
        // Returns 409 with generic message
    }
    
    // Less specific - handles parent class
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseDataDTO> handleDataAccess(...) {
        // Catches all database exceptions
    }
    
    // Catch-all - handles everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDataDTO> handleGeneral(...) {
        // Returns 500 with error reference
    }
}
```

**Execution Order:**
```
Exception thrown: DataIntegrityViolationException
    ↓
1. Check for exact match → handleDatabaseError() ✅ MATCHES
2. If not found, check parent class → handleDataAccess()
3. If not found, check grandparent → handleGeneral()
```

### 🎯 Exercise 10: Custom Business Exception

**Task:** Create business-specific exception with context

**Step 1: Create custom exception**
```java
package com.dechub.tanishq.exception;

public class StoreAccessDeniedException extends RuntimeException {
    
    private final String username;
    private final String requestedStore;
    private final String userType;
    
    public StoreAccessDeniedException(String username, String requestedStore, String userType) {
        super(String.format("User %s (type: %s) denied access to store %s", 
                           username, userType, requestedStore));
        this.username = username;
        this.requestedStore = requestedStore;
        this.userType = userType;
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getRequestedStore() { return requestedStore; }
    public String getUserType() { return userType; }
}
```

**Step 2: Throw in service**
```java
public List<Event> getStoreEvents(String storeCode, HttpSession session) {
    if (!storeContextValidator.validateStoreAccess(session, storeCode)) {
        String username = storeContextValidator.getAuthenticatedUser(session);
        String userType = (String) session.getAttribute("userType");
        
        throw new StoreAccessDeniedException(username, storeCode, userType);
    }
    
    // Continue with business logic
    return eventRepository.findByStoreCode(storeCode);
}
```

**Step 3: Handle in GlobalExceptionHandler**
```java
@ExceptionHandler(StoreAccessDeniedException.class)
public ResponseEntity<ResponseDataDTO> handleStoreAccessDenied(
        StoreAccessDeniedException ex, HttpServletRequest request) {
    
    String errorRef = generateErrorReference();
    
    // Log with full context
    log.error("[{}] Store access denied - User: {}, Store: {}, Type: {}, URI: {}", 
              errorRef, ex.getUsername(), ex.getRequestedStore(), 
              ex.getUserType(), request.getRequestURI());
    
    // Return generic message to user
    ResponseDataDTO response = new ResponseDataDTO();
    response.setStatus(false);
    response.setMessage("You do not have permission to access this store");
    
    Map<String, String> errorDetails = new HashMap<>();
    errorDetails.put("errorReference", errorRef);
    errorDetails.put("timestamp", getTimestamp());
    response.setResult(errorDetails);
    
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
}
```

**Benefits:**
- Type-safe exception with context
- Clear exception name (self-documenting)
- Specific error handling
- Rich logging information

---

### 🎯 Exercise 11: Error Analytics

**Task:** Track and analyze error patterns

```java
@Component
public class ErrorAnalytics {
    
    private final Map<String, ErrorStats> errorStats = new ConcurrentHashMap<>();
    
    public void recordError(String errorType, String endpoint) {
        String key = errorType + ":" + endpoint;
        
        errorStats.computeIfAbsent(key, k -> new ErrorStats())
                  .increment();
    }
    
    public Map<String, ErrorStats> getErrorStats() {
        return new HashMap<>(errorStats);
    }
    
    public List<ErrorStats> getTopErrors(int limit) {
        return errorStats.entrySet().stream()
            .map(e -> e.getValue().setKey(e.getKey()))
            .sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public static class ErrorStats {
        private String key;
        private AtomicInteger count = new AtomicInteger(0);
        private LocalDateTime firstOccurrence = LocalDateTime.now();
        private LocalDateTime lastOccurrence;
        
        public void increment() {
            count.incrementAndGet();
            lastOccurrence = LocalDateTime.now();
        }
        
        public int getCount() { return count.get(); }
        public ErrorStats setKey(String key) { this.key = key; return this; }
        // Other getters
    }
}

// Use in GlobalExceptionHandler
@Autowired
private ErrorAnalytics errorAnalytics;

@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleGeneral(Exception ex, HttpServletRequest request) {
    errorAnalytics.recordError(ex.getClass().getSimpleName(), request.getRequestURI());
    // ... rest of handling
}

// Endpoint to view stats
@GetMapping("/admin/error-stats")
public ResponseEntity<?> getErrorStats(HttpSession session) {
    // Check admin access
    if (!isAdmin(session)) {
        return ResponseEntity.status(403).build();
    }
    
    return ResponseEntity.ok(errorAnalytics.getTopErrors(20));
}
```

---

## 5. PASSWORD HASHING

### 📖 Concept: BCrypt in Action

**From your code (TanishqPageService):**
```java
public EventsLoginResponseDTO eventsLogin(String storeCode, String password) {
    // Fetch user from database
    Optional<User> userOpt = userRepository.findByUsername(storeCode);
    
    if (userOpt.isEmpty()) {
        return new EventsLoginResponseDTO(false, "User not found");
    }
    
    String storedPassword = userOpt.get().getPassword();
    boolean passwordMatches;
    
    // Check if password is BCrypt hashed or plain text
    if (storedPassword.startsWith("$2a$") || 
        storedPassword.startsWith("$2b$") || 
        storedPassword.startsWith("$2y$")) {
        // BCrypt - use encoder
        passwordMatches = passwordEncoder.matches(password, storedPassword);
    } else {
        // Plain text (legacy) - direct comparison
        passwordMatches = password.equals(storedPassword);
        
        // OPTIONAL: Upgrade to BCrypt on successful login
        if (passwordMatches) {
            String hashed = passwordEncoder.encode(password);
            userRepository.updatePassword(userOpt.get().getId(), hashed);
            log.info("Password upgraded to BCrypt for user: {}", storeCode);
        }
    }
    
    if (passwordMatches) {
        return new EventsLoginResponseDTO(true, "Login successful");
    } else {
        return new EventsLoginResponseDTO(false, "Invalid password");
    }
}
```

### 🎯 Exercise 12: Password Strength Validator

**Task:** Validate password meets security requirements

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password does not meet security requirements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int minLength() default 8;
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecial() default true;
}

public class StrongPasswordValidator 
    implements ConstraintValidator<StrongPassword, String> {
    
    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;
    
    @Override
    public void initialize(StrongPassword annotation) {
        this.minLength = annotation.minLength();
        this.requireUppercase = annotation.requireUppercase();
        this.requireLowercase = annotation.requireLowercase();
        this.requireDigit = annotation.requireDigit();
        this.requireSpecial = annotation.requireSpecial();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return true;  // Use @NotBlank separately
        }
        
        List<String> violations = new ArrayList<>();
        
        if (password.length() < minLength) {
            violations.add("at least " + minLength + " characters");
        }
        
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            violations.add("one uppercase letter");
        }
        
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            violations.add("one lowercase letter");
        }
        
        if (requireDigit && !password.matches(".*\\d.*")) {
            violations.add("one digit");
        }
        
        if (requireSpecial && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            violations.add("one special character");
        }
        
        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Password must contain " + String.join(", ", violations)
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}

// Usage
public class ChangePasswordDTO {
    @NotBlank
    private String oldPassword;
    
    @NotBlank
    @StrongPassword(minLength = 12)
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
}
```

---

### 🎯 Exercise 13: Password History

**Task:** Prevent password reuse

**Step 1: Create entity**
```java
@Entity
@Table(name = "password_history")
public class PasswordHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
    
    // Getters and setters
}

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUsernameOrderByChangedAtDesc(String username);
}
```

**Step 2: Check on password change**
```java
public ResponseDataDTO changePassword(String username, String oldPassword, String newPassword) {
    // Verify old password
    // ...
    
    // Check password history
    List<PasswordHistory> history = passwordHistoryRepository
        .findTop5ByUsernameOrderByChangedAtDesc(username);
    
    for (PasswordHistory oldHash : history) {
        if (passwordEncoder.matches(newPassword, oldHash.getPasswordHash())) {
            return new ResponseDataDTO(false, 
                "Cannot reuse any of your last 5 passwords");
        }
    }
    
    // Hash new password
    String hashedPassword = passwordEncoder.encode(newPassword);
    
    // Save to history
    passwordHistoryRepository.save(new PasswordHistory(
        username,
        hashedPassword,
        LocalDateTime.now()
    ));
    
    // Update current password
    userRepository.updatePassword(username, hashedPassword);
    
    return new ResponseDataDTO(true, "Password changed successfully");
}
```

---

## 6. FILTERS

### 📖 Concept: Servlet Filter Chain

**Filter Execution Order:**
```java
// Order 1 - Rate Limiting
@Component
@Order(1)
public class RateLimitingFilter implements Filter { }

// Order 2 - Server Headers
@Component
@Order(2)
public class ServerHeaderFilter implements Filter { }

// Order 3 - Logging (new)
@Component
@Order(3)
public class RequestLoggingFilter implements Filter { }

// Then Spring Security filters
// Then DispatcherServlet → Controller
```

### 🎯 Exercise 14: Request Logging Filter

**Task:** Log all requests with timing

```java
@Component
@Order(3)
public class RequestLoggingFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Add request ID to response header (for tracking)
        res.setHeader("X-Request-ID", requestId);
        
        // Log request
        log.info("[{}] → {} {} from {}", 
                 requestId, 
                 req.getMethod(), 
                 req.getRequestURI(), 
                 getClientIP(req));
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response
            log.info("[{}] ← {} {} - Status: {} - Duration: {}ms", 
                     requestId,
                     req.getMethod(),
                     req.getRequestURI(),
                     res.getStatus(),
                     duration);
            
            // Warn on slow requests
            if (duration > 1000) {
                log.warn("[{}] SLOW REQUEST: {}ms for {} {}", 
                         requestId, duration, req.getMethod(), req.getRequestURI());
            }
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0] : request.getRemoteAddr();
    }
}
```

**Log Output:**
```
[a3b7c9d2] → POST /events/login from 192.168.1.100
[a3b7c9d2] ← POST /events/login - Status: 200 - Duration: 145ms

[f2d8e1a4] → POST /events/upload from 192.168.1.100
[f2d8e1a4] SLOW REQUEST: 2345ms for POST /events/upload
[f2d8e1a4] ← POST /events/upload - Status: 200 - Duration: 2345ms
```

---

### 🎯 Exercise 15: Security Header Filter (Enhanced)

**Task:** Add comprehensive security headers

```java
@Component
@Order(2)
public class SecurityHeadersFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityHeadersFilter.class);
    
    @Value("${app.security.csp:default-src 'self'}")
    private String contentSecurityPolicy;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Server disclosure prevention
        httpResponse.setHeader("Server", "");
        
        // XSS Protection
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Clickjacking prevention
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // HTTPS enforcement (if in production)
        if (isProduction()) {
            httpResponse.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains");
        }
        
        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", contentSecurityPolicy);
        
        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy (limit browser features)
        httpResponse.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=(self)");
        
        chain.doFilter(request, response);
    }
    
    private boolean isProduction() {
        // Check active profile
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
```

---

## 7. TESTING SECURITY

### 🎯 Exercise 16: Unit Test for Authorization

```java
@SpringBootTest
public class StoreContextValidatorTest {
    
    @Autowired
    private StoreContextValidator validator;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private MapStoreRepository mapStoreRepository;
    
    @Test
    @DisplayName("Store manager can access own store")
    public void testStoreManagerAccessOwnStore() {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticatedUser", "STORE001");
        session.setAttribute("userType", "STORE");
        
        // Act
        boolean hasAccess = validator.validateStoreAccess(session, "STORE001");
        
        // Assert
        assertTrue(hasAccess, "Store manager should access own store");
    }
    
    @Test
    @DisplayName("Store manager cannot access other store")
    public void testStoreManagerCannotAccessOtherStore() {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticatedUser", "STORE001");
        session.setAttribute("userType", "STORE");
        
        // Act
        boolean hasAccess = validator.validateStoreAccess(session, "STORE002");
        
        // Assert
        assertFalse(hasAccess, "Store manager should NOT access other store");
    }
    
    @Test
    @DisplayName("ABM can access mapped stores")
    public void testAbmAccessMappedStores() {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticatedUser", "ABM001");
        session.setAttribute("userType", "ABM");
        
        // Mock repository response
        when(mapStoreRepository.findStoresByAbmCode("ABM001"))
            .thenReturn(Arrays.asList("STORE001", "STORE002", "STORE003"));
        
        // Act & Assert
        assertTrue(validator.validateStoreAccess(session, "STORE001"));
        assertTrue(validator.validateStoreAccess(session, "STORE002"));
        assertFalse(validator.validateStoreAccess(session, "STORE999"));
    }
    
    @Test
    @DisplayName("Unauthenticated user cannot access any store")
    public void testUnauthenticatedAccess() {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        // No authentication attributes set
        
        // Act
        boolean hasAccess = validator.validateStoreAccess(session, "STORE001");
        
        // Assert
        assertFalse(hasAccess, "Unauthenticated user should NOT access any store");
    }
}
```

---

### 🎯 Exercise 17: Integration Test for Rate Limiting

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RateLimitingIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Rate limit allows up to 10 requests per minute")
    public void testRateLimitAllowsMaxRequests() throws Exception {
        String loginJson = "{\"code\":\"TEST\",\"password\":\"test123\"}";
        
        // First 10 requests should succeed
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/events/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson)
                    .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andDo(print());
        }
    }
    
    @Test
    @DisplayName("Rate limit blocks 11th request")
    public void testRateLimitBlocks11thRequest() throws Exception {
        String loginJson = "{\"code\":\"TEST\",\"password\":\"test123\"}";
        
        // Make 10 requests
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/events/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson)
                    .header("X-Forwarded-For", "192.168.1.100"));
        }
        
        // 11th request should be blocked
        mockMvc.perform(post("/events/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
                .header("X-Forwarded-For", "192.168.1.100"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"));
    }
    
    @Test
    @DisplayName("Different IPs have separate rate limits")
    public void testSeparateRateLimitsPerIP() throws Exception {
        String loginJson = "{\"code\":\"TEST\",\"password\":\"test123\"}";
        
        // IP1: Make 10 requests
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/events/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson)
                    .header("X-Forwarded-For", "192.168.1.100"));
        }
        
        // IP2: First request should succeed (separate bucket)
        mockMvc.perform(post("/events/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
                .header("X-Forwarded-For", "192.168.1.200"))
            .andExpect(status().isOk());
    }
}
```

---

### 🎯 Exercise 18: Security Test Suite

**Task:** Comprehensive security tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTestSuite {
    
    @Autowired
    private MockMvc mockMvc;
    
    // Test 1: SQL Injection Prevention
    @Test
    public void testSqlInjectionPrevention() throws Exception {
        String sqlInjection = "' OR '1'='1' --";
        
        mockMvc.perform(post("/events/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"code\":\"%s\",\"password\":\"test\"}", sqlInjection)))
            .andExpect(status().isBadRequest())  // Validation should fail
            .andExpect(jsonPath("$.status").value(false));
    }
    
    // Test 2: XSS Prevention
    @Test
    public void testXssInputRejection() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        
        mockMvc.perform(post("/events/attendees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"name\":\"%s\",\"phone\":\"9876543210\"}", xssPayload)))
            .andExpect(status().isBadRequest());
    }
    
    // Test 3: Unauthorized Access
    @Test
    public void testUnauthorizedStoreAccess() throws Exception {
        // Create session for STORE001
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticatedUser", "STORE001");
        session.setAttribute("userType", "STORE");
        
        // Try to access STORE002's data
        mockMvc.perform(get("/events/getevents")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"storeCode\":\"STORE002\"}"))
            .andExpect(status().isForbidden());
    }
    
    // Test 4: Session Timeout
    @Test
    public void testSessionTimeout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticatedUser", "TEST");
        session.setMaxInactiveInterval(1); // 1 second
        
        Thread.sleep(2000); // Wait for timeout
        
        mockMvc.perform(get("/events/api/me").session(session))
            .andExpect(status().isUnauthorized());
    }
    
    // Test 5: HTTP Method Restriction
    @Test
    public void testDeleteMethodBlocked() throws Exception {
        mockMvc.perform(delete("/events/delete-event")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
    
    // Test 6: Error Information Disclosure
    @Test
    public void testErrorDoesNotExposeStackTrace() throws Exception {
        // Trigger error (e.g., invalid event ID)
        MvcResult result = mockMvc.perform(get("/events/get/INVALID_ID"))
            .andExpect(status().is5xxServerError())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        
        // Assert no stack trace
        assertFalse(responseBody.contains("Exception"));
        assertFalse(responseBody.contains("at com.dechub"));
        assertFalse(responseBody.contains("Caused by"));
        
        // Assert error reference present
        assertTrue(responseBody.contains("errorReference"));
    }
}
```

---

## 8. COMPLETE MINI PROJECTS

### 🎯 Project 1: Multi-Factor Authentication (MFA)

**Goal:** Add 2FA to your login system

**Step 1: Generate TOTP secret on user registration**
```java
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class MfaService {
    
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
    
    public boolean validateCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
    
    public String getQRCodeUrl(String username, String secret) {
        return String.format(
            "otpauth://totp/%s?secret=%s&issuer=Tanishq",
            username, secret
        );
    }
}
```

**Step 2: Enable MFA for user**
```java
@PostMapping("/enable-mfa")
public ResponseEntity<?> enableMfa(HttpSession session) {
    String username = storeContextValidator.getAuthenticatedUser(session);
    
    // Generate secret
    String secret = mfaService.generateSecretKey();
    
    // Store in database
    userRepository.updateMfaSecret(username, secret);
    
    // Generate QR code URL
    String qrUrl = mfaService.getQRCodeUrl(username, secret);
    
    return ResponseEntity.ok(Map.of(
        "secret", secret,
        "qrCodeUrl", qrUrl,
        "message", "Scan QR code with Google Authenticator app"
    ));
}
```

**Step 3: Verify MFA on login**
```java
@PostMapping("/verify-mfa")
public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyDTO dto, HttpSession session) {
    // Check if user provided credentials
    String pendingUsername = (String) session.getAttribute("pendingMfaUser");
    if (pendingUsername == null) {
        return ResponseEntity.status(401).body(Map.of("error", "No pending MFA verification"));
    }
    
    // Get user's MFA secret
    String secret = userRepository.getMfaSecret(pendingUsername);
    
    // Verify code
    if (mfaService.validateCode(secret, dto.getCode())) {
        // MFA success - complete login
        storeContextValidator.setAuthenticatedUser(session, pendingUsername, dto.getUserType());
        session.removeAttribute("pendingMfaUser");
        
        return ResponseEntity.ok(Map.of("success", true));
    } else {
        return ResponseEntity.status(401).body(Map.of("success", false, "error", "Invalid code"));
    }
}
```

---

### 🎯 Project 2: JWT Authentication (Alternative)

**Goal:** Convert from session-based to JWT-based auth

**Step 1: Add JWT library**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**Step 2: Create JWT service**
```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:3600000}") // 1 hour
    private long expirationMs;
    
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String username, String userType, List<String> stores) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
            .setSubject(username)
            .claim("userType", userType)
            .claim("authorizedStores", stores)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    public String getUserTypeFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userType", String.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getAuthorizedStoresFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("authorizedStores", List.class);
    }
}
```

**Step 3: JWT authentication filter**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                Claims claims = jwtService.validateToken(token);
                String username = claims.getSubject();
                
                // Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
                
            } catch (InvalidTokenException e) {
                response.setStatus(401);
                response.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

**Step 4: Login with JWT**
```java
@PostMapping("/jwt-login")
public ResponseEntity<?> jwtLogin(@Valid @RequestBody LoginDTO loginDTO) {
    // Authenticate
    EventsLoginResponseDTO authResult = tanishqPageService.eventsLogin(
        loginDTO.getCode(), loginDTO.getPassword()
    );
    
    if (!authResult.isStatus()) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
    
    // Get user details
    String userType = determineUserType(loginDTO.getCode());
    List<String> authorizedStores = getAuthorizedStoresForUser(loginDTO.getCode(), userType);
    
    // Generate JWT
    String token = jwtService.generateToken(
        loginDTO.getCode(),
        userType,
        authorizedStores
    );
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "token", token,
        "expiresIn", 3600
    ));
}
```

**Compare: Session vs JWT**

| Aspect | Session (Your impl) | JWT (Alternative) |
|--------|---------------------|-------------------|
| **Storage** | Server memory/DB | Client (localStorage/cookie) |
| **Scalability** | Needs session replication | Stateless, scales easily |
| **Logout** | Server invalidates | Token valid until expiry |
| **Performance** | Fast (in-memory) | Fast (no DB lookup) |
| **Security** | Server controls state | Token can't be revoked |
| **Best for** | Traditional web apps | APIs, mobile apps |

---

### 🎯 Project 3: API Rate Limit Dashboard

**Goal:** Create admin dashboard showing rate limit status

```java
@RestController
@RequestMapping("/admin")
public class RateLimitAdminController {
    
    @Autowired
    private RateLimitingFilter rateLimitingFilter;
    
    @GetMapping("/rate-limits")
    public ResponseEntity<?> getRateLimitStatus(HttpSession session) {
        // Verify admin access
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, RateLimitStatus> status = rateLimitingFilter.getAllBucketStatus();
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/rate-limits/reset/{ip}")
    public ResponseEntity<?> resetRateLimit(@PathVariable String ip, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).build();
        }
        
        rateLimitingFilter.resetBucketForIP(ip);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "Rate limit reset for " + ip));
    }
}

// Add to RateLimitingFilter
public class RateLimitingFilter implements Filter {
    
    public Map<String, RateLimitStatus> getAllBucketStatus() {
        Map<String, RateLimitStatus> statusMap = new HashMap<>();
        
        for (Map.Entry<String, Bucket> entry : buckets.entrySet()) {
            String clientIP = entry.getKey();
            Bucket bucket = entry.getValue();
            
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(0);
            
            statusMap.put(clientIP, new RateLimitStatus(
                probe.getRemainingTokens(),
                REQUESTS_PER_MINUTE,
                probe.getRemainingTokens() == 0,  // isBlocked
                probe.getNanosToWaitForRefill() / 1_000_000_000  // secondsUntilRefill
            ));
        }
        
        return statusMap;
    }
    
    public void resetBucketForIP(String ip) {
        buckets.remove(ip);
        log.info("Rate limit bucket reset for IP: {}", ip);
    }
    
    public static class RateLimitStatus {
        private long remainingTokens;
        private long maxTokens;
        private boolean isBlocked;
        private long secondsUntilRefill;
        
        // Constructor, getters
    }
}
```

**Frontend Dashboard:**
```javascript
async function loadRateLimitDashboard() {
    const response = await fetch('/admin/rate-limits');
    const rateLimits = await response.json();
    
    const tableBody = document.getElementById('rate-limit-table');
    tableBody.innerHTML = '';
    
    for (const [ip, status] of Object.entries(rateLimits)) {
        const row = `
            <tr class="${status.isBlocked ? 'blocked' : ''}">
                <td>${ip}</td>
                <td>${status.remainingTokens}/${status.maxTokens}</td>
                <td>${status.isBlocked ? '🔴 Blocked' : '🟢 Active'}</td>
                <td>${status.isBlocked ? status.secondsUntilRefill + 's' : '-'}</td>
                <td><button onclick="resetLimit('${ip}')">Reset</button></td>
            </tr>
        `;
        tableBody.innerHTML += row;
    }
}

async function resetLimit(ip) {
    await fetch(`/admin/rate-limits/reset/${ip}`, { method: 'POST' });
    showToast('Rate limit reset for ' + ip);
    loadRateLimitDashboard();
}

// Refresh every 5 seconds
setInterval(loadRateLimitDashboard, 5000);
```

---

## 📚 LEARNING EXERCISES SUMMARY

| Exercise | Session | Difficulty | Time | Key Concepts |
|----------|---------|------------|------|--------------|
| 1. Session Info | 1, 2 | ⭐ Easy | 1h | HttpSession API |
| 2. Session Timeout | 1, 2 | ⭐⭐ Medium | 2h | Session lifecycle |
| 3. Activity Tracking | 1 | ⭐⭐ Medium | 2h | Session attributes |
| 4. Custom PAN Validator | 4 | ⭐⭐ Medium | 2h | Custom constraints |
| 5. Password Matches | 4 | ⭐⭐ Medium | 1h | Cross-field validation |
| 6. Event Date Validator | 4 | ⭐⭐ Medium | 1h | Custom logic |
| 7. Simple Rate Limiter | 3 | ⭐⭐⭐ Hard | 4h | Rate limit algorithm |
| 8. Endpoint-Specific | 3 | ⭐⭐⭐ Hard | 3h | Advanced rate limiting |
| 9. User-Based Limits | 3 | ⭐⭐⭐ Hard | 3h | Context-aware limiting |
| 10. Business Exception | 5 | ⭐⭐ Medium | 2h | Custom exceptions |
| 11. Error Analytics | 5 | ⭐⭐⭐ Hard | 3h | Metrics and monitoring |
| 12. Password Strength | 7 | ⭐⭐ Medium | 2h | Password validation |
| 13. Password History | 7 | ⭐⭐⭐ Hard | 3h | Database design |
| 14. Request Logging | 6 | ⭐⭐ Medium | 2h | Servlet filters |
| 15. Security Headers | 6 | ⭐⭐ Medium | 2h | HTTP headers |
| 16. Unit Tests | All | ⭐⭐ Medium | 4h | Testing |
| 17. Integration Tests | 3 | ⭐⭐⭐ Hard | 4h | Integration testing |
| 18. Security Test Suite | All | ⭐⭐⭐ Hard | 4h | Security testing |
| **Project 1: MFA** | 2 | ⭐⭐⭐⭐ Expert | 8h | 2FA, TOTP |
| **Project 2: JWT** | 2 | ⭐⭐⭐⭐ Expert | 8h | Token auth |
| **Project 3: Dashboard** | 3 | ⭐⭐⭐ Hard | 6h | Monitoring |

**Total Practice Time:** ~60 hours (1.5 weeks full-time)

---

## 🎯 PROGRESSIVE LEARNING PATH

### Phase 1: Foundation (Do First)
✅ Exercise 1, 2 - Session management  
✅ Exercise 4 - Custom validation  
✅ Exercise 12 - Password strength  
✅ Exercise 14 - Request logging  
✅ Exercise 16 - Unit tests

**Outcome:** Understand the basics

---

### Phase 2: Intermediate (Do Second)
✅ Exercise 3 - Activity tracking  
✅ Exercise 5, 6 - Complex validation  
✅ Exercise 10 - Business exceptions  
✅ Exercise 15 - Security headers  
✅ Exercise 17 - Integration tests

**Outcome:** Apply concepts to new scenarios

---

### Phase 3: Advanced (Do Third)
✅ Exercise 7 - Build rate limiter  
✅ Exercise 8, 9 - Advanced rate limiting  
✅ Exercise 11 - Error analytics  
✅ Exercise 13 - Password history  
✅ Exercise 18 - Security test suite

**Outcome:** Master complex implementations

---

### Phase 4: Expert (Do Last)
✅ Project 1 - Multi-factor authentication  
✅ Project 2 - JWT authentication  
✅ Project 3 - Rate limit dashboard

**Outcome:** Design complete security features

---

## 🧪 TESTING YOUR UNDERSTANDING

### Quiz 1: Session Management
1. What happens to session when user closes browser?
2. How does session fixation attack work?
3. Why change session ID after login?
4. What's the difference between session timeout types?
5. How to share sessions across multiple servers?

**Answers in DEVELOPER_LEARNING_GUIDE.md**

---

### Quiz 2: Rate Limiting
1. What's the difference between token bucket and leaky bucket?
2. Why use ConcurrentHashMap instead of HashMap?
3. How to handle rate limits in distributed system?
4. What's the purpose of Refill.greedy()?
5. Why apply rate limiting before Spring Security?

---

### Quiz 3: Validation
1. What's the difference between @NotNull, @NotEmpty, @NotBlank?
2. How does @Valid trigger validation?
3. What exception is thrown on validation failure?
4. How to create cross-field validator?
5. Why sanitize input in addition to validation?

---

## 🎓 CERTIFICATION PREP

### Spring Professional Certification Topics Covered:

✅ **Container, Dependency, and IOC**
- @Component, @Service, @Repository
- @Autowired dependency injection
- Bean scopes and lifecycle

✅ **Aspect Oriented Programming**
- @ControllerAdvice (AOP-based)
- Cross-cutting concerns (logging, error handling)

✅ **Data Management**
- Spring Data JPA repositories
- Transaction management
- Query methods

✅ **Spring MVC**
- @RestController, @RequestMapping
- @Valid for validation
- Exception handling with @ExceptionHandler

✅ **Spring Security**
- SecurityConfig configuration
- Session management
- Authentication and authorization
- Filters and filter chain

✅ **Spring Boot**
- Auto-configuration
- Application properties
- Starter dependencies
- Profiles

**Your implementations cover 80%+ of Spring Professional exam topics!**

---

## 🚀 NEXT STEPS

### After Completing These Exercises:

1. **Build Portfolio Project**
   - Implement all 8 security features in new app
   - Add JWT + Session options
   - Include Redis rate limiting
   - Full test coverage

2. **Contribute to Open Source**
   - Spring Security issues
   - Bucket4j enhancements
   - Security-focused projects

3. **Write Technical Blog**
   - "Implementing Rate Limiting in Spring Boot"
   - "Complete Guide to Spring Security Session Management"
   - "Bean Validation Best Practices"

4. **Mentor Others**
   - Present to your team
   - Create training materials
   - Code review security PRs

---

## 📝 PROGRESS TRACKER

### Exercises Completed:
- [ ] Exercise 1: Session Info
- [ ] Exercise 2: Session Timeout
- [ ] Exercise 3: Activity Tracking
- [ ] Exercise 4: Custom PAN Validator
- [ ] Exercise 5: Password Matches
- [ ] Exercise 6: Event Date Validator
- [ ] Exercise 7: Simple Rate Limiter
- [ ] Exercise 8: Endpoint-Specific Limits
- [ ] Exercise 9: User-Based Limits
- [ ] Exercise 10: Business Exception
- [ ] Exercise 11: Error Analytics
- [ ] Exercise 12: Password Strength
- [ ] Exercise 13: Password History
- [ ] Exercise 14: Request Logging
- [ ] Exercise 15: Security Headers
- [ ] Exercise 16: Unit Tests
- [ ] Exercise 17: Integration Tests
- [ ] Exercise 18: Security Test Suite

### Projects Completed:
- [ ] Project 1: Multi-Factor Authentication
- [ ] Project 2: JWT Authentication
- [ ] Project 3: Rate Limit Dashboard

**Progress:** ___ / 21 (___%)

---

## 🏆 ACHIEVEMENT UNLOCKED

When you complete all exercises:
- 🎓 **Security Implementation Expert** - Can implement production-grade security
- 🧪 **Test-Driven Security** - Can test security controls
- 🏗️ **Security Architect** - Can design secure systems
- 👨‍🏫 **Security Mentor** - Can teach others

---

**Start with Exercise 1 today!** 🚀

**Remember:**
- Code along, don't just read
- Test your implementations
- Break things to understand them
- Document your learning
- Share with your team

---

**Created:** March 5, 2026  
**For:** Hands-on practical learning  
**Difficulty:** Beginner to Expert  
**Time:** ~70 hours total practice


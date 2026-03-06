# Account Takeover Vulnerability Fix - Implementation Guide

## Overview
This document describes the security fixes implemented to address the **Critical Account Takeover vulnerability (OWASP A07: Identification and Authentication Failures)** in the Tanishq Celebration Site application.

## Vulnerability Description

### Original Issue
The application was vulnerable to Account Takeover attacks where:
- Attackers could switch to any store account by simply changing the `storeCode` parameter in API requests
- No server-side validation existed to verify if the authenticated user had permission to access the requested store
- Client-supplied store context was trusted without verification
- Session management was insufficient

### Attack Scenario
1. User logs in as Store A (e.g., `STORE001`)
2. Attacker intercepts/modifies request and changes `storeCode` to `STORE002`
3. Application processes request without validation
4. Attacker gains full access to Store B's data, events, and operations

## Security Fixes Implemented

### 1. **StoreContextValidator Component**
**Location:** `src/main/java/com/dechub/tanishq/security/StoreContextValidator.java`

A centralized security component that:
- **Validates store access permissions** - Checks if authenticated user has access to requested store
- **Manages session authentication** - Stores and retrieves user authentication context
- **Enforces authorization** - Validates every store-specific request
- **Audit logging** - Logs unauthorized access attempts

**Key Methods:**
```java
boolean validateStoreAccess(HttpSession session, String requestedStoreCode)
boolean validateEventAccess(HttpSession session, String eventId)
boolean isAuthenticated(HttpSession session)
void setAuthenticatedUser(HttpSession session, String username, String userType)
void clearAuthentication(HttpSession session)
```

### 2. **Session-Based Authentication**
**Modified Files:** `EventsController.java`

All login endpoints now establish secure sessions:
- `/events/login` - Store login
- `/events/abm_login` - ABM manager login
- `/events/rbm_login` - RBM manager login
- `/events/cee_login` - CEE manager login  
- `/events/corporate_login` - Corporate login

**Implementation:**
```java
@PostMapping("/login")
public EventsLoginResponseDTO eventsLogin(@RequestBody LoginDTO loginDTO, HttpSession session) {
    EventsLoginResponseDTO response = tanishqPageService.eventsLogin(loginDTO.getCode(), loginDTO.getPassword());
    
    if (response.isStatus()) {
        String userType = determineUserType(loginDTO.getCode());
        storeContextValidator.setAuthenticatedUser(session, code, userType);
    }
    
    return response;
}
```

### 3. **Authorization Checks on Critical Endpoints**

All store-sensitive endpoints now validate authorization:

#### Event Management
- **`/events/upload`** - Create event (validates store ownership)
- **`/events/getevents`** - Get events (validates store access)
- **`/events/updateSaleOfAnEvent`** - Update sale (validates event ownership)
- **`/events/updateAdvanceOfAnEvent`** - Update advance (validates event ownership)
- **`/events/updateGhsRgaOfAnEvent`** - Update GHS/RGA (validates event ownership)
- **`/events/updateGmbOfAnEvent`** - Update GMB (validates event ownership)

#### Sensitive Operations
- **`/events/changePassword`** - Change password (validates store ownership)
- **`/events/getinvitedmember`** - Get invitees (validates event ownership)
- **`/events/store/events/download`** - CSV export (validates store access)

**Example Implementation:**
```java
@PostMapping("/upload")
public QrResponseDTO storeEventsDetails(..., HttpSession session) {
    // Authentication check
    if (!storeContextValidator.isAuthenticated(session)) {
        return errorResponse("Authentication required");
    }

    // Authorization check
    if (!storeContextValidator.validateStoreAccess(session, code)) {
        log.error("SECURITY ALERT: Unauthorized access attempt for store: {} by user: {}", 
                 code, storeContextValidator.getAuthenticatedUser(session));
        return errorResponse("Access denied");
    }

    // Process request
    return tanishqPageService.storeEventsDetails(eventsDetailDTO);
}
```

### 4. **Enhanced Session Management**
**Modified File:** `SecurityConfig.java`

Improved Spring Security configuration:
```java
.sessionManagement()
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .sessionFixation().changeSessionId()  // Mitigate session fixation
    .maximumSessions(1)  // One session per user
    .maxSessionsPreventsLogin(false)  // Allow new login to invalidate old session
```

**Security Headers Added:**
- `X-Content-Type-Options: nosniff` - Prevent MIME sniffing attacks
- Session timeout: 30 minutes

### 5. **Logout Functionality**
**New Endpoint:** `/events/logout`

Proper session cleanup:
```java
@PostMapping("/logout")
public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
    storeContextValidator.clearAuthentication(session);
    return ResponseEntity.ok(response);
}
```

### 6. **Event Authorization Helper**
**Modified File:** `TanishqPageService.java`

New method to get store code for an event:
```java
public String getStoreCodeForEvent(String eventId) {
    Optional<Event> event = eventRepository.findById(eventId);
    return event.map(Event::getStoreCode).orElse(null);
}
```

## User Type Hierarchy

The system supports multiple user types with different access levels:

| User Type | Access Level | Example Username |
|-----------|-------------|------------------|
| **STORE** | Single store only | `STORE001` |
| **ABM** | Multiple stores in area | `EAST1-ABM-01` |
| **RBM** | Multiple stores in region | `EAST1`, `NORTH2` |
| **CEE** | Multiple stores (Customer Excellence) | `EAST1-CEE-01` |
| **CORPORATE** | Multiple stores (Corporate level) | `CORP-INDIA-01` |
| **REGIONAL** | Regional access | `east1`, `north2a` |

## Authorization Logic

### Store Access Validation
```
1. Extract authenticated user from session
2. Determine user type (STORE, ABM, RBM, CEE, CORPORATE)
3. Fetch authorized store list for user type:
   - STORE: Only their own store code
   - ABM: All stores under their ABM jurisdiction
   - RBM: All stores under their RBM jurisdiction
   - CEE: All stores under their CEE jurisdiction
   - CORPORATE: All stores under corporate jurisdiction
4. Check if requested store is in authorized list
5. Allow/Deny based on validation
```

### Event Access Validation
```
1. Lookup event in database
2. Get store code associated with event
3. Validate store access (using above logic)
```

## Security Audit Logging

All unauthorized access attempts are logged:
```
log.error("SECURITY ALERT: User '{}' (type: {}) attempted unauthorized access to store '{}'",
         authenticatedUser, userType, requestedStoreCode);
```

This enables:
- **Incident detection** - Identify attack patterns
- **Forensic analysis** - Investigate security breaches
- **Compliance** - Meet audit requirements

## Deployment Instructions

### Prerequisites
- Spring Boot 2.7.18
- Java 8+
- Existing database schema (no changes required)

### Deployment Steps

1. **Backup existing code:**
   ```bash
   git checkout -b pre-security-fix-backup
   git add .
   git commit -m "Backup before security fix"
   ```

2. **Apply fixes:**
   - Copy `StoreContextValidator.java` to `src/main/java/com/dechub/tanishq/security/`
   - Replace `EventsController.java`
   - Replace `SecurityConfig.java`
   - Update `TanishqPageService.java`

3. **Build application:**
   ```bash
   mvn clean install
   ```

4. **Run tests:**
   ```bash
   mvn test
   ```

5. **Deploy:**
   ```bash
   # For pre-prod
   mvn package
   # Deploy WAR to Tomcat
   
   # For production
   # Follow same process after pre-prod validation
   ```

### Post-Deployment Validation

1. **Test authentication:**
   - Login as store user
   - Verify session is established
   - Check browser cookies for JSESSIONID

2. **Test authorization:**
   - Login as STORE001
   - Try to access STORE002 data
   - Verify "Access denied" response
   - Check logs for security alert

3. **Test legitimate access:**
   - Login as ABM user
   - Access authorized stores
   - Verify successful data retrieval

4. **Test session management:**
   - Login and wait for timeout (30 min)
   - Verify session expiration
   - Try to access protected endpoint
   - Verify authentication required

## Testing Scenarios

### Test Case 1: Store User Access
```
Given: User logged in as STORE001
When: User tries to create event for STORE001
Then: Request succeeds

When: User tries to create event for STORE002
Then: Request denied with "Access denied" message
And: Security alert logged
```

### Test Case 2: Manager Access
```
Given: ABM user logged in (manages STORE001, STORE002, STORE003)
When: User accesses any of their stores
Then: Request succeeds

When: User tries to access STORE004 (not under their management)
Then: Request denied with "Access denied" message
```

### Test Case 3: Session Expiration
```
Given: User logged in
When: Session expires (30 minutes)
And: User tries to access protected endpoint
Then: Request denied with "Authentication required" message
```

### Test Case 4: Password Change Security
```
Given: User logged in as STORE001
When: User tries to change password for STORE001
Then: Request succeeds

When: User tries to change password for STORE002
Then: Request denied immediately
And: Security alert logged
```

## API Response Changes

### Before Fix
```json
{
  "status": true,
  "events": [...]  // Returns data regardless of authorization
}
```

### After Fix (Unauthorized)
```json
{
  "status": false,
  "message": "Access denied. You are not authorized to view events for this store."
}
```

### After Fix (Not Authenticated)
```json
{
  "status": false,
  "message": "Authentication required. Please log in."
}
```

## Frontend Changes Required

Frontend applications should:

1. **Handle 401 Unauthorized:**
   ```javascript
   if (response.status === 401) {
       // Redirect to login page
       window.location = '/login';
   }
   ```

2. **Handle 403 Forbidden:**
   ```javascript
   if (response.status === 403) {
       // Show access denied message
       alert('Access denied');
   }
   ```

3. **Implement logout:**
   ```javascript
   async function logout() {
       await fetch('/events/logout', { method: 'POST' });
       window.location = '/login';
   }
   ```

4. **Session timeout handling:**
   ```javascript
   // Warn user before timeout
   setTimeout(() => {
       alert('Session will expire in 5 minutes');
   }, 25 * 60 * 1000);  // 25 minutes
   ```

## Performance Impact

- **Minimal overhead** - Authorization checks are in-memory operations
- **No database changes** - Uses existing store/user relationships
- **Caching** - Authorized stores cached in session
- **Session storage** - Standard HttpSession (already in use)

## Compliance

This fix addresses:
- **OWASP A07:2021** - Identification and Authentication Failures
- **OWASP A01:2021** - Broken Access Control
- **PCI DSS 6.5.10** - Broken Authentication and Session Management
- **CWE-639** - Authorization Bypass Through User-Controlled Key

## Known Limitations

1. **Public endpoints** - `/events/customer/*` endpoints remain public (by design for QR code access)
2. **CSRF disabled** - REST API doesn't use CSRF tokens (consider implementing for production)
3. **Session storage** - Uses default in-memory sessions (consider Redis for clustering)

## Recommendations for Future Enhancements

1. **Implement JWT tokens** for stateless authentication
2. **Add rate limiting** to prevent brute force attacks
3. **Implement 2FA** for sensitive operations
4. **Add CSRF protection** if using session-based auth with forms
5. **Consider Redis** for distributed session management
6. **Add IP whitelist** for corporate users
7. **Implement audit dashboard** for security team
8. **Add automatic session invalidation** on password change

## Support & Contact

For issues or questions:
- **Security Team:** security@company.com
- **Development Team:** dev@company.com
- **Emergency:** Call security hotline

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-03-03 | Security Team | Initial fix implementation |

---

**IMPORTANT:** This fix must be deployed to both pre-production and production environments. Test thoroughly in pre-prod before production deployment.


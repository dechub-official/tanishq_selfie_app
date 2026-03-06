# Event Creation Authorization - Visual Flow Diagram

## BEFORE FIX (Failing)

```
┌─────────────┐
│  Frontend   │
│  (Browser)  │
└──────┬──────┘
       │
       │ POST /events/upload
       │ {
       │   code: null,              ← PROBLEM: code is null!
       │   eventName: "...",
       │   eventType: "...",
       │   ...
       │ }
       ↓
┌─────────────────────────────────┐
│  EventsController               │
│  @PostMapping("/upload")        │
├─────────────────────────────────┤
│                                 │
│  1. isAuthenticated(session)    │
│     ✓ User is authenticated     │
│                                 │
│  2. validateStoreAccess(        │
│        session, code)           │
│                                 │
│     ✗ code = null              │  ← VALIDATION FAILS HERE!
│     ✗ Validation fails!        │
│                                 │
│  3. Return error:               │
│     "Access denied..."          │
│                                 │
└─────────────────────────────────┘
       │
       │ Response:
       │ { status: false,
       │   qrData: "Access denied..." }
       ↓
┌─────────────┐
│  Frontend   │  ← Shows error message
│  (Browser)  │     to user
└─────────────┘

LOGS:
❌ WARN: Invalid validation request - session or storeCode is null
❌ ERROR: Unauthorized store access attempt for store: null by user: TEST
```

---

## AFTER FIX (Working)

```
┌─────────────┐
│  Frontend   │
│  (Browser)  │
└──────┬──────┘
       │
       │ POST /events/upload
       │ {
       │   code: null,              ← Still null, but now handled!
       │   eventName: "Test Event",
       │   eventType: "HOME VISITS",
       │   ...
       │ }
       ↓
┌─────────────────────────────────────────────────────────┐
│  EventsController                                       │
│  @PostMapping("/upload")                                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. isAuthenticated(session)                            │
│     ✓ User is authenticated                             │
│     session.authenticatedUser = "TEST"                  │
│     session.userType = "STORE"                          │
│                                                         │
│  2. NEW LOGIC: Auto-populate store code                 │
│                                                         │
│     if (code == null || code.isEmpty()) {               │
│         code = session.authenticatedUser;  ← FIX!       │
│         log.info("Using authenticated user...");        │
│     }                                                   │
│                                                         │
│     → code is now "TEST"                                │
│                                                         │
│  3. Enhanced security for store users                   │
│                                                         │
│     if (userType == "STORE") {                          │
│         code = session.authenticatedUser;  ← Override! │
│     }                                                   │
│                                                         │
│     → Prevents store users from tampering               │
│                                                         │
│  4. validateStoreAccess(session, code)                  │
│                                                         │
│     ✓ code = "TEST"                                     │
│     ✓ User "TEST" has access to store "TEST"            │
│     ✓ Validation passes!                                │
│                                                         │
│  5. Create event with storeCode = "TEST"                │
│                                                         │
│     → Event saved to database                           │
│     → QR code generated                                 │
│                                                         │
│  6. Return success:                                     │
│     { status: true, qrData: "..." }                     │
│                                                         │
└─────────────────────────────────────────────────────────┘
       │
       │ Response:
       │ { status: true,
       │   qrData: "QR-CODE-DATA" }
       ↓
┌─────────────┐
│  Frontend   │  ← Shows success message
│  (Browser)  │     Event created!
└─────────────┘

LOGS:
✓ INFO: Using authenticated user 'TEST' as store code for event creation
✓ DEBUG: Event created successfully
```

---

## USER TYPE AUTHORIZATION MATRIX

```
┌──────────────┬──────────────────┬─────────────────────────────────┐
│  User Type   │  Can Create For  │  Logic                          │
├──────────────┼──────────────────┼─────────────────────────────────┤
│  STORE       │  Own store only  │  code = authenticatedUser       │
│              │                  │  (Always overridden)            │
├──────────────┼──────────────────┼─────────────────────────────────┤
│  ABM         │  Managed stores  │  validateStoreAccess() checks   │
│              │                  │  if store is in ABM's list      │
├──────────────┼──────────────────┼─────────────────────────────────┤
│  RBM         │  Regional stores │  validateStoreAccess() checks   │
│              │                  │  if store is in RBM's region    │
├──────────────┼──────────────────┼─────────────────────────────────┤
│  CEE         │  Managed stores  │  validateStoreAccess() checks   │
│              │                  │  if store is in CEE's list      │
├──────────────┼──────────────────┼─────────────────────────────────┤
│  CORPORATE   │  All stores      │  validateStoreAccess() allows   │
│              │                  │  access to all stores           │
└──────────────┴──────────────────┴─────────────────────────────────┘
```

---

## SECURITY ENHANCEMENT - Store User Tampering Prevention

### Scenario: Store user tries to create event for another store

```
BEFORE FIX:
┌───────────┐
│ Store A   │ tries to create event for Store B by tampering request
└─────┬─────┘
      │ POST /events/upload { code: "STORE_B", ... }
      ↓
┌──────────────────┐
│  Backend         │  ← Would process if validation passed
└──────────────────┘

PROBLEM: Potential privilege escalation!


AFTER FIX:
┌───────────┐
│ Store A   │ tries to create event for Store B by tampering request
└─────┬─────┘
      │ POST /events/upload { code: "STORE_B", ... }
      ↓
┌──────────────────────────────────────────────────────┐
│  Backend                                             │
│                                                      │
│  authenticatedUser = "STORE_A"                       │
│  userType = "STORE"                                  │
│  code = "STORE_B" (from request)                     │
│                                                      │
│  if (userType == "STORE") {                          │
│      log.warn("Attempted to create for STORE_B");   │
│      code = authenticatedUser;  // Override!         │
│  }                                                   │
│                                                      │
│  → code is now "STORE_A"                             │
│  → Event created for STORE_A only                    │
│  → Security maintained! ✓                            │
│                                                      │
└──────────────────────────────────────────────────────┘

RESULT: Privilege escalation prevented!
```

---

## SESSION FLOW

```
1. USER LOGIN
   ┌────────────┐
   │  Frontend  │
   └──────┬─────┘
          │ POST /events/login
          │ { code: "TEST", password: "***" }
          ↓
   ┌────────────────────────────┐
   │  Backend                   │
   │  - Authenticate user       │
   │  - Set session attributes: │
   │    • authenticatedUser     │
   │    • userType              │
   │    • loginTimestamp        │
   │  - Set timeout: 30 min     │
   └────────────────────────────┘

2. CREATE EVENT (within 30 min)
   ┌────────────┐
   │  Frontend  │
   └──────┬─────┘
          │ POST /events/upload (with session cookie)
          ↓
   ┌────────────────────────────┐
   │  Backend                   │
   │  - Check session           │
   │  - Get authenticatedUser   │
   │  - Validate access         │
   │  - Create event            │
   └────────────────────────────┘

3. SESSION TIMEOUT (after 30 min)
   ┌────────────┐
   │  Frontend  │
   └──────┬─────┘
          │ POST /events/upload (with expired session)
          ↓
   ┌────────────────────────────┐
   │  Backend                   │
   │  - isAuthenticated() = false│
   │  - Return: "Authentication │
   │    required"               │
   └────────────────────────────┘
```

---

## KEY COMPONENTS

```
┌──────────────────────────────────────────────────────────┐
│  StoreContextValidator.java                              │
├──────────────────────────────────────────────────────────┤
│  • isAuthenticated(session)                              │
│    → Checks if user is logged in                         │
│                                                          │
│  • getAuthenticatedUser(session)                         │
│    → Returns username from session                       │
│                                                          │
│  • validateStoreAccess(session, storeCode)               │
│    → Checks if user can access specific store            │
│                                                          │
│  • setAuthenticatedUser(session, username, userType)     │
│    → Called during login to set session attributes       │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  EventsController.java                                   │
├──────────────────────────────────────────────────────────┤
│  @PostMapping("/upload")                                 │
│  • NEW: Auto-populate code from session                  │
│  • NEW: Override code for store users                    │
│  • Validate authorization                                │
│  • Create event                                          │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Session Attributes                                      │
├──────────────────────────────────────────────────────────┤
│  • authenticatedUser: String (e.g., "TEST")              │
│  • userType: String (e.g., "STORE", "ABM", "RBM", ...)   │
│  • loginTimestamp: Long                                  │
│  • timeout: 30 minutes                                   │
└──────────────────────────────────────────────────────────┘
```

---

## SUMMARY

**Problem**: Frontend not sending store code → Backend validation fails

**Solution**: 
1. ✅ Backend auto-populates store code from session
2. ✅ Enhanced security: Store users can't create events for other stores
3. ✅ Regional/corporate users maintain full functionality
4. ✅ Better logging for security monitoring

**Result**: Event creation works + enhanced security!


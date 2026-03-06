# Rate Limiting - Visual Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT REQUESTS                           │
│  (Multiple IPs: 192.168.1.1, 192.168.1.2, 192.168.1.3, ...)    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                       │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              RateLimitingFilter (Order 1)                   │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │  1. Extract Client IP (handle X-Forwarded-For)       │  │ │
│  │  │  2. Check if endpoint requires rate limiting         │  │ │
│  │  │  3. Get/Create bucket for IP                         │  │ │
│  │  │  4. Try to consume 1 token from bucket              │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  │                                                              │ │
│  │  ┌─────────────────┐    ┌────────────────┐                 │ │
│  │  │ Token Available? │───▶│ Allow Request  │───┐             │ │
│  │  │      (YES)       │    │  (Continue)    │   │             │ │
│  │  └─────────────────┘    └────────────────┘   │             │ │
│  │           │                                    │             │ │
│  │           │ NO                                 │             │ │
│  │           ▼                                    │             │ │
│  │  ┌─────────────────┐                          │             │ │
│  │  │ Return HTTP 429 │                          │             │ │
│  │  │ (Rate Limited)  │                          │             │ │
│  │  └─────────────────┘                          │             │ │
│  └──────────────────────────────────────────────┼─────────────┘ │
│                                                  │               │
│                                                  ▼               │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Other Security Filters                        │  │
│  │  (Authentication, Authorization, CSRF, etc.)              │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                                │                                 │
│                                ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  EventsController                          │  │
│  │  (/events/login, /events/upload, /events/attendees, ...)  │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                                │                                 │
│                                ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Service Layer                           │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                                │                                 │
│                                ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      Database                              │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Bucket Storage (In-Memory)

```
┌────────────────────────────────────────────────────────────────────┐
│              ConcurrentHashMap<String, Bucket>                      │
│                                                                     │
│  ┌─────────────────┐     ┌───────────────────────────────────┐   │
│  │ IP: 192.168.1.1 │────▶│ Bucket: [●●●●●○○○○○] (5 tokens)  │   │
│  └─────────────────┘     │ Capacity: 10                      │   │
│                          │ Refill: 10 tokens/minute          │   │
│  ┌─────────────────┐     └───────────────────────────────────┘   │
│  │ IP: 192.168.1.2 │────▶│ Bucket: [●●●●●●●●●●] (10 tokens) │   │
│  └─────────────────┘     │ Capacity: 10                      │   │
│                          │ Refill: 10 tokens/minute          │   │
│  ┌─────────────────┐     └───────────────────────────────────┘   │
│  │ IP: 192.168.1.3 │────▶│ Bucket: [○○○○○○○○○○] (0 tokens)  │   │
│  └─────────────────┘     │ Capacity: 10                      │   │
│                          │ Refill: 10 tokens/minute          │   │
│                          └───────────────────────────────────┘   │
│  ┌─────────────────┐                                             │
│  │      ...        │────▶ ...                                    │
│  └─────────────────┘                                             │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## Token Bucket Algorithm Flow

```
TIME: 0 seconds (Bucket created)
┌────────────────────────────────────┐
│ Bucket for IP: 192.168.1.1         │
│ [●●●●●●●●●●] - 10 tokens available │
└────────────────────────────────────┘

Request 1  ──▶ Consume 1 token ──▶ [●●●●●●●●●○] - 9 tokens - ✓ ALLOW
Request 2  ──▶ Consume 1 token ──▶ [●●●●●●●●○○] - 8 tokens - ✓ ALLOW
Request 3  ──▶ Consume 1 token ──▶ [●●●●●●●○○○] - 7 tokens - ✓ ALLOW
Request 4  ──▶ Consume 1 token ──▶ [●●●●●●○○○○] - 6 tokens - ✓ ALLOW
Request 5  ──▶ Consume 1 token ──▶ [●●●●●○○○○○] - 5 tokens - ✓ ALLOW
Request 6  ──▶ Consume 1 token ──▶ [●●●●○○○○○○] - 4 tokens - ✓ ALLOW
Request 7  ──▶ Consume 1 token ──▶ [●●●○○○○○○○] - 3 tokens - ✓ ALLOW
Request 8  ──▶ Consume 1 token ──▶ [●●○○○○○○○○] - 2 tokens - ✓ ALLOW
Request 9  ──▶ Consume 1 token ──▶ [●○○○○○○○○○] - 1 token  - ✓ ALLOW
Request 10 ──▶ Consume 1 token ──▶ [○○○○○○○○○○] - 0 tokens - ✓ ALLOW

Request 11 ──▶ No tokens!      ──▶ [○○○○○○○○○○] - 0 tokens - ✗ HTTP 429
Request 12 ──▶ No tokens!      ──▶ [○○○○○○○○○○] - 0 tokens - ✗ HTTP 429

TIME: 60 seconds (1 minute later - REFILL)
┌────────────────────────────────────┐
│ Automatic refill: +10 tokens       │
│ [●●●●●●●●●●] - 10 tokens available │
└────────────────────────────────────┘

Request 13 ──▶ Consume 1 token ──▶ [●●●●●●●●●○] - 9 tokens - ✓ ALLOW
```

## Request Flow Sequence

```
┌────────┐                                                     ┌──────────┐
│ Client │                                                     │  Server  │
└───┬────┘                                                     └────┬─────┘
    │                                                               │
    │  1. POST /events/login                                       │
    │─────────────────────────────────────────────────────────────▶│
    │     {"code":"STORE001","password":"test"}                    │
    │                                                               │
    │                        2. RateLimitingFilter intercepts      │
    │                        ┌───────────────────────────┐         │
    │                        │ • Extract IP: 192.168.1.1 │         │
    │                        │ • Check endpoint: /login  │         │
    │                        │ • Get bucket for IP       │         │
    │                        │ • Try consume 1 token     │         │
    │                        └───────────┬───────────────┘         │
    │                                    │                         │
    │                              [Token Available?]              │
    │                                    │                         │
    │                          ┌─────────┴─────────┐               │
    │                          │                   │               │
    │                         YES                 NO               │
    │                          │                   │               │
    │                          ▼                   ▼               │
    │                  3a. Continue         3b. Block Request      │
    │                    to Controller         Return 429          │
    │                          │                   │               │
    │                          │                   │               │
    │  4a. Response            │                   │               │
    │  (200/401/500)           │                   │               │
    │◀─────────────────────────┘                   │               │
    │                                               │               │
    │  4b. Response (429)                           │               │
    │◀──────────────────────────────────────────────┘               │
    │  {"success":false,                                           │
    │   "message":"Too many requests...",                          │
    │   "error":"RATE_LIMIT_EXCEEDED"}                             │
    │                                                               │
```

## Protected Endpoints Map

```
┌──────────────────────────────────────────────────────────────────┐
│                      /events/* Endpoints                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  🔒 RATE LIMITED (10/min per IP)                                 │
│  ├── /events/login                  ← Store Login               │
│  ├── /events/abm_login              ← ABM Login                 │
│  ├── /events/rbm_login              ← RBM Login                 │
│  ├── /events/cee_login              ← CEE Login                 │
│  ├── /events/corporate_login        ← Corporate Login           │
│  ├── /events/upload                 ← Event Upload              │
│  ├── /events/attendees              ← Attendee Submission       │
│  ├── /events/uploadCompletedEvents  ← Bulk Upload              │
│  ├── /events/changePassword         ← Password Change           │
│  ├── /events/updateSaleOfAnEvent    ← Sale Update              │
│  ├── /events/updateAdvanceOfAnEvent ← Advance Update           │
│  ├── /events/updateGhsRgaOfAnEvent  ← GHS/RGA Update           │
│  └── /events/updateGmbOfAnEvent     ← GMB Update               │
│                                                                   │
│  🔓 NOT RATE LIMITED                                             │
│  ├── /events (GET)                  ← Main Page                 │
│  ├── /events/logout                 ← Logout                    │
│  ├── /events/getevents              ← View Events               │
│  ├── /events/getinvitedmember       ← View Attendees            │
│  └── Other GET endpoints            ← Read Operations           │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

## HTTP Status Code Flow

```
Request to /events/login
         │
         ▼
┌─────────────────────┐
│ Rate Limiting Check │
└──────────┬──────────┘
           │
           ├──────────────────────────────────────┐
           │                                      │
    [Tokens Available]                   [No Tokens]
           │                                      │
           ▼                                      ▼
  ┌────────────────┐                    ┌────────────────┐
  │ Continue to    │                    │  Return 429    │
  │ Authentication │                    │  Rate Limited  │
  └───────┬────────┘                    └────────────────┘
          │
          ├──────────────────┬──────────────────┐
          │                  │                  │
   [Valid Creds]      [Invalid Creds]    [Server Error]
          │                  │                  │
          ▼                  ▼                  ▼
  ┌────────────┐     ┌────────────┐    ┌────────────┐
  │ HTTP 200   │     │ HTTP 401   │    │ HTTP 500   │
  │ Success    │     │ Unauthorized│    │ Error      │
  └────────────┘     └────────────┘    └────────────┘
```

## Attack Scenario Mitigation

### Before Rate Limiting Implementation
```
Attacker (192.168.1.100)
    │
    ├─ Request 1   ──▶ Processed ✓
    ├─ Request 2   ──▶ Processed ✓
    ├─ Request 3   ──▶ Processed ✓
    ├─ Request 100 ──▶ Processed ✓
    ├─ Request 500 ──▶ Processed ✓
    └─ Request 1000 ─▶ Processed ✓
    
Result: 1000 database queries, server overload, potential DoS
```

### After Rate Limiting Implementation
```
Attacker (192.168.1.100)
    │
    ├─ Request 1   ──▶ Processed ✓ (9 tokens left)
    ├─ Request 2   ──▶ Processed ✓ (8 tokens left)
    ├─ Request 3   ──▶ Processed ✓ (7 tokens left)
    ├─ Request 10  ──▶ Processed ✓ (0 tokens left)
    ├─ Request 11  ──▶ BLOCKED ✗ (HTTP 429)
    ├─ Request 12  ──▶ BLOCKED ✗ (HTTP 429)
    └─ Request 1000 ─▶ BLOCKED ✗ (HTTP 429)
    
Result: Only 10 requests processed, attack mitigated
```

## Legend

```
● = Available token
○ = Consumed token
✓ = Request allowed
✗ = Request blocked
▶ = Flow direction
```

---

**Implementation Date:** March 4, 2026  
**Status:** ✅ Complete


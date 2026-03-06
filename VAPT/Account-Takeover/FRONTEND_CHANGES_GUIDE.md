# 🎨 Frontend Changes Required - Account Takeover Fix

## 📋 Executive Summary

**Changes Required:** ✅ YES - But they are MINIMAL and straightforward  
**Complexity:** 🟢 LOW - Mostly error handling  
**Time Estimate:** 2-4 hours for implementation + testing  
**Breaking Changes:** ❌ NO - All backwards compatible

---

## 🎯 What Needs to Change (3 Required + 1 Optional)

### ✅ REQUIRED CHANGES:

1. **Add `credentials: 'include'` to ALL API calls** → Enable session cookies
2. **Handle 401 (Unauthorized)** → Session expired, redirect to login
3. **Handle 403 (Forbidden)** → Access denied error message

### ⚠️ OPTIONAL BUT RECOMMENDED:

4. **Add Logout functionality** → Call `/events/logout` endpoint

---

## 🔧 Change #1: Enable Session Cookies (REQUIRED)

### Why This is Needed
The backend now uses HTTP sessions (cookies) to track authenticated users. Without `credentials: 'include'`, cookies won't be sent with requests.

### Where to Change
**ALL fetch/axios API calls to `/events/*` endpoints**

### Before (Current Code):
```javascript
// ❌ This won't work - cookies not included
fetch('/events/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        code: storeCode,
        password: password
    })
});
```

### After (Required Code):
```javascript
// ✅ This works - cookies included
fetch('/events/login', {
    method: 'POST',
    credentials: 'include',  // ← ADD THIS LINE
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        code: storeCode,
        password: password
    })
});
```

### If Using Axios:
```javascript
// Set globally for all axios requests
axios.defaults.withCredentials = true;

// OR per request
axios.post('/events/login', {
    code: storeCode,
    password: password
}, {
    withCredentials: true  // ← ADD THIS
});
```

### If Using Fetch Wrapper:
```javascript
// Create a wrapper function
function apiFetch(url, options = {}) {
    return fetch(url, {
        ...options,
        credentials: 'include',  // Always include credentials
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    });
}

// Use it everywhere
apiFetch('/events/login', {
    method: 'POST',
    body: JSON.stringify({code, password})
});
```

---

## 🔧 Change #2: Handle Session Timeout - 401 (REQUIRED)

### Why This is Needed
After 30 minutes of inactivity, the user's session expires. Backend returns `401 Unauthorized`. Frontend must redirect to login.

### Where to Change
**Add response interceptor OR check in every API call**

### Option A: Global Interceptor (Recommended)
```javascript
// Add this ONCE in your main app file or API utility

// For Fetch
const originalFetch = window.fetch;
window.fetch = async function(...args) {
    const response = await originalFetch(...args);
    
    // Check for session timeout
    if (response.status === 401 && !args[0].includes('/login')) {
        // Session expired - redirect to login
        alert('Your session has expired. Please log in again.');
        window.location.href = '/login';  // Adjust your login path
        return response;
    }
    
    return response;
};

// OR for Axios
axios.interceptors.response.use(
    response => response,  // Success case
    error => {
        if (error.response && error.response.status === 401) {
            // Session expired
            alert('Your session has expired. Please log in again.');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
```

### Option B: Check in Each API Call
```javascript
async function getEvents(storeCode) {
    const response = await fetch('/events/getevents', {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({storeCode})
    });
    
    // ✅ Add this check
    if (response.status === 401) {
        alert('Your session has expired. Please log in again.');
        window.location.href = '/login';
        return null;
    }
    
    if (!response.ok) {
        throw new Error('API call failed');
    }
    
    return await response.json();
}
```

### User Experience
```
User: *inactive for 35 minutes*
User: *clicks "View Events"*
App: Shows alert: "Your session has expired. Please log in again."
App: Redirects to login page
User: Logs in again
User: Continues working
```

---

## 🔧 Change #3: Handle Access Denied - 403 (REQUIRED)

### Why This is Needed
If a user tries to access an unauthorized store (shouldn't happen in normal usage), backend returns `403 Forbidden`. Frontend should show error.

### Where to Change
**Add to response handling in API calls**

### Option A: Global Interceptor (Recommended)
```javascript
// For Fetch
const originalFetch = window.fetch;
window.fetch = async function(...args) {
    const response = await originalFetch(...args);
    
    // Check for access denied
    if (response.status === 403) {
        alert('Access denied. You are not authorized for this action.');
        return response;
    }
    
    // Check for session timeout
    if (response.status === 401 && !args[0].includes('/login')) {
        alert('Your session has expired. Please log in again.');
        window.location.href = '/login';
        return response;
    }
    
    return response;
};

// OR for Axios
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            if (error.response.status === 401) {
                alert('Your session has expired. Please log in again.');
                window.location.href = '/login';
            } else if (error.response.status === 403) {
                alert('Access denied. You are not authorized for this action.');
            }
        }
        return Promise.reject(error);
    }
);
```

### Option B: Check in API Calls
```javascript
async function createEvent(eventData) {
    const response = await fetch('/events/upload', {
        method: 'POST',
        credentials: 'include',
        body: formData
    });
    
    // ✅ Add these checks
    if (response.status === 401) {
        alert('Your session has expired. Please log in again.');
        window.location.href = '/login';
        return null;
    }
    
    if (response.status === 403) {
        alert('Access denied. You are not authorized to create events for this store.');
        return null;
    }
    
    if (!response.ok) {
        throw new Error('Failed to create event');
    }
    
    return await response.json();
}
```

---

## 🔧 Change #4: Add Logout Functionality (OPTIONAL)

### Why This is Recommended
Allows users to explicitly log out and clear their session. Good UX practice.

### Where to Add
**Logout button/link in your header/navbar**

### Implementation:

#### HTML:
```html
<!-- Add logout button to your navbar -->
<button onclick="handleLogout()">Logout</button>

<!-- OR -->
<a href="#" onclick="handleLogout(); return false;">Logout</a>
```

#### JavaScript:
```javascript
async function handleLogout() {
    try {
        // Call backend logout endpoint
        await fetch('/events/logout', {
            method: 'POST',
            credentials: 'include'
        });
        
        // Redirect to login page
        window.location.href = '/login';
    } catch (error) {
        console.error('Logout failed:', error);
        // Still redirect to login even if logout fails
        window.location.href = '/login';
    }
}
```

#### React Example:
```javascript
function LogoutButton() {
    const handleLogout = async () => {
        try {
            await fetch('/events/logout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout failed:', error);
        } finally {
            window.location.href = '/login';
        }
    };
    
    return <button onClick={handleLogout}>Logout</button>;
}
```

---

## 📝 Complete Example: API Service with All Changes

### Recommended: Create a Centralized API Service

```javascript
/**
 * API Service for Tanishq Events
 * Includes session management and error handling
 */

class EventsAPI {
    constructor() {
        this.baseURL = '/events';
    }
    
    /**
     * Internal fetch wrapper with error handling
     */
    async _fetch(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        
        const response = await fetch(url, {
            ...options,
            credentials: 'include',  // ✅ Always include credentials
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        
        // ✅ Handle session timeout
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            window.location.href = '/login';
            throw new Error('Session expired');
        }
        
        // ✅ Handle access denied
        if (response.status === 403) {
            const data = await response.json().catch(() => ({}));
            alert(data.message || 'Access denied. You are not authorized for this action.');
            throw new Error('Access denied');
        }
        
        // Handle other errors
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        return response;
    }
    
    /**
     * Login - Store/Manager
     */
    async login(code, password) {
        const response = await this._fetch('/login', {
            method: 'POST',
            body: JSON.stringify({ code, password })
        });
        return await response.json();
    }
    
    /**
     * Login - ABM
     */
    async loginAbm(username, password) {
        const response = await this._fetch('/abm_login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        return await response.json();
    }
    
    /**
     * Logout
     */
    async logout() {
        try {
            await this._fetch('/logout', { method: 'POST' });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            window.location.href = '/login';
        }
    }
    
    /**
     * Get events for a store
     */
    async getEvents(storeCode, startDate = null, endDate = null) {
        const response = await this._fetch('/getevents', {
            method: 'POST',
            body: JSON.stringify({ storeCode, startDate, endDate })
        });
        return await response.json();
    }
    
    /**
     * Create event
     */
    async createEvent(formData) {
        const response = await fetch(`${this.baseURL}/upload`, {
            method: 'POST',
            credentials: 'include',  // Note: Don't set Content-Type for FormData
            body: formData
        });
        
        // Same error handling
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            window.location.href = '/login';
            throw new Error('Session expired');
        }
        
        if (response.status === 403) {
            const data = await response.json().catch(() => ({}));
            alert(data.qrData || 'Access denied');
            throw new Error('Access denied');
        }
        
        if (!response.ok) {
            throw new Error('Failed to create event');
        }
        
        return await response.json();
    }
    
    /**
     * Change password
     */
    async changePassword(storeCode, oldPassword, newPassword, confirmPassword) {
        const response = await fetch(`${this.baseURL}/changePassword`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                storeCode,
                oldPassword,
                newPassword,
                confirmPassword
            })
        });
        
        // Same error handling
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            window.location.href = '/login';
            throw new Error('Session expired');
        }
        
        if (response.status === 403) {
            alert('Access denied. You cannot change password for this store.');
            throw new Error('Access denied');
        }
        
        return await response.json();
    }
    
    /**
     * Download CSV report
     */
    async downloadEvents(storeCode, startDate = null, endDate = null) {
        const params = new URLSearchParams({ storeCode });
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        
        const response = await fetch(`${this.baseURL}/store/events/download?${params}`, {
            method: 'GET',
            credentials: 'include'
        });
        
        // Check authorization
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            window.location.href = '/login';
            return;
        }
        
        if (response.status === 403) {
            alert('Access denied. You cannot download data for this store.');
            return;
        }
        
        // Download the file
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `events_${storeCode}_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }
}

// Export singleton instance
const eventsAPI = new EventsAPI();

// Usage examples:
// await eventsAPI.login('STORE001', 'password123');
// await eventsAPI.getEvents('STORE001');
// await eventsAPI.logout();
```

---

## 🎨 UI Components to Add/Update

### 1. Session Timeout Warning (Optional Enhancement)

Show warning 5 minutes before session expires:

```javascript
// Session timeout warning
let sessionTimeoutWarning;

function startSessionTimeoutWarning() {
    // Clear any existing timeout
    if (sessionTimeoutWarning) {
        clearTimeout(sessionTimeoutWarning);
    }
    
    // Show warning after 25 minutes (5 minutes before 30-minute timeout)
    sessionTimeoutWarning = setTimeout(() => {
        const continueSession = confirm(
            'Your session will expire in 5 minutes due to inactivity. ' +
            'Click OK to continue working.'
        );
        
        if (continueSession) {
            // Make a lightweight API call to keep session alive
            fetch('/events/getStoresByRegion/dummy', {
                credentials: 'include'
            }).catch(() => {});
            
            // Restart the warning timer
            startSessionTimeoutWarning();
        }
    }, 25 * 60 * 1000);  // 25 minutes
}

// Start timer after successful login
// Call this in your login success handler
startSessionTimeoutWarning();

// Reset timer on user activity (optional)
document.addEventListener('click', () => {
    startSessionTimeoutWarning();
}, { passive: true });
```

### 2. Better Error Messages

Replace generic alerts with styled modals or toasts:

```javascript
// Using a toast library (e.g., toastify)
function showSessionExpiredError() {
    Toastify({
        text: "Your session has expired. Redirecting to login...",
        duration: 3000,
        gravity: "top",
        position: "center",
        backgroundColor: "#f44336",
        stopOnFocus: true
    }).showToast();
    
    setTimeout(() => {
        window.location.href = '/login';
    }, 3000);
}

function showAccessDeniedError() {
    Toastify({
        text: "Access denied. You are not authorized for this action.",
        duration: 5000,
        gravity: "top",
        position: "right",
        backgroundColor: "#ff9800",
        stopOnFocus: true
    }).showToast();
}
```

### 3. Login Page Update (Clear Session on Load)

```javascript
// On login page load
window.addEventListener('DOMContentLoaded', () => {
    // Clear any existing session storage
    sessionStorage.clear();
    localStorage.removeItem('userData');  // If you store user data
});
```

---

## 🧪 Testing Checklist for Frontend

### Test 1: Login Flow
- [ ] Login with valid credentials → Should succeed
- [ ] Store code saved in session (check cookies in DevTools)
- [ ] Redirected to dashboard/home page

### Test 2: API Calls with Session
- [ ] Make API call (e.g., get events) → Should succeed
- [ ] Check Network tab → Cookie sent with request
- [ ] Response received successfully

### Test 3: Session Timeout
- [ ] Login successfully
- [ ] Wait 30+ minutes OR manually delete session cookie
- [ ] Try to make API call
- [ ] Should see "Session expired" message
- [ ] Should be redirected to login

### Test 4: Access Denied (Edge Case)
- [ ] Login as STORE001
- [ ] Manually change API request storeCode to STORE002 (in DevTools)
- [ ] Should see "Access denied" message
- [ ] Should NOT see data from STORE002

### Test 5: Logout
- [ ] Login successfully
- [ ] Click logout button
- [ ] Session should be cleared
- [ ] Redirected to login page
- [ ] Try to access protected page → Should redirect to login

### Test 6: Multiple Tabs
- [ ] Login in Tab 1
- [ ] Open Tab 2 (same app)
- [ ] Both tabs should work
- [ ] Logout in Tab 1
- [ ] Try to use Tab 2 → Should show session expired

---

## 📦 File Structure Recommendations

```
src/
├── services/
│   └── api.js                 # EventsAPI class (create this)
├── utils/
│   ├── auth.js                # Authentication helpers
│   └── session.js             # Session timeout warnings
├── components/
│   ├── LoginPage.jsx          # Update: handle session init
│   ├── Header.jsx             # Update: add logout button
│   └── ErrorBoundary.jsx      # Update: handle 401/403
└── App.js                     # Update: add global interceptors
```

---

## 🚀 Implementation Steps (Frontend Team)

### Step 1: Add Credentials to All API Calls (Day 1 - 2 hours)
- [ ] Find all `fetch()` calls to `/events/*`
- [ ] Add `credentials: 'include'` to each
- [ ] OR set `axios.defaults.withCredentials = true`
- [ ] Test: Login and verify cookie is set

### Step 2: Add Error Handling (Day 1 - 2 hours)
- [ ] Create global interceptor for 401/403
- [ ] OR add checks to each API call
- [ ] Test: Manually trigger 401 and 403 responses

### Step 3: Add Logout (Day 2 - 1 hour)
- [ ] Add logout button to UI
- [ ] Implement logout function
- [ ] Test: Logout and verify session cleared

### Step 4: Testing (Day 2 - 2 hours)
- [ ] Run all test scenarios
- [ ] Fix any issues found
- [ ] Document any edge cases

### Total Time: 2 days (7-8 hours)

---

## ⚠️ Common Pitfalls to Avoid

### ❌ Pitfall 1: Forgetting `credentials: 'include'`
**Problem:** Session cookie not sent, every request returns 401  
**Solution:** Add to ALL API calls

### ❌ Pitfall 2: Infinite Redirect Loop
**Problem:** Login page also triggers 401 handling  
**Solution:** Check URL before redirecting:
```javascript
if (response.status === 401 && !window.location.pathname.includes('/login')) {
    window.location.href = '/login';
}
```

### ❌ Pitfall 3: CORS Issues
**Problem:** Browser blocks credentials in cross-origin requests  
**Solution:** Backend must have:
```java
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: [specific origin, not *]
```
(Already configured in SecurityConfig.java)

### ❌ Pitfall 4: Not Handling FormData
**Problem:** Setting `Content-Type` for FormData breaks multipart upload  
**Solution:** Don't set Content-Type for FormData:
```javascript
// ❌ Wrong
fetch('/events/upload', {
    headers: {'Content-Type': 'application/json'},  // Don't do this
    body: formData
});

// ✅ Correct
fetch('/events/upload', {
    credentials: 'include',
    body: formData  // Browser sets correct Content-Type automatically
});
```

---

## 📞 Support for Frontend Team

### Questions During Implementation?
- Check existing code patterns in your project
- Test incrementally (don't change everything at once)
- Use browser DevTools Network tab to debug

### Need Help?
- Review the complete API service example above
- Check browser console for detailed error messages
- Verify cookies are being set (DevTools → Application → Cookies)

---

## ✅ Quick Reference Card

```javascript
// ✅ DO THIS for all API calls
fetch('/events/endpoint', {
    credentials: 'include',  // Required
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
});

// ✅ DO THIS - Check responses
if (response.status === 401) {
    alert('Session expired');
    window.location.href = '/login';
}

if (response.status === 403) {
    alert('Access denied');
}

// ✅ DO THIS - Logout
async function logout() {
    await fetch('/events/logout', {
        method: 'POST',
        credentials: 'include'
    });
    window.location.href = '/login';
}
```

---

**Version:** 1.0  
**Last Updated:** March 3, 2026  
**Status:** 🟢 READY FOR IMPLEMENTATION  
**Estimated Time:** 7-8 hours total


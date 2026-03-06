# ✅ Frontend Changes Checklist - Quick Reference

## 🎯 Summary: 3 REQUIRED Changes + 1 Optional

### ✅ Change 1: Add `credentials: 'include'` (REQUIRED)
**Where:** Every `fetch()` or `axios` call to `/events/*`

```javascript
// Before: ❌
fetch('/events/login', { method: 'POST', body: data });

// After: ✅
fetch('/events/login', { 
    method: 'POST', 
    credentials: 'include',  // ← ADD THIS
    body: data 
});
```

---

### ✅ Change 2: Handle 401 - Session Timeout (REQUIRED)
**Where:** Add global interceptor OR check in each API call

```javascript
// Option A: Global (Recommended)
window.fetch = new Proxy(window.fetch, {
    apply: async (target, thisArg, args) => {
        const response = await target.apply(thisArg, args);
        if (response.status === 401 && !args[0].includes('/login')) {
            alert('Session expired. Please log in again.');
            window.location.href = '/login';
        }
        return response;
    }
});

// Option B: Per Call
if (response.status === 401) {
    alert('Session expired');
    window.location.href = '/login';
}
```

---

### ✅ Change 3: Handle 403 - Access Denied (REQUIRED)
**Where:** Add to response handling

```javascript
if (response.status === 403) {
    alert('Access denied. You are not authorized for this action.');
}
```

---

### ⚠️ Change 4: Add Logout Button (RECOMMENDED)
**Where:** Header/navbar

```javascript
async function logout() {
    await fetch('/events/logout', {
        method: 'POST',
        credentials: 'include'
    });
    window.location.href = '/login';
}
```

```html
<button onclick="logout()">Logout</button>
```

---

## 📝 Implementation Checklist

### Day 1: Core Changes (4 hours)
- [ ] Find all API calls to `/events/*` endpoints
- [ ] Add `credentials: 'include'` to each fetch call
- [ ] OR set `axios.defaults.withCredentials = true` (if using axios)
- [ ] Test: Login and verify session cookie is created
- [ ] Add 401 handler (session timeout)
- [ ] Add 403 handler (access denied)
- [ ] Test: Manually delete cookie, verify redirect to login

### Day 2: Logout & Polish (3 hours)
- [ ] Add logout button to UI
- [ ] Implement logout function calling `/events/logout`
- [ ] Test: Logout and verify session cleared
- [ ] Test all user flows end-to-end
- [ ] Fix any bugs found

---

## 🧪 Testing Script

```javascript
// Test 1: Login
// ✅ Login works
// ✅ Cookie created (check DevTools → Application → Cookies)

// Test 2: API Call
// ✅ API call succeeds
// ✅ Cookie sent with request (check Network tab)

// Test 3: Session Timeout
// ⏰ Wait 31 minutes OR delete cookie manually
// ✅ Next API call shows "Session expired"
// ✅ Redirected to login

// Test 4: Logout
// ✅ Click logout
// ✅ Cookie deleted
// ✅ Redirected to login
```

---

## 🚨 Common Mistakes to Avoid

### ❌ Mistake 1: Missing `credentials: 'include'`
**Symptom:** Every request returns 401  
**Fix:** Add to ALL fetch calls

### ❌ Mistake 2: Infinite Redirect Loop
**Symptom:** Login page keeps redirecting to itself  
**Fix:** Check URL before redirecting:
```javascript
if (response.status === 401 && !window.location.pathname.includes('/login')) {
    window.location.href = '/login';
}
```

### ❌ Mistake 3: Wrong Content-Type for FormData
**Symptom:** File upload fails  
**Fix:** Don't set Content-Type for FormData:
```javascript
// ❌ Wrong
fetch('/events/upload', {
    headers: {'Content-Type': 'multipart/form-data'},  // Don't do this
    body: formData
});

// ✅ Correct
fetch('/events/upload', {
    credentials: 'include',
    body: formData  // Browser sets Content-Type automatically
});
```

---

## 📊 Files You'll Need to Change

Likely locations (adjust for your project):
```
src/
├── api/
│   └── events.js           # ← Add credentials: 'include'
├── services/
│   └── api-client.js       # ← Add 401/403 handlers
├── components/
│   ├── Login.jsx           # ← Test session creation
│   ├── Header.jsx          # ← Add logout button
│   └── Dashboard.jsx       # ← Test session checks
└── App.js                  # ← Add global interceptors
```

---

## 🎯 Quick Copy-Paste Solutions

### Solution 1: Complete Fetch Wrapper
```javascript
// Add this to your API utility file
async function apiFetch(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    });
    
    if (response.status === 401 && !url.includes('/login')) {
        alert('Your session has expired. Please log in again.');
        window.location.href = '/login';
        throw new Error('Session expired');
    }
    
    if (response.status === 403) {
        alert('Access denied. You are not authorized for this action.');
        throw new Error('Access denied');
    }
    
    return response;
}

// Use it everywhere
const response = await apiFetch('/events/getevents', {
    method: 'POST',
    body: JSON.stringify({storeCode})
});
```

### Solution 2: Axios Configuration
```javascript
// Add this to your axios setup file
import axios from 'axios';

// Enable credentials globally
axios.defaults.withCredentials = true;

// Add response interceptor
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

export default axios;
```

### Solution 3: Logout Function
```javascript
// Add this to your auth service
export async function logout() {
    try {
        await fetch('/events/logout', {
            method: 'POST',
            credentials: 'include'
        });
    } catch (error) {
        console.error('Logout failed:', error);
    } finally {
        // Always redirect, even if logout fails
        window.location.href = '/login';
    }
}
```

---

## 📱 What Users Will See

### Scenario 1: Normal Usage
```
User: Logs in ✅
User: Works normally ✅
User: Logs out ✅
→ NO CHANGE from user perspective
```

### Scenario 2: Session Timeout
```
User: Logs in ✅
User: Leaves for 35 minutes ⏰
User: Clicks something
App: "Your session has expired. Please log in again."
User: Redirected to login page
User: Logs in again ✅
→ Minor inconvenience, but clear message
```

### Scenario 3: Unauthorized Access (Shouldn't happen)
```
User: Somehow tries to access unauthorized store
App: "Access denied. You are not authorized for this action."
→ Rare edge case
```

---

## ✅ Definition of Done

Your frontend implementation is complete when:

- [ ] All API calls include `credentials: 'include'`
- [ ] 401 responses redirect to login with message
- [ ] 403 responses show access denied message
- [ ] Logout button calls `/events/logout` endpoint
- [ ] Login creates session cookie (visible in DevTools)
- [ ] Session timeout tested and working
- [ ] All existing features still work
- [ ] No console errors related to session handling

---

## 🆘 If You Get Stuck

### Check These First:
1. **DevTools → Network Tab**
   - Are cookies being sent?
   - What status code is returned?
   - Is `credentials: 'include'` in the request?

2. **DevTools → Application → Cookies**
   - Is JSESSIONID cookie present after login?
   - Does it have the correct domain?

3. **DevTools → Console**
   - Any CORS errors?
   - Any JavaScript errors?

### Still Stuck?
- Review `FRONTEND_CHANGES_GUIDE.md` for detailed examples
- Check backend logs for error messages
- Verify backend is running the updated code

---

## 📞 Quick Reference

| Need | Code Snippet |
|------|--------------|
| **Enable cookies** | `credentials: 'include'` in fetch |
| **Handle timeout** | `if (response.status === 401) { redirect to login }` |
| **Handle denied** | `if (response.status === 403) { show error }` |
| **Logout** | `POST /events/logout` with credentials |

---

**Time Estimate:** 7-8 hours total  
**Complexity:** 🟢 LOW  
**Impact:** ✅ Minimal for users  
**Status:** 🟢 READY TO START

---

**Next Step:** Review `FRONTEND_CHANGES_GUIDE.md` for complete implementation details.


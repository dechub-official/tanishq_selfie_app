# 🎯 Frontend Changes for YOUR PROJECT - Axios Implementation

## 📋 Your Current Setup
- **HTTP Client:** Axios
- **Base URL:** Imported from `base-url.js`
- **Framework:** React with React Router
- **Current Issue:** Missing `withCredentials: true` on axios calls

---

## 🚨 CRITICAL CHANGES REQUIRED

### Change #1: Configure Axios Globally (REQUIRED)
**File to create/update:** `src/utils/axiosConfig.js` or `src/config/api.js`

```javascript
import axios from 'axios';
import { baseUrl } from '../base-url';

// Configure axios defaults globally
axios.defaults.baseURL = baseUrl;
axios.defaults.withCredentials = true;  // ✅ CRITICAL: Enable cookies
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Add response interceptor for session management
axios.interceptors.response.use(
  (response) => {
    // Success - just return response
    return response;
  },
  (error) => {
    // Handle errors
    if (error.response) {
      const status = error.response.status;
      const currentPath = window.location.pathname;
      
      // 401 = Session expired
      if (status === 401 && !currentPath.includes('/login')) {
        alert('Your session has expired. Please log in again.');
        // Clear any local storage
        sessionStorage.clear();
        localStorage.clear();
        // Redirect to login
        window.location.href = '/';  // Or your login route
        return Promise.reject(error);
      }
      
      // 403 = Access denied
      if (status === 403) {
        const message = error.response.data?.qrData || error.response.data?.message || 'Access denied. You are not authorized for this action.';
        alert(message);
        return Promise.reject(error);
      }
    }
    
    return Promise.reject(error);
  }
);

export default axios;
```

---

### Change #2: Update ALL Component Imports (REQUIRED)

**BEFORE (Current - in Login.jsx and CreateEvent.jsx):**
```javascript
import axios from 'axios'
```

**AFTER (Required):**
```javascript
import axios from '../utils/axiosConfig'  // Use configured axios
// OR if you put it in config folder:
// import axios from '../config/api'
```

**Files to update:**
1. `Login.jsx` (or whatever your login component is called)
2. `CreateEvent.jsx` (the component you showed)
3. **ALL other components that use axios**

---

### Change #3: Your Login Component - Minimal Changes

**File:** `Login.jsx` (or your login page)

**Changes needed:**
```javascript
// 1. Change the import at the top
import axios from '../utils/axiosConfig'  // ← CHANGE THIS LINE

// 2. Update handleLogin function
const handleLogin = async () => {
    if (status === "Loading") return;
    console.log("enter", baseUrl + "/login");
    setStatus("Loading");
    
    try {
        // axios.post already has withCredentials: true from config
        const login = await axios.post(baseUrl + "/login", data);
        console.log(login);
        
        const store = login.data.storeData ? login.data.storeData : login.data.manager;
        
        if (login.data.status) {
            // ✅ Store session info if needed
            sessionStorage.setItem('storeCode', store.BtqCode || data.code);
            sessionStorage.setItem('storeName', store.BtqName || 'Store');
            
            nav("/dashboard", { state: { storeData: store } });
        } else {
            setStatus("failed");
        }
    } catch (error) {
        console.log(error);
        setStatus("failed");
        
        // Error handling is now in axios interceptor
        // No need to check for 401/403 here
    }
}
```

---

### Change #4: Your CreateEvent Component - Minimal Changes

**File:** `CreateEvent.jsx` (or your event creation component)

**Changes needed:**
```javascript
// 1. Change the import at the top
import axios from '../utils/axiosConfig'  // ← CHANGE THIS LINE

// 2. The rest of your code stays THE SAME
// The SubmitDetails function will work automatically because:
// - axios.defaults.withCredentials = true is set globally
// - interceptor handles 401/403 automatically

const SubmitDetails = async () => {
    if (status.status === "loading") return;
    setStatus({ status: "loading" });
    
    // ... your existing validation code ...
    
    try {
        const formData = new FormData();
        
        // ... your existing FormData code ...
        
        // ✅ This will now include credentials automatically
        const res = await axios.post(`${baseUrl}/upload`, formData);
        
        // ... your existing response handling ...
        
    } catch (error) {
        console.error(error);
        // Interceptor will handle 401/403
        // You can still show generic error
        setStatus({ status: "failed", msg: "Something went wrong" });
    }
};

// fetchStoreSummary - No changes needed
const fetchStoreSummary = async ({ startDate, endDate }) => {
    try {
        setStatus({ status: "loadingFilter" });
        // ✅ This will now include credentials automatically
        const response = await axios.get(`${baseUrl}/store-summary?storeCode=${code}&startDate=${startDate}&endDate=${endDate}`);
        
        // ... rest of your code stays the same ...
    } catch (error) {
        console.error("Error fetching store summary:", error);
        setStatus({
            status: "failed",
            msg: "Failed to fetch store summary. Please try again."
        });
    }
};

// exportEvents - No changes needed
const exportEvents = async () => {
    try {
        setStatus({ status: "report-loading" });
        
        let downloadUrl = `${baseUrl}/store/events/download?storeCode=${code}`;
        
        if (filteredDates.startDate) {
            downloadUrl += `&startDate=${filteredDates.startDate}`;
        }
        
        if (filteredDates.endDate) {
            downloadUrl += `&endDate=${filteredDates.endDate}`;
        }
        
        // ✅ This will now include credentials automatically
        const response = await axios({
            url: downloadUrl,
            method: 'GET',
            responseType: 'blob',
        });
        
        // ... rest of your download code stays the same ...
        
    } catch (error) {
        console.error("Error exporting events:", error);
        setStatus({
            status: "failed",
            msg: "Failed to export events. Please try again."
        });
    }
};
```

---

## 🎯 OPTIONAL: Add Logout Functionality

### Add Logout Button to Your Dashboard/Header

**Create a new file:** `src/components/LogoutButton.jsx`

```javascript
import axios from '../utils/axiosConfig';
import { useNavigate } from 'react-router-dom';

export default function LogoutButton() {
    const navigate = useNavigate();
    
    const handleLogout = async () => {
        try {
            await axios.post('/events/logout');
        } catch (error) {
            console.error('Logout error:', error);
            // Continue with logout even if request fails
        } finally {
            // Clear local storage
            sessionStorage.clear();
            localStorage.clear();
            // Redirect to login
            navigate('/');
        }
    };
    
    return (
        <button 
            onClick={handleLogout}
            className="py-2 px-4 bg-red-900 text-white rounded-md hover:bg-red-800"
        >
            Logout
        </button>
    );
}
```

**Then add it to your Dashboard or Header component:**
```javascript
import LogoutButton from './LogoutButton';

// Inside your component's return:
<div className="header flex justify-between items-center p-4">
    <div className="logo">
        <img src={tanishq_logo} alt="Tanishq" />
    </div>
    <LogoutButton />  {/* ← Add this */}
</div>
```

---

## 📝 Complete Implementation Checklist

### ✅ Step 1: Create Axios Config (30 minutes)
- [ ] Create `src/utils/axiosConfig.js` with the code above
- [ ] Copy the complete configuration including interceptors
- [ ] Save the file

### ✅ Step 2: Update Imports (30 minutes)
- [ ] Update `Login.jsx` → Change axios import
- [ ] Update `CreateEvent.jsx` → Change axios import
- [ ] Find ALL other files using `import axios from 'axios'`
- [ ] Update them to use `import axios from '../utils/axiosConfig'`

**How to find all files:**
Search your codebase for: `import axios from 'axios'`

### ✅ Step 3: Test Login (15 minutes)
- [ ] Start your application
- [ ] Open browser DevTools → Network tab
- [ ] Try to login
- [ ] Check the `/events/login` request
- [ ] Verify `Cookie` header is being sent
- [ ] Verify `JSESSIONID` cookie is set (in Application → Cookies)

### ✅ Step 4: Test Session Timeout (15 minutes)
- [ ] Login successfully
- [ ] Open DevTools → Application → Cookies
- [ ] Delete the `JSESSIONID` cookie
- [ ] Try to click "Set Filter" or any API action
- [ ] Should see "Session expired" alert
- [ ] Should redirect to login page

### ✅ Step 5: Add Logout (Optional - 30 minutes)
- [ ] Create `LogoutButton.jsx`
- [ ] Add it to your Dashboard/Header
- [ ] Test logout functionality

### ✅ Step 6: Full Testing (30 minutes)
- [ ] Login → Logout → Login again
- [ ] Create event
- [ ] Download report
- [ ] Let session expire (wait 30 min) → Try action
- [ ] Test in multiple tabs

---

## 🔍 How to Find All Files That Need Import Changes

Run this search in your code editor:

**Search for:** `import axios from 'axios'`

**In folder:** `src/`

**Expected files to update:**
- Login.jsx or Login.js
- CreateEvent.jsx
- Dashboard.jsx
- Any other component making API calls

---

## ⚠️ Common Issues and Solutions

### Issue 1: "Network Error" after changes
**Cause:** CORS configuration issue  
**Solution:** Backend already configured. Check that baseUrl is correct.

### Issue 2: Redirect loop on login page
**Cause:** Login page triggering 401 interceptor  
**Solution:** Already handled in interceptor (checks `!currentPath.includes('/login')`)

### Issue 3: Session not persisting
**Cause:** `withCredentials: true` not set  
**Solution:** That's what we're fixing - it will work after Step 1

### Issue 4: 401 on every request
**Cause:** Forgot to import configured axios  
**Solution:** Make sure ALL components import from `../utils/axiosConfig`

---

## 📞 Quick Testing Commands

### Test 1: Check if cookie is being sent
```
1. Login
2. Open DevTools → Network tab
3. Click any action (like "Set Filter")
4. Click the request
5. Check "Request Headers" section
6. Should see: Cookie: JSESSIONID=xxxx
```

### Test 2: Check if session expires correctly
```
1. Login
2. DevTools → Application → Cookies → localhost
3. Delete JSESSIONID cookie
4. Click any action
5. Should see alert and redirect
```

---

## 🎯 Summary

**What you need to do:**

1. **Create 1 file:** `src/utils/axiosConfig.js` (copy code from above)

2. **Update imports in ~5-10 files:**
   - Change: `import axios from 'axios'`
   - To: `import axios from '../utils/axiosConfig'`

3. **Add logout button:** (Optional but recommended)
   - Create: `src/components/LogoutButton.jsx`
   - Add to: Dashboard/Header component

4. **Test everything** (30 minutes)

**Total time: 2-3 hours**

---

## ✅ Verification

After implementation, verify:
- [ ] Login works
- [ ] Cookie (JSESSIONID) is set after login
- [ ] All API calls include the cookie
- [ ] Session timeout redirects to login
- [ ] Logout clears session

---

**Last Updated:** March 4, 2026  
**Status:** 🟢 READY FOR YOUR PROJECT


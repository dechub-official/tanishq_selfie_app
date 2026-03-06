# Frontend Security Checklist - Quick Reference

## ✅ Implementation Tasks

### 1. Update All Login Functions
- [ ] Store Login - use `/api/me` after login
- [ ] ABM Login - use `/api/me` after login  
- [ ] RBM Login - use `/api/me` after login
- [ ] CEE Login - use `/api/me` after login
- [ ] Corporate Login - use `/api/me` after login

### 2. Add `credentials: 'include'` Everywhere
- [ ] All fetch() calls
- [ ] All axios calls (set default)
- [ ] Login requests
- [ ] API requests

### 3. Create Auth Utility
```javascript
export async function fetchUserContext() {
    const response = await fetch('/events/api/me', { credentials: 'include' });
    return response.ok ? await response.json() : null;
}
```

### 4. Add Route Guards
- [ ] Check auth on protected routes
- [ ] Redirect to login if not authenticated

### 5. Remove Insecure Code
- [ ] Delete localStorage user data storage
- [ ] Don't trust login response body
- [ ] Use HTTP status codes only

---

See `AUTHENTICATION_BYPASS_FIX.md` for complete guide.


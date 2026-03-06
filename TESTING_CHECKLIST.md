# Event Creation Fix - Testing Checklist

## Pre-Deployment Testing (Optional)
- [ ] Code compiles without errors
- [ ] No new warnings introduced
- [ ] Security validation logic is correct

## Deployment Steps
- [ ] Backup current WAR file
- [ ] Build new WAR file with fix
- [ ] Stop application server
- [ ] Deploy new WAR file
- [ ] Start application server
- [ ] Wait for application startup (~30 seconds)

## Post-Deployment Testing

### Test 1: Store User Event Creation ✅
- [ ] Log in as store user (e.g., "TEST")
- [ ] Navigate to "Create Event" page
- [ ] Fill in required fields:
  - Event Name: "Test Event"
  - Event Type: "HOME VISITS AND REACH OUTS"
  - Event Sub Type: Select any
  - Date: Select date
  - Time: Enter time
  - Customer Name: "John Doe"
  - Customer Contact: "9876543210"
- [ ] Click "Create Event" button
- [ ] **Expected**: Event created successfully
- [ ] **Expected**: Redirected to event details or dashboard
- [ ] **Expected**: No "Access denied" error

### Test 2: Verify Event in Database
- [ ] Check events table: `SELECT * FROM events WHERE store_code = 'TEST' ORDER BY created_at DESC LIMIT 1;`
- [ ] **Expected**: New event exists with correct store_code
- [ ] **Expected**: Event details match what was entered

### Test 3: Check Logs
- [ ] View logs: `tail -100 /opt/tanishq/applications_preprod/application.log`
- [ ] **Expected**: See line like: `INFO: Using authenticated user 'TEST' as store code for event creation`
- [ ] **Expected**: No "SECURITY ALERT" messages
- [ ] **Expected**: No "Access denied" errors

### Test 4: ABM/RBM User (if applicable)
- [ ] Log in as ABM/RBM user
- [ ] Select a store from their region
- [ ] Create an event
- [ ] **Expected**: Event created successfully
- [ ] **Expected**: Can create events for all stores they manage

### Test 5: Security Test - Store User Cannot Create for Another Store
- [ ] Log in as store "TEST"
- [ ] Try to manually edit request (using browser DevTools) to send different store code
- [ ] **Expected**: Event is still created with store code "TEST" (their own store)
- [ ] **Expected**: Log shows security override message

### Test 6: Session and Logout
- [ ] Create an event
- [ ] Log out
- [ ] Try to access event creation page
- [ ] **Expected**: Redirected to login
- [ ] Log in again
- [ ] **Expected**: Can create events again

### Test 7: Session Timeout
- [ ] Log in
- [ ] Wait 30 minutes (or adjust session timeout for testing)
- [ ] Try to create an event
- [ ] **Expected**: Redirected to login or shows "Authentication required" error

## Security Validation

### Check 1: Session Security
- [ ] Verify session cookie is HttpOnly
- [ ] Verify session cookie has Secure flag (if HTTPS)
- [ ] Verify session timeout is set (30 minutes)

### Check 2: Authorization
- [ ] Store user can only create events for their own store
- [ ] ABM can create events for stores they manage
- [ ] RBM can create events for stores they manage
- [ ] CEE can create events for stores they manage
- [ ] Corporate user can create events for all stores

### Check 3: Log Review
- [ ] No SQL injection attempts in logs
- [ ] No unusual error patterns
- [ ] Security alerts are working correctly
- [ ] User actions are being logged properly

## Performance Testing (Optional)
- [ ] Create 10 events in quick succession
- [ ] **Expected**: All events created successfully
- [ ] **Expected**: No performance degradation
- [ ] Check memory usage: `free -h`
- [ ] Check CPU usage: `top`

## Rollback Testing (If Issues Found)
- [ ] Stop application server
- [ ] Restore backup WAR file
- [ ] Start application server
- [ ] Verify application works with old version
- [ ] Document issues found

## Sign-Off

**Tested By**: _______________________

**Date**: _______________________

**Environment**: 
- [ ] Local Dev
- [ ] Pre-Prod
- [ ] Production

**Result**: 
- [ ] ✅ All tests passed
- [ ] ⚠️ Some tests passed (document failures)
- [ ] ❌ Tests failed (rollback performed)

**Notes**:
```
_______________________________________________________________
_______________________________________________________________
_______________________________________________________________
```

**Approval for Production Deployment**: 
- [ ] Approved
- [ ] Rejected

**Approved By**: _______________________

**Date**: _______________________

---

## Troubleshooting

If event creation still fails:

1. **Check Authentication**:
   ```bash
   # Check if user authenticated
   grep "User.*authenticated" /opt/tanishq/applications_preprod/application.log | tail -5
   ```

2. **Check Session**:
   - Verify browser is accepting cookies
   - Check session timeout hasn't expired
   - Try clearing browser cache and cookies

3. **Check Database**:
   ```sql
   -- Verify user exists
   SELECT * FROM users WHERE store_code = 'TEST';
   
   -- Check store configuration
   SELECT * FROM stores WHERE store_code = 'TEST';
   ```

4. **Check Logs for Specific Error**:
   ```bash
   grep -i "error\|exception\|failed" /opt/tanishq/applications_preprod/application.log | tail -20
   ```

5. **Verify Session Attributes**:
   - Add debug logging to check session attributes
   - Ensure `authenticatedUser` and `userType` are set in session

6. **Check Frontend**:
   - Open browser DevTools → Network tab
   - Check request to `/events/upload`
   - Verify `code` parameter is being sent (or not sent)
   - Check response status code and body

If all else fails, contact development team with:
- Full error message from browser
- Backend logs (last 200 lines)
- Browser console errors
- Network request/response details


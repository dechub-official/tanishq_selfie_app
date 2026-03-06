# Password Hashing - Testing Guide

## 🧪 Complete Testing Procedures

**Security Fix**: OWASP A02 - Cryptographic Failures  
**Feature**: BCrypt Password Hashing  
**Status**: Ready for Testing

---

## 📋 Test Environment Setup

### **Prerequisites**
- Application deployed with password hashing changes
- Access to database
- API testing tool (Postman, curl, or browser)
- Test user credentials

---

## 🔬 Test Scenarios

### **Test 1: Login with Existing Plain Text Password**

**Objective**: Verify backward compatibility with plain text passwords

**Steps**:
1. Identify a user with plain text password in database
2. Attempt login via API
3. Verify successful authentication

**API Call**:
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ABM001",
    "password": "Test@123"
  }'
```

**Expected Result**:
- ✅ HTTP 200 OK
- ✅ Response: `{"status": 200, "message": "Login successful", "data": {...}}`
- ✅ Session created
- ✅ Logs show: `Successful ABM login for user: ABM001`

**Database Verification**:
```sql
-- Password should still be plain text (not changed on login)
SELECT abm_user_id, LEFT(password, 20) as pwd_preview 
FROM abm_login WHERE abm_user_id = 'ABM001';
```

---

### **Test 2: Login with BCrypt Hashed Password**

**Objective**: Verify BCrypt password authentication works

**Preparation**:
```sql
-- Manually update a test user with BCrypt hash (password: Test@123)
UPDATE abm_login 
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5uyrbP1CQ'
WHERE abm_user_id = 'ABM002';
```

**API Call**:
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ABM002",
    "password": "Test@123"
  }'
```

**Expected Result**:
- ✅ HTTP 200 OK
- ✅ Successful authentication
- ✅ BCrypt verification used (check logs)

---

### **Test 3: Failed Login - Wrong Password**

**Objective**: Verify authentication fails with incorrect password

**API Call**:
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ABM001",
    "password": "WrongPassword"
  }'
```

**Expected Result**:
- ✅ HTTP 401 Unauthorized
- ✅ Response: `{"status": 401, "message": "Invalid credentials", "data": null}`
- ✅ Logs show: `Failed ABM login attempt for username: ABM001`

---

### **Test 4: Password Change - Plain Text to BCrypt**

**Objective**: Verify password change hashes the new password

**Preparation**:
```sql
-- Ensure user has plain text password
SELECT username, password FROM users WHERE username = 'STORE001';
```

**API Call**:
```bash
curl -X POST http://localhost:8080/events/changePasswordForEventManager \
  -H "Content-Type: application/json" \
  -d '{
    "storeCode": "STORE001",
    "oldPassword": "oldpass123",
    "newPassword": "NewSecure@123",
    "confirmPassword": "NewSecure@123"
  }'
```

**Expected Result**:
- ✅ HTTP 200 OK
- ✅ Response: `{"status": true, "message": "Password changed successfully"}`

**Database Verification**:
```sql
-- New password should be BCrypt hashed
SELECT username, 
       LEFT(password, 10) as pwd_prefix,
       LENGTH(password) as pwd_length
FROM users WHERE username = 'STORE001';

-- Expected:
-- pwd_prefix: $2a$12$...
-- pwd_length: 60
```

**Login with New Password**:
```bash
curl -X POST http://localhost:8080/events/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STORE001",
    "password": "NewSecure@123"
  }'
```

**Expected**: ✅ Successful login

---

### **Test 5: Password Change - BCrypt to BCrypt**

**Objective**: Verify changing an already-hashed password works

**Steps**:
1. User with BCrypt password changes password
2. New password should also be BCrypt hashed
3. Login with new password should work

**Verification**:
```sql
-- Both old and new should be BCrypt hashes
SELECT * FROM password_history 
WHERE btq_code = 'STORE001' 
ORDER BY changed_at DESC LIMIT 1;
```

---

### **Test 6: All Login Endpoints**

Test each login endpoint separately:

#### **A. Store/Regional Manager Login**
```bash
curl -X POST http://localhost:8080/events/login \
  -H "Content-Type: application/json" \
  -d '{"code": "STORE001", "password": "Test@123"}'
```

#### **B. ABM Login**
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "ABM001", "password": "Test@123"}'
```

#### **C. RBM Login**
```bash
curl -X POST http://localhost:8080/events/rbm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "RBM001", "password": "Test@123"}'
```

#### **D. CEE Login**
```bash
curl -X POST http://localhost:8080/events/cee_login \
  -H "Content-Type: application/json" \
  -d '{"username": "CEE001", "password": "Test@123"}'
```

#### **E. Corporate Login**
```bash
curl -X POST http://localhost:8080/events/corporate_login \
  -H "Content-Type: application/json" \
  -d '{"username": "CORP001", "password": "Test@123"}'
```

**Expected**: All should authenticate successfully

---

### **Test 7: Migration Utility (Optional)**

**Objective**: Test bulk password migration

**Preparation**:
```sql
-- Take database backup
CREATE TABLE abm_login_test_backup AS SELECT * FROM abm_login;
```

**Run Migration**:
```bash
java -jar tanishq.war --spring.profiles.active=migrate-passwords,test
```

**Monitor Logs**:
```
INFO  PASSWORD MIGRATION UTILITY - STARTING
INFO  Found 25 ABM users
INFO  Migrated ABM user: ABM001
INFO  Migrated ABM user: ABM002
...
INFO  PASSWORD MIGRATION COMPLETED
INFO  Total Migrated: 150
INFO  Total Skipped: 10
INFO  Total Errors: 0
```

**Database Verification**:
```sql
-- All passwords should now be BCrypt hashed
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN password LIKE '$2a$%' THEN 1 ELSE 0 END) as hashed,
    SUM(CASE WHEN password NOT LIKE '$2a$%' THEN 1 ELSE 0 END) as plain_text
FROM abm_login;

-- Expected: hashed = total, plain_text = 0
```

**Test Login After Migration**:
- Try logging in with multiple users
- All should work with their original passwords

---

### **Test 8: Performance Test**

**Objective**: Verify acceptable performance impact

**Test Script** (use JMeter or similar):
```
Endpoint: POST /events/abm_login
Concurrent Users: 10
Duration: 1 minute
```

**Expected**:
- ✅ Average response time: <500ms (including BCrypt ~150ms)
- ✅ No timeouts
- ✅ No error rate increase
- ✅ CPU usage acceptable

---

### **Test 9: Edge Cases**

#### **A. Empty Password**
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "ABM001", "password": ""}'
```
**Expected**: ❌ 400 Bad Request - Validation error

#### **B. Special Characters in Password**
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "ABM001", "password": "Test@#$%^&*()123"}'
```
**Expected**: ✅ Should work if password matches

#### **C. Very Long Password**
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "ABM001", "password": "VeryLongPasswordWith100CharactersVeryLongPasswordWith100CharactersVeryLongPasswordWith100Char"}'
```
**Expected**: ✅ Should work if password matches

#### **D. SQL Injection Attempt**
```bash
curl -X POST http://localhost:8080/events/abm_login \
  -H "Content-Type: application/json" \
  -d '{"username": "ABM001", "password": "admin' OR '1'='1"}'
```
**Expected**: ❌ 401 Unauthorized - Should NOT authenticate

---

### **Test 10: Concurrent Logins**

**Objective**: Test thread safety of password encoder

**Steps**:
1. Create 5 concurrent login requests
2. All with same user/password
3. Verify all succeed or fail consistently

**Expected**:
- ✅ All requests get same result
- ✅ No race conditions
- ✅ No deadlocks

---

## 📊 Database Verification Queries

### **Check Password Migration Status**
```sql
SELECT 
    'ABM' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' 
        THEN 1 ELSE 0 END) as bcrypt_hashed,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' 
        THEN 1 ELSE 0 END) as plain_text,
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' 
        THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as migration_percentage
FROM abm_login

UNION ALL

SELECT 'RBM', COUNT(*), 
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END),
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END),
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2)
FROM rbm_login

UNION ALL

SELECT 'CEE', COUNT(*),
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END),
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END),
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2)
FROM cee_login

UNION ALL

SELECT 'Corporate', COUNT(*),
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END),
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END),
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2)
FROM corporate_login

UNION ALL

SELECT 'Store Users', COUNT(*),
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END),
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END),
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2)
FROM users
WHERE password IS NOT NULL;
```

### **Check Password Change History**
```sql
SELECT * FROM password_history 
ORDER BY changed_at DESC 
LIMIT 10;
```

### **Find Users Still Using Plain Text**
```sql
SELECT 'ABM' as type, abm_user_id as username FROM abm_login 
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%'
UNION ALL
SELECT 'RBM', rbm_user_id FROM rbm_login 
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%'
UNION ALL
SELECT 'CEE', cee_user_id FROM cee_login 
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%'
UNION ALL
SELECT 'Corporate', corporate_user_id FROM corporate_login 
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%'
UNION ALL
SELECT 'Store', username FROM users 
WHERE password IS NOT NULL 
  AND password NOT LIKE '$2a$%' 
  AND password NOT LIKE '$2b$%' 
  AND password NOT LIKE '$2y$%';
```

---

## ✅ Test Results Checklist

### **Functional Tests**
- [ ] Login with plain text password works
- [ ] Login with BCrypt password works
- [ ] Failed login returns 401
- [ ] Password change creates BCrypt hash
- [ ] Login with new password works
- [ ] All 5 login endpoints tested
- [ ] ABM login works
- [ ] RBM login works
- [ ] CEE login works
- [ ] Corporate login works
- [ ] Store login works

### **Security Tests**
- [ ] SQL injection blocked
- [ ] Special characters handled
- [ ] Empty password rejected
- [ ] Invalid credentials rejected

### **Performance Tests**
- [ ] Login response time <500ms
- [ ] Password change response time <1s
- [ ] Concurrent logins work
- [ ] No timeouts under load

### **Database Tests**
- [ ] New passwords are BCrypt hashed
- [ ] Password length is 60 characters
- [ ] Password starts with $2a$12$
- [ ] Password history updated
- [ ] Password cache updated

### **Migration Tests** (if applicable)
- [ ] Migration utility runs successfully
- [ ] All passwords migrated
- [ ] No errors in logs
- [ ] Users can login after migration

---

## 🐛 Common Issues & Solutions

### **Issue**: User can't login after password change
**Cause**: Password encoding issue  
**Check**: Verify password in DB starts with $2a$  
**Solution**: Reset password if corrupted

### **Issue**: Performance degraded
**Cause**: BCrypt is CPU intensive  
**Check**: Monitor CPU usage  
**Solution**: Normal for BCrypt, consider caching

### **Issue**: Migration fails
**Cause**: Database timeout  
**Check**: Migration logs  
**Solution**: Run in smaller batches

---

## 📈 Success Criteria

- ✅ All login tests pass
- ✅ Password change tests pass
- ✅ Security tests pass
- ✅ Performance acceptable
- ✅ No errors in logs
- ✅ Database queries show BCrypt hashes
- ✅ Backward compatibility maintained

---

## 📝 Test Report Template

```
# Password Hashing Test Report

Date: ___________
Tester: ___________
Environment: ___________

## Summary
- Total Tests: ___
- Passed: ___
- Failed: ___
- Blocked: ___

## Test Results
[Copy checklist results here]

## Issues Found
1. [Issue description]
2. [Issue description]

## Recommendations
[Any recommendations]

## Sign-off
Tester: ___________ Date: ___________
Reviewer: ___________ Date: ___________
```

---

**Testing Status**: Ready for QA  
**Last Updated**: March 5, 2026


# Password Hashing - Frequently Asked Questions

## 🔐 Security & Implementation

### **Q: Why do we need password hashing?**
**A**: Passwords stored in plain text are vulnerable if the database is compromised. BCrypt hashing protects passwords even if an attacker gains database access. It's a critical security requirement (OWASP A02) and compliance mandate (PCI DSS, ISO 27001).

### **Q: What is BCrypt?**
**A**: BCrypt is an industry-standard password hashing algorithm designed to be slow and computationally expensive, making brute-force attacks impractical. It automatically handles salt generation and can adapt to increasing computing power over time.

### **Q: Can we reverse BCrypt hashes to get the original password?**
**A**: No, BCrypt is a one-way function. You cannot decrypt a BCrypt hash. This is by design and what makes it secure. To verify a password, you must hash the input and compare it to the stored hash.

### **Q: What is the strength level (12) and why?**
**A**: BCrypt strength (cost factor) determines the number of hashing iterations: 2^12 = 4,096 iterations. Higher is more secure but slower. Strength 12 is the recommended balance: secure enough to resist attacks, fast enough for good user experience (~150ms per hash).

---

## 🔄 Migration & Compatibility

### **Q: Do we need to migrate all passwords immediately?**
**A**: No! The implementation supports both plain text and BCrypt passwords. Existing users can continue using their current passwords. New passwords (via password change) are automatically hashed. You can optionally run bulk migration later.

### **Q: Will existing users need to reset their passwords?**
**A**: No, users can continue using their current passwords. The authentication system checks if a password is hashed or plain text and verifies accordingly.

### **Q: How long will migration take?**
**A**: Gradual migration happens automatically as users change passwords (days/weeks). Bulk migration via utility takes <5 minutes for typical database sizes (~1000 users).

### **Q: What happens if migration fails partway through?**
**A**: The application handles mixed states (some hashed, some plain text). You can re-run the migration utility safely - it skips already-hashed passwords.

---

## 🌐 Frontend & API

### **Q: Do we need to change the frontend?**
**A**: No, absolutely zero frontend changes required. Password hashing is transparent to the client. All API endpoints, request formats, and response formats remain identical.

### **Q: Will API responses change?**
**A**: No, API responses are unchanged. The hashing happens server-side only. Login endpoints return the same success/failure responses as before.

### **Q: Do we need to update mobile apps?**
**A**: No, mobile apps continue working without any changes. The password hashing is completely server-side.

---

## ⚡ Performance

### **Q: Will login be slower?**
**A**: Slightly. BCrypt adds ~150-200ms per login (one-time cost). This is acceptable and intentional - the slowness makes brute-force attacks impractical. Most users won't notice the difference.

### **Q: Can we speed up BCrypt?**
**A**: Yes, by reducing the strength level, but this compromises security. Strength 12 is the recommended minimum for modern systems. Lower values make brute-force attacks easier.

### **Q: Does BCrypt affect scalability?**
**A**: BCrypt is CPU-intensive but only used during login/password change (infrequent operations). For typical application load, the impact is negligible. High-traffic systems may need load balancing, which is standard practice anyway.

---

## 🔧 Technical Details

### **Q: Where are passwords hashed?**
**A**: In the `TanishqPageService` class:
- `authenticateAbm/Rbm/Cee/Corporate()` - Verify passwords
- `eventsLogin()` - Verify passwords
- `changePasswordForEventManager()` - Hash new passwords

### **Q: What tables store passwords?**
**A**: Five tables:
1. `abm_login` - Area Business Managers
2. `rbm_login` - Regional Business Managers
3. `cee_login` - Customer Experience Executives
4. `corporate_login` - Corporate users
5. `users` - Store login credentials

### **Q: How do we identify BCrypt hashes?**
**A**: BCrypt hashes always:
- Start with `$2a$`, `$2b$`, or `$2y$`
- Are exactly 60 characters long
- Example: `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5uyrbP1CQ`

### **Q: What's the difference between $2a$, $2b$, and $2y$?**
**A**: These are BCrypt versions:
- `$2a$` - Original BCrypt (most common)
- `$2b$` - Fixed version (minor bug fix)
- `$2y$` - PHP-compatible version
All are secure and interchangeable. Spring uses `$2a$` by default.

---

## 🗄️ Database

### **Q: Do we need to change database schema?**
**A**: No, `VARCHAR(255)` is sufficient for BCrypt hashes (60 chars). The migration script includes optional column updates for documentation purposes only.

### **Q: Should we backup the database before migration?**
**A**: Yes, always! The migration script includes backup instructions. Even though the migration is safe, backups are a best practice.

### **Q: Can we rollback after migration?**
**A**: Yes, but you'll lose any password changes made after migration. The migration script includes rollback instructions. Test thoroughly before migrating production.

### **Q: What about password history?**
**A**: Password history now stores BCrypt hashes instead of plain text. This is more secure - even password history is protected.

---

## 🧪 Testing

### **Q: How do we test password hashing?**
**A**: See `TESTING_GUIDE.md` for complete procedures. Key tests:
1. Login with plain text password
2. Login with BCrypt password
3. Change password (creates hash)
4. Login with new password

### **Q: How do we verify migration status?**
**A**: Run verification SQL queries (see `TESTING_GUIDE.md`):
```sql
SELECT COUNT(*) as hashed 
FROM abm_login 
WHERE password LIKE '$2a$%';
```

### **Q: Can we test in production?**
**A**: The gradual migration approach is production-safe. You can deploy and monitor. For bulk migration, test in UAT first.

---

## 🚨 Troubleshooting

### **Q: User says "password not working" after deployment**
**A**: Check:
1. Password field in database - is it BCrypt or plain text?
2. Application logs - any BCrypt errors?
3. User input - special characters causing encoding issues?
4. Try with a test user to isolate the issue

### **Q: Login returns 500 error**
**A**: Check application logs for:
- `PasswordEncoder` bean not found - Configuration issue
- BCrypt exceptions - Password format corrupted
- Database connection errors - Connectivity issue

### **Q: Performance degraded after deployment**
**A**: Monitor:
- CPU usage - BCrypt is CPU-intensive
- Response times - Should be <500ms
- Concurrent logins - Check thread pool configuration
If severe, consider load balancing or caching strategies.

### **Q: Migration utility won't start**
**A**: Verify:
- Profile is active: `--spring.profiles.active=migrate-passwords`
- Database connectivity
- Sufficient memory/CPU
- Check logs for detailed error

---

## 📊 Monitoring

### **Q: What should we monitor post-deployment?**
**A**: Monitor:
1. Failed login attempts (should not increase)
2. Login response times (~150ms BCrypt overhead)
3. CPU usage (BCrypt is CPU-intensive)
4. Application error logs (BCrypt exceptions)
5. Database query performance

### **Q: How do we track migration progress?**
**A**: Run verification queries regularly:
```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN password LIKE '$2a$%' THEN 1 ELSE 0 END) as migrated,
    ROUND(SUM(CASE WHEN password LIKE '$2a$%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as percentage
FROM abm_login;
```

---

## 🔒 Security Questions

### **Q: Is BCrypt better than MD5/SHA?**
**A**: Yes! MD5 and SHA are fast hash functions designed for data integrity, not password storage. They can be attacked with rainbow tables and GPU brute-forcing. BCrypt is specifically designed for passwords: slow, salted, and adaptive.

### **Q: What if our database is compromised?**
**A**: With BCrypt:
- Passwords are protected (cannot be reversed)
- Attacker must brute-force each password individually
- Each attempt takes ~150ms (6-7 attempts/second)
- Strong passwords are practically uncrackable

Without BCrypt (plain text):
- All passwords immediately exposed
- Attacker has instant access to all accounts

### **Q: Should we use a different algorithm like Argon2?**
**A**: BCrypt is sufficient for most applications. Argon2 is newer and more secure but requires additional libraries. BCrypt is:
- Battle-tested (20+ years)
- Industry standard
- Built into Spring Security
- Proven secure when configured correctly

### **Q: Do we need additional security measures?**
**A**: Password hashing is one layer. Also implement:
- Rate limiting (already implemented in your system)
- Account lockout after failed attempts
- Two-factor authentication (optional)
- Password complexity requirements
- Regular security audits

---

## 📝 Compliance

### **Q: Does this meet OWASP requirements?**
**A**: Yes, BCrypt password hashing addresses:
- OWASP A02:2021 - Cryptographic Failures
- Specifically prevents: Plain text password storage

### **Q: What about PCI DSS?**
**A**: PCI DSS Requirement 8.2.1 requires passwords to be unreadable during storage. BCrypt satisfies this requirement.

### **Q: Is this GDPR compliant?**
**A**: Yes, protecting passwords with proper encryption is a GDPR requirement for data protection.

---

## 🚀 Deployment

### **Q: Is this a breaking change?**
**A**: No, it's fully backward compatible:
- Existing passwords work
- No frontend changes
- No API changes
- No configuration changes

### **Q: Can we deploy during business hours?**
**A**: Yes! The implementation has zero downtime. Users continue working normally. Optional bulk migration should run during maintenance window.

### **Q: What's the deployment order?**
**A**:
1. Deploy application with password hashing code
2. Monitor logs for 24 hours
3. Verify existing users can login
4. Optionally run bulk migration (maintenance window)
5. Continue monitoring

---

## 💡 Best Practices

### **Q: Should we force users to change passwords?**
**A**: Not necessary for this security fix. Gradual migration is fine. However, if you have other password policy changes, this is a good time to implement them.

### **Q: Should we notify users?**
**A**: Optional. The change is transparent to users. If you want to educate users about security improvements, you can send a notification.

### **Q: How often should we update password hashing?**
**A**: BCrypt is adaptive - you can increase the strength level over time as computing power increases. Review every 3-5 years. BCrypt 12 is current standard.

---

## 📚 Resources

### **Q: Where can I learn more about BCrypt?**
**A**: 
- [BCrypt Wikipedia](https://en.wikipedia.org/wiki/Bcrypt)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [Spring Security Password Encoding](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)

### **Q: Who do I contact for support?**
**A**: 
- Security Team for security questions
- DevOps Team for deployment issues
- Database Team for database issues

---

## ❓ Still Have Questions?

If your question isn't answered here:

1. Check `IMPLEMENTATION_GUIDE.md` for technical details
2. Check `TESTING_GUIDE.md` for testing procedures
3. Review application logs for specific errors
4. Contact the Security Team

---

**Document Version**: 1.0  
**Last Updated**: March 5, 2026  
**Maintained by**: Security Team


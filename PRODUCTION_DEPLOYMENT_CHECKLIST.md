# ✅ PRODUCTION DEPLOYMENT CHECKLIST

## Status: **NOT READY FOR PRODUCTION** ❌
**Last Updated:** December 20, 2025

---

## 🔴 CRITICAL BLOCKERS (Must Complete Before Production)

### Security Issues

- [ ] **Implement Password Hashing**
  - [ ] Install BCrypt password encoder
  - [ ] Create password migration script
  - [ ] Hash all existing passwords in database
  - [ ] Update all login methods to use BCrypt
  - [ ] Test authentication thoroughly
  - **Estimated Time:** 2-3 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Remove Hardcoded Credentials**
  - [ ] Move database password to environment variable
  - [ ] Move email password to environment variable
  - [ ] Move API passwords to environment variable
  - [ ] Update application.properties files
  - [ ] Test with environment variables
  - [ ] Remove credentials from Git history (BFG Repo-Cleaner)
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Rotate All Exposed Passwords**
  - [ ] Change MySQL root password (Dechub#2025)
  - [ ] Change email password (Titan@2024)
  - [ ] Change API password (admin_t!tan_mule)
  - [ ] Update password vaults/documentation
  - [ ] Notify affected team members
  - **Estimated Time:** 4 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Implement JWT Authentication**
  - [ ] Add JWT dependencies (jjwt)
  - [ ] Create JWT utility class
  - [ ] Implement token generation on login
  - [ ] Create JWT authentication filter
  - [ ] Add token validation to all endpoints
  - [ ] Configure token expiration (1 hour)
  - [ ] Implement refresh token mechanism
  - [ ] Test authentication flow
  - **Estimated Time:** 3-5 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Implement Authorization (RBAC)**
  - [ ] Define roles (STORE_MANAGER, RBM, ABM, CEE, ADMIN)
  - [ ] Add role column to user tables
  - [ ] Create authorization annotations
  - [ ] Add role checks to all endpoints
  - [ ] Test role-based access
  - [ ] Verify cross-store access denied
  - **Estimated Time:** 2-3 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Secure Delete Endpoints**
  - [ ] Add authentication to DELETE /greetings/{id}
  - [ ] Add ownership verification
  - [ ] Add audit logging for deletions
  - [ ] Test unauthorized delete attempts
  - **Estimated Time:** 4 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Implement Rate Limiting**
  - [ ] Add Bucket4j dependency
  - [ ] Create rate limiting filter
  - [ ] Configure 5 attempts per 15 minutes for login
  - [ ] Test rate limiting enforcement
  - [ ] Add rate limit headers to responses
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

### Dependency Updates

- [ ] **Upgrade Apache Tomcat** (CRITICAL - 11 CVEs)
  - [ ] Update pom.xml: tomcat-embed-core to 11.0.12+
  - [ ] Update pom.xml: tomcat-embed-websocket to 11.0.12+
  - [ ] Test application startup
  - [ ] Test all file upload endpoints
  - [ ] Verify no breaking changes
  - **Estimated Time:** 2-4 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Upgrade Spring Security** (1 CVE)
  - [ ] Update pom.xml: spring-security-core to 5.7.14+
  - [ ] Update related Spring Security dependencies
  - [ ] Test authentication flow
  - [ ] Verify security configurations
  - **Estimated Time:** 2 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

---

## 🟠 HIGH PRIORITY (Complete Before Production)

### Configuration

- [ ] **Update CORS Configuration**
  - [ ] Change `app.cors.allowedOrigins=*` to specific domain
  - [ ] Preprod: `https://celebrationsite-preprod.tanishq.co.in`
  - [ ] Prod: `https://celebrations.tanishq.co.in`
  - [ ] Test cross-origin requests
  - **Estimated Time:** 1 hour
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Change Hibernate DDL Auto**
  - [ ] In production properties: `ddl-auto=update` → `validate`
  - [ ] Verify database schema matches entities
  - [ ] Create manual migration script if needed
  - [ ] Test application startup
  - **Estimated Time:** 2 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Configure Database Connection Pool**
  ```properties
  spring.datasource.hikari.maximum-pool-size=20
  spring.datasource.hikari.minimum-idle=5
  spring.datasource.hikari.connection-timeout=30000
  spring.datasource.hikari.idle-timeout=600000
  ```
  - [ ] Add HikariCP configuration
  - [ ] Test under load
  - [ ] Monitor connection pool metrics
  - **Estimated Time:** 2 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

### Code Quality

- [ ] **Add Global Exception Handler**
  - [ ] Create @ControllerAdvice class
  - [ ] Handle all exception types
  - [ ] Standardize error response format
  - [ ] Hide internal error details from users
  - [ ] Add proper logging
  - [ ] Test exception scenarios
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Remove printStackTrace() Calls**
  - [ ] Fix StoreServices.java:72
  - [ ] Fix EmailService.java:80
  - [ ] Replace with log.error()
  - [ ] Search for any other instances
  - **Estimated Time:** 30 minutes
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Add Input Validation**
  - [ ] Add Bean Validation dependency
  - [ ] Create DTOs with @Valid annotations
  - [ ] Add @NotNull, @NotBlank, @Size constraints
  - [ ] Add validation to all POST/PUT endpoints
  - [ ] Test validation error responses
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Implement File Upload Whitelist**
  - [ ] Define allowed file extensions
  - [ ] Add whitelist validation to EventsController
  - [ ] Add whitelist validation to GreetingController
  - [ ] Add whitelist validation to TanishqPageController
  - [ ] Test with malicious file types
  - **Estimated Time:** 4 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

### Monitoring & Health Checks

- [ ] **Add Spring Boot Actuator**
  - [ ] Add actuator dependency
  - [ ] Configure health check endpoint
  - [ ] Configure info endpoint
  - [ ] Secure actuator endpoints (basic auth or IP whitelist)
  - [ ] Test health check
  - **Estimated Time:** 3 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Configure Application Logging**
  - [ ] Review log levels (INFO in prod)
  - [ ] Configure log file rotation
  - [ ] Set up centralized logging (CloudWatch/ELK)
  - [ ] Add structured logging format
  - [ ] Test log aggregation
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

---

## 🟡 MEDIUM PRIORITY (Should Complete)

### Testing

- [ ] **Write Unit Tests**
  - [ ] Test GreetingService (100% coverage)
  - [ ] Test TanishqPageService (critical methods)
  - [ ] Test authentication logic
  - [ ] Test authorization logic
  - [ ] Run tests: `mvn test`
  - **Estimated Time:** 3 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Write Integration Tests**
  - [ ] Test GreetingController endpoints
  - [ ] Test EventsController endpoints
  - [ ] Test authentication flow
  - [ ] Test file upload flow
  - [ ] Run tests: `mvn verify`
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Security Testing**
  - [ ] Test authentication bypass attempts
  - [ ] Test authorization bypass attempts
  - [ ] Test SQL injection (should be safe with JPA)
  - [ ] Test XSS in file uploads
  - [ ] Test CSRF protection
  - [ ] Run OWASP ZAP scan
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Load Testing**
  - [ ] Test with 100 concurrent users
  - [ ] Test with 1000 concurrent users
  - [ ] Test file upload performance
  - [ ] Test database connection pool under load
  - [ ] Identify bottlenecks
  - [ ] Tools: JMeter, Gatling
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________
  - **Due Date:** _____________

### Documentation

- [ ] **Add API Documentation**
  - [ ] Add Springdoc OpenAPI dependency
  - [ ] Add @Operation annotations to controllers
  - [ ] Add @Schema annotations to DTOs
  - [ ] Generate Swagger UI
  - [ ] Review and test API docs
  - [ ] URL: /swagger-ui.html
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Create Deployment Documentation**
  - [ ] Document deployment steps
  - [ ] Document environment variables
  - [ ] Document database setup
  - [ ] Document rollback procedure
  - [ ] Document monitoring dashboards
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Create Operations Runbook**
  - [ ] Common issues and solutions
  - [ ] Emergency contacts
  - [ ] Incident response procedures
  - [ ] Backup and restore procedures
  - [ ] Performance tuning guide
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

### Additional Security

- [ ] **Add CAPTCHA to Login Forms**
  - [ ] Integrate Google reCAPTCHA v3
  - [ ] Add CAPTCHA validation to login endpoints
  - [ ] Configure CAPTCHA threshold
  - [ ] Test CAPTCHA flow
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Implement Password Strength Validation**
  - [ ] Minimum 8 characters
  - [ ] Require uppercase, lowercase, number, special char
  - [ ] Check against common passwords list
  - [ ] Add validation to change password endpoint
  - [ ] Show strength indicator in UI
  - **Estimated Time:** 4 hours
  - **Assigned To:** _____________
  - **Due Date:** _____________

- [ ] **Add Audit Logging**
  - [ ] Log all authentication attempts
  - [ ] Log all authorization failures
  - [ ] Log all data modifications
  - [ ] Log all administrative actions
  - [ ] Store audit logs separately
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________
  - **Due Date:** _____________

---

## 🟢 NICE TO HAVE (Optional Enhancements)

### Performance

- [ ] **Implement Response Caching**
  - [ ] Add caching for store details
  - [ ] Add caching for event lists
  - [ ] Configure cache expiration
  - [ ] Add cache headers to responses
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________

- [ ] **Add Redis for Session Management**
  - [ ] Install Redis
  - [ ] Configure Spring Session
  - [ ] Store sessions in Redis
  - [ ] Test session failover
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________

### Monitoring

- [ ] **Add Application Metrics**
  - [ ] Add Micrometer dependency
  - [ ] Configure Prometheus endpoint
  - [ ] Create Grafana dashboard
  - [ ] Monitor key metrics (requests, errors, latency)
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________

- [ ] **Add Distributed Tracing**
  - [ ] Add Spring Cloud Sleuth
  - [ ] Configure Zipkin
  - [ ] Test trace correlation
  - [ ] Create trace dashboard
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________

### Database

- [ ] **Add Database Migration Tool**
  - [ ] Choose: Flyway or Liquibase
  - [ ] Create initial migration scripts
  - [ ] Configure migration tool
  - [ ] Test migrations
  - [ ] Document migration process
  - **Estimated Time:** 2 days
  - **Assigned To:** _____________

- [ ] **Add Database Backup Strategy**
  - [ ] Configure automated daily backups
  - [ ] Test backup restoration
  - [ ] Document backup procedure
  - [ ] Set up off-site backup storage
  - **Estimated Time:** 1 day
  - **Assigned To:** _____________

### CI/CD

- [ ] **Set Up CI/CD Pipeline**
  - [ ] Configure GitHub Actions / Jenkins
  - [ ] Add automated tests
  - [ ] Add security scanning (OWASP Dependency Check)
  - [ ] Add code quality checks (SonarQube)
  - [ ] Add automated deployment
  - **Estimated Time:** 3 days
  - **Assigned To:** _____________

---

## 📊 PROGRESS TRACKING

### Overall Completion

```
Critical Blockers:     [░░░░░░░░░░░░░░░░░░░░]  0% (0/7 completed)
High Priority:         [░░░░░░░░░░░░░░░░░░░░]  0% (0/10 completed)
Medium Priority:       [░░░░░░░░░░░░░░░░░░░░]  0% (0/12 completed)
Nice to Have:          [░░░░░░░░░░░░░░░░░░░░]  0% (0/6 completed)

TOTAL:                 [░░░░░░░░░░░░░░░░░░░░]  0% (0/35 completed)
```

### Timeline Estimate

| Phase | Duration | Status |
|-------|----------|--------|
| **Phase 1: Critical Security** | 2 weeks | ⏳ Not Started |
| **Phase 2: High Priority** | 1 week | ⏳ Not Started |
| **Phase 3: Testing** | 1 week | ⏳ Not Started |
| **Phase 4: Production Deploy** | 1 week | ⏳ Not Started |
| **TOTAL** | **5 weeks** | ⏳ Not Started |

---

## 🚀 PRE-DEPLOYMENT VERIFICATION

### Final Checklist (Complete ALL before production)

- [ ] All critical blockers resolved
- [ ] All high priority items completed
- [ ] Security audit passed
- [ ] Load testing passed
- [ ] All tests passing (unit + integration)
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Rollback plan documented
- [ ] Monitoring configured
- [ ] Alerts configured
- [ ] Database backed up
- [ ] Environment variables configured
- [ ] SSL certificates installed
- [ ] DNS configured correctly
- [ ] Firewall rules configured
- [ ] Disaster recovery plan in place

### Deployment Sign-Off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| **Tech Lead** | __________ | __________ | ______ |
| **Security Lead** | __________ | __________ | ______ |
| **QA Lead** | __________ | __________ | ______ |
| **DevOps Lead** | __________ | __________ | ______ |
| **Product Owner** | __________ | __________ | ______ |

---

## 📞 CONTACTS & ESCALATION

### Development Team
- **Tech Lead:** _________________
- **Backend Lead:** _________________
- **Security Lead:** _________________

### Operations Team
- **DevOps Lead:** _________________
- **DBA:** _________________
- **SRE:** _________________

### Emergency Escalation
- **Level 1:** _________________
- **Level 2:** _________________
- **Level 3:** _________________

---

## 📝 NOTES

### Decision Log
- Date: __________ Decision: __________________
- Date: __________ Decision: __________________
- Date: __________ Decision: __________________

### Issues Identified
- [ ] Issue #1: __________________
- [ ] Issue #2: __________________
- [ ] Issue #3: __________________

### Additional Requirements
- [ ] Requirement #1: __________________
- [ ] Requirement #2: __________________
- [ ] Requirement #3: __________________

---

**Checklist Version:** 1.0  
**Last Updated:** December 20, 2025  
**Next Review:** Weekly until production deployment  
**Status:** ❌ NOT READY FOR PRODUCTION


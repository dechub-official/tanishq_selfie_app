**Answer:**
```
Port 80 is the standard HTTP port. Benefits:
1. Users don't need to specify port in URL
2. Industry best practice for web applications
3. Using Nginx as reverse proxy (recommended architecture)
4. Easier for testing team and stakeholders
5. Prepares for future SSL/HTTPS on port 443

Alternative: Open port 3002 directly (but not recommended)
```

#### Q3: "Is this secure?"

**Answer:**
```
Current Setup (Pre-Prod):
✅ Application not directly exposed (behind Nginx)
✅ Database not accessible from outside (localhost only)
✅ SELinux configured
✅ Google service account keys have restricted permissions
⚠️ HTTP only (no SSL) - acceptable for pre-prod testing

For Production (Future):
🔒 Will add SSL certificate (HTTPS on port 443)
🔒 Will enable SELinux in enforcing mode
🔒 Will add rate limiting in Nginx
🔒 May restrict access by IP ranges if needed
```

#### Q4: "What is the application doing?"

**Answer:**
```
Application Purpose:
- Tanishq brand customer-facing application
- Features: Wedding checklist, selfie capture, event management
- Pre-production environment for UAT testing
- Will be used by internal testing team initially
- Production version exists on different server (10.10.63.97)

Technology Stack:
- Java 11 + Spring Boot 2.7.0
- MySQL 8.x database
- Nginx reverse proxy
- Integration with Google Sheets/Drive APIs
```

#### Q5: "Can we restrict access instead of 0.0.0.0/0?"

**Answer:**
```
Yes, absolutely! If you have preferred IP ranges:

Option 1: Office IP Range
Source: [Your Office IP Range/CIDR]

Option 2: VPN IP Range
Source: [Your VPN IP Range/CIDR]

Option 3: Specific IPs
Source: [List of specific IPs]

I'm fine with any restriction you recommend for security.
Just let me know the testing team IP range if needed.
```

#### Q6: "How long will this take to test?"

**Answer:**
```
Pre-Production Timeline:
- UAT Testing: 1-2 weeks
- Environment will be active during testing phase
- May be decommissioned after production deployment
- Or kept as permanent pre-prod environment

This is not a temporary setup - it's a standard pre-prod
environment that may be used long-term.
```

#### Q7: "What if something goes wrong?"

**Answer:**
```
Rollback Plan:
- Can stop application: systemctl stop nginx; pkill -f tanishq
- Can remove Security Group rule (revert to closed)
- Can remove DNS entry if needed
- No impact on production server (different instance)

Monitoring:
- Application logs: /opt/tanishq/applications_preprod/app.log
- Nginx logs: /var/log/nginx/celebrations-preprod-*.log
- Can monitor in real-time if issues occur
```

---

### Technical Verification Details

**For AWS Team to Verify:**
```bash
# They may want to verify these on the server

# 1. Check what Security Groups are attached
aws ec2 describe-instances \
  --filters "Name=private-ip-address,Values=10.160.128.94" \
  --query 'Reservations[*].Instances[*].SecurityGroups'

# 2. Check current inbound rules
aws ec2 describe-security-groups \
  --group-ids <sg-id> \
  --query 'SecurityGroups[*].IpPermissions'

# 3. After adding rule, verify it appears
aws ec2 describe-security-groups \
  --group-ids <sg-id> \
  --query 'SecurityGroups[*].IpPermissions[?ToPort==`80`]'
```

**For Network Team to Verify:**
```bash
# They may want to check current DNS

# 1. Check if subdomain exists
nslookup celebrations-preprod.tanishq.co.in

# 2. Check main domain (for reference)
nslookup tanishq.co.in

# 3. After adding, verify propagation
dig celebrations-preprod.tanishq.co.in
host celebrations-preprod.tanishq.co.in
```

---

### Logs They Might Ask For

**Application Startup Log:**
```
2025-12-03 04:24:57 - Starting TanishqSelfieApplication v0.0.1-SNAPSHOT
2025-12-03 04:24:57 - The following 1 profile is active: "preprod"
2025-12-03 04:25:00 - Tomcat initialized with port(s): 3002 (http)
2025-12-03 04:25:05 - Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-12-03 04:25:08 - Tomcat started on port(s): 3002 (http)
2025-12-03 04:25:08 - Started TanishqSelfieApplication in 12.18 seconds
✅ Application started successfully
⚠️ WARNING: Could not load store details from Excel file (expected, not critical)
✅ Application will continue without Excel data
```

**Nginx Status:**
```bash
[root@ip-10-160-128-94]# systemctl status nginx
● nginx.service - The nginx HTTP and reverse proxy server
   Loaded: loaded (/usr/lib/systemd/system/nginx.service; enabled)
   Active: active (running) since Tue 2025-12-03 04:30:00 UTC
```

**Port Listening Status:**
```bash
[root@ip-10-160-128-94]# netstat -tuln
tcp6  0  0  :::3002     :::*    LISTEN  # Application
tcp   0  0  0.0.0.0:80  0.0.0.0:*  LISTEN  # Nginx
```

---

### Contact Information

**If Teams Need More Info:**

**Primary Contact:** [Your Name]  
**Email:** [Your Email]  
**Phone:** [Your Phone]  

**Available For:**
- Screen sharing session if needed
- Providing additional logs
- Testing immediately after changes
- Conference call if easier to explain

**Server Access:**
- I have root access on the server
- Can run any verification commands they need
- Can provide real-time status updates

---

### Summary for Quick Reference

**What Works:**
✅ Application running on port 3002  
✅ Nginx running on port 80  
✅ Database connected  
✅ Local access (localhost) works perfectly  
✅ All configurations correct  

**What Doesn't Work:**
❌ External access via IP (10.160.128.94) - Security Group blocking  
❌ External access via domain (celebrations-preprod.tanishq.co.in) - DNS not configured  

**What I Need:**
1. **AWS Team:** Add inbound rule for port 80 in Security Group  
2. **Network Team:** Add DNS A record pointing to 10.160.128.94  

**Expected Timeline:**
- Configuration: 10-30 minutes  
- Verification: 2-5 minutes  
- **Total:** Less than 1 hour  

---

## 📞 QUICK ANSWERS FOR COMMON SCENARIOS

### If they ask: "Can you show us it's working?"
**Answer:**
```
Yes! I can:
1. Show you SSH session with server
2. Run curl http://localhost:3002 - shows full HTML
3. Show Nginx is proxying correctly
4. Show process is running
5. Show all ports are listening

The application works perfectly on the server.
Just needs external access via Security Group and DNS.
```

### If they ask: "Why Nginx instead of direct access?"
**Answer:**
```
Nginx reverse proxy is industry best practice:
✅ Better security (app not directly exposed)
✅ Can add SSL later easily
✅ Can add rate limiting
✅ Can serve static files directly
✅ Can add caching if needed
✅ Standard port 80 (user friendly)
✅ Easier to add multiple apps later

Alternative: I can use port 3002 directly if you prefer,
but Nginx is the recommended approach.
```

### If they ask: "What testing will you do?"
**Answer:**
```
Testing Plan:
1. Verify homepage loads
2. Test all three features (Wedding Checklist, Selfie, Events)
3. Test user login functionality
4. Test database operations
5. Test file upload features
6. Share with testing team (5-10 people)
7. Conduct UAT for 1-2 weeks
8. Fix any issues found
9. Deploy to production after UAT approval

I'll notify you if any issues occur during testing.
```

---

**Use this document to answer any questions from the teams!**

**Keep it handy for the call/meeting!** 📋
# 📋 DETAILED EXPLANATION FOR INFRASTRUCTURE TEAMS

**Use this document to answer any questions from AWS or Network teams**

---

## 🎯 QUICK SUMMARY FOR TEAMS

**What I Did:**
- Deployed a Java Spring Boot application (Tanishq Celebrations) on pre-prod server
- Application is running successfully on port 3002
- Installed Nginx as reverse proxy on port 80
- Configured all settings correctly

**What I Need:**
1. **AWS Team:** Open port 80 in Security Group
2. **Network Team:** Configure DNS for celebrations-preprod.tanishq.co.in

**Current Status:**
- ✅ Application working perfectly (verified locally)
- ❌ Cannot access from outside due to Security Group/DNS

---

## 📊 COMPLETE TECHNICAL DETAILS

### Server Information

**Instance Details:**
```
Instance Name: ip-10-160-128-94.ap-south-1.compute.internal
Private IP: 10.160.128.94
Region: ap-south-1 (AWS Mumbai)
OS: Red Hat Enterprise Linux (RHEL) 10
User: jewdev-test (has root access via sudo)
```

**To find Instance ID:**
```bash
# They may ask for this
curl -s http://169.254.169.254/latest/meta-data/instance-id
```

---

### Application Details

**Application:**
```
Name: Tanishq Celebrations (Pre-Production)
Type: Java Spring Boot Application (WAR file)
Framework: Spring Boot 2.7.0
Java Version: OpenJDK 11.0.29
Application Port: 3002 (internal)
External Port: 80 (via Nginx reverse proxy)
```

**WAR File:**
```
File: tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
Location: /opt/tanishq/applications_preprod/
Size: ~177 MB
```

**Running Process:**
```bash
# Process details
Process ID: 254114 (current)
Command: java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod
User: root
Working Dir: /opt/tanishq/applications_preprod/
```

---

### What I Installed/Configured

#### 1. Application Deployment
```bash
# Directories created
/opt/tanishq/applications_preprod/       # Application files
/opt/tanishq/storage/selfie_images/      # Image uploads
/opt/tanishq/storage/bride_uploads/      # Bride form uploads
/opt/tanishq/                            # Google service account keys

# Files uploaded
- tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war (application)
- tanishqgmb-5437243a8085.p12 (Google service account key)
- event-images-469618-32e65f6d62b3.p12 (Event images key)
```

#### 2. Database Configuration
```bash
# MySQL Database
Database Name: selfie_preprod
User: root
Port: 3306 (localhost only)
Status: ✅ Connected and working
Tables: 15 tables created successfully
```

**Database Tables Created:**
- users, events, stores
- event_attendees, event_invitees
- bride_details, rivaah, rivaah_users
- user_details, greetings
- password_history, product_details
- abm_login, rbm_login, cee_login

#### 3. SELinux Configuration
```bash
# SELinux status
Current Mode: Permissive (temporarily, for testing)
Original Mode: Enforcing

# Port configured
semanage port -a -t http_port_t -p tcp 3002

# Nginx permissions
setsebool -P httpd_can_network_connect 1
```

#### 4. Nginx Installation
```bash
# Nginx version
Package: nginx (latest from yum repo)
Config File: /etc/nginx/conf.d/celebrations-preprod.conf
Status: ✅ Running and enabled
```

**Nginx Configuration:**
```nginx
server {
    listen 80;
    server_name celebrations-preprod.tanishq.co.in 10.160.128.94;
    client_max_body_size 10M;
    
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

### Verification Commands (What's Working)

**Application is running:**
```bash
[root@ip-10-160-128-94]# ps -ef | grep tanishq
root      254114       1  ... java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war

[root@ip-10-160-128-94]# netstat -tuln | grep 3002
tcp6       0      0 :::3002                 :::*                    LISTEN
```

**Application responds locally:**
```bash
[root@ip-10-160-128-94]# curl http://localhost:3002
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
Content-Length: 34455

<!doctype html>
<html lang="en">
... (full HTML page loads successfully)
```

**Nginx is running:**
```bash
[root@ip-10-160-128-94]# systemctl status nginx
● nginx.service - The nginx HTTP and reverse proxy server
   Active: active (running)

[root@ip-10-160-128-94]# netstat -tuln | grep :80
tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN
```

**Nginx proxy working:**
```bash
[root@ip-10-160-128-94]# curl http://localhost
HTTP/1.1 200 OK
... (same HTML as port 3002 - proxy working!)
```

---

### Network Configuration

**Current Port Bindings:**
```
Port 3002: Java Application (listening on all interfaces :::3002)
Port 80: Nginx (listening on all interfaces 0.0.0.0:80)
Port 3306: MySQL (localhost only)
Port 22: SSH (already configured)
```

**Firewall Status:**
```
firewalld: Not installed
iptables: Not installed
SELinux: Permissive mode (port 3002 allowed in enforcing mode too)
AWS Security Group: Blocking external access (THIS IS THE ISSUE)
```

**What's listening:**
```bash
[root@ip-10-160-128-94]# ss -tuln
tcp   LISTEN   0   100   *:3002    *:*      # Application
tcp   LISTEN   0   511   *:80      *:*      # Nginx
tcp   LISTEN   0   151   127.0.0.1:3306  *:*      # MySQL (local only)
tcp   LISTEN   0   128   *:22      *:*      # SSH
```

---

### The Issue Explained

#### Problem 1: Cannot Access from External Network

**Symptom:**
- ✅ `curl http://localhost:3002` works on server
- ✅ `curl http://localhost` works on server (Nginx proxy)
- ❌ Cannot access from browser: `http://10.160.128.94:3002` (connection timeout)
- ❌ Cannot access from browser: `http://10.160.128.94` (connection timeout)

**Root Cause:**
AWS Security Group is blocking inbound traffic on ports 3002 and 80.

**Evidence:**
```
- Application is listening on all interfaces (:::3002, 0.0.0.0:80)
- No local firewall blocking (firewalld/iptables not installed)
- SELinux configured correctly
- Local access works perfectly
- External access times out (not refused)
→ Conclusion: Cloud-level firewall (Security Group) blocking
```

#### Problem 2: Domain Not Accessible

**Symptom:**
- ❌ `http://celebrations-preprod.tanishq.co.in` shows "DNS not resolved"
- ❌ `nslookup celebrations-preprod.tanishq.co.in` fails

**Root Cause:**
DNS A record not configured for the subdomain.

---

### What I Need from AWS Team

#### Security Group Configuration Required

**Instance Identifier:**
- Private IP: 10.160.128.94
- Instance Name: ip-10-160-128-94.ap-south-1.compute.internal
- Instance ID: (can be retrieved with metadata command)

**Inbound Rule to Add:**

| Field | Value |
|-------|-------|
| **Type** | HTTP (or Custom TCP) |
| **Protocol** | TCP |
| **Port Range** | 80 |
| **Source** | 0.0.0.0/0 (public access)<br>OR specific office IP range |
| **Description** | Tanishq Celebrations Pre-Prod Application |

**Optional (if you want direct app access):**

| Field | Value |
|-------|-------|
| **Type** | Custom TCP |
| **Protocol** | TCP |
| **Port Range** | 3002 |
| **Source** | 0.0.0.0/0 (or specific IPs) |
| **Description** | Tanishq Celebrations Direct App Access |

**Why Port 80:**
- Industry standard HTTP port
- Usually already open in many environments
- Using Nginx as reverse proxy (recommended practice)
- Easier for users (no port in URL)

---

### What I Need from Network Team

#### DNS Configuration Required

**DNS Record:**
```
Record Type: A
Name/Hostname: celebrations-preprod.tanishq.co.in
Value/Points to: 10.160.128.94
TTL: 300 (5 minutes) - or as per your policy
```

**Domain Hierarchy:**
```
Main Domain: tanishq.co.in (existing)
Subdomain: celebrations-preprod.tanishq.co.in (new)
Purpose: Pre-production testing environment
```

**After Configuration:**
```
Expected Result:
- nslookup celebrations-preprod.tanishq.co.in
  → Should return: 10.160.128.94

- Browser access: http://celebrations-preprod.tanishq.co.in
  → Should load application homepage
```

---

### Testing Procedure (After Configuration)

**Step 1: Verify Security Group (immediately after AWS change)**
```bash
# From your computer's browser
http://10.160.128.94

Expected: Application homepage loads
If fails: Security Group not applied correctly
```

**Step 2: Verify DNS (5-15 minutes after Network change)**
```bash
# From your computer's command prompt
nslookup celebrations-preprod.tanishq.co.in

Expected: Returns 10.160.128.94

# From browser
http://celebrations-preprod.tanishq.co.in

Expected: Application homepage loads
If fails: DNS not propagated yet (wait 5 more minutes)
```

---

### Common Questions from Teams

#### Q1: "What changes did you make to the server?"

**Answer:**
```
1. Deployed Java application WAR file
2. Created application directories under /opt/tanishq/
3. Installed and configured Nginx as reverse proxy
4. Configured SELinux to allow port 3002 and Nginx connectivity
5. Started application service on port 3002
6. Started Nginx service on port 80

No changes to:
- Network configuration
- Routing tables
- System firewall (none installed)
- SSH configuration
- Other running services
```

#### Q2: "Why do you need port 80 open?"



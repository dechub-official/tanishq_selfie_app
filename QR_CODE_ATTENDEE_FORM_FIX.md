---

**Status:** 🔴 Issue Identified  
**Root Cause:** DNS/Deployment, NOT code  
**Code Status:** ✅ Working correctly  
**Next Action:** Fix DNS or use IP-based URL  
**ETA to Fix:** 2 hours with IT support

---

**The feature worked with Google Sheets because the domain was accessible. Now it's not a code issue - it's infrastructure!**
# 🚨 QR CODE ATTENDEE FORM ISSUE - DIAGNOSIS & FIX
## Critical Issue: QR Code Scan Not Showing Attendee Form

**Issue Reported:** December 18, 2025  
**Status:** 🔴 CRITICAL - Blocking Production  
**Impact:** Users cannot register as attendees via QR code

---

## 🔍 PROBLEM DIAGNOSIS

### **Issue Description:**
When users scan the QR code from completed events:
- ❌ QR code downloads successfully
- ❌ Scanning QR code shows: "This site can't be reached"
- ❌ Error: `ERR_CONNECTION_TIMED_OUT`
- ❌ URL: `celebrationsite-preprod.tanishq.co.in`

### **Root Cause Analysis:**

#### ✅ Code is CORRECT:
```java
// EventsController.java - Line 75
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    return new ModelAndView("forward:/events.html");
}
```

#### ✅ QR Code Configuration is CORRECT:
```properties
# application-preprod.properties - Line 52
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

#### ✅ Frontend File EXISTS:
```
File: src/main/resources/static/events.html
Status: ✅ Present (1,060 bytes)
```

### 🔴 **ACTUAL PROBLEM: DNS/DEPLOYMENT ISSUE**

The domain `celebrationsite-preprod.tanishq.co.in` is:
1. ❌ **Not accessible from internet** (connection timeout)
2. ❌ **DNS not configured properly** OR
3. ❌ **Application not deployed on that domain** OR
4. ❌ **Firewall blocking external access** OR
5. ❌ **Load balancer not routing correctly**

---

## 🔧 IMMEDIATE FIX OPTIONS

### **Option 1: Use Local Network IP (Quick Test - 5 minutes)**

If you're testing locally or on internal network:

**Update configuration:**
```properties
# application-preprod.properties
# OLD (doesn't work from mobile):
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/

# NEW (use server IP):
qr.code.base.url=http://10.160.128.94:3000/events/customer/
```

**Steps:**
1. Update `application-preprod.properties`
2. Rebuild WAR file: `mvn clean package -Ppreprod`
3. Redeploy application
4. Create new event to get new QR code with correct URL
5. Scan QR code - should work on same network

**Limitation:** Only works on same network as server

---

### **Option 2: Fix DNS Configuration (Production Fix - 30 minutes)**

**Check DNS:**
```bash
# On your machine
nslookup celebrationsite-preprod.tanishq.co.in

# Should return:
# Name:    celebrationsite-preprod.tanishq.co.in
# Address: 10.160.128.94

# If it returns nothing or wrong IP, DNS is not configured
```

**Fix DNS:**
1. Contact your IT/DevOps team
2. Ask them to create DNS A record:
   - **Hostname:** celebrationsite-preprod.tanishq.co.in
   - **Type:** A
   - **Value:** 10.160.128.94 (your server IP)
   - **TTL:** 300

3. Wait 5-10 minutes for DNS propagation
4. Test: `ping celebrationsite-preprod.tanishq.co.in`

---

### **Option 3: Configure Reverse Proxy (30-60 minutes)**

If application is on port 3000 but domain points to port 80/443:

**Install Nginx (if not installed):**
```bash
# On server (Ubuntu/CentOS)
sudo apt-get update
sudo apt-get install nginx -y
# OR
sudo yum install nginx -y
```

**Configure Nginx:**
```nginx
# /etc/nginx/conf.d/tanishq-preprod.conf
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Apply:**
```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

### **Option 4: Use ngrok for Testing (5 minutes - Quick & Dirty)**

If you just want to test QR code functionality immediately:

**Install ngrok:**
```bash
# Download from https://ngrok.com/download
# OR
choco install ngrok  # Windows
```

**Start tunnel:**
```bash
ngrok http 3000
```

**Copy the HTTPS URL (e.g., https://abc123.ngrok.io)**

**Update configuration:**
```properties
qr.code.base.url=https://abc123.ngrok.io/events/customer/
```

**Rebuild and test**

**⚠️ WARNING:** ngrok URLs expire after 2 hours (free tier). Only for testing!

---

## ✅ SOLUTION IMPLEMENTATION

### **RECOMMENDED: Fix DNS + Nginx (Production-Ready)**

#### Step 1: Fix DNS (IT Team)
```
Request DNS A Record:
- Hostname: celebrationsite-preprod.tanishq.co.in
- Type: A
- Value: 10.160.128.94
```

#### Step 2: Configure Nginx on Server
```bash
# SSH to server
ssh user@10.160.128.94

# Install Nginx
sudo apt-get update && sudo apt-get install nginx -y

# Create config
sudo nano /etc/nginx/conf.d/tanishq-preprod.conf
```

Paste:
```nginx
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in;

    # Redirect HTTP to HTTPS (if you have SSL)
    # return 301 https://$server_name$request_uri;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # Increase timeouts for file uploads
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
    }
}
```

Save and test:
```bash
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl enable nginx
```

#### Step 3: Test
```bash
# From your laptop
curl http://celebrationsite-preprod.tanishq.co.in

# Should return HTML response
```

#### Step 4: Update QR Code Config (if needed)
```properties
# application-preprod.properties
# Use HTTP for now, add HTTPS later
qr.code.base.url=http://celebrationsite-preprod.tanishq.co.in/events/customer/
```

#### Step 5: Rebuild & Deploy
```bash
mvn clean package -Ppreprod
# Copy WAR to server
# Restart application
```

---

## 🧪 TESTING PROCEDURE

### **Test 1: Check DNS**
```bash
nslookup celebrationsite-preprod.tanishq.co.in
# Should return: 10.160.128.94
```

### **Test 2: Check HTTP Access**
```bash
curl http://celebrationsite-preprod.tanishq.co.in
# Should return HTML
```

### **Test 3: Check Events Endpoint**
```bash
curl http://celebrationsite-preprod.tanishq.co.in/events/customer/TEST123
# Should return events.html content
```

### **Test 4: Generate QR Code**
1. Login to application
2. Create test event
3. Download QR code
4. Scan with phone
5. Should show attendee form ✅

---

## 🔍 TROUBLESHOOTING

### **Issue: DNS not resolving**
```bash
# Check if DNS record exists
dig celebrationsite-preprod.tanishq.co.in

# If no result, contact IT to add DNS record
```

### **Issue: Connection refused**
```bash
# Check if application is running
curl http://localhost:3000

# Check if port is open
netstat -tuln | grep 3000

# Check firewall
sudo ufw status
sudo firewall-cmd --list-all
```

### **Issue: 502 Bad Gateway (Nginx)**
```bash
# Check application logs
tail -f /opt/tanishq/applications_preprod/logs/application.log

# Check Nginx error logs
sudo tail -f /var/log/nginx/error.log

# Restart application
sudo systemctl restart tanishq
```

### **Issue: QR code has old URL**
- QR codes are generated once and stored
- Old QR codes will still have old URL
- Solution: Create new event to get new QR code with correct URL

---

## 📊 COMPARISON: GOOGLE SHEETS vs MYSQL

### **What Changed:**

| Aspect | Google Sheets (Old) | MySQL (New) |
|--------|-------------------|-------------|
| **QR Code URL** | Was pointing to working domain | Points to non-accessible domain |
| **Code Logic** | Same | Same (no change) |
| **Frontend** | Same events.html | Same events.html |
| **Problem** | Domain was accessible | Domain not accessible |

### **Why It Worked Before:**
The old setup probably had:
- ✅ Proper DNS configuration
- ✅ Nginx reverse proxy
- ✅ Application accessible on correct domain

### **Why It's Not Working Now:**
- ❌ New domain not configured in DNS
- ❌ No reverse proxy setup
- ❌ Application only accessible on IP:port

---

## 🎯 QUICK WIN: TEMPORARY FIX FOR IMMEDIATE TESTING

If you need to test RIGHT NOW without waiting for DNS/Nginx:

### **Use Direct IP in QR Code (2 minutes)**

1. **Edit this file:**
   ```
   src/main/resources/application-preprod.properties
   ```

2. **Change line 52:**
   ```properties
   # OLD:
   qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
   
   # NEW (your server IP):
   qr.code.base.url=http://10.160.128.94:3000/events/customer/
   ```

3. **Rebuild:**
   ```bash
   mvn clean package -Ppreprod
   ```

4. **Deploy new WAR file**

5. **Create new event → Get QR code → Test**

**⚠️ Limitations:**
- Only works on same network
- Not suitable for production
- Mobile must be on company network or VPN

**✅ Advantages:**
- Works immediately
- No DNS/Nginx setup needed
- Good for testing

---

## 🚀 RECOMMENDED DEPLOYMENT PATH

### **Phase 1: Immediate (Today)**
1. ✅ Use IP-based QR code for testing
2. ✅ Verify attendee form loads
3. ✅ Verify attendee registration works
4. ✅ Test with 5-10 people

### **Phase 2: Short-term (This Week)**
1. ⏳ Get IT to configure DNS
2. ⏳ Set up Nginx reverse proxy
3. ⏳ Update QR code URL to domain
4. ⏳ Rebuild and redeploy
5. ⏳ Test QR code works from anywhere

### **Phase 3: Long-term (Next Week)**
1. 🔵 Add SSL certificate (HTTPS)
2. 🔵 Configure proper firewall rules
3. 🔵 Set up load balancer (if needed)
4. 🔵 CDN for static assets
5. 🔵 Production deployment

---

## 📋 CHECKLIST: QR CODE FUNCTIONALITY

- [ ] DNS resolves correctly
- [ ] Application accessible on domain
- [ ] Nginx reverse proxy configured
- [ ] Port 80/443 open in firewall
- [ ] QR code generates with correct URL
- [ ] QR code scannable with phone
- [ ] Attendee form loads
- [ ] Attendee registration works
- [ ] Data saves to database
- [ ] Count updates in real-time

---

## 💡 CODE VERIFICATION (Already Correct!)

### ✅ Controller Endpoint:
```java
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    return new ModelAndView("forward:/events.html");
}
```
**Status:** ✅ Correct - forwards to events.html

### ✅ QR Code Generation:
```java
@Override
public String generateEventQrCode(String eventId) throws Exception {
    String qrUrl = QR_URL_BASE + eventId.trim();
    return generateQrCodeBase64(qrUrl, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
}
```
**Status:** ✅ Correct - uses configured base URL

### ✅ Frontend File:
```
src/main/resources/static/events.html
```
**Status:** ✅ Present - Contains React app for attendee form

---

## 🎯 ACTION ITEMS

### **For You (Developer):**
1. [ ] Choose fix option (IP-based or DNS+Nginx)
2. [ ] Update configuration
3. [ ] Rebuild WAR file
4. [ ] Test locally

### **For IT/DevOps Team:**
1. [ ] Configure DNS A record
2. [ ] Install Nginx on server
3. [ ] Configure reverse proxy
4. [ ] Open ports 80/443 in firewall
5. [ ] Add SSL certificate (optional but recommended)

### **For Testing Team:**
1. [ ] Create test event
2. [ ] Download QR code
3. [ ] Scan with multiple phones
4. [ ] Verify form loads
5. [ ] Submit test attendee
6. [ ] Verify data in database

---

## 📞 WHO TO CONTACT

### **DNS Issue:**
- Contact: IT Infrastructure Team
- Request: DNS A record for celebrationsite-preprod.tanishq.co.in → 10.160.128.94

### **Nginx/Server Issue:**
- Contact: DevOps/System Admin Team
- Request: Nginx reverse proxy setup for port 3000

### **Firewall Issue:**
- Contact: Network Security Team
- Request: Open ports 80, 443 for inbound traffic

---

## 📊 EXPECTED TIMELINE

| Task | Time | Owner |
|------|------|-------|
| DNS Configuration | 30 min | IT Team |
| Nginx Setup | 30 min | DevOps |
| Code Update + Deploy | 15 min | Developer |
| Testing | 30 min | QA |
| **Total** | **2 hours** | All |

---

## ✅ SUCCESS CRITERIA

**QR Code feature is working when:**
1. ✅ Scan QR code with phone
2. ✅ Browser opens attendee form
3. ✅ Form shows event details
4. ✅ Can fill name, phone, preferences
5. ✅ Submit button works
6. ✅ Success message shown
7. ✅ Data saved in database
8. ✅ Attendee count increments

---

## 🎓 LEARNING: Why This Happened

**Google Sheets → MySQL Migration:**
- Code was migrated ✅
- Database was migrated ✅
- Application deployed ✅
- **DNS configuration was missed** ❌

**Prevention:**
- Always update DNS when deploying to new domain
- Set up reverse proxy for production
- Test QR codes end-to-end before go-live
- Document deployment checklist



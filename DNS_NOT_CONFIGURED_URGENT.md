# ⚠️ DNS NOT CONFIGURED - URGENT FOLLOW-UP REQUIRED

**Status:** DNS is **NOT actually configured** despite network team's response  
**Date:** December 3, 2025  
**Issue:** Domain returns NXDOMAIN (Non-existent domain)

---

## 🔴 PROBLEM CONFIRMATION

### Test Results:

**From Your Computer (Windows):**
```
C:\Users\Prasanna> nslookup celebrations-preprod.tanishq.co.in
Server:  TCLBLRCORPDC02.titan.com
Address:  172.25.6.22

*** TCLBLRCORPDC02.titan.com can't find celebrations-preprod.tanishq.co.in: Non-existent domain
```

**From Server (Linux):**
```
[root@ip-10-160-128-94]# nslookup celebrations-preprod.tanishq.co.in
Server:         10.160.128.2
Address:        10.160.128.2#53

** server can't find celebrations-preprod.tanishq.co.in: NXDOMAIN
```

**Conclusion:** DNS A record does **NOT exist** in the DNS system.

---

## 📧 URGENT FOLLOW-UP EMAIL TO NETWORK TEAM

**Send this email NOW:**

---

**To:** Dona Manuel  
**CC:** Prema, Santhosh, Misha, Anna, Atul, Viji  
**Subject:** URGENT: DNS for celebrations-preprod.tanishq.co.in NOT Resolving - Need Immediate Assistance

Hi Dona,

Thank you for your earlier response regarding the DNS configuration.

However, I'm unable to resolve the domain name. The DNS queries are returning **NXDOMAIN** (Non-existent domain) from multiple locations.

**DNS Test Results:**

**From Corporate Network (Windows):**
```
Server:  TCLBLRCORPDC02.titan.com (172.25.6.22)
Result:  *** can't find celebrations-preprod.tanishq.co.in: Non-existent domain
```

**From AWS Server (Linux - 10.160.128.94):**
```
Server:  10.160.128.2
Result:  ** server can't find celebrations-preprod.tanishq.co.in: NXDOMAIN
```

**Requested DNS Configuration:**
```
Record Type: A
Hostname: celebrations-preprod.tanishq.co.in
IP Address: 10.160.128.94
TTL: 300
```

**Issue:**
The DNS record does not appear to exist in the DNS system. Both internal DNS servers (corporate and AWS) are unable to find the domain.

**Request:**
Could you please:
1. **Verify** the DNS entry was actually added to the zone file
2. **Confirm** the exact hostname configured (is it celebrations-preprod.tanishq.co.in with hyphen?)
3. **Check** if the DNS zone was reloaded after the entry was added
4. **Provide** the authoritative DNS server for tanishq.co.in domain
5. **Share** a screenshot or confirmation of the DNS entry if possible

**Alternative Test:**
I've also tested using:
- nslookup from multiple locations
- Different DNS servers (Google DNS 8.8.8.8, Cloudflare 1.1.1.1)
- Multiple devices and networks

All return the same NXDOMAIN error.

**Impact:**
This is blocking our UAT testing. The application is ready and running, but cannot be accessed via the domain name.

**Workaround (Temporary):**
Currently accessible via IP: http://10.160.128.94 (but this requires AWS Security Group access)

Could you please investigate this urgently and confirm when the DNS entry will be active?

Thank you for your immediate attention to this matter.

Regards,  
Nagaraj

---

---

## 🔍 ADDITIONAL VERIFICATION STEPS

**You can provide these details if they ask:**

### Test from Multiple DNS Servers:

**Using Google DNS:**
```cmd
nslookup celebrations-preprod.tanishq.co.in 8.8.8.8
```

**Using Cloudflare DNS:**
```cmd
nslookup celebrations-preprod.tanishq.co.in 1.1.1.1
```

**Expected Result:** All should return NXDOMAIN (proving DNS not configured)

### Online DNS Checker:

1. Visit: https://dnschecker.org
2. Enter: celebrations-preprod.tanishq.co.in
3. Result: Should show "No records found" globally

**Take screenshot** of these results to attach to email if needed.

---

## 🎯 POSSIBLE REASONS FOR THIS ISSUE

### Why DNS Might Not Be Configured:

1. **Entry Not Added:** The DNS entry was not actually added to the zone file
2. **Wrong Zone:** Entry added to wrong DNS zone (not tanishq.co.in)
3. **Typo in Hostname:** Hostname configured incorrectly (missing hyphen, extra space, etc.)
4. **DNS Not Reloaded:** Zone file updated but DNS server not reloaded
5. **Wrong DNS Server:** Entry added to wrong authoritative DNS server
6. **Pending Approval:** DNS change awaiting approval/authorization
7. **Misunderstanding:** Team thought you wanted something else configured

### What Network Team Should Check:

```bash
# On their DNS server, they should verify:

# 1. Check if zone exists
dig tanishq.co.in SOA

# 2. Check if entry exists in zone file
grep "celebrations-preprod" /var/named/tanishq.co.in.zone
# (path may vary)

# 3. Check DNS server configuration
named-checkzone tanishq.co.in /var/named/tanishq.co.in.zone

# 4. Verify DNS is serving the zone
dig @localhost celebrations-preprod.tanishq.co.in

# 5. Check DNS server logs
tail -50 /var/log/messages | grep named
```

---

## ⚡ TEMPORARY WORKAROUND

### Option 1: Use IP Address (Current)

**Until DNS is fixed, you can access via:**
```
http://10.160.128.94
```

**Requirements:**
- Need to be on VPN
- AWS Security Group must have port 80 open

### Option 2: Hosts File (Local Testing Only)

**For testing purposes, you can temporarily edit hosts file:**

**On Windows:**
1. Run Notepad as Administrator
2. Open: `C:\Windows\System32\drivers\etc\hosts`
3. Add line: `10.160.128.94  celebrations-preprod.tanishq.co.in`
4. Save file
5. Test: `ping celebrations-preprod.tanishq.co.in`

**On Server (if needed):**
```bash
echo "10.160.128.94  celebrations-preprod.tanishq.co.in" >> /etc/hosts
```

**Note:** This only works on computers where you add it - NOT a real solution!

---

## 📊 WHAT'S CONFIGURED IN YOUR APPLICATION

### Application Configuration:

**File:** `application-preprod.properties`

```properties
# QR Code URL - Uses the domain
qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/
```

**Status:** ✅ Correctly configured in application

### Nginx Configuration (On Server):

**File:** `/etc/nginx/conf.d/celebrations-preprod.conf`

```nginx
server {
    listen 80;
    server_name celebrations-preprod.tanishq.co.in 10.160.128.94 _;
    ...
}
```

**Status:** ✅ Correctly configured to accept both domain and IP

**Current Access:**
- ✅ Works: http://localhost:3002 (on server)
- ✅ Works: http://localhost (on server, via Nginx)
- ⚠️ Works: http://10.160.128.94 (if AWS Security Group open)
- ❌ Doesn't Work: http://celebrations-preprod.tanishq.co.in (DNS not configured)

---

## 🔄 EXPECTED NETWORK TEAM RESPONSE

### What They Should Do:

1. **Acknowledge** the DNS is not configured
2. **Add** the DNS A record properly
3. **Reload** the DNS server
4. **Verify** it's working from their end
5. **Confirm** with you once it's active

### What They Might Ask:

**Q: "What exact domain name do you need?"**  
A: celebrations-preprod.tanishq.co.in (with hyphen between preprod and tanishq)

**Q: "What IP should it point to?"**  
A: 10.160.128.94

**Q: "What type of DNS record?"**  
A: A record (IPv4 address)

**Q: "What TTL?"**  
A: 300 seconds (5 minutes) - or whatever is standard

**Q: "Is the server accessible?"**  
A: Yes, accessible via http://10.160.128.94 (need AWS Security Group port 80 open)

**Q: "Do you have authorization?"**  
A: This is for official pre-production environment for Tanishq Celebrations project

---

## 📞 IF NO RESPONSE IN 2 HOURS

### Escalation Email:

**To:** Dona Manuel  
**CC:** Prema, Santhosh, Misha, Anna, Atul, Viji, [Your Manager]  
**Subject:** URGENT ESCALATION: DNS Configuration Blocking UAT - Need Immediate Resolution

Hi Team,

Following up on my earlier email regarding DNS configuration for celebrations-preprod.tanishq.co.in.

**Current Status:**
- DNS still not resolving (NXDOMAIN)
- UAT testing blocked
- 2+ hours since initial request

**Business Impact:**
- Cannot proceed with scheduled UAT testing
- Testing team waiting
- Deployment timeline at risk

**Immediate Action Required:**
Please prioritize this DNS configuration. The application is ready and waiting for DNS to be configured.

**Contact:** Available for conference call if needed to resolve quickly.

Regards,  
Nagaraj

---

## ✅ ONCE DNS IS ACTUALLY CONFIGURED

**You'll know it's working when:**

```cmd
nslookup celebrations-preprod.tanishq.co.in
```

**Returns:**
```
Server:  ...
Address:  ...

Name:    celebrations-preprod.tanishq.co.in
Address:  10.160.128.94
```

**And browser loads:**
```
http://celebrations-preprod.tanishq.co.in
→ Shows your Tanishq Celebrations homepage ✅
```

**Then send thank you emails** (use templates from REPLY_EMAIL_TEMPLATES.md)

---

## 🎯 IMMEDIATE ACTION PLAN

### DO THIS NOW:

1. ✅ **Send follow-up email** to Dona Manuel (copy from above)
2. ⏱️ **Wait 1 hour** for response
3. 📧 **Follow up** if no response
4. 🆙 **Escalate** to manager if still no response after 2 hours
5. 🔄 **Keep testing** DNS every 15 minutes: `nslookup celebrations-preprod.tanishq.co.in`

### WHILE WAITING:

- ✅ Application is ready
- ✅ Database is ready
- ✅ Nginx is ready
- ✅ Configuration is correct
- ⏳ Just waiting for DNS to be properly configured

---

## 📝 DOCUMENTATION

**Evidence to provide if asked:**

1. **nslookup results** (from both Windows and Linux)
2. **Screenshot** of browser showing "can't resolve"
3. **Proof app works via IP** (http://10.160.128.94)
4. **Your original request email** with exact details
5. **Online DNS checker** results (dnschecker.org)

---

## 🔑 KEY MESSAGE TO NETWORK TEAM

**The issue is clear:**
- ✅ You requested: celebrations-preprod.tanishq.co.in → 10.160.128.94
- ❌ Current state: DNS returns NXDOMAIN (does not exist)
- 🎯 Need: Actual DNS A record to be created and active

**Not a propagation issue** - the record simply doesn't exist in DNS.

---

**SEND THE FOLLOW-UP EMAIL NOW!** 📧

**File:** Copy from section "URGENT FOLLOW-UP EMAIL TO NETWORK TEAM" above


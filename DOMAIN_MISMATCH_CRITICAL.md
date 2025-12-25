# ⚠️ CRITICAL: DOMAIN NAME MISMATCH - MUST BE RESOLVED

**Date:** December 3, 2025  
**Issue:** Network team configured WRONG domain name

---

## 🔴 THE PROBLEM

### What YOU Requested and Configured in Code:
```
celebrations-preprod.tanishq.co.in
```
*(Note: "celebrations" with 's' at the end)*

### What NETWORK TEAM Configured:
```
celebrationsite-preprod.tanishq.co.in
```
*(Note: "celebrationsite" - completely different!)*

**This is a CRITICAL MISMATCH!**

---

## ✅ VERIFICATION - WHAT'S IN YOUR CODE

I've verified your entire project. Here's what domain is configured:

### 1. Application Properties (`application-preprod.properties`)

**Line 97:**
```properties
qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/
```

**Line 3:**
```properties
app.cors.allowedOrigins=*
```
*(Currently allows all origins, but for production should be specific domain)*

### 2. Server Configuration (Nginx on server)

**File:** `/etc/nginx/conf.d/celebrations-preprod.conf`

```nginx
server_name celebrations-preprod.tanishq.co.in 10.160.128.94 _;
```

---

## 📊 COMPLETE DOMAIN USAGE IN YOUR PROJECT

**Domain used:** `celebrations-preprod.tanishq.co.in`

**Where it's used:**
1. ✅ **QR Code Generation** - `qr.code.base.url` property
2. ✅ **Nginx Server Name** - Accepts requests for this domain
3. ✅ **Email templates** (if any) - References to the domain
4. ✅ **Frontend URLs** - May have hardcoded references

**CORS Configuration:**
- Currently: `*` (allows all)
- Recommended for production: Specific domain only

---

## 🎯 WHAT NETWORK TEAM CONFIGURED (WRONG!)

**From Anna Mariya's email:**

```
Pre-Production URL: celebrationsite-preprod.tanishq.co.in  ❌ WRONG!
Sub-Domain: preprod.tanishq.co.in                         ❌ WRONG!
DNS Name: internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
Record Type: CNAME
```

**Problems:**
1. ❌ Domain is `celebrationsite-preprod` not `celebrations-preprod`
2. ❌ Also mentioned `preprod.tanishq.co.in` - different domain
3. ❌ Using CNAME to ELB (Load Balancer) - different architecture than requested
4. ❌ None of these match your application configuration

---

## 📧 URGENT REPLY TO ANNA MARIYA

**Send this email NOW:**

---

**To:** Anna Mariya  
**CC:** Dona Manuel, Prema, Santhosh, Misha, Atul, Viji  
**Subject:** URGENT: Wrong Domain Configured - Need Correction

Hi Anna,

Thank you for the DNS configuration details. However, there's a critical mismatch.

**What You Configured:**
- `celebrationsite-preprod.tanishq.co.in` (with "site")
- `preprod.tanishq.co.in`

**What I Requested and Need:**
- `celebrations-preprod.tanishq.co.in` (with "s", NO "site")

**Issue:**
The domain name is different. My application is configured to work with:
```
celebrations-preprod.tanishq.co.in
```

**Current Configuration in My Application:**

1. **Nginx Configuration:**
   ```
   server_name celebrations-preprod.tanishq.co.in 10.160.128.94;
   ```

2. **Application Properties:**
   ```
   qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/
   ```

3. **All application URLs reference:** `celebrations-preprod.tanishq.co.in`

**What I Need:**

**Option 1 (Preferred):** Configure the domain I requested:
```
Record Type: A (or CNAME if using load balancer)
Hostname: celebrations-preprod.tanishq.co.in
IP/Target: 10.160.128.94 (or your ELB DNS if using load balancer)
```

**Option 2:** If you prefer the domain you configured:
- I will need to reconfigure my entire application
- Update Nginx configuration
- Update application properties
- Rebuild and redeploy WAR file
- This will take additional 2-3 hours

**Questions:**

1. **Why CNAME to ELB?** 
   - Is there a load balancer in front of my server (10.160.128.94)?
   - Or is this for a different environment?

2. **Which domain should we use?**
   - `celebrations-preprod.tanishq.co.in` (as I requested)
   - `celebrationsite-preprod.tanishq.co.in` (as you configured)
   - `preprod.tanishq.co.in` (also mentioned)

3. **ELB Configuration:**
   - If using load balancer, is it configured to route to 10.160.128.94?
   - Do I need to configure anything on my server for the ELB?

**Impact:**
This mismatch is blocking our UAT testing. The application is ready but cannot be accessed due to domain name mismatch.

**Recommendation:**
Please configure DNS for the exact domain I requested: `celebrations-preprod.tanishq.co.in`

If there's a reason to use a different domain, please let me know and I'll reconfigure the application accordingly.

Thank you for your help in resolving this urgently!

Regards,  
Nagaraj

---

---

## 🔍 ADDITIONAL INVESTIGATION NEEDED

### Questions to Clarify with Network Team:

**1. About the ELB (Load Balancer):**
```
DNS Name: internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
```

**This suggests:**
- They may have set up a load balancer
- It's an INTERNAL ELB (not public-facing)
- They're using CNAME (not A record)

**You need to ask:**
- Is this ELB routing to your server (10.160.128.94)?
- Is the ELB already configured?
- Do you need to do anything on your server for this?
- Should you use the ELB instead of direct server access?

**2. About Multiple Domains:**
They mentioned TWO different domains:
- `celebrationsite-preprod.tanishq.co.in`
- `preprod.tanishq.co.in`

**You need to clarify:**
- Which one should you use?
- Are both configured?
- Why are there two different domains?

---

## 🎯 TWO POSSIBLE SOLUTIONS

### **SOLUTION 1: They Fix DNS (Recommended)**

**Network team should:**
1. Configure CORRECT domain: `celebrations-preprod.tanishq.co.in`
2. Point it to either:
   - A record → `10.160.128.94` (direct)
   - OR CNAME → `internal-Jew-Testing-ELB-...` (via load balancer)
3. Ensure ELB (if used) routes to `10.160.128.94`

**You do:** Nothing! Just wait and test.

**Timeline:** 30-60 minutes

---

### **SOLUTION 2: You Reconfigure Application (Not Recommended)**

**You would need to:**
1. Decide which domain to use (`celebrationsite-preprod` or `preprod`)
2. Update `application-preprod.properties`:
   ```properties
   qr.code.base.url=http://celebrationsite-preprod.tanishq.co.in/events/customer/
   # OR
   qr.code.base.url=http://preprod.tanishq.co.in/events/customer/
   ```
3. Update Nginx configuration on server:
   ```nginx
   server_name celebrationsite-preprod.tanishq.co.in 10.160.128.94 _;
   # OR
   server_name preprod.tanishq.co.in 10.160.128.94 _;
   ```
4. Rebuild WAR file
5. Redeploy to server
6. Restart application

**Timeline:** 2-3 hours

**Downside:** 
- Extra work for you
- Delay in UAT
- Inconsistent with your original plan

---

## 💡 RECOMMENDATION

### **Recommended Approach:**

**Request network team to configure the EXACT domain you need:**
```
celebrations-preprod.tanishq.co.in
```

**Reasons:**
1. ✅ Your application is already configured for this
2. ✅ Nginx is already configured for this
3. ✅ No code changes needed
4. ✅ Faster to resolve
5. ✅ Less risk of errors
6. ✅ Consistent naming with production (`celebrations.tanishq.co.in`)

---

## 🔄 ABOUT THE ELB (LOAD BALANCER)

### Understanding the Setup:

**What they configured:**
```
CNAME: celebrationsite-preprod.tanishq.co.in 
→ Points to: internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
```

**What this means:**
- They set up an AWS Elastic Load Balancer (ELB)
- It's INTERNAL (not public-facing)
- Domain points to ELB, not directly to your server

**Implications:**

**If ELB is properly configured:**
- ELB should route traffic to your server (10.160.128.94:80)
- You can use the ELB DNS name directly (but it's internal)
- They need to configure correct domain name to point to ELB

**If ELB is NOT configured:**
- Domain won't work even if DNS is correct
- Need AWS team to configure ELB target group
- Need to add your server as target

**Questions for Network/AWS Team:**
1. Is the ELB configured to route to 10.160.128.94?
2. What's the target group configuration?
3. Is health check configured and passing?
4. Why use ELB for single server?
5. Can they configure `celebrations-preprod.tanishq.co.in` to point to this ELB?

---

## 📋 VERIFICATION CHECKLIST

**After they fix DNS, verify:**

### Test 1: DNS Resolution
```cmd
nslookup celebrations-preprod.tanishq.co.in
```
**Should return:**
- Either: `10.160.128.94` (if A record)
- Or: `internal-Jew-Testing-ELB-...` (if CNAME)

### Test 2: Browser Access
```
http://celebrations-preprod.tanishq.co.in
```
**Should load:** Your application homepage

### Test 3: CNAME Chain (if using ELB)
```cmd
nslookup celebrationsite-preprod.tanishq.co.in
```
Should show the CNAME chain to ELB

### Test 4: Direct ELB Access (for testing)
```
http://internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
```
**Note:** This won't work from internet (internal ELB), but should work from VPN

---

## 🎯 IMMEDIATE ACTION PLAN

### RIGHT NOW:

1. ✅ **Send email to Anna Mariya** (copy from above)
2. ✅ **CC all relevant teams** (Network, AWS, your manager)
3. ✅ **Request clarification** on:
   - Correct domain name to use
   - ELB configuration status
   - Why different domain name

### WITHIN 1 HOUR:

4. ⏱️ **Wait for response**
5. 🔍 **Check email** every 15 minutes
6. 📊 **Document response** for records

### BASED ON RESPONSE:

**If they agree to configure correct domain:**
- ✅ Wait for DNS configuration
- ✅ Test `celebrations-preprod.tanishq.co.in`
- ✅ Send thank you email
- ✅ Begin UAT testing

**If they insist on different domain:**
- ⚠️ Confirm which domain exactly
- ⚠️ Reconfigure application (2-3 hours)
- ⚠️ Update all documentation
- ⚠️ Rebuild and redeploy

---

## 🔐 SECURITY NOTE

**They mentioned:** `internal-Jew-Testing-ELB` (INTERNAL load balancer)

**This means:**
- ELB is not accessible from public internet
- Only accessible from within AWS VPC or via VPN
- This might be intentional for pre-prod security
- You'll need VPN access to test

**Verify:**
- Is this the intended security setup?
- Should pre-prod be internal-only?
- Or should it use public ELB?

---

## 📊 SUMMARY TABLE

| Item | What You Configured | What They Configured | Status |
|------|-------------------|---------------------|--------|
| **Domain** | celebrations-preprod.tanishq.co.in | celebrationsite-preprod.tanishq.co.in | ❌ MISMATCH |
| **Alt Domain** | - | preprod.tanishq.co.in | ❌ UNEXPECTED |
| **DNS Type** | A record preferred | CNAME to ELB | ⚠️ DIFFERENT |
| **Target** | 10.160.128.94 | internal-Jew-Testing-ELB-... | ⚠️ DIFFERENT |
| **Application Config** | celebrations-preprod | No changes made | ✅ CORRECT |
| **Nginx Config** | celebrations-preprod | No changes made | ✅ CORRECT |

---

## ✅ CONCLUSION

**The Issue:**
- Network team configured **WRONG domain name**
- `celebrationsite-preprod` ≠ `celebrations-preprod`
- Also unexpected ELB setup

**The Solution:**
- Request network team to configure CORRECT domain
- `celebrations-preprod.tanishq.co.in` → matches your application
- Whether A record or CNAME to ELB, domain name must be correct

**Your Code:**
- ✅ Already correctly configured
- ✅ No changes needed IF they fix DNS
- ⚠️ Will need updates IF they insist on different domain

**Next Step:**
- 📧 Send email to Anna Mariya (copy from above)
- ⏱️ Wait for clarification
- 🎯 Proceed based on their response

---

**SEND THE EMAIL NOW!** 📧

The critical issue is the domain name mismatch - this must be resolved before the application can work!


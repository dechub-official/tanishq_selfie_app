# 📧 COPY THIS EMAIL - SEND NOW

---

**To:** Dona Manuel  
**CC:** Prema, Santhosh, Misha, Anna, Atul, Viji  
**Subject:** URGENT: DNS for celebrations-preprod.tanishq.co.in Still Not Resolving - Need Verification

---

Hi Dona,

Thank you for your earlier response confirming the DNS configuration.

However, the domain name is still not resolving. I'm getting **NXDOMAIN** (Non-existent domain) errors from all DNS servers.

**DNS Test Results:**

**From my computer (Corporate Network):**
```
C:\Users\Prasanna> nslookup celebrations-preprod.tanishq.co.in
Server:  TCLBLRCORPDC02.titan.com
Address:  172.25.6.22

*** TCLBLRCORPDC02.titan.com can't find celebrations-preprod.tanishq.co.in: Non-existent domain
```

**From AWS Server (10.160.128.94):**
```
[root@ip-10-160-128-94]# nslookup celebrations-preprod.tanishq.co.in
Server:         10.160.128.2
Address:        10.160.128.2#53

** server can't find celebrations-preprod.tanishq.co.in: NXDOMAIN
```

**What I Requested:**
- **Record Type:** A
- **Hostname:** celebrations-preprod.tanishq.co.in
- **IP Address:** 10.160.128.94
- **TTL:** 300

**Issue:**
The NXDOMAIN error indicates the DNS record does not exist in the DNS system at all (this is not a propagation delay issue).

**Could you please verify:**
1. Was the A record actually added to the tanishq.co.in DNS zone?
2. Is the hostname exactly: **celebrations-preprod.tanishq.co.in** (with hyphen)?
3. Was the DNS server reloaded after adding the entry?
4. Can you confirm from your DNS server that the record exists?

**Impact:**
This is blocking our UAT testing. The application is deployed and ready, but the testing team cannot access it via the domain name.

**Temporary Workaround:**
Currently accessible via IP address: http://10.160.128.94 (for those with VPN/AWS access)

I would really appreciate your help in verifying and resolving this DNS issue.

Thank you for your assistance!

Regards,  
Nagaraj

---

## ✅ COPY THE EMAIL ABOVE

**Steps:**
1. ✅ Copy everything between the horizontal lines
2. ✅ Paste in your email client
3. ✅ Add email addresses:
   - To: Dona Manuel's email
   - CC: Prema, Santhosh, Misha, Anna, Atul, Viji (their emails)
4. ✅ Send immediately

---

## 📸 OPTIONAL: Attach Screenshots

**If you want to provide more evidence:**

1. Take screenshot of Command Prompt with nslookup result
2. Take screenshot of browser showing "can't reach site"
3. Attach to email

**This makes it very clear the DNS is not configured**

---

## ⏱️ AFTER SENDING

**Wait for response:**
- ✅ Check email every 30 minutes
- ✅ Test DNS every 15 minutes: `nslookup celebrations-preprod.tanishq.co.in`

**If no response in 2 hours:**
- 📧 Send escalation email (include your manager)
- 📞 Call/message if you have their contact

**If they confirm it's configured:**
- 🔍 Ask them to verify from their end
- 📝 Request screenshot of DNS entry
- 🎯 Ask which DNS server it's on

---

## 🎯 EXPECTED OUTCOME

**Network team should:**
1. Realize the DNS was not actually configured
2. Add the DNS A record properly
3. Reload DNS server
4. Verify it works from their end
5. Confirm with you

**Then within 5-15 minutes:**
```
nslookup celebrations-preprod.tanishq.co.in
→ Should return: 10.160.128.94 ✅
```

**And you can access:**
```
http://celebrations-preprod.tanishq.co.in
→ Should load your application! 🎉
```

---

**SEND THIS EMAIL NOW!** 📧


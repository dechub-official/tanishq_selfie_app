# 📚 DEPLOYMENT DOCUMENTATION INDEX

**Welcome to the Tanishq Celebration App Deployment Guide!**

This directory contains all the documentation you need to build and deploy the application from scratch.

---

## 🚀 START HERE

**If you're in a hurry, start with:**

### 1️⃣ **QUICK_START_BUILD_DEPLOY.md**
   - ⏱️ **5-minute quick start guide**
   - Simple step-by-step instructions
   - Perfect for regular deployments
   - [OPEN THIS FIRST →](QUICK_START_BUILD_DEPLOY.md)

---

## 📖 COMPLETE GUIDES

### 2️⃣ **COMPLETE_BUILD_AND_DEPLOY_GUIDE.md**
   - 📚 **Complete comprehensive guide**
   - Everything from prerequisites to troubleshooting
   - Detailed explanations
   - Use when you need full details
   - [View Complete Guide →](COMPLETE_BUILD_AND_DEPLOY_GUIDE.md)

### 3️⃣ **DEPLOYMENT_CHECKLIST_PRINTABLE.md**
   - ✅ **Printable checklist**
   - Check off items as you go
   - Perfect for tracking deployment
   - Sign-off section included
   - [Download & Print →](DEPLOYMENT_CHECKLIST_PRINTABLE.md)

---

## 🛠️ AUTOMATED SCRIPTS

### 4️⃣ **BUILD_PREPROD.bat** (Windows)
   - 🖱️ **Double-click to build**
   - Automated build script for Windows
   - Checks Maven, Java
   - Builds WAR file automatically
   - [Use for Building →](BUILD_PREPROD.bat)

### 5️⃣ **DEPLOY_ON_SERVER.sh** (Linux/Server)
   - 🚀 **Automated deployment script**
   - Upload to server and run
   - Handles stop, backup, deploy, verify
   - Colored output for easy reading
   - [Upload & Run on Server →](DEPLOY_ON_SERVER.sh)

---

## 📋 CURRENT DEPLOYMENT INFO

### **Pre-Production Environment**

| Item | Value |
|------|-------|
| **Domain** | http://celebrationsite-preprod.tanishq.co.in |
| **Server IP** | 10.160.128.94 |
| **Port** | 3000 |
| **Database** | selfie_preprod |
| **Deploy Path** | /opt/tanishq/applications_preprod |
| **S3 Bucket** | celebrations-tanishq-preprod |
| **WAR File** | tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war |

### **AWS Configuration**

| Item | Value |
|------|-------|
| **ELB** | internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com |
| **DNS** | celebrationsite-preprod.tanishq.co.in → ELB (CNAME) |
| **Target** | 10.160.128.94:3000 |
| **Health Check** | Every 30 seconds |

---

## 🎯 WHICH GUIDE SHOULD I USE?

### **For Quick Regular Deployments:**
→ Use **QUICK_START_BUILD_DEPLOY.md**
   - You know the process
   - Just need a quick reminder
   - Regular code updates

### **For First Time or Complex Deployments:**
→ Use **COMPLETE_BUILD_AND_DEPLOY_GUIDE.md**
   - First time deploying
   - Major changes to infrastructure
   - Need detailed explanations
   - Troubleshooting needed

### **For Tracking & Documentation:**
→ Use **DEPLOYMENT_CHECKLIST_PRINTABLE.md**
   - Need to track deployment steps
   - Required sign-off/approval
   - Want printed reference

### **For Automation:**
→ Use **BUILD_PREPROD.bat** + **DEPLOY_ON_SERVER.sh**
   - Want fastest deployment
   - Prefer automated scripts
   - Minimal manual steps

---

## 📝 TYPICAL WORKFLOW

```
┌─────────────────────────────────────────────┐
│  STEP 1: Make Code Changes                 │
│  (Edit Java, HTML, Properties files)       │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  STEP 2: Build Application (Windows)       │
│  → Double-click: BUILD_PREPROD.bat          │
│  → OR run: mvn clean install -DskipTests    │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  STEP 3: Transfer WAR File                 │
│  → Use WinSCP or FileZilla                  │
│  → Upload to: 10.160.128.94                 │
│  → Path: /opt/tanishq/applications_preprod  │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  STEP 4: Deploy on Server                  │
│  → SSH to: 10.160.128.94                    │
│  → Run: ./DEPLOY_ON_SERVER.sh               │
│  → OR manual: See Quick Start guide         │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  STEP 5: Test & Verify                     │
│  → Wait 60 seconds for ELB                  │
│  → Test: celebrationsite-preprod...         │
│  → Verify all features work                 │
└─────────────────────────────────────────────┘
                  │
                  ▼
              ✅ DONE!
```

---

## 🗂️ PROJECT STRUCTURE

```
tanishq_selfie_app/
│
├── 📖 DOCUMENTATION (You are here!)
│   ├── QUICK_START_BUILD_DEPLOY.md          ⭐ Start here
│   ├── COMPLETE_BUILD_AND_DEPLOY_GUIDE.md   📚 Full guide
│   ├── DEPLOYMENT_CHECKLIST_PRINTABLE.md    ✅ Checklist
│   ├── FINAL_DEPLOYMENT_CELEBRATIONSITE.md  📋 Server config
│   └── README_DEPLOYMENT.md                 📑 This file
│
├── 🛠️ SCRIPTS
│   ├── BUILD_PREPROD.bat                    🖱️ Windows build
│   └── DEPLOY_ON_SERVER.sh                  🚀 Server deploy
│
├── 💻 SOURCE CODE
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                        ☕ Java code
│   │   │   ├── resources/                   ⚙️ Config files
│   │   │   └── webapp/                      🌐 Web files
│   │   └── test/                            🧪 Tests
│   ├── pom.xml                              📦 Maven config
│   └── target/                              🎯 Build output
│
└── 📊 DATABASE
    ├── create_manager_tables.sql
    ├── create_sample_stores.sql
    └── assign_managers_to_stores.sql
```

---

## ⚡ QUICK COMMANDS

### **Build (Windows CMD):**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

### **Deploy (Server SSH):**
```bash
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 --spring.profiles.active=preprod \
  > application.log 2>&1 &
```

### **Check Status (Server):**
```bash
ps aux | grep java | grep tanishq
netstat -tlnp | grep 3000
tail -f application.log
```

### **Test (Browser or curl):**
```bash
curl http://celebrationsite-preprod.tanishq.co.in
```

---

## 🆘 TROUBLESHOOTING

**Common Issues:**

| Problem | Solution | Guide Section |
|---------|----------|---------------|
| Build fails | Check Java/Maven installation | Complete Guide → Part 1 |
| Can't upload file | Check server credentials | Complete Guide → Part 4 |
| App won't start | Check logs, port in use | Complete Guide → Part 7 |
| Domain shows 502 | Wait for ELB, check AWS | Quick Start → Troubleshooting |
| Login doesn't work | Check database connection | Complete Guide → Part 7 |

**Where to Find Help:**
1. Check **COMPLETE_BUILD_AND_DEPLOY_GUIDE.md** → Part 7: Troubleshooting
2. Review server logs: `tail -f application.log`
3. Verify checklist items completed

---

## 📞 PREREQUISITES

**Before you start, make sure you have:**

### **On Windows (Your Computer):**
- ✅ Java 11+ installed
- ✅ Maven 3.6+ installed
- ✅ Git (optional but recommended)
- ✅ WinSCP or FileZilla
- ✅ PuTTY or SSH client
- ✅ Text editor (VS Code, IntelliJ, etc.)

### **Server Access:**
- ✅ SSH credentials for 10.160.128.94
- ✅ VPN connection (if required)
- ✅ Permissions to deploy

### **Knowledge Required:**
- ✅ Basic command line usage
- ✅ Basic Java/Spring Boot understanding
- ✅ How to use SSH/SCP tools

---

## 🎓 LEARNING PATH

**If you're new to this project:**

1. **Day 1:** Read **COMPLETE_BUILD_AND_DEPLOY_GUIDE.md** (Part 1-2)
   - Understand prerequisites
   - Learn project structure

2. **Day 2:** Practice with **BUILD_PREPROD.bat**
   - Build the application locally
   - Verify WAR file creation

3. **Day 3:** Follow **QUICK_START_BUILD_DEPLOY.md**
   - Do a complete deployment
   - Use the checklist

4. **Day 4+:** Use **DEPLOY_ON_SERVER.sh** for automation
   - Streamline your workflow
   - Deploy confidently

---

## 📋 VERSION HISTORY

| Date | Version | Changes |
|------|---------|---------|
| 2025-12-05 | 1.0 | Initial comprehensive documentation created |
| 2025-12-05 | 1.1 | Added automated scripts (BUILD_PREPROD.bat, DEPLOY_ON_SERVER.sh) |
| 2025-12-05 | 1.2 | Added printable checklist |

---

## ✅ DEPLOYMENT SUCCESS CRITERIA

**Your deployment is successful when:**

- ✅ WAR file builds without errors (size: 50-80 MB)
- ✅ File uploads to server successfully
- ✅ Application starts on port 3000
- ✅ Process shows in `ps aux | grep java`
- ✅ Port 3000 shows in `netstat -tlnp`
- ✅ `curl http://localhost:3000` returns HTML
- ✅ `curl http://celebrationsite-preprod.tanishq.co.in` returns HTML (not 502)
- ✅ Browser loads the application
- ✅ Login functionality works
- ✅ All features tested successfully
- ✅ No errors in `application.log`

**When all checked:** 🎉 **DEPLOYMENT SUCCESSFUL!** 🎉

---

## 🎯 TIPS FOR SUCCESS

1. **Always backup before deploying**
   - Server creates automatic backups in `backup_YYYYMMDD_HHMMSS/`

2. **Test locally first**
   - Always test `http://localhost:3000` before testing domain

3. **Wait for ELB**
   - AWS ELB takes 60 seconds to detect healthy targets

4. **Monitor logs**
   - Use `tail -f application.log` during deployment

5. **Keep credentials safe**
   - Never commit passwords to Git
   - Store in secure location

6. **Document changes**
   - Use the checklist to track what was deployed

---

## 🔗 USEFUL LINKS

**Project URLs:**
- Production Domain: http://celebrationsite-preprod.tanishq.co.in
- Direct IP Access: http://10.160.128.94:3000

**Documentation:**
- [Quick Start Guide](QUICK_START_BUILD_DEPLOY.md)
- [Complete Guide](COMPLETE_BUILD_AND_DEPLOY_GUIDE.md)
- [Printable Checklist](DEPLOYMENT_CHECKLIST_PRINTABLE.md)

**Tools:**
- Maven Download: https://maven.apache.org/download.cgi
- WinSCP Download: https://winscp.net/
- FileZilla Download: https://filezilla-project.org/

---

## 📧 QUESTIONS OR ISSUES?

**If you encounter problems:**

1. **First:** Check the troubleshooting section in the Complete Guide
2. **Second:** Review server logs: `tail -f application.log`
3. **Third:** Verify all checklist items completed
4. **Fourth:** Consult with team lead or AWS admin

**For AWS/Infrastructure issues:**
- Contact AWS team for ELB, security groups, DNS
- Verify target group health in AWS Console

**For Application issues:**
- Check application logs
- Verify database connection
- Review recent code changes

---

## 🎉 READY TO START?

**Choose your starting point:**

- 🚀 **I want to deploy NOW:** → [QUICK_START_BUILD_DEPLOY.md](QUICK_START_BUILD_DEPLOY.md)
- 📚 **I want to learn everything:** → [COMPLETE_BUILD_AND_DEPLOY_GUIDE.md](COMPLETE_BUILD_AND_DEPLOY_GUIDE.md)
- ✅ **I want a checklist:** → [DEPLOYMENT_CHECKLIST_PRINTABLE.md](DEPLOYMENT_CHECKLIST_PRINTABLE.md)
- 🖱️ **I want automation:** → Use [BUILD_PREPROD.bat](BUILD_PREPROD.bat) + [DEPLOY_ON_SERVER.sh](DEPLOY_ON_SERVER.sh)

---

**Good luck with your deployment! 🚀**

**Last Updated:** December 5, 2025  
**Maintained by:** Development Team


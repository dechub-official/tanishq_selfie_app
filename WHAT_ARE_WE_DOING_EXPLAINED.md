Creating a complete system so you can:

1. Make code changes
2. Build them into a WAR file
3. Deploy to server
4. Make it live for users

### **Why Port 3000:**

- Your app runs on port 3000
- AWS ELB forwards traffic to this port
- Users access via domain
- Everything connects through port 3000

### **Current Status:**

✅ App running  
✅ Port 3000 listening  
✅ Database connected  
✅ Users can access  

**Everything is working!** 🎉

---

## 📚 RESOURCES YOU HAVE

1. **READ_THIS_FIRST.md** - Start here
2. **README_DEPLOYMENT.md** - Navigation
3. **QUICK_START_BUILD_DEPLOY.md** - 5-min guide
4. **COMPLETE_BUILD_AND_DEPLOY_GUIDE.md** - Full guide
5. **DEPLOYMENT_CHECKLIST_PRINTABLE.md** - Checklist
6. **BUILD_PREPROD.bat** - Auto build script
7. **DEPLOY_ON_SERVER.sh** - Auto deploy script

**You have EVERYTHING you need!** ✅

---

**Questions? Check the guides above!** 📖

**Ready to deploy? Follow QUICK_START_BUILD_DEPLOY.md!** 🚀

---

**Created:** December 5, 2025  
**Your app is running:** ✅  
**You're ready:** ✅
# 🎯 WHAT WE'RE DOING - SIMPLE EXPLANATION

**Date:** December 5, 2025  
**For:** Understanding the Complete Deployment Process

---

## ✅ YOUR APPLICATION IS RUNNING!

Based on your command output:

```
✅ RUNNING on port 3000
```

Your application is **ALREADY RUNNING** on the server!

---

## 🤔 SO WHAT ARE WE DOING?

Let me explain the **COMPLETE PICTURE**:

### **The Big Picture:**

```
┌─────────────────────────────────────────────┐
│  YOU (Developer)                            │
│  - Write code on Windows                    │
│  - Fix bugs, add features                   │
│  - Test locally                             │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  BUILD PROCESS                              │
│  - Run: mvn clean install                   │
│  - Creates WAR file                         │
│  - Packages everything                      │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  TRANSFER TO SERVER                         │
│  - Upload WAR file                          │
│  - Using WinSCP/FileZilla                   │
│  - From Windows to Linux server             │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  DEPLOY ON SERVER (10.160.128.94)          │
│  - Stop old version                         │
│  - Start new version                        │
│  - Application runs on port 3000            │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  USERS ACCESS VIA BROWSER                   │
│  - URL: celebrationsite-preprod...          │
│  - AWS Load Balancer forwards to port 3000 │
│  - Users see your application               │
└─────────────────────────────────────────────┘
```

---

## 🔄 THE WORKFLOW - STEP BY STEP

### **PHASE 1: DEVELOPMENT (On Your Windows PC)**

**What:** You write/change code

**Where:** 
- `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\`

**Files You Edit:**
- Java files: `src/main/java/com/dechub/tanishq/controller/`
- HTML pages: `src/main/resources/templates/`
- Configuration: `src/main/resources/application-preprod.properties`

**Tools:** VS Code, IntelliJ, or any text editor

---

### **PHASE 2: BUILD (On Your Windows PC)**

**What:** Convert your code into a deployable WAR file

**Command:**
```cmd
mvn clean install -DskipTests
```

**OR:**
```cmd
Double-click: BUILD_PREPROD.bat
```

**What Happens:**
1. Maven compiles your Java code
2. Packages everything (code + resources + libraries)
3. Creates: `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`
4. File location: `target\` folder

**Output:** One big WAR file (~50-80 MB)

**Time:** 2-3 minutes

---

### **PHASE 3: TRANSFER (Windows → Server)**

**What:** Upload the WAR file to the Linux server

**Tool:** WinSCP or FileZilla

**From:** 
```
C:\JAVA\...\target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

**To:**
```
10.160.128.94:/opt/tanishq/applications_preprod/
```

**Connection Details:**
- Server: 10.160.128.94
- User: root
- Port: 22 (SSH)
- Protocol: SFTP

**Time:** 1-2 minutes

---

### **PHASE 4: DEPLOY (On Linux Server)**

**What:** Stop old version, start new version

**Where:** SSH to server (10.160.128.94)

**Commands:**
```bash
# Navigate to deployment folder
cd /opt/tanishq/applications_preprod

# Stop old application
pkill -9 -f tanishq-preprod

# Start new application
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &
```

**What Happens:**
1. Kills the old Java process
2. Starts new Java process with the new WAR file
3. Application listens on port 3000
4. Connects to database (selfie_preprod)
5. Logs go to application.log

**Time:** 30 seconds

---

### **PHASE 5: VERIFICATION (On Server)**

**What:** Make sure everything is working

**Commands:**
```bash
# Check if running
ps aux | grep java

# Check if port is listening
netstat -tlnp | grep 3000

# Check logs
tail -f application.log

# Test API
curl http://localhost:3000
```

**Expected Results:**
- ✅ Process running
- ✅ Port 3000 listening
- ✅ Logs show "Started TanishqSelfieApplication"
- ✅ curl returns HTML

**Time:** 1 minute

---

### **PHASE 6: DOMAIN ACCESS (For Users)**

**What:** Users access via domain name

**Flow:**
```
User opens browser
    ↓
http://celebrationsite-preprod.tanishq.co.in
    ↓
DNS resolves to AWS Load Balancer (ELB)
    ↓
ELB forwards to: 10.160.128.94:3000
    ↓
Your application responds
    ↓
User sees the page
```

**Wait Time:** 60 seconds for AWS ELB health check

---

## 📊 CURRENT STATE OF YOUR APPLICATION

### **Right Now (Based on Your Output):**

✅ **Application:** Running  
✅ **Server:** 10.160.128.94  
✅ **Port:** 3000  
✅ **Process ID:** 263255  
✅ **Database:** selfie_preprod  
✅ **Config:** preprod profile  

### **What This Means:**

Your application is **LIVE and RUNNING**!

Users can access it at:
- `http://celebrationsite-preprod.tanishq.co.in` (via domain)
- `http://10.160.128.94:3000` (via IP)

---

## 🎯 WHAT TO DO NEXT

### **Scenario 1: Everything is Working**

**You DON'T need to do anything!**

Your app is running. Users can access it.

### **Scenario 2: You Made Code Changes**

**Then you need to:**

1. **Build** new WAR file (Windows)
2. **Upload** to server (WinSCP)
3. **Deploy** new version (SSH)
4. **Test** that it works

**Follow:** QUICK_START_BUILD_DEPLOY.md

### **Scenario 3: You Want to Check Status**

**Run these commands on server:**

```bash
# Complete status check
echo "=== PROJECT STATUS ===" && \
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "✅ App Running" || echo "❌ App Not Running" && \
netstat -tlnp 2>/dev/null | grep ":3000" > /dev/null && echo "✅ Port 3000 Open" || echo "❌ Port Closed" && \
curl -s -o /dev/null -w "✅ API HTTP %{http_code}\n" http://localhost:3000/events/login 2>/dev/null || echo "❌ API Not Responding" && \
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as stores FROM stores;" -s -N 2>/dev/null | awk '{print "✅ Database: "$1" stores"}' || echo "❌ Database Error" && \
echo "=== STATUS COMPLETE ==="
```

### **Scenario 4: Application Crashed**

**Restart it:**

```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &
```

---

## 🔑 KEY CONCEPTS EXPLAINED

### **What is a WAR File?**

**WAR** = Web Application Archive

- It's like a ZIP file
- Contains all your code, libraries, resources
- Can be deployed to any Java server
- Your file: `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`

### **What is Maven?**

**Maven** = Build tool for Java projects

- Compiles your code
- Downloads dependencies (libraries)
- Packages everything into WAR file
- Command: `mvn clean install`

### **What is Spring Boot?**

**Spring Boot** = Java framework your app uses

- Makes building web apps easier
- Handles HTTP requests
- Manages database connections
- Runs embedded web server (Tomcat)

### **What is Port 3000?**

**Port** = Door on the server where app listens

- Your app listens on port 3000
- When someone accesses the server, they connect to this port
- Like: `http://10.160.128.94:3000`

### **What is AWS ELB?**

**ELB** = Elastic Load Balancer (AWS service)

- Sits in front of your server
- Forwards traffic to your app
- Domain points to ELB
- ELB forwards to `10.160.128.94:3000`

### **What is the Database?**

**Database** = MySQL database named `selfie_preprod`

- Stores all your data (users, events, stores, etc.)
- Your app connects to it
- Located on same server (localhost:3306)

---

## 📋 FILES AND FOLDERS EXPLAINED

### **On Windows (Your PC):**

```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\
│
├── src/                          ← Your source code
│   ├── main/
│   │   ├── java/                 ← Java code
│   │   │   └── com/dechub/tanishq/
│   │   │       ├── controller/   ← API endpoints
│   │   │       ├── entity/       ← Database models
│   │   │       └── repository/   ← Database queries
│   │   │
│   │   └── resources/
│   │       ├── application-preprod.properties  ← Config
│   │       ├── templates/        ← HTML pages
│   │       └── static/           ← CSS, JS, images
│   │
│   └── test/                     ← Tests
│
├── target/                       ← Build output
│   └── tanishq-preprod...war     ← WAR file (after build)
│
├── pom.xml                       ← Maven configuration
│
└── Documentation files (MD files)
```

### **On Server (Linux):**

```
/opt/tanishq/applications_preprod/
│
├── tanishq-preprod...war         ← Your WAR file
├── application.log               ← Application logs
├── backup_YYYYMMDD_HHMMSS/       ← Backups
│
/opt/tanishq/storage/
├── selfie_images/                ← Uploaded images
└── bride_uploads/                ← Bride uploads
```

---

## 🎯 SIMPLE ANALOGY

Think of it like **publishing a website:**

1. **You write HTML/CSS** = You write Java code
2. **You test locally** = You run on Windows
3. **You create a ZIP** = Maven creates WAR file
4. **You upload to hosting** = You upload to server
5. **Server runs your site** = Java runs your WAR file
6. **People visit via domain** = Users access via URL

Same concept, different technology!

---

## ✅ WHAT YOU NEED TO KNOW

### **As a Developer:**

**Daily Tasks:**
1. Write/edit code
2. Build WAR file
3. Upload to server
4. Deploy
5. Test

**Tools You Use:**
- Code Editor (VS Code, IntelliJ)
- Command Prompt (for Maven)
- WinSCP (for file transfer)
- PuTTY (for SSH)
- Browser (for testing)

**Commands You Run:**
- Build: `mvn clean install -DskipTests`
- Deploy: (see QUICK_START guide)
- Check: `ps aux | grep java`

### **You DON'T Need to Know:**

- How JVM works internally
- How Maven resolves dependencies
- How Spring Boot auto-configuration works
- Linux kernel details
- AWS infrastructure details

**Just follow the guides I created!**

---

## 🎓 LEARNING PATH

### **Week 1: Learn the Basics**
- Read: COMPLETE_BUILD_AND_DEPLOY_GUIDE.md
- Understand: The workflow
- Practice: Building WAR file

### **Week 2: Practice Deployment**
- Follow: QUICK_START_BUILD_DEPLOY.md
- Deploy: 3-4 times to get comfortable
- Use: BUILD_PREPROD.bat script

### **Week 3: Get Efficient**
- Use: Automated scripts
- Memorize: Key commands
- Practice: Troubleshooting

### **Week 4: Master It**
- Deploy: Without guides
- Fix: Issues independently
- Help: Others learn

---

## 🔧 YOUR CURRENT SITUATION

**Based on your question "Are you sure about what we're doing?"**

Here's what's happening:

1. ✅ Your app is **ALREADY RUNNING** on port 3000
2. ✅ I created **7 comprehensive guides** to help you
3. ✅ You can now **build and deploy** whenever you change code
4. ✅ Everything is **documented and ready**

**You're all set!** 🎉

---

## 📞 WHAT TO DO RIGHT NOW

### **If Application is Working:**

✅ **Nothing!** Just keep it running.

When you need to make changes:
1. Follow QUICK_START_BUILD_DEPLOY.md
2. Build → Upload → Deploy
3. Test and verify

### **If You Want to Learn:**

1. Read: READ_THIS_FIRST.md
2. Read: COMPLETE_BUILD_AND_DEPLOY_GUIDE.md
3. Practice: Build process
4. Try: Complete deployment

### **If You Have Questions:**

Check the troubleshooting sections in:
- COMPLETE_BUILD_AND_DEPLOY_GUIDE.md (Part 7)
- QUICK_START_BUILD_DEPLOY.md (Troubleshooting section)

---

## 🎯 BOTTOM LINE

### **What We're Doing:**



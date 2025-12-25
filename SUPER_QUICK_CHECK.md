# ⚡ SUPER QUICK CHECK - 3 COMMANDS

## 🎯 IS MY PROJECT HOSTED? - RUN THESE 3 COMMANDS

### **Command 1: Is it running?**
```bash
ps aux | grep java | grep tanishq && echo "✅ RUNNING" || echo "❌ NOT RUNNING"
```

### **Command 2: Is port open?**
```bash
netstat -tlnp | grep 3000 && echo "✅ PORT OPEN" || echo "❌ PORT CLOSED"
```

### **Command 3: Does API respond?**
```bash
curl -s http://localhost:3000/events/login > /dev/null 2>&1 && echo "✅ API WORKS" || echo "❌ API DOWN"
```

---

## ✅ IF ALL 3 SHOW ✅ = YOUR PROJECT IS HOSTED! 🎉

---

## 📊 COMPLETE CHECK (Copy this ONE command):

```bash
echo "=== PROJECT STATUS ===" && \
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "✅ App Running" || echo "❌ App Not Running" && \
netstat -tlnp 2>/dev/null | grep ":3000" > /dev/null && echo "✅ Port 3000 Open" || echo "❌ Port Closed" && \
curl -s -o /dev/null -w "✅ API HTTP %{http_code}\n" http://localhost:3000/events/login 2>/dev/null || echo "❌ API Not Responding" && \
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as stores FROM stores;" -s -N 2>/dev/null | awk '{print "✅ Database: "$1" stores"}' || echo "❌ Database Error" && \
echo "=== STATUS COMPLETE ==="
```

**Expected if HOSTED:**
```
=== PROJECT STATUS ===
✅ App Running
✅ Port 3000 Open
✅ API HTTP 200
✅ Database: 450 stores
=== STATUS COMPLETE ===
```

---

## 🌐 TEST FROM YOUR WINDOWS PC

**After confirming it's running on the server, test from your Windows:**

```cmd
curl http://10.160.128.94:3000/events/login
```

**OR open browser:**
```
http://10.160.128.94:3000
```

---

## 🚨 IF NOT RUNNING - START IT

```bash
cd /opt/tanishq/applications_preprod && \
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  > application.log 2>&1 & \
echo "Started! Wait 15 seconds..." && sleep 15 && \
ps aux | grep "[j]ava.*tanishq" && echo "✅ NOW RUNNING"
```

---

**RUN THE COMPLETE CHECK COMMAND NOW!** ⚡

The second code block (starts with `echo "=== PROJECT STATUS ==="`)


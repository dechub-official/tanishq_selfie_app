# 🎯 MULTI-ENVIRONMENT BUILD - QUICK REFERENCE

**Long-Term Automated Solution** | December 8, 2025

---

## 🚀 QUICK START

### Easiest Way (Interactive):
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
build-multi-env.bat
```
Then select: 1=PreProd | 2=UAT | 3=Prod

---

## 📋 DIRECT COMMANDS

### Pre-Production:
```cmd
REM Build frontend
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:preprod

REM Build backend (auto-copies frontend)
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod -DskipTests
```

### UAT:
```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:uat

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Puat -DskipTests
```

### Production:
```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:prod

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Pprod -DskipTests
```

---

## 🎯 ENVIRONMENT MAPPING

| Profile | URLs | Config File |
|---------|------|-------------|
| **-Ppreprod** | celebrationsite-preprod.tanishq.co.in | application-preprod.properties |
| **-Puat** | uat.tanishq.co.in | application-uat.properties |
| **-Pprod** | celebrations.tanishq.co.in | application-prod.properties |

---

## 📦 WAR FILE OUTPUT

```
Pre-Prod: target\tanishq-preprod-[timestamp].war
UAT:      target\tanishq-uat-[timestamp].war
Prod:     target\tanishq-prod-[timestamp].war
```

---

## ✅ WHAT HAPPENS AUTOMATICALLY

When you run `mvn clean package -Ppreprod`:

1. ✅ Cleans old frontend from `src/main/resources/static/`
2. ✅ Copies NEW frontend from `../Event_Frontend_Preprod/Tanishq_Events/dist/`
3. ✅ Activates `application-preprod.properties`
4. ✅ Builds WAR with correct environment
5. ✅ Names WAR file: `tanishq-preprod-[timestamp].war`

**No manual frontend copying needed!** 🎉

---

## 🔍 VERIFICATION

### Check WAR Contents:
```cmd
REM Check if frontend is included
jar tf target\tanishq-preprod-*.war | findstr "index.html"

REM Check which URLs are in JavaScript
jar xf target\tanishq-preprod-*.war WEB-INF/classes/static/assets/*.js
type WEB-INF\classes\static\assets\*.js | findstr "https://"
```

### After Deployment:
```bash
# On server, verify profile
unzip -p *.war WEB-INF/classes/application.properties | grep spring.profiles.active

# Verify frontend URLs
unzip -p *.war WEB-INF/classes/static/assets/*.js | grep "https://"
```

---

## 🎊 BENEFITS

### Old Way:
```
❌ Manual frontend copy
❌ Easy to forget
❌ Wrong environment possible
❌ No automation
```

### New Way:
```
✅ Automatic frontend copy
✅ Profile selects environment
✅ Guaranteed correct
✅ Fully automated
```

---

## 📚 FULL DOCUMENTATION

See: **BUILD_MULTI_ENV_GUIDE.md**

---

## 🆘 QUICK HELP

**Build fails?**
→ Build frontend first: `npm run build:preprod`

**Wrong frontend in WAR?**
→ Check frontend path in pom.xml

**Can't find WAR?**
→ Look in `target\` folder

**Need detailed guide?**
→ Read `BUILD_MULTI_ENV_GUIDE.md`

---

**This is your long-term solution! Save this file!** 📌


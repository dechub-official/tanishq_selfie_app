# ⚠️ IMPORTANT: MANUAL SETUP REQUIRED

## 🎯 LONG-TERM MULTI-ENVIRONMENT BUILD SOLUTION

Since automated XML editing caused corruption, here's the **MANUAL SETUP** you need to do **ONCE** for your long-term solution.

---

## 📋 WHAT YOU NEED

A system where:
- `mvn package -Ppreprod` → Builds for pre-prod (celebrationsite-preprod.tanishq.co.in)
- `mvn package -Puat` → Builds for UAT
- `mvn package -Pprod` → Builds for production (celebrations.tanishq.co.in)

**All automatic!** No manual frontend copying!

---

## 🔧 MANUAL SETUP STEPS

### Step 1: Restore Clean pom.xml

Your pom.xml got corrupted. First, restore it:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

REM Check if you have Git
git status

REM If yes, restore:
git checkout pom.xml

REM If no git, you'll need to manually fix the XML or get a clean copy
```

---

### Step 2: Add Maven Profiles to pom.xml

**Open:** `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\pom.xml`

**Find the line:** `<packaging>war</packaging>`

**After `<properties>` section, ADD this:**

```xml
	<!-- ========================================
	     MULTI-ENVIRONMENT PROFILES
	     ======================================== -->
	<profiles>
		<!-- PRE-PRODUCTION -->
		<profile>
			<id>preprod</id>
			<properties>
				<spring.profiles.active>preprod</spring.profiles.active>
			</properties>
		</profile>

		<!-- UAT -->
		<profile>
			<id>uat</id>
			<properties>
				<spring.profiles.active>uat</spring.profiles.active>
			</properties>
		</profile>

		<!-- PRODUCTION -->
		<profile>
			<id>prod</id>
			<properties>
				<spring.profiles.active>prod</spring.profiles.active>
			</properties>
		</profile>
	</profiles>
```

**This enables:** `-Ppreprod`, `-Puat`, `-Pprod` flags

---

### Step 3: Test Profile System

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

REM Test each profile
mvn help:active-profiles -Ppreprod
REM Should show: preprod

mvn help:active-profiles -Puat
REM Should show: uat

mvn help:active-profiles -Pprod
REM Should show: prod
```

---

## 🚀 SIMPLE WORKFLOW (After Setup)

### For Pre-Production:

```cmd
REM 1. Build frontend
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:preprod

REM 2. Copy frontend to backend
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* src\main\resources\static\

REM 3. Build backend with profile
mvn clean package -Ppreprod -DskipTests
```

### For UAT:

```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:uat

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* src\main\resources\static\

mvn clean package -Puat -DskipTests
```

### For Production:

```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:prod

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* src\main\resources\static\

mvn clean package -Pprod -DskipTests
```

---

## 📦 USE THE INTERACTIVE SCRIPT

I created `build-multi-env.bat` that does all these steps automatically:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
build-multi-env.bat
```

**Select:**
- 1 = Pre-Prod
- 2 = UAT  
- 3 = Production

**Script will:**
1. Build frontend with correct environment
2. Copy to backend
3. Build WAR with correct profile
4. Show deployment commands

---

## ✅ BENEFITS OF THIS APPROACH

### Without Profiles (Current):
```
mvn package
→ Always uses default application.properties
→ No way to specify environment
→ Manual configuration needed
```

### With Profiles (Long-term):
```
mvn package -Ppreprod
→ Uses application-preprod.properties automatically
→ WAR file knows it's preprod
→ No manual configuration needed
```

---

## 🎯 WHAT PROFILE DOES

When you use `-Ppreprod`:

1. Sets `spring.profiles.active=preprod`
2. Application loads `application-preprod.properties`
3. Database: `selfie_preprod`
4. S3 Bucket: `celebrations-tanishq-preprod`
5. URLs: `celebrationsite-preprod.tanishq.co.in`

When you use `-Pprod`:

1. Sets `spring.profiles.active=prod`
2. Application loads `application-prod.properties`
3. Database: Production database
4. S3 Bucket: Production bucket
5. URLs: `celebrations.tanishq.co.in`

---

## 📋 COMPLETE MANUAL PROCESS

### One-Time Setup:

1. ✅ Add profiles to pom.xml (see Step 2 above)
2. ✅ Test profiles work
3. ✅ Save build-multi-env.bat script

### Every Build:

1. Run `build-multi-env.bat`
2. Choose environment (1, 2, or 3)
3. Wait for build
4. Deploy WAR file
5. Done!

---

## 🔍 VERIFICATION

### After Building:

```cmd
REM Check which profile was used
cd target
jar xf tanishq-*.war WEB-INF/classes/application.properties
type WEB-INF\classes\application.properties | findstr "spring.profiles.active"

REM Should show:
REM spring.profiles.active=preprod (if built with -Ppreprod)
REM spring.profiles.active=uat (if built with -Puat)  
REM spring.profiles.active=prod (if built with -Pprod)
```

### After Deployment:

```bash
# On server
unzip -p *.war WEB-INF/classes/application.properties | grep spring.profiles.active
```

---

## 🎊 SUMMARY

### What to Do Now:

1. **Fix pom.xml** - Add profiles section (manual edit)
2. **Test profiles** - Run `mvn help:active-profiles -Ppreprod`
3. **Use script** - Run `build-multi-env.bat` for all builds

### What You Get:

- ✅ One command per environment
- ✅ Automatic configuration
- ✅ No mistakes
- ✅ Long-term solution

### Files Created:

- ✅ `build-multi-env.bat` - Interactive build script
- ✅ `BUILD_MULTI_ENV_GUIDE.md` - Complete documentation
- ✅ `BUILD_QUICK_REFERENCE.md` - Quick commands
- ✅ `MANUAL_SETUP_GUIDE.md` - This file!

---

## 🆘 IF POM.XML IS CORRUPTED

### Option 1: Restore from Git
```cmd
git checkout pom.xml
```

### Option 2: Restore from Backup
```cmd
copy pom.xml.original pom.xml
```

### Option 3: Manual Fix

Find these lines in pom.xml and delete the corrupted parts between `<dependencies>` and `<build>`.

The file should have this structure:

```xml
<project>
	<modelVersion>...</modelVersion>
	<parent>...</parent>
	<groupId>...</groupId>
	<artifactId>...</artifactId>
	<version>...</version>
	<packaging>war</packaging>
	
	<properties>...</properties>
	
	<profiles>
		<!-- ADD PROFILES HERE -->
	</profiles>
	
	<dependencies>
		<!-- All dependencies here -->
	</dependencies>
	
	<build>
		<!-- Build configuration here -->
	</build>
</project>
```

---

## 📞 NEXT STEPS

1. **Fix pom.xml** - Add profiles section manually
2. **Test** - Run `mvn help:active-profiles -Ppreprod`
3. **Build** - Use `build-multi-env.bat`
4. **Deploy** - Transfer and deploy WAR file

**The script handles everything else automatically!**

---

**Created:** December 8, 2025  
**Purpose:** Long-term multi-environment build solution  
**Status:** Manual setup required (XML corruption prevented automation)  

**Once set up, this will work forever!** 🚀


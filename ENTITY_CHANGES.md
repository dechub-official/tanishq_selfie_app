# Entity Changes Summary

## Changes Made

### ✅ Store.java Entity Updates

**Added Fields:**
```java
private String region;    // e.g. "North1", "South2", "East1"
private String level;     // Store level/tier
```

**Added Getters/Setters:**
```java
public String getRegion() { return region; }
public void setRegion(String region) { this.region = region; }

public String getLevel() { return level; }
public void setLevel(String level) { this.level = level; }
```

**Location:** After `isCollection` field, before `abmUsername`

---

### ✅ User.java Entity Updates

**Changed Table Name:**
```java
@Table(name = "store_codes")       // ⬅ Changed from "users"
```

**Renamed Field:**
```java
// OLD:
private String username;

// NEW:
@Column(name = "store_code")   // ⬅ maps to DB column store_code
private String storeCode;      // ⬅ field renamed
```

**Updated Methods:**
```java
// OLD:
public String getUsername() { return username; }
public void setUsername(String username) { this.username = username; }

// NEW:
public String getStoreCode() { return storeCode; }
public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
```

---

## ⚠️ IMPORTANT: Breaking Changes

### Database Impact

The User entity now maps to a **different table**: `store_codes` instead of `users`

**This means:**
1. ❌ Your import scripts will **NOT WORK** with the current database
2. ❌ All authentication code referencing `users` table will **FAIL**
3. ❌ The `complete_store_import.sql` script inserts into `users` table, not `store_codes`

### Code Impact

All code using `user.getUsername()` or `user.setUsername()` must be changed to:
- `user.getStoreCode()`
- `user.setStoreCode()`

**Files that need updating:**
- `TanishqPageService.java` - Authentication methods
- `EventsController.java` - Login endpoints
- `UserRepository.java` - Query methods
- Any other service/controller using User entity

---

## 🔧 Required Actions

### Option 1: Keep Using `users` Table (RECOMMENDED)

**Revert the User.java changes:**

```java
@Table(name = "users")  // ⬅ Keep original
public class User {
    
    private String username;  // ⬅ Keep original field name
    
    // Keep original getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

**Why this is recommended:**
- ✅ Your entire codebase uses `users` table
- ✅ Import scripts already target `users` table
- ✅ No code changes needed
- ✅ Authentication already working with this structure

### Option 2: Rename Database Table

**If you want to use `store_codes` table:**

1. **Rename database table:**
```sql
ALTER TABLE users RENAME TO store_codes;
ALTER TABLE store_codes CHANGE username store_code VARCHAR(255);
```

2. **Update all Java code** that references `username` to use `storeCode`

3. **Update import script** (`complete_store_import.sql`):
   - Change all `INSERT INTO users` to `INSERT INTO store_codes`
   - Change all column references from `username` to `store_code`

4. **Update all repository queries:**
```java
// OLD:
User findByUsername(String username);

// NEW:
User findByStoreCode(String storeCode);
```

---

## 💡 Recommendation

**DO NOT proceed with these entity changes** unless you:

1. ✅ Have a clear reason to rename `users` → `store_codes`
2. ✅ Are prepared to update 10+ Java files
3. ✅ Will update the database schema
4. ✅ Will rewrite the import scripts

**Current Status:**
- ✅ Store entity: Updated with `region` and `level` fields (GOOD)
- ⚠️ User entity: Changed to incompatible structure (PROBLEMATIC)

**Suggested Action:**
Revert User.java to use `users` table and `username` field to maintain compatibility with your existing system.

---

## 📊 Entity Structure Comparison

### Store Entity (After Changes)
```
stores table
├── storeCode (PK)
├── storeName, storeAddress, storeCity, etc.
├── region ✨ NEW
├── level ✨ NEW
├── abmUsername (FK)
├── rbmUsername (FK)
└── ceeUsername (FK)
```

### User Entity (After Changes - BREAKS COMPATIBILITY)
```
store_codes table ⚠️ RENAMED
├── id (PK)
├── name
├── storeCode ⚠️ RENAMED from username
├── password
├── role
└── email
```

---

**Date:** November 26, 2025
**Status:** ⚠️ Changes made but may cause system breakage

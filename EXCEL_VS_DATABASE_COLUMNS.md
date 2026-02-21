# 📊 EXCEL vs DATABASE COLUMNS - COMPLETE MAPPING

**Date:** January 24, 2026  
**Purpose:** Clarify the difference between Excel input columns and Database storage columns

---

## ✅ THE ANSWER: BOTH ARE CORRECT!

### 🎯 Key Understanding:
- **Excel has 15 columns** = What YOU provide (business data)
- **Database has 22 columns** = What the SYSTEM stores (business data + auto-generated fields)

---

## 📋 COMPLETE COLUMN COMPARISON

| # | Excel Column | Database Column | Source | Example Value |
|---|--------------|-----------------|--------|---------------|
| 1 | ✅ **store_code** | **store_code** | YOU provide | `TEST`, `BTQ001` |
| 2 | ✅ **event_name** | **event_name** | YOU provide | `Diwali Festival 2025` |
| 3 | ✅ **event_type** | **event_type** | YOU provide | `SHOPPING FESTIVAL` |
| 4 | ✅ **event_sub_type** | **event_sub_type** | YOU provide | `Diwali` |
| 5 | ✅ **start_date** | **start_date** | YOU provide | `15-11-2025` |
| 6 | ✅ **region** | **region** | YOU provide | `South1` |
| 7 | ✅ **rso** | **rso** | YOU provide | `Ramesh Kumar` |
| 8 | ✅ **invitees** | **invitees** | YOU provide | `200` |
| 9 | ✅ **attendees** | **attendees** | YOU provide | `180` |
| 10 | ✅ **location** | **location** | YOU provide | `In store` |
| 11 | ✅ **community** | **community** | YOU provide | `Local Community` |
| 12 | ✅ **sale** | **sale** | YOU provide | `500000.00` |
| 13 | ✅ **advance** | **advance** | YOU provide | `100000.00` |
| 14 | ✅ **ghs_or_rga** | **ghs_or_rga** | YOU provide | `20000.00` |
| 15 | ✅ **gmb** | **gmb** | YOU provide | `10000.00` |
| - | ❌ NOT in Excel | **id** | ⚙️ AUTO-GENERATED | `GUT_c4dfa69c-2b46-4090-b9be-e21c4eef59b5` |
| - | ❌ NOT in Excel | **created_at** | ⚙️ AUTO-GENERATED | `2025-10-01 10:09:50` |
| - | ❌ NOT in Excel | **attendees_uploaded** | ⚙️ AUTO-GENERATED | `FALSE` |
| - | ❌ NOT in Excel | **completed_events_drive_link** | ⚙️ SET LATER | Empty or Google Drive URL |
| - | ❌ NOT in Excel | **diamond_awareness** | ⚙️ AUTO-GENERATED | `0` or `FALSE` |
| - | ❌ NOT in Excel | **ghs_flag** | ⚙️ AUTO-GENERATED | `FALSE` |
| - | ❌ NOT in Excel | **image** | ⚙️ AUTO-GENERATED | `/static/assets/event2-B8cJ3Wja.png` |

---

## 🔍 YOUR SAMPLE DATA EXPLAINED

Your Excel export shows:
```
id | advance | attendees | attendees_uploaded | community | completed_events_drive_link | 
created_at | diamond_awareness | event_name | event_sub_type | event_type | ghs_flag | 
ghs_or_rga | gmb | image | invitees | location | region | rso | sale | start_date | store_code
```

**This is the FULL DATABASE VIEW** (all 22 columns after import)

Example row:
```
GUT_c4dfa69c-2b46-4090-b9be-e21c4eef59b5 | 0.00 | 0 | FALSE | | | 2025/10/01 10:09:50 | 
| Birthday | DAILY CELEBRATION | | 0.00 | 0.00 | /static/assets/event2-B8cJ3Wja.png | 
0 | customer's house | | | 0.00 | 2025-10-01 | GUT
```

---

## 📝 WHAT YOU NEED TO CREATE IN EXCEL

**Excel Template (15 columns only):**
```csv
store_code,event_name,event_type,event_sub_type,start_date,region,rso,invitees,attendees,location,community,sale,advance,ghs_or_rga,gmb
GUT,Birthday,DAILY CELEBRATION,,2025-10-01,,,0,0,customer's house,,0.00,0.00,0.00,0.00
```

**After Import, Database will have (22 columns):**
```
✅ All your 15 input columns
PLUS
⚙️ id = GUT_c4dfa69c-2b46-4090-b9be-e21c4eef59b5 (auto-generated)
⚙️ created_at = 2025-10-01 10:09:50 (auto-generated)
⚙️ attendees_uploaded = FALSE (default)
⚙️ completed_events_drive_link = (empty)
⚙️ diamond_awareness = 0 (default)
⚙️ ghs_flag = FALSE (default)
⚙️ image = /static/assets/event2-B8cJ3Wja.png (default)
```

---

## 🎯 STEP-BY-STEP IMPORT FLOW

### 1️⃣ YOU CREATE EXCEL (15 columns)
```
store_code | event_name | event_type | ... | gmb
TEST | Diwali 2025 | SHOPPING FESTIVAL | ... | 10000
```

### 2️⃣ SYSTEM IMPORTS TO DATABASE
- Reads your 15 columns
- Generates `id` using formula: `CONCAT(store_code, '_', UUID())`
- Sets `created_at` to current timestamp
- Sets defaults for other fields

### 3️⃣ DATABASE NOW HAS (22 columns)
```
id | advance | attendees | ... | store_code
TEST_abc-123-def | 100000 | 180 | ... | TEST
```

---

## 📋 IMPORT SQL COMMAND BREAKDOWN

```sql
LOAD DATA LOCAL INFILE 'events.csv'
INTO TABLE events
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS

-- Step 1: Map your 15 Excel columns to temporary variables
(store_code, event_name, event_type, @event_sub_type, start_date, 
 @region, @rso, @invitees, @attendees, @location, @community, 
 @sale, @advance, @ghs_or_rga, @gmb)

-- Step 2: Set the auto-generated columns + handle nulls
SET 
    id = CONCAT(store_code, '_', UUID()),              -- Auto-generate ID
    created_at = NOW(),                                -- Auto-generate timestamp
    event_sub_type = NULLIF(@event_sub_type, ''),     -- Convert empty to NULL
    region = NULLIF(@region, ''),
    rso = NULLIF(@rso, ''),
    invitees = COALESCE(@invitees, 0),                -- Convert empty to 0
    attendees = COALESCE(@attendees, 0),
    location = NULLIF(@location, ''),
    community = NULLIF(@community, ''),
    sale = COALESCE(@sale, 0.00),
    advance = COALESCE(@advance, 0.00),
    ghs_or_rga = COALESCE(@ghs_or_rga, 0.00),
    gmb = COALESCE(@gmb, 0.00),
    attendees_uploaded = FALSE,                        -- Default value
    diamond_awareness = 0,                             -- Default value
    ghs_flag = FALSE,                                  -- Default value
    image = '/static/assets/event2-B8cJ3Wja.png';    -- Default image
```

---

## ✅ FINAL ANSWER

### Question: Which columns should be in Excel?

**Answer:** Use the **15 INPUT COLUMNS** from `EXCEL_QUICK_REF.md`:
```
store_code, event_name, event_type, event_sub_type, start_date, 
region, rso, invitees, attendees, location, community, 
sale, advance, ghs_or_rga, gmb
```

### Question: Why does the database export show 22 columns?

**Answer:** Because the database stores:
- Your 15 input columns (business data)
- 7 auto-generated columns (system data: id, created_at, flags, defaults)

### Question: Is my Excel template correct?

**Answer:** ✅ **YES!** Your Excel template with 15 columns is 100% correct.

---

## 📁 REFERENCE FILES

1. **Quick Reference:** `EXCEL_QUICK_REF.md` - Use this for daily work
2. **Complete Guide:** `EXCEL_TEMPLATE_GUIDE.md` - Detailed rules and examples
3. **Sample Template:** `database_backup/events_import_template_SAMPLE.csv`
4. **This Mapping:** `EXCEL_VS_DATABASE_COLUMNS.md` - Column comparison

---

## 🎓 BEST PRACTICES

✅ **For Data Entry:**
- Use Excel with 15 columns (what you provide)
- Don't include id, created_at, or other auto-fields
- Follow format rules from EXCEL_QUICK_REF.md

✅ **For Verification:**
- Export from database shows 22 columns (what's stored)
- Use this to verify import worked correctly
- Check that auto-generated fields have correct values

✅ **For Troubleshooting:**
- Compare your 15 Excel columns with database's matching 15 columns
- Ignore the 7 auto-generated columns when comparing data
- Refer to this document if confused

---

**Created:** January 24, 2026  
**Purpose:** Eliminate confusion between Excel input format and Database storage format


# 🎯 QUICK EXCEL TEMPLATE REFERENCE

**VERIFIED:** January 24, 2026  
**Status:** ✅ Excel columns match database import requirements

---

## 🔍 UNDERSTANDING THE COLUMNS

### 📊 DATABASE HAS 22 COLUMNS (Full Schema):
```
id | advance | attendees | attendees_uploaded | community | completed_events_drive_link | 
created_at | diamond_awareness | event_name | event_sub_type | event_type | ghs_flag | 
ghs_or_rga | gmb | image | invitees | location | region | rso | sale | start_date | store_code
```

### 📝 EXCEL NEEDS ONLY 15 COLUMNS (For Import):
```
store_code | event_name | event_type | event_sub_type | start_date | region | rso | 
invitees | attendees | location | community | sale | advance | ghs_or_rga | gmb
```

### ⚙️ AUTO-GENERATED COLUMNS (Don't Include in Excel):
- **id** - Auto-generated as `store_code_UUID`
- **created_at** - Auto-set to current timestamp
- **attendees_uploaded** - Defaults to FALSE
- **completed_events_drive_link** - Set later via app
- **diamond_awareness** - Defaults to FALSE/0
- **ghs_flag** - Defaults to FALSE/0
- **image** - Set via app or defaults to `/static/assets/event2-B8cJ3Wja.png`

---

## 📋 EXCEL HEADER ROW (Copy to Excel Row 1)

```
store_code,event_name,event_type,event_sub_type,start_date,region,rso,invitees,attendees,location,community,sale,advance,ghs_or_rga,gmb
```

---

## ✅ REQUIRED FIELDS (Must Fill)

1. **store_code** - Example: `TEST`, `BTQ001` (must exist in stores table)
2. **event_name** - Example: `Diwali Festival 2025` (NEVER "-" or blank!)
3. **event_type** - Example: `SPECIAL DAY CELEBRATION`
4. **start_date** - Example: `15-11-2025` (DD-MM-YYYY format!)

## ⚠️ OPTIONAL FIELDS (Can be empty/0)

5. **event_sub_type** - Example: `Birthday`, `Diwali`, `Wedding`
6. **region** - Example: `South1`, `North2`
7. **rso** - Example: `Ramesh Kumar`
8. **invitees** - Number, Example: `200`
9. **attendees** - Number, Example: `180`
10. **location** - Example: `In store`, `customer's house`, `External`
11. **community** - Example: `Local Community`, `VIP Customers`
12. **sale** - Number, Example: `500000` (no commas or currency symbols)
13. **advance** - Number, Example: `100000`
14. **ghs_or_rga** - Number, Example: `20000`
15. **gmb** - Number, Example: `10000`

---

## 📊 EXAMPLE ROW (Copy & Modify)

```
TEST,Diwali Festival 2025,SHOPPING FESTIVAL,Diwali,01-11-2025,South1,Ramesh Kumar,200,180,In store,Local Community,500000,100000,20000,10000
```

---

## 🚫 NEVER DO THIS

❌ event_name: `-` or blank  
❌ start_date: `2025-11-15` or `15/11/2025` or `2025/11/15`  
❌ Numbers: `1,25,000` or `₹125000` or `1.25E5`  
❌ Include auto-generated columns: `id`, `created_at`, `image`

---

## ✅ ALWAYS DO THIS

✅ event_name: `Diwali Festival 2025` (descriptive and unique)  
✅ start_date: `15-11-2025` (DD-MM-YYYY format)  
✅ Numbers: `125000` (plain numbers, no formatting)  
✅ Use only 15 input columns listed above

---

## 📥 IMPORT COMMAND

```sql
LOAD DATA LOCAL INFILE 'C:/path/to/your.csv'
INTO TABLE events
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(store_code, event_name, event_type, @event_sub_type, start_date, @region, @rso, @invitees, @attendees, @location, @community, @sale, @advance, @ghs_or_rga, @gmb)
SET 
    id = CONCAT(store_code, '_', UUID()), 
    created_at = NOW(),
    event_sub_type = NULLIF(@event_sub_type, ''),
    region = NULLIF(@region, ''),
    rso = NULLIF(@rso, ''),
    invitees = COALESCE(@invitees, 0),
    attendees = COALESCE(@attendees, 0),
    location = NULLIF(@location, ''),
    community = NULLIF(@community, ''),
    sale = COALESCE(@sale, 0.00),
    advance = COALESCE(@advance, 0.00),
    ghs_or_rga = COALESCE(@ghs_or_rga, 0.00),
    gmb = COALESCE(@gmb, 0.00),
    attendees_uploaded = FALSE,
    diamond_awareness = 0,
    ghs_flag = FALSE,
    image = '/static/assets/event2-B8cJ3Wja.png';
```

---

## 📁 FILES

- **Template:** `database_backup/events_import_template_SAMPLE.csv`
- **Full Guide:** `EXCEL_TEMPLATE_GUIDE.md`
- **Your Data:** Save as `events_november_december_2025.csv`

---

## ✅ VERIFICATION

Your sample data shows database has these columns:
```
id, advance, attendees, attendees_uploaded, community, completed_events_drive_link, 
created_at, diamond_awareness, event_name, event_sub_type, event_type, ghs_flag, 
ghs_or_rga, gmb, image, invitees, location, region, rso, sale, start_date, store_code
```

**Excel columns are CORRECT** - they provide the 15 business inputs, and the system auto-generates the remaining 7 technical fields.

---

**Created:** Jan 24, 2026  
**Last Verified:** Jan 24, 2026 - Matched against actual database schema


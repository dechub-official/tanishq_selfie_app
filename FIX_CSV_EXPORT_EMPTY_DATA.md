# Store Events CSV Export - Empty Data Fix

## 🔍 Problem
When exporting store events to CSV using the filter:
- URL: `/events/store/events/download?storeCode=TEST&startDate=2025-11-01&endDate=2026-01-30`
- Store summary API shows 10 events with data ✅
- CSV export shows only headers (duplicated) but NO data rows ❌

## 🔧 Fix Applied

### Added Debug Logging
Added comprehensive logging to the `downloadStoreEventsAsCsv` method to track:
1. Number of events fetched from database
2. Number of events after date filtering
3. Number of rows being written to CSV
4. Sample data structure for debugging

### Changes Made

**File:** `EventsController.java`
**Method:** `downloadStoreEventsAsCsv()`

```java
@GetMapping("/store/events/download")
public void downloadStoreEventsAsCsv(
        @RequestParam String storeCode,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        HttpServletResponse response
) throws Exception {
    // Added logging to track data flow
    log.info("Downloading events for storeCode: {}, startDate: {}, endDate: {}", 
             storeCode, startDate, endDate);
    
    List<String> storeCodes = new ArrayList<>();
    storeCodes.add(storeCode);
    List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
    log.info("Found {} total events for store {}", allEvents.size(), storeCode);
    
    List<Map<String, Object>> filteredEvents;
    if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
        filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        log.info("After date filtering: {} events remain", filteredEvents.size());
    } else {
        filteredEvents = allEvents;
    }

    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=store_events_" + storeCode + ".csv");

    CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

    // Headers and field mapping (unchanged)
    List<String> displayHeaders = Arrays.asList(
            "createdAt", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", 
            "Event Name", "RSO", "Start Date", "Image", "Invitees", "Attendees", 
            "completed Events", "Community", "location", "isAttendeesUploaded", 
            "sale", "advance", "ghs/rga", "gmb", "Drive link", "Diamond Awareness", "GHS"
    );
    
    List<String> dbFields = Arrays.asList(
            "created_at", "store_code", "region", "id", "event_type", "event_sub_type", 
            "event_name", "rso", "start_date", "image", "invitees", "attendees", 
            "completed_events_drive_link", "community", "location", "attendees_uploaded", 
            "sale", "advance", "ghs_or_rga", "gmb", "completed_events_drive_link", 
            "diamond_awareness", "ghs_flag"
    );

    writer.writeNext(displayHeaders.toArray(new String[0]));

    // Added logging before writing rows
    log.info("Writing {} rows to CSV for store {}", filteredEvents.size(), storeCode);
    if (!filteredEvents.isEmpty()) {
        log.debug("Sample event data (first row keys): {}", filteredEvents.get(0).keySet());
    }

    for (Map<String, Object> row : filteredEvents) {
        List<String> rowData = dbFields.stream()
                .map(field -> {
                    Object value = row.get(field);
                    return value != null ? value.toString() : "";
                })
                .collect(Collectors.toList());
        writer.writeNext(rowData.toArray(new String[0]));
    }

    log.info("CSV export completed successfully for store {}", storeCode);
    writer.flush();
    writer.close();
}
```

## 🧪 How to Test

### Step 1: Deploy Updated Code
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
# Deploy WAR file to Tomcat
# Restart Tomcat
```

### Step 2: Test CSV Export
```bash
# Try the export again
curl "https://celebrations.tanishq.co.in/events/store/events/download?storeCode=TEST&startDate=2025-11-01&endDate=2026-01-30" -o test_export.csv
```

### Step 3: Check Logs
```bash
# Check Tomcat logs for debugging information
tail -f /path/to/tomcat/logs/catalina.out | grep -i "Downloading events\|Found\|After date\|Writing.*rows"
```

## 📊 Expected Log Output

### If Data is Found:
```
INFO  : Downloading events for storeCode: TEST, startDate: 2025-11-01, endDate: 2026-01-30
INFO  : Found 10 total events for store TEST
INFO  : After date filtering: 10 events remain
INFO  : Writing 10 rows to CSV for store TEST
DEBUG : Sample event data (first row keys): [id, advance, attendees, attendees_uploaded, ...]
INFO  : CSV export completed successfully for store TEST
```

### If No Data is Found:
```
INFO  : Downloading events for storeCode: TEST, startDate: 2025-11-01, endDate: 2026-01-30
INFO  : Found 0 total events for store TEST
INFO  : After date filtering: 0 events remain
INFO  : Writing 0 rows to CSV for store TEST
INFO  : CSV export completed successfully for store TEST
```

## 🔍 Possible Issues & Solutions

### Issue 1: No Events Found (`Found 0 total events`)
**Cause:** Events not in database or wrong store code
**Solution:** 
- Check if events exist: `SELECT * FROM events WHERE store_code = 'TEST'`
- Verify store code is correct (case-sensitive)

### Issue 2: Events Found But Filtered Out
**Logs:**
```
Found 10 total events for store TEST
After date filtering: 0 events remain
```
**Cause:** Date filtering removing all events
**Solution:**
- Check `filterEventsByStartDate` method
- Verify start_date format in database matches filter
- Check date comparison logic

### Issue 3: Events Found But Map Keys Don't Match
**Logs:**
```
Found 10 total events
Writing 10 rows to CSV
Sample event data: [someKey, otherKey, ...]
```
**Cause:** Field names in map don't match `dbFields` array
**Solution:**
- Compare logged keys with `dbFields` array
- Update `dbFields` array to match actual keys from `convertEventToMap`

## 🔑 Field Name Mapping

The CSV export uses these field mappings:

| Display Header | Database Field Name |
|----------------|---------------------|
| createdAt | `created_at` |
| Store Code | `store_code` |
| Region | `region` |
| Id | `id` |
| Event Type | `event_type` |
| Event Sub Type | `event_sub_type` |
| Event Name | `event_name` |
| RSO | `rso` |
| Start Date | `start_date` |
| Image | `image` |
| Invitees | `invitees` |
| Attendees | `attendees` |
| completed Events | `completed_events_drive_link` |
| Community | `community` |
| location | `location` |
| isAttendeesUploaded | `attendees_uploaded` |
| sale | `sale` |
| advance | `advance` |
| ghs/rga | `ghs_or_rga` |
| gmb | `gmb` |
| Drive link | `completed_events_drive_link` |
| Diamond Awareness | `diamond_awareness` |
| GHS | `ghs_flag` |

These field names must match what's returned by `convertEventToMap()` in `TanishqPageService.java`.

## 🧐 Data Verification

### Check Summary API Works:
```bash
curl "https://celebrations.tanishq.co.in/events/store-summary?storeCode=TEST&startDate=2025-11-01&endDate=2026-01-30"
```

Expected response:
```json
{
  "status": 200,
  "message": "Store summary fetched successfully",
  "data": {
    "storeCode": "TEST",
    "totalEvents": 10,
    "totalInvitees": 36,
    "totalAttendees": 10,
    "totalAdvance": 0.0,
    "totalGhsOrRga": 0.0,
    "totalSale": 0.0
  }
}
```

### Check Events API:
```bash
curl "https://celebrations.tanishq.co.in/events/store/events?storeCode=TEST"
```

This should return the actual event data in JSON format.

## 🐛 Debugging Steps

1. **Check if events exist:**
   ```sql
   SELECT * FROM events WHERE store_code = 'TEST' LIMIT 5;
   ```

2. **Check date formats:**
   ```sql
   SELECT id, event_name, start_date 
   FROM events 
   WHERE store_code = 'TEST';
   ```

3. **Test without date filter:**
   ```bash
   curl "https://celebrations.tanishq.co.in/events/store/events/download?storeCode=TEST" -o test_no_filter.csv
   ```

4. **Check logs for actual keys:**
   - Look for: `Sample event data (first row keys): [...]`
   - Compare with `dbFields` array

5. **Test with different store code:**
   ```bash
   curl "https://celebrations.tanishq.co.in/events/store/events/download?storeCode=ANOTHER_STORE" -o test_other.csv
   ```

## 📝 What to Check After Deployment

- [ ] Logs show number of events found
- [ ] Logs show number of events after filtering
- [ ] Logs show number of rows being written
- [ ] CSV file contains data rows (not just headers)
- [ ] CSV data matches database records
- [ ] All columns populated correctly
- [ ] No duplicate headers in CSV

## 🎯 Next Steps

1. Deploy the updated code
2. Test the CSV export
3. Check the logs for the debugging information
4. If still no data:
   - Share the log output
   - Check database records
   - Verify date format and filtering logic

The logging will tell us exactly where the data is being lost!


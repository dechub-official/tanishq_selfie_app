# Excel + MySQL Dual-Mode Strategy for Production Deployment
## 1-Month Testing Plan with Zero Business Impact

---

## 📋 Executive Summary

**Goal**: Maintain Excel sheet functionality alongside MySQL database for 1 month testing period without affecting production business operations.

**Strategy**: Implement a **dual-write, configurable-read** system with feature flags that allows seamless switching between Excel and MySQL data sources.

**Business Impact**: ZERO - System works exactly as before, with MySQL running in parallel for validation.

---

## 🎯 Strategy Overview

### Current Architecture (What We Have Now)
```
┌─────────────────┐
│  Application    │
│                 │
│  ✓ MySQL DB     │ ← All data stored here
│  ✓ Excel Upload │ ← Import capability exists
│                 │
└─────────────────┘
```

### Proposed Architecture (Dual-Mode)
```
┌─────────────────────────────────────┐
│         Application                 │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Data Access Layer           │  │
│  │  (Configurable Source)       │  │
│  └──────────────────────────────┘  │
│           ↓           ↓             │
│    ┌──────────┐  ┌──────────┐     │
│    │  MySQL   │  │  Excel   │     │
│    │  (New)   │  │  Sheets  │     │
│    └──────────┘  └──────────┘     │
│                                     │
│  Feature Flag: data.source=mysql   │
│                or excel or both    │
└─────────────────────────────────────┘
```

---

## 🔧 Implementation Plan

### Phase 1: Add Dual-Write Capability (Week 1)
✅ **Already Done**: MySQL database is working
🔄 **Add**: Google Sheets sync service (parallel writes)

### Phase 2: Add Feature Flags (Week 1)
Configure data source selection via properties file

### Phase 3: Excel Export APIs (Week 2)
Add endpoints to export MySQL data back to Excel

### Phase 4: Testing & Validation (Week 3-4)
Compare data between both sources

### Phase 5: Production Deployment (Month 2)
Switch to MySQL-only mode after validation

---

## 📝 Technical Implementation

### 1. Configuration Properties

**Add to `application-preprod.properties` and `application-prod.properties`:**

```properties
# ========================================
# DATA SOURCE CONFIGURATION
# ========================================
# Options: mysql, excel, both
# - mysql: Read/Write only to MySQL (recommended for production)
# - excel: Read/Write only to Google Sheets (legacy mode)
# - both: Write to both, read from MySQL (testing mode)
data.source.mode=both
data.source.primary=mysql
data.source.backup=excel

# Google Sheets Sync Configuration
google.sheets.sync.enabled=true
google.sheets.sync.async=true
google.sheets.sync.on-write=true
google.sheets.sync.on-read=false
```

### 2. Feature Flag Service

**Create: `DataSourceConfigService.java`**

```java
package com.dechub.tanishq.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class DataSourceConfigService {
    
    @Value("${data.source.mode:mysql}")
    private String dataSourceMode;
    
    @Value("${data.source.primary:mysql}")
    private String primarySource;
    
    @Value("${google.sheets.sync.enabled:false}")
    private boolean sheetsSyncEnabled;
    
    public boolean shouldWriteToMySQL() {
        return "mysql".equals(dataSourceMode) || "both".equals(dataSourceMode);
    }
    
    public boolean shouldWriteToExcel() {
        return "excel".equals(dataSourceMode) || "both".equals(dataSourceMode);
    }
    
    public boolean shouldReadFromMySQL() {
        return "mysql".equals(primarySource) || "mysql".equals(dataSourceMode);
    }
    
    public boolean shouldReadFromExcel() {
        return "excel".equals(primarySource) || "excel".equals(dataSourceMode);
    }
    
    public boolean isDualMode() {
        return "both".equals(dataSourceMode);
    }
}
```

### 3. Dual-Write Service Layer

**Enhance existing services with dual-write capability:**

```java
package com.dechub.tanishq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EventDataSyncService {
    
    @Autowired
    private DataSourceConfigService configService;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private GoogleSheetsSyncService sheetsSyncService;
    
    /**
     * Save event to both MySQL and Google Sheets (based on configuration)
     */
    @Transactional
    public Event saveEvent(Event event) {
        Event savedEvent = null;
        
        // Write to MySQL if configured
        if (configService.shouldWriteToMySQL()) {
            savedEvent = eventRepository.save(event);
            log.info("Event saved to MySQL: {}", savedEvent.getId());
        }
        
        // Write to Google Sheets if configured (async)
        if (configService.shouldWriteToExcel()) {
            syncToGoogleSheetsAsync(event);
        }
        
        return savedEvent;
    }
    
    @Async
    public void syncToGoogleSheetsAsync(Event event) {
        try {
            sheetsSyncService.syncEventToSheet(event);
            log.info("Event synced to Google Sheets: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to sync event to Google Sheets: {}", event.getId(), e);
            // Don't fail the main transaction - log for monitoring
        }
    }
}
```

### 4. Excel Export Service

**Create: `ExcelExportService.java`**

```java
package com.dechub.tanishq.service.excel;

import com.dechub.tanishq.entity.*;
import com.dechub.tanishq.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExcelExportService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private AttendeeRepository attendeeRepository;
    
    @Autowired
    private InviteeRepository inviteeRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Export all events to Excel file
     */
    public byte[] exportEventsToExcel() throws IOException {
        List<Event> events = eventRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Events");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Event ID", "Store Code", "Store Name", "Event Date", 
                "Invitees", "Attendees", "GMB", "Created At"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (Event event : events) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(event.getId());
                row.createCell(1).setCellValue(event.getStoreCode());
                row.createCell(2).setCellValue(event.getStoreName());
                row.createCell(3).setCellValue(event.getEventDate() != null ? 
                    event.getEventDate().format(DATE_FORMAT) : "");
                row.createCell(4).setCellValue(event.getInvitees());
                row.createCell(5).setCellValue(event.getAttendees());
                row.createCell(6).setCellValue(event.getGmb());
                row.createCell(7).setCellValue(event.getCreatedAt() != null ? 
                    event.getCreatedAt().format(DATE_FORMAT) : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Export attendees for a specific event to Excel
     */
    public byte[] exportAttendeesToExcel(String eventId) throws IOException {
        List<Attendee> attendees = attendeeRepository.findByEventId(eventId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendees");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Event ID", "Name", "Phone", "Like", "First Time", 
                "Created At", "Uploaded From Excel", "RSO Name"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (Attendee attendee : attendees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(eventId);
                row.createCell(1).setCellValue(attendee.getName());
                row.createCell(2).setCellValue(attendee.getPhone());
                row.createCell(3).setCellValue(attendee.getLike());
                row.createCell(4).setCellValue(attendee.isFirstTimeAtTanishq() ? "Yes" : "No");
                row.createCell(5).setCellValue(attendee.getCreatedAt() != null ? 
                    attendee.getCreatedAt().format(DATE_FORMAT) : "");
                row.createCell(6).setCellValue(attendee.getIsUploadedFromExcel() != null && 
                    attendee.getIsUploadedFromExcel() ? "Yes" : "No");
                row.createCell(7).setCellValue(attendee.getRsoName());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
```

### 5. Export Controller Endpoints

**Add to `EventsController.java`:**

```java
/**
 * Export events data to Excel
 */
@GetMapping("/api/events/export/excel")
public ResponseEntity<byte[]> exportEventsToExcel() {
    try {
        byte[] excelData = excelExportService.exportEventsToExcel();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", 
            "events_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
            
    } catch (Exception e) {
        log.error("Failed to export events to Excel", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

/**
 * Export attendees data to Excel
 */
@GetMapping("/api/events/{eventId}/attendees/export/excel")
public ResponseEntity<byte[]> exportAttendeesToExcel(@PathVariable String eventId) {
    try {
        byte[] excelData = excelExportService.exportAttendeesToExcel(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", 
            "attendees_" + eventId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
            
    } catch (Exception e) {
        log.error("Failed to export attendees to Excel for event: " + eventId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

---

## 🚀 Deployment Strategy

### Week 1: Pre-Production Testing
```properties
# application-preprod.properties
data.source.mode=both
data.source.primary=mysql
google.sheets.sync.enabled=true
```

**Actions:**
1. Deploy to pre-prod server
2. Test all CRUD operations
3. Verify data appears in both MySQL and Google Sheets
4. Export Excel files and compare with database

### Week 2-4: Production with Dual Mode
```properties
# application-prod.properties
data.source.mode=both
data.source.primary=mysql
google.sheets.sync.enabled=true
```

**Actions:**
1. Deploy to production
2. Monitor both data sources daily
3. Export Excel reports weekly
4. Compare data integrity
5. Fix any discrepancies immediately

### Month 2: MySQL-Only Mode
```properties
# application-prod.properties
data.source.mode=mysql
data.source.primary=mysql
google.sheets.sync.enabled=false
```

**Actions:**
1. Switch to MySQL-only after validation
2. Keep Google Sheets sync as optional feature
3. Maintain Excel export capability for reporting

---

## 📊 Monitoring & Validation

### Daily Checks (Automated)
```java
@Scheduled(cron = "0 0 23 * * *") // Every day at 11 PM
public void validateDataIntegrity() {
    if (configService.isDualMode()) {
        // Count records in MySQL
        long mysqlEventCount = eventRepository.count();
        long mysqlAttendeeCount = attendeeRepository.count();
        
        // Count records in Google Sheets
        long sheetsEventCount = sheetsSyncService.getEventCount();
        long sheetsAttendeeCount = sheetsSyncService.getAttendeeCount();
        
        // Compare and alert if mismatch
        if (mysqlEventCount != sheetsEventCount) {
            emailService.sendAlert("Data Mismatch", 
                "Events count differs: MySQL=" + mysqlEventCount + 
                ", Sheets=" + sheetsEventCount);
        }
    }
}
```

### Weekly Reports
- Export all data to Excel
- Compare with Google Sheets
- Review discrepancies
- Document issues

---

## 🎯 Benefits of This Approach

### ✅ Zero Business Impact
- System continues to work exactly as before
- Excel data available for legacy processes
- No training needed for users

### ✅ Safe Migration
- Dual-write ensures no data loss
- Easy rollback if issues occur
- Gradual validation period

### ✅ Flexibility
- Can switch between modes via config
- No code changes needed
- Feature flag control

### ✅ Export Capability
- Generate Excel reports anytime
- Share data with stakeholders
- Audit trail maintained

---

## 🔄 Rollback Plan

If issues occur during testing:

### Option 1: Switch to Excel-Only Mode
```properties
data.source.mode=excel
data.source.primary=excel
google.sheets.sync.enabled=false
```

### Option 2: Keep Dual Mode, Fix Issues
```properties
data.source.mode=both
data.source.primary=excel  # Temporarily switch primary
google.sheets.sync.enabled=true
```

### Option 3: Emergency Rollback
1. Stop application
2. Restore previous WAR file
3. Verify Google Sheets data
4. Restart application

---

## 📋 Checklist for Production Deployment

### Pre-Deployment
- [ ] Test dual-write in pre-prod environment
- [ ] Verify Excel export functionality
- [ ] Test data validation scripts
- [ ] Setup monitoring alerts
- [ ] Document rollback procedure
- [ ] Train support team on new features

### Deployment Day
- [ ] Backup current database
- [ ] Backup current Google Sheets
- [ ] Deploy new WAR file
- [ ] Verify application starts
- [ ] Test one complete flow (create event → add attendees → export Excel)
- [ ] Monitor logs for errors
- [ ] Check both MySQL and Google Sheets have data

### Post-Deployment (Daily for 1 week, then weekly)
- [ ] Check data integrity
- [ ] Review error logs
- [ ] Export Excel reports
- [ ] Compare counts
- [ ] User feedback review

---

## 💡 Best Practices

1. **Always Write to MySQL First** - Primary source of truth
2. **Google Sheets Sync is Async** - Don't block main operations
3. **Log All Sync Failures** - Monitor and fix immediately
4. **Export Excel Weekly** - Validate data regularly
5. **Keep Feature Flags** - Easy configuration changes
6. **Monitor Performance** - Dual-write may increase latency slightly
7. **Plan Communication** - Inform stakeholders about the testing period

---

## 🎓 Summary

This dual-mode strategy ensures:
- ✅ **Zero business disruption** - Excel data continues to be available
- ✅ **Safe migration** - MySQL validated in parallel
- ✅ **Easy rollback** - Configuration-based switching
- ✅ **Full audit trail** - Data in both systems for comparison
- ✅ **Smooth transition** - 1-month validation before full switch
- ✅ **Future-proof** - Can maintain Excel export indefinitely

After 1 month of successful testing, simply change configuration to MySQL-only mode - no code changes, no deployment risks, smooth transition to production!

---

**Status**: Ready for Implementation
**Estimated Timeline**: 4 weeks
**Risk Level**: LOW ✅
**Business Impact**: ZERO ✅


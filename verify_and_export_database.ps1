# =====================================================
# Database Verification and Export Script
# Purpose: Export database data to CSV for comparison with Google Sheets
# Date: January 29, 2026
# =====================================================

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "    DATABASE VERIFICATION & EXPORT TOOL" -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "selfie_preprod"
$dbUser = "root"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$exportDir = "database_verification_$timestamp"

Write-Host "📋 Configuration:" -ForegroundColor Green
Write-Host "   Database: $dbName" -ForegroundColor White
Write-Host "   Host: ${dbHost}:${dbPort}" -ForegroundColor White
Write-Host "   Export Folder: $exportDir" -ForegroundColor White
Write-Host ""

# Create export directory
New-Item -ItemType Directory -Force -Path $exportDir | Out-Null
Write-Host "✅ Created export directory: $exportDir" -ForegroundColor Green
Write-Host ""

# Prompt for database password
Write-Host "🔐 Please enter the database password:" -ForegroundColor Yellow
$dbPassword = Read-Host -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "Starting Database Verification..." -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Function to execute MySQL query and export to CSV
function Export-MySQLQuery {
    param(
        [string]$Query,
        [string]$OutputFile,
        [string]$Description
    )

    Write-Host "📊 $Description..." -ForegroundColor Cyan

    $mysqlCmd = "mysql -h $dbHost -P $dbPort -u $dbUser -p$plainPassword $dbName -e `"$Query`" --batch --skip-column-names"

    try {
        $result = Invoke-Expression $mysqlCmd 2>&1

        if ($LASTEXITCODE -eq 0) {
            $result | Out-File -FilePath "$exportDir\$OutputFile" -Encoding UTF8
            Write-Host "   ✅ Exported to: $OutputFile" -ForegroundColor Green

            # Count lines
            $lineCount = (Get-Content "$exportDir\$OutputFile").Count
            Write-Host "   📝 Records: $lineCount" -ForegroundColor White
        } else {
            Write-Host "   ❌ Error: $result" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ❌ Failed: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# 1. Export All Stores with Full Details
Write-Host "1️⃣  EXPORTING STORES DATA" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$storesQuery = @"
SELECT
    store_code as 'Store_Code',
    store_name as 'Store_Name',
    store_city as 'City',
    store_state as 'State',
    region as 'Region',
    abm_username as 'ABM',
    rbm_username as 'RBM',
    cee_username as 'CEE',
    store_manager_name as 'Manager_Name',
    store_phone_no_one as 'Phone'
FROM stores
ORDER BY region, store_code
"@

Export-MySQLQuery -Query $storesQuery -OutputFile "01_all_stores.csv" -Description "Exporting all stores"

# 2. Export Region-wise Store Count
Write-Host "2️⃣  EXPORTING REGION SUMMARY" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$regionSummaryQuery = @"
SELECT
    COALESCE(region, 'NULL_EMPTY') as 'Region',
    COUNT(*) as 'Total_Stores'
FROM stores
GROUP BY region
ORDER BY region
"@

Export-MySQLQuery -Query $regionSummaryQuery -OutputFile "02_region_summary.csv" -Description "Exporting region summary"

# 3. Export Stores WITHOUT Region
Write-Host "3️⃣  EXPORTING STORES WITHOUT REGION" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$noRegionQuery = @"
SELECT
    store_code as 'Store_Code',
    store_name as 'Store_Name',
    store_city as 'City',
    store_state as 'State',
    abm_username as 'ABM',
    rbm_username as 'RBM',
    cee_username as 'CEE'
FROM stores
WHERE region IS NULL OR region = '' OR region = 'NULL'
ORDER BY store_state, store_city
"@

Export-MySQLQuery -Query $noRegionQuery -OutputFile "03_stores_without_region.csv" -Description "Exporting stores without region"

# 4. Export ABM Logins
Write-Host "4️⃣  EXPORTING ABM LOGINS" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$abmQuery = @"
SELECT
    a.username as 'ABM_Username',
    a.password as 'Password',
    COUNT(s.store_code) as 'Stores_Assigned'
FROM abm_login a
LEFT JOIN stores s ON a.username = s.abm_username
GROUP BY a.username, a.password
ORDER BY a.username
"@

Export-MySQLQuery -Query $abmQuery -OutputFile "04_abm_logins.csv" -Description "Exporting ABM logins"

# 5. Export RBM Logins
Write-Host "5️⃣  EXPORTING RBM LOGINS" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$rbmQuery = @"
SELECT
    r.username as 'RBM_Username',
    r.password as 'Password',
    COUNT(s.store_code) as 'Stores_Assigned'
FROM rbm_login r
LEFT JOIN stores s ON r.username = s.rbm_username
GROUP BY r.username, r.password
ORDER BY r.username
"@

Export-MySQLQuery -Query $rbmQuery -OutputFile "05_rbm_logins.csv" -Description "Exporting RBM logins"

# 6. Export CEE Logins
Write-Host "6️⃣  EXPORTING CEE LOGINS" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$ceeQuery = @"
SELECT
    c.username as 'CEE_Username',
    c.password as 'Password',
    COUNT(s.store_code) as 'Stores_Assigned'
FROM cee_login c
LEFT JOIN stores s ON c.username = s.cee_username
GROUP BY c.username, c.password
ORDER BY c.username
"@

Export-MySQLQuery -Query $ceeQuery -OutputFile "06_cee_logins.csv" -Description "Exporting CEE logins"

# 7. Export Events with Region
Write-Host "7️⃣  EXPORTING EVENTS DATA" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$eventsQuery = @"
SELECT
    e.id as 'Event_ID',
    e.event_name as 'Event_Name',
    e.store_code as 'Store_Code',
    s.store_name as 'Store_Name',
    e.region as 'Event_Region',
    s.region as 'Store_Region',
    CASE
        WHEN e.region IS NULL OR e.region = '' THEN 'MISSING'
        WHEN e.region = s.region THEN 'MATCH'
        ELSE 'MISMATCH'
    END as 'Region_Status',
    e.start_date as 'Start_Date',
    e.attendees as 'Attendees'
FROM events e
LEFT JOIN stores s ON e.store_code = s.store_code
ORDER BY e.created_at DESC
"@

Export-MySQLQuery -Query $eventsQuery -OutputFile "07_events_region_check.csv" -Description "Exporting events with region status"

# 8. Export Region-wise detailed breakdown
Write-Host "8️⃣  EXPORTING DETAILED REGION BREAKDOWN" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

# North Region
$northQuery = @"
SELECT store_code, store_name, store_city, store_state, region, abm_username, rbm_username, cee_username
FROM stores WHERE region LIKE 'North%' OR region LIKE 'north%'
ORDER BY region, store_code
"@
Export-MySQLQuery -Query $northQuery -OutputFile "08_north_region_stores.csv" -Description "Exporting North region stores"

# South Region
$southQuery = @"
SELECT store_code, store_name, store_city, store_state, region, abm_username, rbm_username, cee_username
FROM stores WHERE region LIKE 'South%' OR region LIKE 'south%'
ORDER BY region, store_code
"@
Export-MySQLQuery -Query $southQuery -OutputFile "09_south_region_stores.csv" -Description "Exporting South region stores"

# East Region
$eastQuery = @"
SELECT store_code, store_name, store_city, store_state, region, abm_username, rbm_username, cee_username
FROM stores WHERE region LIKE 'East%' OR region LIKE 'east%'
ORDER BY region, store_code
"@
Export-MySQLQuery -Query $eastQuery -OutputFile "10_east_region_stores.csv" -Description "Exporting East region stores"

# West Region
$westQuery = @"
SELECT store_code, store_name, store_city, store_state, region, abm_username, rbm_username, cee_username
FROM stores WHERE region LIKE 'West%' OR region LIKE 'west%'
ORDER BY region, store_code
"@
Export-MySQLQuery -Query $westQuery -OutputFile "11_west_region_stores.csv" -Description "Exporting West region stores"

# 9. Export Manager Assignment Issues
Write-Host "9️⃣  EXPORTING MANAGER ASSIGNMENT ISSUES" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$managerIssuesQuery = @"
SELECT
    s.store_code as 'Store_Code',
    s.store_name as 'Store_Name',
    s.region as 'Region',
    s.abm_username as 'ABM_Assigned',
    CASE WHEN a.username IS NULL THEN 'NO_ACCOUNT' ELSE 'HAS_ACCOUNT' END as 'ABM_Status',
    s.rbm_username as 'RBM_Assigned',
    CASE WHEN r.username IS NULL THEN 'NO_ACCOUNT' ELSE 'HAS_ACCOUNT' END as 'RBM_Status',
    s.cee_username as 'CEE_Assigned',
    CASE WHEN c.username IS NULL THEN 'NO_ACCOUNT' ELSE 'HAS_ACCOUNT' END as 'CEE_Status'
FROM stores s
LEFT JOIN abm_login a ON s.abm_username = a.username
LEFT JOIN rbm_login r ON s.rbm_username = r.username
LEFT JOIN cee_login c ON s.cee_username = c.username
WHERE (s.abm_username IS NOT NULL AND a.username IS NULL)
   OR (s.rbm_username IS NOT NULL AND r.username IS NULL)
   OR (s.cee_username IS NOT NULL AND c.username IS NULL)
ORDER BY s.region, s.store_code
"@

Export-MySQLQuery -Query $managerIssuesQuery -OutputFile "12_manager_assignment_issues.csv" -Description "Exporting manager assignment issues"

# 10. Export Summary Statistics
Write-Host "🔟 EXPORTING SUMMARY STATISTICS" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray

$summaryQuery = @"
SELECT 'Total Stores' as 'Metric', COUNT(*) as 'Count' FROM stores
UNION ALL
SELECT 'Stores with Region', COUNT(*) FROM stores WHERE region IS NOT NULL AND region != ''
UNION ALL
SELECT 'Stores without Region', COUNT(*) FROM stores WHERE region IS NULL OR region = ''
UNION ALL
SELECT 'Total Events', COUNT(*) FROM events
UNION ALL
SELECT 'Events with Region', COUNT(*) FROM events WHERE region IS NOT NULL AND region != ''
UNION ALL
SELECT 'Events without Region', COUNT(*) FROM events WHERE region IS NULL OR region = ''
UNION ALL
SELECT 'Total ABM Accounts', COUNT(*) FROM abm_login
UNION ALL
SELECT 'Total RBM Accounts', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'Total CEE Accounts', COUNT(*) FROM cee_login
"@

Export-MySQLQuery -Query $summaryQuery -OutputFile "00_summary_statistics.csv" -Description "Exporting summary statistics"

# Create README file for the export
Write-Host "📝 Creating README file..." -ForegroundColor Cyan
$readmeContent = @"
# Database Verification Export
Generated: $(Get-Date -Format "yyyy-MM-DD HH:mm:ss")
Database: $dbName

## Files Included:

### Summary & Overview
- 00_summary_statistics.csv - Overall database statistics
- 02_region_summary.csv - Store count by region

### Store Data
- 01_all_stores.csv - Complete list of all stores
- 03_stores_without_region.csv - Stores missing region data
- 08_north_region_stores.csv - All North region stores
- 09_south_region_stores.csv - All South region stores
- 10_east_region_stores.csv - All East region stores
- 11_west_region_stores.csv - All West region stores

### Manager Logins
- 04_abm_logins.csv - ABM login accounts and store assignments
- 05_rbm_logins.csv - RBM login accounts and store assignments
- 06_cee_logins.csv - CEE login accounts and store assignments
- 12_manager_assignment_issues.csv - Stores with manager assignment issues

### Events Data
- 07_events_region_check.csv - Events with region verification status

## How to Use:

1. Open any CSV file in Excel or Google Sheets
2. Compare with your Google Sheets data
3. Check for discrepancies in:
   - Store codes
   - Region assignments
   - Manager assignments
   - Missing data

## Key Things to Verify:

✓ Check if all stores from Google Sheets are in the database
✓ Verify region assignments match
✓ Ensure all ABM/RBM/CEE accounts exist
✓ Check for stores without region data
✓ Verify events have correct region assignments

## Next Steps:

If you find issues:
1. Note the store codes or usernames with problems
2. Update the database using SQL scripts
3. Re-run this verification to confirm fixes
"@

$readmeContent | Out-File -FilePath "$exportDir\README.txt" -Encoding UTF8
Write-Host "   ✅ README.txt created" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "    EXPORT COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📁 All files exported to: $exportDir" -ForegroundColor Yellow
Write-Host ""
Write-Host "📋 What to do next:" -ForegroundColor Cyan
Write-Host "   1. Open the CSV files in Excel" -ForegroundColor White
Write-Host "   2. Compare with your Google Sheets data" -ForegroundColor White
Write-Host "   3. Check 03_stores_without_region.csv for missing data" -ForegroundColor White
Write-Host "   4. Review 12_manager_assignment_issues.csv for login problems" -ForegroundColor White
Write-Host "   5. Verify 07_events_region_check.csv for event region issues" -ForegroundColor White
Write-Host ""
Write-Host "💡 Tip: Open the folder with:" -ForegroundColor Yellow
Write-Host "   explorer.exe $exportDir" -ForegroundColor White
Write-Host ""

# Ask if user wants to open the folder
$openFolder = Read-Host "Would you like to open the export folder now? (Y/N)"
if ($openFolder -eq "Y" -or $openFolder -eq "y") {
    explorer.exe $exportDir
    Write-Host "✅ Folder opened!" -ForegroundColor Green
}

Write-Host ""
Write-Host "✅ Verification complete!" -ForegroundColor Green
Write-Host ""


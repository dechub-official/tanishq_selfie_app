# =====================================================
# MySQL Database Verification Script (Windows/PowerShell)
# Verifies database structure and data integrity
# =====================================================

Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         MySQL Database Verification Tool                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Database connection details
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_USER = "root"
$DB_PASS = "Dechub#2025"
$DB_NAME = "selfie_preprod"

# MySQL executable path - update if different on your system
$MYSQL_PATH = "mysql"  # Assumes mysql is in PATH, otherwise specify full path

# Test if MySQL is accessible
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔌 Testing Database Connection..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

# Function to execute MySQL query
function Execute-MySQLQuery {
    param(
        [string]$Query,
        [string]$Database = $DB_NAME
    )

    $arguments = @(
        "-h", $DB_HOST,
        "-P", $DB_PORT,
        "-u", $DB_USER,
        "-p$DB_PASS",
        "-e", $Query
    )

    if ($Database) {
        $arguments = @("-h", $DB_HOST, "-P", $DB_PORT, "-u", $DB_USER, "-p$DB_PASS", $Database, "-e", $Query)
    }

    try {
        $result = & $MYSQL_PATH $arguments 2>&1
        return $result
    }
    catch {
        Write-Host "Error executing query: $_" -ForegroundColor Red
        return $null
    }
}

# Test connection
$connectionTest = Execute-MySQLQuery -Query "SELECT 1;" -Database ""
if ($connectionTest) {
    Write-Host "✓ Database connection successful`n" -ForegroundColor Green
} else {
    Write-Host "✗ Database connection failed" -ForegroundColor Red
    Write-Host "Please ensure MySQL is installed and credentials are correct.`n" -ForegroundColor Yellow
    Write-Host "If MySQL is not in PATH, update the `$MYSQL_PATH variable in this script." -ForegroundColor Yellow
    exit 1
}

# Verify all required tables exist
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📋 Verifying Tables..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

$requiredTables = @(
    "events",
    "stores",
    "attendees",
    "invitees",
    "users",
    "user_details",
    "bride_details",
    "greetings",
    "abm_login",
    "rbm_login",
    "cee_login",
    "password_history",
    "rivaah",
    "rivaah_users",
    "product_details"
)

$missingTables = 0
$existingTables = @()

foreach ($table in $requiredTables) {
    $result = Execute-MySQLQuery -Query "SHOW TABLES LIKE '$table';"
    if ($result -match $table) {
        Write-Host "✓ Table '$table' exists" -ForegroundColor Green
        $existingTables += $table
    } else {
        Write-Host "✗ Table '$table' MISSING" -ForegroundColor Red
        $missingTables++
    }
}

Write-Host ""
if ($missingTables -eq 0) {
    Write-Host "✓ All 15 tables verified successfully!" -ForegroundColor Green
} else {
    Write-Host "⚠ Warning: $missingTables table(s) missing!" -ForegroundColor Yellow
}
Write-Host ""

# Check table record counts
if ($existingTables.Count -gt 0) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "📊 Table Record Counts..." -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

    $countQuery = @"
SELECT 'events' as table_name, COUNT(*) as record_count FROM events
UNION ALL SELECT 'stores', COUNT(*) FROM stores
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'user_details', COUNT(*) FROM user_details
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL SELECT 'abm_login', COUNT(*) FROM abm_login
UNION ALL SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL SELECT 'cee_login', COUNT(*) FROM cee_login
UNION ALL SELECT 'password_history', COUNT(*) FROM password_history
UNION ALL SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL SELECT 'rivaah_users', COUNT(*) FROM rivaah_users
UNION ALL SELECT 'product_details', COUNT(*) FROM product_details;
"@

    $counts = Execute-MySQLQuery -Query $countQuery
    Write-Host $counts
    Write-Host ""
}

# Verify Foreign Key Relationships
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔗 Verifying Foreign Key Relationships..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

$fkQuery = @"
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = '$DB_NAME'
AND REFERENCED_TABLE_NAME IS NOT NULL;
"@

$foreignKeys = Execute-MySQLQuery -Query $fkQuery
Write-Host $foreignKeys
Write-Host ""

# Check for data integrity issues
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔍 Checking Data Integrity..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

$integrityIssues = 0

# Check for orphaned attendees
if ($existingTables -contains "attendees" -and $existingTables -contains "events") {
    $orphanedAttendees = Execute-MySQLQuery -Query "SELECT COUNT(*) FROM attendees WHERE event_id NOT IN (SELECT id FROM events);"
    $count = ($orphanedAttendees -split "`n")[1].Trim()

    if ($count -eq "0") {
        Write-Host "✓ No orphaned attendees found" -ForegroundColor Green
    } else {
        Write-Host "⚠ Found $count orphaned attendee records" -ForegroundColor Yellow
        $integrityIssues++
    }
}

# Check for orphaned invitees
if ($existingTables -contains "invitees" -and $existingTables -contains "events") {
    $orphanedInvitees = Execute-MySQLQuery -Query "SELECT COUNT(*) FROM invitees WHERE event_id NOT IN (SELECT id FROM events);"
    $count = ($orphanedInvitees -split "`n")[1].Trim()

    if ($count -eq "0") {
        Write-Host "✓ No orphaned invitees found" -ForegroundColor Green
    } else {
        Write-Host "⚠ Found $count orphaned invitee records" -ForegroundColor Yellow
        $integrityIssues++
    }
}

# Check for events without stores
if ($existingTables -contains "events" -and $existingTables -contains "stores") {
    $eventsNoStore = Execute-MySQLQuery -Query "SELECT COUNT(*) FROM events WHERE store_code NOT IN (SELECT store_code FROM stores);"
    $count = ($eventsNoStore -split "`n")[1].Trim()

    if ($count -eq "0") {
        Write-Host "✓ All events have valid store codes" -ForegroundColor Green
    } else {
        Write-Host "⚠ Found $count events with invalid store codes" -ForegroundColor Yellow
        $integrityIssues++
    }
}

Write-Host ""

# Database size information
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "💾 Database Size Information..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

$sizeQuery = @"
SELECT
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size_MB'
FROM information_schema.TABLES
WHERE table_schema = '$DB_NAME'
ORDER BY (data_length + index_length) DESC;
"@

$sizes = Execute-MySQLQuery -Query $sizeQuery
Write-Host $sizes
Write-Host ""

# Summary
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📝 Verification Summary" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

if ($missingTables -eq 0) {
    Write-Host "✓ Database Structure: VALID" -ForegroundColor Green
} else {
    Write-Host "✗ Database Structure: INCOMPLETE" -ForegroundColor Red
}

if ($integrityIssues -eq 0) {
    Write-Host "✓ Data Integrity: GOOD" -ForegroundColor Green
} else {
    Write-Host "⚠ Data Integrity: ISSUES FOUND ($integrityIssues)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Verification completed at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Gray

# Pause to keep window open
Write-Host "Press any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")


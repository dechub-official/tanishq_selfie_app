# Test Database Schema - Check if stores table has required columns
# Run this script to verify if the database migration is needed

Write-Host "=== TESTING DATABASE SCHEMA ===" -ForegroundColor Cyan
Write-Host ""

# Database connection details (from application-preprod.properties)
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "selfie_preprod"
$dbUser = "root"
$dbPassword = "Dechub#2025"

# Check if MySQL client is available
$mysqlPath = "mysql"
try {
    $null = Get-Command $mysqlPath -ErrorAction Stop
} catch {
    Write-Host "ERROR: MySQL client not found in PATH" -ForegroundColor Red
    Write-Host "Please install MySQL client or add it to PATH" -ForegroundColor Yellow
    exit 1
}

Write-Host "Checking stores table for required columns..." -ForegroundColor Yellow
Write-Host ""

# SQL query to check if columns exist
$query = @"
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = '$dbName'
  AND TABLE_NAME = 'stores'
  AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username')
ORDER BY COLUMN_NAME;
"@

# Execute query
$result = & $mysqlPath -h $dbHost -P $dbPort -u $dbUser -p$dbPassword -D $dbName -e $query 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Query Results:" -ForegroundColor Green
    Write-Host $result
    Write-Host ""

    # Count how many columns exist
    $lines = ($result -split "`n").Count - 1  # Subtract header line

    if ($lines -eq 6) {
        Write-Host "✅ SUCCESS: All 6 required columns exist in the stores table!" -ForegroundColor Green
        Write-Host ""
        Write-Host "The database schema is correct. If you're still getting errors:" -ForegroundColor Yellow
        Write-Host "1. Restart your Spring Boot application" -ForegroundColor White
        Write-Host "2. Clear browser cache and cookies" -ForegroundColor White
        Write-Host "3. Check application logs for other errors" -ForegroundColor White
    } elseif ($lines -eq 0) {
        Write-Host "❌ ERROR: None of the required columns exist!" -ForegroundColor Red
        Write-Host ""
        Write-Host "ACTION REQUIRED:" -ForegroundColor Yellow
        Write-Host "Run the database migration script:" -ForegroundColor White
        Write-Host "  mysql -u root -p selfie_preprod < sql\FIX_MISSING_COLUMNS.sql" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Or manually add columns:" -ForegroundColor White
        Write-Host "  ALTER TABLE stores ADD COLUMN region VARCHAR(100);" -ForegroundColor Cyan
        Write-Host "  ALTER TABLE stores ADD COLUMN level VARCHAR(50);" -ForegroundColor Cyan
        Write-Host "  ALTER TABLE stores ADD COLUMN abm_username VARCHAR(255);" -ForegroundColor Cyan
        Write-Host "  ALTER TABLE stores ADD COLUMN rbm_username VARCHAR(255);" -ForegroundColor Cyan
        Write-Host "  ALTER TABLE stores ADD COLUMN cee_username VARCHAR(255);" -ForegroundColor Cyan
        Write-Host "  ALTER TABLE stores ADD COLUMN corporate_username VARCHAR(255);" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️ WARNING: Only $lines out of 6 required columns exist!" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "ACTION REQUIRED:" -ForegroundColor Yellow
        Write-Host "Run the database migration script to add missing columns:" -ForegroundColor White
        Write-Host "  mysql -u root -p selfie_preprod < sql\FIX_MISSING_COLUMNS.sql" -ForegroundColor Cyan
    }
} else {
    Write-Host "❌ ERROR: Could not connect to database" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error details:" -ForegroundColor Yellow
    Write-Host $result
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor White
    Write-Host "1. MySQL server is running" -ForegroundColor White
    Write-Host "2. Database 'selfie_preprod' exists" -ForegroundColor White
    Write-Host "3. Credentials are correct (user: $dbUser)" -ForegroundColor White
    Write-Host "4. MySQL client is installed and in PATH" -ForegroundColor White
}

Write-Host ""
Write-Host "=== TEST COMPLETE ===" -ForegroundColor Cyan


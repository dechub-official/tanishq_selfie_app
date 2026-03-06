# ========================================
# COMPREHENSIVE DIAGNOSTIC AND FIX SCRIPT
# ========================================

Write-Host "=== TANISHQ STORE SUMMARY ERROR DIAGNOSTICS ===" -ForegroundColor Cyan
Write-Host ""

$workDir = "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean"
Set-Location $workDir

Write-Host "Step 1: Checking which profile is active..." -ForegroundColor Yellow
$propertiesFile = "src\main\resources\application-preprod.properties"
if (Test-Path $propertiesFile) {
    $dbUrl = Select-String -Path $propertiesFile -Pattern "spring.datasource.url"
    $dbUser = Select-String -Path $propertiesFile -Pattern "spring.datasource.username"
    Write-Host "Database Configuration:" -ForegroundColor Green
    Write-Host $dbUrl -ForegroundColor White
    Write-Host $dbUser -ForegroundColor White
} else {
    Write-Host "⚠️ WARNING: application-preprod.properties not found" -ForegroundColor Red
}
Write-Host ""

Write-Host "Step 2: Checking if old WAR files exist in target..." -ForegroundColor Yellow
if (Test-Path "target\*.war") {
    $warFiles = Get-ChildItem "target\*.war"
    Write-Host "Found WAR files:" -ForegroundColor Green
    $warFiles | ForEach-Object { Write-Host "  - $($_.Name) ($(($_.Length / 1MB).ToString('0.00')) MB)" -ForegroundColor White }
    Write-Host ""
    Write-Host "⚠️ NOTE: These are old builds. You need to rebuild after code changes." -ForegroundColor Yellow
} else {
    Write-Host "No WAR files found in target/" -ForegroundColor White
}
Write-Host ""

Write-Host "Step 3: Checking Java entity files..." -ForegroundColor Yellow
$storeEntity = "src\main\java\com\dechub\tanishq\entity\Store.java"
$eventEntity = "src\main\java\com\dechub\tanishq\entity\Event.java"

if (Test-Path $storeEntity) {
    $hasRegion = Select-String -Path $storeEntity -Pattern "private String region" -Quiet
    $hasAbmUsername = Select-String -Path $storeEntity -Pattern "private String abmUsername" -Quiet
    $hasColumnAnnotations = Select-String -Path $storeEntity -Pattern '@Column\(name = "region"\)' -Quiet

    Write-Host "Store.java:" -ForegroundColor Green
    Write-Host "  - Has region field: $hasRegion" -ForegroundColor White
    Write-Host "  - Has abmUsername field: $hasAbmUsername" -ForegroundColor White
    Write-Host "  - Has @Column annotations: $hasColumnAnnotations" -ForegroundColor White
}

if (Test-Path $eventEntity) {
    $hasLazyFetch = Select-String -Path $eventEntity -Pattern "fetch = FetchType.LAZY" -Quiet
    Write-Host "Event.java:" -ForegroundColor Green
    Write-Host "  - Has LAZY fetch for Store: $hasLazyFetch" -ForegroundColor White
}
Write-Host ""

Write-Host "Step 4: Action Required - Database Verification" -ForegroundColor Yellow
Write-Host "You MUST manually verify the database has the required columns:" -ForegroundColor Red
Write-Host ""
Write-Host "Run this SQL query:" -ForegroundColor White
Write-Host @"
USE selfie_preprod;
SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'stores'
  AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username');
"@ -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected: You should see 6 rows returned with those column names" -ForegroundColor White
Write-Host ""

Write-Host "Step 5: Rebuild the application..." -ForegroundColor Yellow
Write-Host "This will compile the updated Entity files with LAZY fetch" -ForegroundColor White
Write-Host ""

$rebuild = Read-Host "Do you want to rebuild the application now? (y/n)"
if ($rebuild -eq 'y' -or $rebuild -eq 'Y') {
    Write-Host ""
    Write-Host "Cleaning old build..." -ForegroundColor Yellow
    mvn clean

    Write-Host ""
    Write-Host "Building with preprod profile..." -ForegroundColor Yellow
    mvn clean package -Ppreprod -DskipTests

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ BUILD SUCCESSFUL!" -ForegroundColor Green
        Write-Host ""
        Write-Host "New WAR file created:" -ForegroundColor Green
        Get-ChildItem "target\*.war" | ForEach-Object {
            Write-Host "  - $($_.Name) ($(($_.Length / 1MB).ToString('0.00')) MB)" -ForegroundColor White
        }
        Write-Host ""
        Write-Host "⚠️ CRITICAL: You MUST restart/redeploy the application now!" -ForegroundColor Red
        Write-Host "The old running application has cached metadata." -ForegroundColor Yellow
    } else {
        Write-Host ""
        Write-Host "❌ BUILD FAILED" -ForegroundColor Red
        Write-Host "Check the error messages above" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "Skipping rebuild. Remember to rebuild before deploying!" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== DIAGNOSTIC COMPLETE ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. ✅ Verify database has 6 new columns (run the SQL query above)" -ForegroundColor White
Write-Host "2. ✅ Rebuild application (mvn clean package -Ppreprod)" -ForegroundColor White
Write-Host "3. ✅ Redeploy/restart the application server" -ForegroundColor White
Write-Host "4. ✅ Clear browser cache and test again" -ForegroundColor White
Write-Host ""


# Database Verification Script for Tanishq Selfie App
# Run this script to check if data is being saved to the database

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   Tanishq Selfie App - Database Status Check" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Database connection details
$username = "root"
$password = "Root@12345"
$database = "tanishq_app"

Write-Host "Checking database: $database" -ForegroundColor Yellow
Write-Host ""

# Check Events
Write-Host "1. EVENTS TABLE:" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT COUNT(*) as total_events FROM $database.events;" 2>$null
Write-Host ""

# Check Attendees
Write-Host "2. ATTENDEES TABLE:" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT COUNT(*) as total_attendees FROM $database.attendees;" 2>$null
Write-Host ""

# Check Invitees
Write-Host "3. INVITEES TABLE:" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT COUNT(*) as total_invitees FROM $database.invitees;" 2>$null
Write-Host ""

# Check Stores
Write-Host "4. STORES TABLE:" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT COUNT(*) as total_stores FROM $database.stores;" 2>$null
Write-Host ""

# Latest Events (if any)
Write-Host "5. LATEST 5 EVENTS (if any):" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT id, event_name, store_code, created_at FROM $database.events ORDER BY created_at DESC LIMIT 5;" 2>$null
Write-Host ""

# Latest Attendees (if any)
Write-Host "6. LATEST 5 ATTENDEES (if any):" -ForegroundColor Green
mysql -u $username -p$password -e "SELECT id, name, phone, created_at FROM $database.attendees ORDER BY created_at DESC LIMIT 5;" 2>$null
Write-Host ""

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Check complete! Run this script anytime to verify." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Event Creation Fix - Build and Package Script (Windows)
# Run this locally to build the WAR file, then upload to server

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Event Creation Authorization Fix - Build Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$projectDir = "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean"
$outputDir = "$projectDir\target"

Write-Host "Step 1: Checking Maven installation..." -ForegroundColor Yellow
$mavenVersion = & mvn -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Maven not found! Please install Maven or add it to PATH." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Maven found" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Cleaning previous build..." -ForegroundColor Yellow
Set-Location $projectDir
& mvn clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Clean failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Clean successful" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Compiling and packaging application..." -ForegroundColor Yellow
Write-Host "   This may take a few minutes..." -ForegroundColor Gray
& mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Build failed! Check output above for errors." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful!" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Locating WAR file..." -ForegroundColor Yellow
$warFile = Get-ChildItem -Path $outputDir -Filter "*.war" | Select-Object -First 1

if ($null -eq $warFile) {
    Write-Host "✗ WAR file not found in $outputDir" -ForegroundColor Red
    exit 1
}

Write-Host "✓ WAR file created: $($warFile.Name)" -ForegroundColor Green
Write-Host "   Location: $($warFile.FullName)" -ForegroundColor Gray
Write-Host "   Size: $([math]::Round($warFile.Length / 1MB, 2)) MB" -ForegroundColor Gray

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Upload WAR file to server:" -ForegroundColor White
Write-Host "   scp $($warFile.FullName) root@your-server:/opt/tanishq/applications_preprod/" -ForegroundColor Gray
Write-Host ""
Write-Host "2. On the server, run:" -ForegroundColor White
Write-Host "   systemctl stop tomcat" -ForegroundColor Gray
Write-Host "   cp /opt/tanishq/applications_preprod/$($warFile.Name) /opt/tanishq/applications_preprod/tanishq-preprod.war" -ForegroundColor Gray
Write-Host "   systemctl start tomcat" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Monitor logs:" -ForegroundColor White
Write-Host "   tail -f /opt/tanishq/applications_preprod/application.log" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Test event creation:" -ForegroundColor White
Write-Host "   - Log in as TEST user" -ForegroundColor Gray
Write-Host "   - Create a new event" -ForegroundColor Gray
Write-Host "   - Verify no 'Access denied' errors" -ForegroundColor Gray
Write-Host ""


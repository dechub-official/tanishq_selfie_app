#!/usr/bin/env pwsh
# ============================================
# Greeting Feature QR Fix Verification Script
# ============================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Greeting Feature QR Code Fix - Verification" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$BASE_URL = "http://localhost:8080"

Write-Host "Step 1: Creating a new greeting..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/greetings/generate" -Method POST
    $GREETING_ID = $response
    Write-Host "  ✓ Created Greeting ID: $GREETING_ID" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Failed to create greeting: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "Step 2: Generating QR Code..." -ForegroundColor Yellow
try {
    $qrResponse = Invoke-WebRequest -Uri "$BASE_URL/greetings/$GREETING_ID/qr" -Method GET -OutFile "qr_code_$GREETING_ID.png"
    Write-Host "  ✓ QR Code saved to: qr_code_$GREETING_ID.png" -ForegroundColor Green
    Write-Host "  ℹ File size: $((Get-Item qr_code_$GREETING_ID.png).Length) bytes" -ForegroundColor Cyan
} catch {
    Write-Host "  ✗ Failed to generate QR code: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "Step 3: Checking greeting status (should be pending)..." -ForegroundColor Yellow
try {
    $viewResponse = Invoke-RestMethod -Uri "$BASE_URL/greetings/$GREETING_ID/view" -Method GET
    Write-Host "  ✓ Status: $($viewResponse.status)" -ForegroundColor Green
    Write-Host "  ✓ Has Video: $($viewResponse.hasVideo)" -ForegroundColor Green

    if ($viewResponse.status -eq "pending" -and $viewResponse.hasVideo -eq $false) {
        Write-Host "  ✓ Correct behavior: Greeting is in pending state" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Unexpected state!" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Failed to check status: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "IMPORTANT: QR Code Verification" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "The QR code should encode ONLY: $GREETING_ID" -ForegroundColor White
Write-Host ""
Write-Host "❌ NOT this:" -ForegroundColor Red
Write-Host "   https://celebrationsite-preprod.tanishq.co.in/greetings/$GREETING_ID/upload" -ForegroundColor Red
Write-Host ""
Write-Host "✅ ONLY this:" -ForegroundColor Green
Write-Host "   $GREETING_ID" -ForegroundColor Green
Write-Host ""
Write-Host "Use any QR scanner app to verify the content." -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Summary of Changes Made:" -ForegroundColor Cyan
Write-Host "  • Modified: GreetingService.java" -ForegroundColor White
Write-Host "  • Changed: QR generation to encode only uniqueId" -ForegroundColor White
Write-Host "  • Removed: Full URL construction (greetingBaseUrl + uniqueId + '/upload')" -ForegroundColor White
Write-Host "  • Result: Frontend now receives plain uniqueId as expected" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Scan the generated QR code with your phone" -ForegroundColor White
Write-Host "  2. Verify it contains only the greeting ID" -ForegroundColor White
Write-Host "  3. Test the full upload flow" -ForegroundColor White
Write-Host ""

# Optionally prompt for video upload test
Write-Host "Press Enter to see the video upload command..." -ForegroundColor Yellow
$null = Read-Host

Write-Host ""
Write-Host "To test video upload, run this command:" -ForegroundColor Cyan
Write-Host "curl -X POST $BASE_URL/greetings/$GREETING_ID/upload \" -ForegroundColor White
Write-Host "  -F 'video=@YOUR_VIDEO.mp4' \" -ForegroundColor White
Write-Host "  -F 'name=Test User' \" -ForegroundColor White
Write-Host "  -F 'message=Test Message'" -ForegroundColor White
Write-Host ""


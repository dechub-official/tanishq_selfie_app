# Rate Limiting - Quick Test Script
# Use this to test the rate limiting implementation after deployment

Write-Host "=== Rate Limiting Test Script ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$endpoint = "$baseUrl/events/login"

Write-Host "Testing endpoint: $endpoint" -ForegroundColor Yellow
Write-Host ""

# Test 1: Normal usage (within limit)
Write-Host "Test 1: Sending 5 requests (within 10/min limit)..." -ForegroundColor Green
for ($i = 1; $i -le 5; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $endpoint `
            -Method POST `
            -Headers @{"Content-Type"="application/json"} `
            -Body '{"code":"STORE001","password":"test"}' `
            -UseBasicParsing `
            -ErrorAction SilentlyContinue

        Write-Host "Request $i : Status $($response.StatusCode)" -ForegroundColor White
    } catch {
        Write-Host "Request $i : Status $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor White
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "Test 2: Sending 15 rapid requests (should hit rate limit)..." -ForegroundColor Red
$success = 0
$rateLimited = 0

for ($i = 1; $i -le 15; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $endpoint `
            -Method POST `
            -Headers @{"Content-Type"="application/json"} `
            -Body '{"code":"STORE001","password":"test"}' `
            -UseBasicParsing `
            -ErrorAction SilentlyContinue

        $success++
        Write-Host "Request $i : Status $($response.StatusCode) - ALLOWED" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        if ($statusCode -eq 429) {
            $rateLimited++
            Write-Host "Request $i : Status 429 - RATE LIMITED ✓" -ForegroundColor Yellow
        } else {
            Write-Host "Request $i : Status $statusCode" -ForegroundColor White
        }
    }
}

Write-Host ""
Write-Host "=== Test Results ===" -ForegroundColor Cyan
Write-Host "Requests allowed: $success" -ForegroundColor Green
Write-Host "Requests rate-limited: $rateLimited" -ForegroundColor Yellow
Write-Host ""

if ($rateLimited -gt 0) {
    Write-Host "✓ Rate limiting is WORKING!" -ForegroundColor Green
} else {
    Write-Host "✗ Rate limiting may not be working. Check logs." -ForegroundColor Red
}

Write-Host ""
Write-Host "Expected: First 10 requests allowed, remaining 5 rate-limited (429)" -ForegroundColor Gray
Write-Host "Note: If server is not running, all requests will fail" -ForegroundColor Gray


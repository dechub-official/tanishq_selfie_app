# HTTP Methods Restriction - Testing Script
# Run this script to test the HTTP method restrictions

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HTTP METHODS RESTRICTION - TEST SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

Write-Host "Testing HTTP Method Restrictions..." -ForegroundColor Yellow
Write-Host ""

# Test 1: GET request (should work)
Write-Host "1. Testing GET request (should work)..." -ForegroundColor White
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/events/login" -Method GET -ErrorAction SilentlyContinue
    Write-Host "   ✅ GET request: Status $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  GET request: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
}
Write-Host ""

# Test 2: POST request (should work)
Write-Host "2. Testing POST request (should work)..." -ForegroundColor White
try {
    $body = @{username="test"; password="test"} | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$baseUrl/events/abm_login" -Method POST -Body $body -ContentType "application/json" -ErrorAction SilentlyContinue
    Write-Host "   ✅ POST request: Status $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  POST request: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
}
Write-Host ""

# Test 3: OPTIONS request (should work - CORS)
Write-Host "3. Testing OPTIONS request (should work - CORS)..." -ForegroundColor White
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/events/login" -Method OPTIONS -ErrorAction SilentlyContinue
    Write-Host "   ✅ OPTIONS request: Status $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  OPTIONS request: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: PUT request (should be blocked - 403/405)
Write-Host "4. Testing PUT request (should be BLOCKED)..." -ForegroundColor White
try {
    $body = @{data="test"} | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$baseUrl/greetings/test123" -Method PUT -Body $body -ContentType "application/json" -ErrorAction SilentlyContinue
    Write-Host "   ❌ PUT request NOT blocked: Status $($response.StatusCode)" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403 -or $statusCode -eq 405) {
        Write-Host "   ✅ PUT request BLOCKED: Status $statusCode" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  PUT request: Unexpected status $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 5: DELETE request (should be blocked - 403/405)
Write-Host "5. Testing DELETE request (should be BLOCKED)..." -ForegroundColor White
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/greetings/test123" -Method DELETE -ErrorAction SilentlyContinue
    Write-Host "   ❌ DELETE request NOT blocked: Status $($response.StatusCode)" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403 -or $statusCode -eq 405) {
        Write-Host "   ✅ DELETE request BLOCKED: Status $statusCode" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  DELETE request: Unexpected status $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 6: PATCH request (should be blocked - 403/405)
Write-Host "6. Testing PATCH request (should be BLOCKED)..." -ForegroundColor White
try {
    $body = @{data="test"} | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$baseUrl/greetings/test123" -Method PATCH -Body $body -ContentType "application/json" -ErrorAction SilentlyContinue
    Write-Host "   ❌ PATCH request NOT blocked: Status $($response.StatusCode)" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403 -or $statusCode -eq 405) {
        Write-Host "   ✅ PATCH request BLOCKED: Status $statusCode" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  PATCH request: Unexpected status $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TESTING COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Yellow
Write-Host "  ✅ GET request: Should work (200 or similar)" -ForegroundColor White
Write-Host "  ✅ POST request: Should work (200 or similar)" -ForegroundColor White
Write-Host "  ✅ OPTIONS request: Should work (200 or similar)" -ForegroundColor White
Write-Host "  ✅ PUT request: Should be blocked (403 or 405)" -ForegroundColor White
Write-Host "  ✅ DELETE request: Should be blocked (403 or 405)" -ForegroundColor White
Write-Host "  ✅ PATCH request: Should be blocked (403 or 405)" -ForegroundColor White
Write-Host ""
Write-Host "NOTE: Make sure the application is running before executing this script!" -ForegroundColor Cyan
Write-Host "      Start the application with: java -jar target/*.war" -ForegroundColor Cyan
Write-Host ""


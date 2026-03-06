# Server Information Disclosure - Testing Script
# Tests to verify server headers are properly suppressed
# Date: March 5, 2026

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Server Information Disclosure - Test Suite" -ForegroundColor Cyan
Write-Host "   OWASP A05:2021 - Security Misconfiguration" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$baseUrl = "http://localhost:3000"  # Change this to your server URL
# For production: $baseUrl = "https://celebrations.tanishq.co.in"

# Test endpoints
$endpoints = @(
    "/events/login",
    "/events/upload",
    "/events/attendees",
    "/checklist",
    "/greetings"
)

Write-Host "Testing against: $baseUrl" -ForegroundColor Yellow
Write-Host ""

# Function to test headers
function Test-ServerHeaders {
    param (
        [string]$url,
        [string]$endpoint
    )

    $fullUrl = "$url$endpoint"
    Write-Host "Testing: $endpoint" -ForegroundColor White

    try {
        # Make HEAD request to get headers without body
        $response = Invoke-WebRequest -Uri $fullUrl -Method HEAD -UseBasicParsing -ErrorAction Stop

        # Check for Server header
        $serverHeader = $response.Headers["Server"]

        if ([string]::IsNullOrEmpty($serverHeader)) {
            Write-Host "  ✅ Server header: HIDDEN (PASS)" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Server header: $serverHeader (FAIL)" -ForegroundColor Red
        }

        # Check for X-Powered-By header
        $poweredByHeader = $response.Headers["X-Powered-By"]

        if ([string]::IsNullOrEmpty($poweredByHeader)) {
            Write-Host "  ✅ X-Powered-By: HIDDEN (PASS)" -ForegroundColor Green
        } else {
            Write-Host "  ❌ X-Powered-By: $poweredByHeader (FAIL)" -ForegroundColor Red
        }

        # Check for AWS headers
        $awsHeader = $response.Headers["X-Amzn-Trace-Id"]

        if ([string]::IsNullOrEmpty($awsHeader)) {
            Write-Host "  ✅ X-Amzn-Trace-Id: HIDDEN (PASS)" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  X-Amzn-Trace-Id: Present (ELB level)" -ForegroundColor Yellow
        }

        # Verify security headers are still present
        $xFrameOptions = $response.Headers["X-Frame-Options"]
        $xContentType = $response.Headers["X-Content-Type-Options"]

        Write-Host "  Security Headers Check:" -ForegroundColor Cyan

        if ($xFrameOptions) {
            Write-Host "    ✅ X-Frame-Options: $xFrameOptions" -ForegroundColor Green
        } else {
            Write-Host "    ⚠️  X-Frame-Options: Missing" -ForegroundColor Yellow
        }

        if ($xContentType) {
            Write-Host "    ✅ X-Content-Type-Options: $xContentType" -ForegroundColor Green
        } else {
            Write-Host "    ⚠️  X-Content-Type-Options: Missing" -ForegroundColor Yellow
        }

        Write-Host ""
        return $true

    } catch {
        Write-Host "  ⚠️  Error accessing endpoint: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host ""
        return $false
    }
}

# Function to display all headers
function Show-AllHeaders {
    param (
        [string]$url
    )

    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "   Complete Header Analysis" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""

    try {
        $response = Invoke-WebRequest -Uri $url -Method HEAD -UseBasicParsing -ErrorAction Stop

        Write-Host "HTTP Status: $($response.StatusCode)" -ForegroundColor White
        Write-Host ""
        Write-Host "All Response Headers:" -ForegroundColor White
        Write-Host "---------------------" -ForegroundColor White

        foreach ($header in $response.Headers.Keys) {
            $value = $response.Headers[$header]
            Write-Host "  $header : $value" -ForegroundColor Gray
        }

        Write-Host ""

    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Run tests
Write-Host "Starting Security Tests..." -ForegroundColor Cyan
Write-Host ""

$passCount = 0
$failCount = 0

foreach ($endpoint in $endpoints) {
    $result = Test-ServerHeaders -url $baseUrl -endpoint $endpoint
    if ($result) {
        $passCount++
    } else {
        $failCount++
    }
}

# Summary
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Test Summary" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Endpoints Tested: $($endpoints.Count)" -ForegroundColor White
Write-Host "Successful Tests: $passCount" -ForegroundColor Green
Write-Host "Failed Tests: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Show detailed headers for first endpoint
Write-Host "Detailed Header Analysis for: $($endpoints[0])" -ForegroundColor Cyan
Show-AllHeaders -url "$baseUrl$($endpoints[0])"

# Final recommendations
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Recommendations" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "✅ All tests passed!" -ForegroundColor Green
    Write-Host "✅ Server information disclosure is properly mitigated." -ForegroundColor Green
    Write-Host "✅ Security headers are in place." -ForegroundColor Green
} else {
    Write-Host "⚠️  Some tests failed. Please review:" -ForegroundColor Yellow
    Write-Host "   - Ensure ServerHeaderFilter is deployed and active" -ForegroundColor White
    Write-Host "   - Verify application.properties has server.server-header=" -ForegroundColor White
    Write-Host "   - Check application logs for filter initialization" -ForegroundColor White
    Write-Host "   - Restart the application if recently deployed" -ForegroundColor White
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Additional curl command examples
Write-Host "Manual Testing Commands:" -ForegroundColor Cyan
Write-Host "------------------------" -ForegroundColor Cyan
Write-Host ""
Write-Host "Using curl (if installed):" -ForegroundColor White
Write-Host "  curl -I $baseUrl/events/login" -ForegroundColor Gray
Write-Host ""
Write-Host "Using PowerShell:" -ForegroundColor White
Write-Host "  Invoke-WebRequest -Uri '$baseUrl/events/login' -Method HEAD | Select-Object -ExpandProperty Headers" -ForegroundColor Gray
Write-Host ""

# Export results to file
$timestamp = Get-Date -Format "yyyy-MM-dd_HHmmss"
$outputFile = "server-disclosure-test-results-$timestamp.txt"

$results = @"
Server Information Disclosure Test Results
Date: $(Get-Date)
Base URL: $baseUrl

Test Summary:
- Endpoints Tested: $($endpoints.Count)
- Successful Tests: $passCount
- Failed Tests: $failCount

Status: $(if ($failCount -eq 0) { "✅ PASS - All tests successful" } else { "❌ FAIL - $failCount test(s) failed" })

Details:
Run the script to see detailed test results.

"@

$results | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "Results saved to: $outputFile" -ForegroundColor Green
Write-Host ""


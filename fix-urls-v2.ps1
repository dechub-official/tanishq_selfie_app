$files = @(
    "src\main\resources\static\globalPage\celebrate.html",
    "src\main\resources\static_backup\globalPage\celebrate.html",
    "src\main\resources\static\globalPage\globalAssets\celebrate.html",
    "src\main\resources\static\checklist\verify.html",
    "src\main\resources\static_backup\checklist\verify.html"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8

        # Fix URLs - only change the href value, keep all other attributes
        $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/checklist"', 'href="/checklist"'
        $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/selfie"', 'href="/selfie"'
        $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/events"', 'href="/events"'

        $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/checklist"', 'href="/checklist"'
        $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/selfie"', 'href="/selfie"'
        $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/events"', 'href="/events"'

        # Fix API endpoints
        $content = $content -replace "xhr\.open\('POST',\s*'https://celebrations\.tanishq\.co\.in/tanishq/selfie/brideImage'", "xhr.open('POST', '/tanishq/selfie/brideImage'"
        $content = $content -replace "xhr\.open\('POST',\s*'https://celebrationsite-preprod\.tanishq\.co\.in/tanishq/selfie/brideImage'", "xhr.open('POST', '/tanishq/selfie/brideImage'"

        Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
        Write-Host "Fixed: $file"
    } else {
        Write-Host "Skipped (not found): $file"
    }
}

Write-Host ""
Write-Host "All files updated successfully!"
Write-Host "URLs changed from absolute to relative paths."


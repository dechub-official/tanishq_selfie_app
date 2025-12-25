$files = @(
    "src\main\resources\static\globalPage\celebrate.html",
    "src\main\resources\static_backup\globalPage\celebrate.html",
    "src\main\resources\static\globalPage\globalAssets\celebrate.html"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw -Encoding UTF8

    # Remove duplicate href attributes and fix URLs
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/checklist"\s+target="_blank"', 'href="/checklist"'
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/selfie"\s+target="_blank"', 'href="/selfie"'
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/events"\s+target="_blank"', 'href="/events"'

    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/checklist"\s+target="_blank"', 'href="/checklist"'
    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/selfie"\s+target="_blank"', 'href="/selfie"'
    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/events"\s+target="_blank"', 'href="/events"'

    # Fix ones without target="_blank"
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/checklist"', 'href="/checklist"'
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/selfie"', 'href="/selfie"'
    $content = $content -replace 'href="https://celebrations\.tanishq\.co\.in/events"', 'href="/events"'

    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/checklist"', 'href="/checklist"'
    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/selfie"', 'href="/selfie"'
    $content = $content -replace 'href="https://celebrationsite-preprod\.tanishq\.co\.in/events"', 'href="/events"'

    # Remove any duplicate href attributes that may exist
    $content = $content -replace 'href="[^"]*"\s+href="([^"]*)"', 'href="$1"'

    # Remove target="_blank" if URL is relative
    $content = $content -replace '(href="/[^"]*")\s+target="_blank"', '$1'

    # Fix API endpoints
    $content = $content -replace "xhr\.open\('POST',\s*'https://celebrations\.tanishq\.co\.in/tanishq/selfie/brideImage'", "xhr.open('POST', '/tanishq/selfie/brideImage'"
    $content = $content -replace "xhr\.open\('POST',\s*'https://celebrationsite-preprod\.tanishq\.co\.in/tanishq/selfie/brideImage'", "xhr.open('POST', '/tanishq/selfie/brideImage'"

    Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
    Write-Host "Fixed: $file"
}

Write-Host "All files updated successfully!"


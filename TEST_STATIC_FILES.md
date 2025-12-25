# Test Static Files Accessibility

Run these commands on your server to diagnose the static files issue:

```bash
# Test 1: Check if JS file is accessible
curl -I http://localhost:3000/static/assets/index-CLJQELnM.js

# Expected: HTTP 200 OK
# If 404: File path is wrong

# Test 2: Check alternative path (nested static)
curl -I http://localhost:3000/static/static/assets/index-CLJQELnM.js

# Test 3: Check CSS file
curl -I http://localhost:3000/static/assets/index-CjU3bZCB.css

# Test 4: Check alternative CSS path
curl -I http://localhost:3000/static/static/assets/index-CjU3bZCB.css

# Test 5: List what's actually in static directory
curl http://localhost:3000/static/ 

# Test 6: Check if assets directory is accessible
curl http://localhost:3000/static/assets/
```

## Expected Results:

If you get **404 Not Found** for `/static/assets/` but **200 OK** for `/static/static/assets/`, then we have a path mismatch issue.

## Quick Fix:

The issue is that the HTML file references `/static/assets/` but the files are actually at `/static/static/assets/`.

This is likely because:
1. The React build output has files in `static/assets/`
2. But Spring Boot serves from `src/main/resources/static/` which becomes the web root
3. So `static/static/assets/` in the file system becomes `/static/assets/` in the URL

We need to either:
- **Option A**: Fix the HTML to reference the correct path
- **Option B**: Move the assets to the correct location
- **Option C**: Configure Spring to serve static files correctly


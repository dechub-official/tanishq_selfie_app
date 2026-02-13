# How to Fix Reset Password Feature - Frontend Instructions

## Problem
You implemented:
1. ✅ Backend with confirmPassword parameter - **WORKING**
2. ✅ Backend with password hint endpoint - **WORKING**  
3. ❌ Frontend form doesn't include confirmPassword field - **NOT REFLECTING**
4. ❌ Frontend doesn't show password hint - **NOT REFLECTING**

## Root Cause
The React frontend source code needs to be updated to call the backend with the correct parameters and display the password hint.

---

## Step-by-Step Fix Instructions

### Step 1: Locate React Source Code

The React source should be at:
```
C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events\
```

Open PowerShell and run:
```powershell
cd C:\JAVA\celebration-preprod-latest\celeb
dir
```

**Check if you see `Event_Frontend_Preprod` folder.**

---

### Step 2: Find the Reset Password Component

Navigate to the React project:
```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events
```

Search for reset password files:
```powershell
# Search in src folder
Get-ChildItem -Path "src" -Recurse -Include "*.jsx","*.js","*.tsx","*.ts" | Select-String -Pattern "Reset Your Password" -List | Select-Object Path
```

Or manually look in these common locations:
- `src/pages/ResetPassword.jsx`
- `src/components/ResetPassword.jsx`
- `src/views/ResetPassword.jsx`
- `src/App.jsx` (might have inline component)

---

### Step 3: Update the Reset Password Component

Once you find the file, make these changes:

#### A. Add State for Confirm Password and Password Hint

Find the useState section and add:
```javascript
const [passwordData, setPasswordData] = useState({
  code: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''  // ADD THIS LINE
});

const [passwordHint, setPasswordHint] = useState('');  // ADD THIS LINE
```

#### B. Add Function to Fetch Password Hint

Add this function before the handleSubmit function:
```javascript
const fetchPasswordHint = async (storeCode) => {
  if (!storeCode || storeCode.length < 3) {
    setPasswordHint('');
    return;
  }
  
  try {
    const response = await axios.get(`${baseURL}/events/getPasswordHint`, {
      params: { storeCode }
    });
    
    if (response.data.status) {
      setPasswordHint(response.data.result);
    } else {
      setPasswordHint('');
    }
  } catch (error) {
    console.error('Error fetching password hint:', error);
    setPasswordHint('');
  }
};
```

#### C. Add useEffect to Fetch Hint When Store Code Changes

Add this after your other useEffects:
```javascript
useEffect(() => {
  if (passwordData.code && passwordData.code.length >= 3) {
    fetchPasswordHint(passwordData.code);
  } else {
    setPasswordHint('');
  }
}, [passwordData.code]);
```

#### D. Update the Form JSX

**Find the Store Code Input** and update its onChange:
```jsx
<input
  type="text"
  value={passwordData.code}
  placeholder="Enter Your Store Code (All Capital)"
  onChange={(e) => setPasswordData({...passwordData, code: e.target.value.toUpperCase()})}
  className="px-5 placeholder:text-[#9E9E9E] py-3 rounded-md border-2 shadow-sm bg-[#f7f8f97c] w-[80%]"
/>
```

**Find the Old Password Input** and add hint below it:
```jsx
<div className="box flex justify-center my-10 items-center flex-col">
  <input
    type="password"
    value={passwordData.oldPassword}
    placeholder="Type your old password"
    onChange={(e) => setPasswordData({...passwordData, oldPassword: e.target.value})}
    className="px-5 placeholder:text-[#9E9E9E] py-3 rounded-md border-2 shadow-sm bg-[#f7f8f97c] w-[80%]"
  />
  {/* ADD THIS PASSWORD HINT */}
  {passwordHint && (
    <p className="text-xs text-gray-600 mt-2 w-[80%] px-2">
      💡 Hint: Your current password is <span className="font-semibold">{passwordHint}</span>
    </p>
  )}
</div>
```

**After New Password Input, Add Confirm Password Field:**
```jsx
{/* ADD THIS NEW FIELD */}
<div className="box flex justify-center my-10 items-center flex-col">
  <input
    type="password"
    value={passwordData.confirmPassword}
    placeholder="Confirm new password"
    onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
    className="px-5 placeholder:text-[#9E9E9E] py-3 rounded-md border-2 shadow-sm bg-[#f7f8f97c] w-[80%]"
  />
</div>
```

#### E. Update the API Call in handleSubmit/handleReset Function

Find your submit/reset function and update it:

**OLD CODE (what you have now):**
```javascript
const response = await axios.post(
  `${baseURL}/events/changePassword?storeCode=${passwordData.code}&oldPassword=${passwordData.oldPassword}&newPassword=${passwordData.newPassword}`
);
```

**NEW CODE (what it should be):**
```javascript
const response = await axios.post(
  `${baseURL}/events/changePassword`,
  null,
  {
    params: {
      storeCode: passwordData.code,
      oldPassword: passwordData.oldPassword,
      newPassword: passwordData.newPassword,
      confirmPassword: passwordData.confirmPassword  // ADD THIS
    }
  }
);
```

#### F. Add Client-Side Validation

Before making the API call, add validation:
```javascript
const handleResetPassword = async () => {
  // Clear any previous errors
  setError('');
  
  // Validate all fields
  if (!passwordData.code || !passwordData.oldPassword || 
      !passwordData.newPassword || !passwordData.confirmPassword) {
    setError('All fields are required');
    return;
  }
  
  // Check if passwords match
  if (passwordData.newPassword !== passwordData.confirmPassword) {
    setError('New password and confirm password do not match');
    return;
  }
  
  // Check password strength (optional)
  if (passwordData.newPassword.length < 6) {
    setError('New password must be at least 6 characters long');
    return;
  }
  
  setLoading(true);
  
  try {
    const response = await axios.post(
      `${baseURL}/events/changePassword`,
      null,
      {
        params: {
          storeCode: passwordData.code,
          oldPassword: passwordData.oldPassword,
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.confirmPassword
        }
      }
    );
    
    if (response.data.status) {
      setSuccess('Password changed successfully! Redirecting to login...');
      setTimeout(() => {
        navigate('/'); // or navigate('/login')
      }, 2000);
    } else {
      setError(response.data.message || 'Failed to change password');
    }
  } catch (error) {
    console.error('Error changing password:', error);
    setError('Error changing password. Please try again.');
  } finally {
    setLoading(false);
  }
};
```

---

### Step 4: Rebuild the Frontend

After making changes, rebuild the React app:

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events

# Install dependencies (if needed)
npm install

# Build for production
npm run build
```

This will create/update the `dist` folder with compiled files.

---

### Step 5: Rebuild the Backend WAR

The Maven build will automatically copy the frontend dist files to the WAR:

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean and build WAR file
mvn clean package -Ppreprod
```

The WAR file will be created at:
```
target/tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war
```

---

### Step 6: Deploy and Test

1. **Stop your application server** (Tomcat/etc.)

2. **Deploy the new WAR file:**
   ```powershell
   # Copy to Tomcat webapps (adjust path as needed)
   Copy-Item "target/tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war" -Destination "C:\path\to\tomcat\webapps\events.war"
   ```

3. **Start the application server**

4. **Test the reset password page:**
   - Navigate to: `https://celebrationsite-preprod.tanishq.co.in/events/reset-password`
   - Enter a store code (e.g., "EAST1")
   - ✅ Password hint should appear below old password field
   - Enter old password
   - Enter new password
   - ✅ Confirm password field should be visible
   - Enter confirm password (same as new password)
   - Click Reset button
   - ✅ Should show success message if passwords match and old password is correct
   - ✅ Should show error if passwords don't match

---

## Testing Checklist

After deployment, verify:

- [ ] Navigate to `/events/reset-password` URL loads correctly
- [ ] Store code field accepts input
- [ ] Password hint appears after entering store code (3+ characters)
- [ ] Password hint shows correct current password
- [ ] Old password field works
- [ ] New password field works  
- [ ] Confirm password field is visible and works
- [ ] Error message if new password ≠ confirm password
- [ ] Error message if old password is wrong
- [ ] Error message if store code not found
- [ ] Success message on successful password change
- [ ] Can login with new password after reset
- [ ] Old password no longer works after reset

---

## Troubleshooting

### Frontend not found?

If `Event_Frontend_Preprod` folder doesn't exist:

**Option 1: Check other locations**
```powershell
Get-ChildItem -Path "C:\JAVA\celebration-preprod-latest" -Recurse -Filter "package.json" | Select-Object DirectoryName
```

**Option 2: Check Git repository**
```powershell
cd C:\JAVA\celebration-preprod-latest\celeb
git log --all --full-history --name-only -- "*ResetPassword*"
```

**Option 3: Extract from built files (not recommended)**
The minified JavaScript is at:
```
src/main/resources/static/assets/index-Bl1_SFlI.js
```
But this is minified and very hard to modify.

### Build fails?

**Check Node.js version:**
```powershell
node --version
npm --version
```

**Clear node_modules and reinstall:**
```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
npm install
npm run build
```

### Maven build fails?

**Check if dist folder was created:**
```powershell
Test-Path "C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events\dist"
```

**Manual copy if needed:**
```powershell
Copy-Item -Path "C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events\dist\*" `
  -Destination "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static" `
  -Recurse -Force
```

---

## Quick Reference: API Endpoints

### Change Password
```
POST /events/changePassword
Parameters: storeCode, oldPassword, newPassword, confirmPassword
```

### Get Password Hint
```
GET /events/getPasswordHint
Parameters: storeCode
Returns: { status: true, result: "actual_password" }
```

---

## Need Help?

If React source is truly not available, you may need to:
1. Contact the original developer
2. Check version control (Git) history
3. Recreate the component from scratch (time-consuming but ensures quality)

The backend is ready and working. Once frontend is updated, everything will work perfectly! 🎉


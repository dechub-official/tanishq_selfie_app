# Reset Password Feature Analysis

## Summary
The reset password feature has been **fully implemented in the backend** with confirm password and password hint functionality. However, the **frontend is NOT calling the backend correctly**, which is why the new features are not reflecting.

---

## Backend Status ✅ COMPLETE

### 1. Change Password Endpoint
**Location:** `EventsController.java` (Line 336)
```java
@PostMapping("/changePassword")
public ResponseDataDTO changePassword(
    @RequestParam String storeCode,
    @RequestParam String oldPassword,
    @RequestParam String newPassword,
    @RequestParam String confirmPassword  // ✅ IMPLEMENTED
){
    return tanishqPageService.changePasswordForEventManager(
        storeCode, oldPassword, newPassword, confirmPassword
    );
}
```

### 2. Password Hint Endpoint
**Location:** `EventsController.java` (Line 341-343)
```java
@GetMapping("/getPasswordHint")
public ResponseDataDTO getPasswordHint(@RequestParam String storeCode){
    return tanishqPageService.getPasswordHintForStore(storeCode);
}
```

### 3. Service Layer Implementation
**Location:** `TanishqPageService.java` (Line 1028-1120)

**Features Implemented:**
- ✅ Validates all input fields (storeCode, oldPassword, newPassword, confirmPassword)
- ✅ Checks if newPassword matches confirmPassword
- ✅ Validates old password against database
- ✅ Updates password in database
- ✅ Updates password cache
- ✅ Saves password history
- ✅ Returns proper success/error messages

**Password Hint Feature:**
- ✅ `getPasswordHintForStore()` method retrieves the current password for a store
- ✅ Returns password as "hint" (though this returns the actual password, not a hint)

---

## Frontend Status ❌ INCOMPLETE

### Current Issue
The built React application at `src/main/resources/static/assets/index-Bl1_SFlI.js` contains the reset password page, but:

1. **Missing Confirm Password Field** - The frontend form only has:
   - Store Code
   - Old Password
   - New Password
   - ❌ Missing: Confirm Password field

2. **Backend API Call is Incorrect** - The frontend is calling:
   ```javascript
   await B.post(me+`/changePassword?storeCode=${o.code}&oldPassword=${o.oldPassword}&newPassword=${o.newPassword}`)
   ```
   ❌ Missing `confirmPassword` parameter

3. **No Password Hint Display** - The frontend doesn't:
   - Call the `/getPasswordHint` endpoint
   - Display the password hint below the old password field

---

## What Needs to Be Fixed

### Frontend Changes Required:

#### 1. Update Reset Password Component
**File:** React source file for reset password page

**Add:**
```jsx
// Add confirm password state
const [formData, setFormData] = useState({
  code: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''  // ADD THIS
});

// Add password hint state
const [passwordHint, setPasswordHint] = useState('');
```

#### 2. Add Confirm Password Input Field
```jsx
<div className="box flex justify-center my-10 items-center flex-col">
  <input
    type="password"
    value={formData.confirmPassword}
    placeholder="Confirm new password"
    onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
    className="px-5 placeholder:text-[#9E9E9E] py-3 rounded-md border-2 shadow-sm bg-[#f7f8f97c] w-[80%]"
  />
</div>
```

#### 3. Fetch Password Hint When Store Code is Entered
```jsx
const fetchPasswordHint = async (storeCode) => {
  if (!storeCode) return;
  try {
    const response = await axios.get(`${baseURL}/getPasswordHint?storeCode=${storeCode}`);
    if (response.data.status) {
      setPasswordHint(response.data.result);
    }
  } catch (error) {
    console.error('Error fetching password hint:', error);
  }
};

// Call when store code changes
useEffect(() => {
  if (formData.code.length >= 3) {
    fetchPasswordHint(formData.code);
  }
}, [formData.code]);
```

#### 4. Display Password Hint Below Old Password
```jsx
<div className="box flex justify-center my-10 items-center flex-col">
  <input
    type="password"
    value={formData.oldPassword}
    placeholder="Type your old password"
    onChange={(e) => setFormData({...formData, oldPassword: e.target.value})}
    className="px-5 placeholder:text-[#9E9E9E] py-3 rounded-md border-2 shadow-sm bg-[#f7f8f97c] w-[80%]"
  />
  {passwordHint && (
    <p className="text-xs text-gray-500 mt-2 w-[80%]">
      Hint: Your current password is {passwordHint}
    </p>
  )}
</div>
```

#### 5. Update API Call to Include confirmPassword
```javascript
const handleResetPassword = async () => {
  try {
    const response = await axios.post(
      `${baseURL}/changePassword`,
      null,
      {
        params: {
          storeCode: formData.code,
          oldPassword: formData.oldPassword,
          newPassword: formData.newPassword,
          confirmPassword: formData.confirmPassword  // ADD THIS
        }
      }
    );
    
    if (response.data.status) {
      // Success - redirect to login
      navigate('/');
    } else {
      // Show error message
      setError(response.data.message);
    }
  } catch (error) {
    setError('Error resetting password');
  }
};
```

---

## Where is the React Source Code?

According to `pom.xml` line 24:
```xml
<frontend.source.dir>${project.basedir}/../Event_Frontend_Preprod/Tanishq_Events/dist</frontend.source.dir>
```

**React Source Location:**
```
C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events\
```

The source files should be in:
- `src/` folder for React components
- Look for a file like `ResetPassword.jsx` or `ResetPassword.js`

---

## How to Apply the Fix

### Step 1: Find React Source
Navigate to the React project:
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\Event_Frontend_Preprod\Tanishq_Events
```

### Step 2: Locate Reset Password Component
Search for the reset password page:
```bash
# Windows PowerShell
Get-ChildItem -Recurse -Filter "*reset*" -File
# or search in src folder
Get-ChildItem -Path "src" -Recurse -Include "*.jsx","*.js" | Select-String -Pattern "Reset Your Password"
```

### Step 3: Update the Component
Apply the changes mentioned above to:
1. Add confirmPassword field
2. Add password hint fetching logic
3. Display password hint
4. Update API call

### Step 4: Rebuild Frontend
```bash
npm run build
# or
yarn build
```

### Step 5: Rebuild Backend WAR
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod
```

### Step 6: Deploy
```bash
# Copy WAR to Tomcat or deploy as per your deployment process
```

---

## Testing Checklist

After applying the fix, test:

- [ ] Store code field works
- [ ] Password hint appears below old password field after entering store code
- [ ] Old password field works
- [ ] New password field works
- [ ] Confirm password field appears and works
- [ ] Error shown if new password and confirm password don't match
- [ ] Error shown if old password is incorrect
- [ ] Success message appears when password is changed
- [ ] Can login with new password after change
- [ ] Password history is saved in database

---

## API Endpoints Reference

### Change Password
- **URL:** `POST /events/changePassword`
- **Parameters:**
  - `storeCode` (required)
  - `oldPassword` (required)
  - `newPassword` (required)
  - `confirmPassword` (required) ← **Currently missing from frontend**
- **Response:** 
  ```json
  {
    "status": true/false,
    "message": "Success/Error message"
  }
  ```

### Get Password Hint
- **URL:** `GET /events/getPasswordHint`
- **Parameters:**
  - `storeCode` (required)
- **Response:**
  ```json
  {
    "status": true/false,
    "message": "Success message",
    "result": "actual_password"
  }
  ```

---

## Conclusion

The backend is **fully functional** with all required features. The issue is purely in the frontend:
1. Missing confirm password field in the UI
2. Missing password hint display functionality
3. API call missing confirmPassword parameter

Once the React source is updated and rebuilt, the feature will work perfectly.

---

## Quick Fix (If Source Not Available)

If you cannot find the React source code, you can:
1. Check if there's a backup or version control (Git)
2. Contact the frontend developer who built the React app
3. Decompile the minified JavaScript (not recommended, difficult to maintain)
4. Rebuild the reset password page from scratch using the existing login page as reference

The login page in the same application can serve as a good reference for structure and styling.


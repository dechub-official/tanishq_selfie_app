# FRONTEND FIXES - ACTION PLAN

## 📋 What You Need to Fix in Frontend

Your frontend React code has **4 critical issues** causing mobile submission failures:

---

## ❌ Issue 1: Missing or Incorrect API Implementation

### Problem
Your `UserForm` component imports:
```typescript
import { submitVideoMessage } from "@/lib/api";
```

But this function either doesn't exist or doesn't properly send the FormData to the backend.

### ✅ Fix: Create/Update API File

**Location**: Find your API file (likely `src/lib/api.ts` or similar in your React project)

**Add this function**:
```typescript
export interface SubmitVideoMessageParams {
  name: string;
  message: string;
  qrId: string;
}

export async function submitVideoMessage(
  params: SubmitVideoMessageParams,
  videoBlob: Blob | null
): Promise<any> {
  // Validate video blob exists
  if (!videoBlob) {
    throw new Error("No video recorded. Please record a video first.");
  }

  // Create FormData (required for file upload)
  const formData = new FormData();
  formData.append("name", params.name);
  formData.append("message", params.message);
  formData.append("video", videoBlob, "message.mp4");

  // Log for debugging
  console.log("Submitting to:", `https://celebrations.tanishq.co.in/greetings/${params.qrId}/upload`);
  console.log("Form data:", { name: params.name, message: params.message, videoSize: videoBlob.size });

  // Make API call
  const response = await fetch(
    `https://celebrations.tanishq.co.in/greetings/${params.qrId}/upload`,
    {
      method: "POST",
      body: formData,
      // DO NOT set Content-Type header - browser will set it automatically with boundary
    }
  );

  // Handle response
  if (!response.ok) {
    const errorText = await response.text();
    console.error("API Error Response:", errorText);
    throw new Error(errorText || "Failed to submit video");
  }

  const result = await response.text();
  console.log("API Success Response:", result);
  return result;
}
```

**Why this fix is needed**:
- FormData must be correctly structured with video blob
- Backend expects `name`, `message`, and `video` parameters
- Content-Type header must NOT be set manually (browser adds boundary automatically)

---

## ❌ Issue 2: No Error Display to User

### Problem
Your current code:
```typescript
catch (error) {
  console.error("Submission error:", error);
  setErrors((prev) => ({ ...prev, message: "Failed to submit. Please try again." }));
}
```

This only logs to console - **mobile users can't see console logs!**

### ✅ Fix: Add Visible Error Display

**In your UserForm component:**

**1. Add state for submit error:**
```typescript
const [submitError, setSubmitError] = useState<string>("");
```

**2. Add error display in JSX (add this BEFORE your `<form>` tag):**
```tsx
{/* Global submit error message */}
{submitError && (
  <div className="p-4 mb-4 bg-red-50 border border-red-200 rounded-lg">
    <p className="text-red-600 text-sm font-medium">{submitError}</p>
  </div>
)}
```

**3. Update handleSubmit to set the error:**
```typescript
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setSubmitError(""); // Clear previous errors

  if (!validateForm()) {
    return;
  }

  try {
    await mutation.mutateAsync(formData as FormValues);
    navigate("/video-message", {
      state: { name: formData.name, message: formData.message },
    });
  } catch (error) {
    console.error("Submission error:", error);
    // Show error to user (not just console)
    const errorMessage = error instanceof Error 
      ? error.message 
      : "Failed to submit. Please try again.";
    setSubmitError(errorMessage);
  }
};
```

**4. Clear error when user types:**
```typescript
const handleInputChange = (field: keyof FormValues, value: string) => {
  setFormData((prev) => ({ ...prev, [field]: value }));
  if (errors[field]) {
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  }
  // Clear submit error when user starts typing
  if (submitError) {
    setSubmitError("");
  }
};
```

**Why this fix is needed**:
- Users need to see what went wrong
- Console logs are invisible on mobile
- Clear feedback improves UX

---

## ❌ Issue 3: No Video Validation

### Problem
Your form doesn't check if video exists before submitting. This causes the submission to fail with unclear error.

### ✅ Fix: Add Video Validation

**Update your `validateForm` function:**

```typescript
const validateForm = (): boolean => {
  const newErrors: FormErrors = {};
  setSubmitError(""); // Clear previous submit error

  // VIDEO VALIDATION - Add this first!
  if (!recordedVideoBlob) {
    setSubmitError("Please record a video before submitting.");
    return false;
  }

  // Check video size (optional but recommended)
  if (recordedVideoBlob.size < 1024) { // Less than 1KB
    setSubmitError("Video appears to be invalid. Please record again.");
    return false;
  }

  // Name validation
  if (!formData.name.trim()) {
    newErrors.name = "Name is required";
  } else if (formData.name.trim().length < 2) {
    newErrors.name = "Name must be at least 2 characters";
  }

  // Message validation
  if (!formData.message.trim()) {
    newErrors.message = "Custom message is required";
  } else if (formData.message.trim().length < 10) {
    newErrors.message = "Message must be at least 10 characters";
  }

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};
```

**Why this fix is needed**:
- Prevents submission without video
- Catches invalid video blobs
- Provides clear error message to user

---

## ❌ Issue 4: Mobile Text Encoding Issues

### Problem
Mobile keyboards (especially non-English) can send garbled text like "wlkwehkwehrwe rweio" that causes issues.

### ✅ Fix: Add Text Sanitization

**Add this helper function at the top of your component:**

```typescript
// Text sanitization for mobile input
const sanitizeText = (text: string): string => {
  return text
    .trim()
    .replace(/[\u0000-\u0008\u000B-\u000C\u000E-\u001F\u007F]/g, '') // Remove control chars
    .replace(/\s+/g, ' '); // Normalize whitespace (multiple spaces to single)
};
```

**Update handleSubmit to sanitize before sending:**

```typescript
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setSubmitError("");

  if (!validateForm()) {
    return;
  }

  try {
    // Sanitize inputs before sending
    const sanitizedData = {
      name: sanitizeText(formData.name),
      message: sanitizeText(formData.message),
    };
    
    console.log("Original data:", formData);
    console.log("Sanitized data:", sanitizedData);
    
    await mutation.mutateAsync(sanitizedData as FormValues);
    
    navigate("/video-message", {
      state: { name: sanitizedData.name, message: sanitizedData.message },
    });
  } catch (error) {
    console.error("Submission error:", error);
    const errorMessage = error instanceof Error 
      ? error.message 
      : "Failed to submit. Please try again.";
    setSubmitError(errorMessage);
  }
};
```

**Why this fix is needed**:
- Mobile keyboards can insert control characters
- Multiple spaces can cause validation issues
- Backend also sanitizes, but frontend should too for better UX

---

## 📝 Complete Changes Summary

### Files to Create/Modify:

1. **API File** (e.g., `src/lib/api.ts`)
   - Add `submitVideoMessage` function with proper FormData handling

2. **UserForm Component** (your form file)
   - Add `submitError` state
   - Add error display in JSX
   - Add `sanitizeText` function
   - Update `validateForm` to check video
   - Update `handleSubmit` to sanitize and show errors
   - Update `handleInputChange` to clear errors

---

## 🧪 How to Test Each Fix

### Test 1: API Fix
```
1. Open browser console
2. Record video
3. Fill form
4. Submit
5. Check console for "Submitting to:" and "Form data:" logs
6. Should see successful API response
```

### Test 2: Error Display
```
1. Record video
2. Leave name empty
3. Submit
4. Should see red error box: "Name is required"
5. Start typing in name field
6. Error box should disappear
```

### Test 3: Video Validation
```
1. Don't record video
2. Fill name and message
3. Submit
4. Should see red error box: "Please record a video before submitting."
```

### Test 4: Text Sanitization
```
1. Record video
2. Type name with multiple spaces: "John    Doe"
3. Type message: "Hello     World"
4. Submit
5. Check console - sanitized data should show single spaces
6. Should submit successfully
```

---

## 🚀 Quick Implementation Checklist

- [ ] **Step 1**: Find your API file (or create `src/lib/api.ts`)
- [ ] **Step 2**: Add/update `submitVideoMessage` function
- [ ] **Step 3**: Find your UserForm component file
- [ ] **Step 4**: Add `submitError` state
- [ ] **Step 5**: Add error display JSX (red box)
- [ ] **Step 6**: Add `sanitizeText` function
- [ ] **Step 7**: Update `validateForm` to check video
- [ ] **Step 8**: Update `handleSubmit` with sanitization and error handling
- [ ] **Step 9**: Update `handleInputChange` to clear errors
- [ ] **Step 10**: Build and test on mobile

---

## 🔧 Build & Deploy Frontend

After making changes:

```bash
# If using Vite/React build
npm run build

# If using Maven build
mvn clean package -P preprod

# Deploy to server
# (copy build files or WAR to server)
```

---

## 🎯 Expected Result After All Fixes

✅ Desktop submission works (already works)  
✅ Mobile submission works  
✅ Garbled text gets cleaned automatically  
✅ User sees clear error messages (not just console)  
✅ Video validation prevents empty uploads  
✅ Form clears errors as user types  
✅ Server logs show detailed debugging info  

---

## 📞 If You Still Have Issues

1. **Check browser console** for API errors
2. **Check server logs** for backend errors  
3. **Test on WiFi first** (not mobile data)
4. **Try different mobile browsers**
5. **Clear browser cache**
6. **Verify video blob is created** (console.log recordedVideoBlob)

---

## 📚 Related Documentation

- Full frontend code example: `FRONTEND_FIXES_MOBILE_UPLOAD.md`
- Backend fixes: `MOBILE_GREETING_UPLOAD_COMPLETE_FIX.md`
- Quick summary: `COMPLETE_FIX_SUMMARY.md`


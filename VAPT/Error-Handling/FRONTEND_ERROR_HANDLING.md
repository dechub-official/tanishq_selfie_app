# Error Handling - Frontend Integration Guide

**Date**: March 4, 2026  
**Vulnerability Fix**: OWASP A05 - Security Misconfiguration  
**Impact**: MEDIUM - Error Response Format Changes  
**Action Required**: Update error handling in frontend code

---

## 🎯 Overview

The backend now implements centralized error handling with enhanced security. Error responses have been standardized and no longer expose stack traces or SQL errors. Frontend code needs to be updated to handle the new error response formats.

---

## 🔄 What Changed?

### Before (Insecure):
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "NullPointerException at line 1658",
  "trace": "java.lang.NullPointerException\n\tat com.dechub..."
}
```

### After (Secure):
```json
{
  "status": false,
  "message": "An unexpected error occurred. Please try again later.",
  "result": {
    "errorReference": "ERR-A3B7C9D2",
    "timestamp": "2026-03-04 14:23:15"
  }
}
```

---

## 📝 New Error Response Formats

### 1. Validation Errors (400 Bad Request)

**Response Format:**
```json
{
  "status": false,
  "message": "Validation failed",
  "result": {
    "phone": "Invalid phone number. Must be 10 digits starting with 6, 7, 8, or 9",
    "email": "Invalid email format",
    "brideName": "Name is required"
  }
}
```

**Frontend Handling:**
```javascript
function handleValidationError(errorResponse) {
  const errors = errorResponse.result;
  
  // Clear previous errors
  clearAllFieldErrors();
  
  // Display error for each field
  Object.keys(errors).forEach(fieldName => {
    const errorMessage = errors[fieldName];
    showFieldError(fieldName, errorMessage);
  });
  
  // Show summary message
  showErrorToast('Please correct the errors and try again');
}

// Example: Show error next to field
function showFieldError(fieldName, message) {
  const fieldElement = document.querySelector(`[name="${fieldName}"]`);
  const errorElement = document.getElementById(`${fieldName}-error`);
  
  if (fieldElement) {
    fieldElement.classList.add('is-invalid');
  }
  
  if (errorElement) {
    errorElement.textContent = message;
    errorElement.style.display = 'block';
  }
}
```

---

### 2. Database Errors (409 Conflict / 500 Internal Server Error)

**Response Format:**
```json
{
  "status": false,
  "message": "A database constraint was violated. Please check your input data.",
  "result": {
    "errorReference": "ERR-A3B7C9D2",
    "timestamp": "2026-03-04 14:23:15"
  }
}
```

**Frontend Handling:**
```javascript
function handleDatabaseError(errorResponse) {
  const message = errorResponse.message;
  const errorRef = errorResponse.result?.errorReference;
  const timestamp = errorResponse.result?.timestamp;
  
  // Show user-friendly message
  showErrorDialog({
    title: 'Unable to Save',
    message: message,
    details: errorRef ? `Error Reference: ${errorRef}\nTime: ${timestamp}` : null,
    actionText: 'Contact Support'
  });
  
  // Log for debugging (but don't show technical details to user)
  console.error(`Database error [${errorRef}]:`, message);
}
```

---

### 3. Generic Server Errors (500 Internal Server Error)

**Response Format:**
```json
{
  "status": false,
  "message": "An unexpected error occurred. Please try again later.",
  "result": {
    "errorReference": "ERR-F2D8E1A4",
    "timestamp": "2026-03-04 14:25:30"
  }
}
```

**Frontend Handling:**
```javascript
function handleServerError(errorResponse) {
  const errorRef = errorResponse.result?.errorReference;
  const timestamp = errorResponse.result?.timestamp;
  
  // Show generic error to user
  showErrorDialog({
    title: 'Something Went Wrong',
    message: 'An unexpected error occurred. Please try again later.',
    details: errorRef ? `If this problem persists, please contact support with this reference:\n${errorRef}` : null,
    actions: [
      { text: 'Try Again', onClick: () => retryLastAction() },
      { text: 'Contact Support', onClick: () => openSupportForm(errorRef) }
    ]
  });
  
  // Log for debugging
  console.error(`Server error [${errorRef}] at ${timestamp}`);
}
```

---

### 4. Access Denied (403 Forbidden)

**Response Format:**
```json
{
  "status": false,
  "message": "You do not have permission to access this resource"
}
```

**Frontend Handling:**
```javascript
function handleAccessDenied(errorResponse) {
  showErrorDialog({
    title: 'Access Denied',
    message: errorResponse.message,
    actionText: 'Go Back',
    onAction: () => window.history.back()
  });
}
```

---

### 5. Not Found (404)

**Response Format:**
```json
{
  "status": false,
  "message": "The requested resource was not found"
}
```

**Frontend Handling:**
```javascript
function handleNotFound(errorResponse) {
  showErrorToast('The requested resource was not found', 'warning');
  // Optionally redirect to home or previous page
  setTimeout(() => window.location.href = '/events', 2000);
}
```

---

### 6. File Size Exceeded (413 Payload Too Large)

**Response Format:**
```json
{
  "status": false,
  "message": "File size exceeds maximum allowed limit (100MB)"
}
```

**Frontend Handling:**
```javascript
function handleFileSizeError(errorResponse) {
  showErrorDialog({
    title: 'File Too Large',
    message: errorResponse.message,
    details: 'Please compress your file or choose a smaller file.',
    actionText: 'Choose Another File'
  });
}
```

---

## 🔧 Centralized Error Handler (Recommended)

Create a centralized error handler for all API calls:

```javascript
/**
 * Centralized error handler for all API responses
 * @param {Error} error - Axios error object
 * @param {Object} options - Additional options
 */
function handleApiError(error, options = {}) {
  // Network error (no response from server)
  if (!error.response) {
    showErrorDialog({
      title: 'Network Error',
      message: 'Unable to connect to the server. Please check your internet connection.',
      actionText: 'Retry',
      onAction: options.retryAction
    });
    console.error('Network error:', error.message);
    return;
  }
  
  const { status, data } = error.response;
  
  // Handle different status codes
  switch (status) {
    case 400: // Validation Error
      if (data.result && typeof data.result === 'object') {
        handleValidationError(data);
      } else {
        showErrorToast(data.message || 'Invalid request');
      }
      break;
      
    case 401: // Unauthorized
      handleUnauthorized(data);
      break;
      
    case 403: // Forbidden
      handleAccessDenied(data);
      break;
      
    case 404: // Not Found
      handleNotFound(data);
      break;
      
    case 409: // Conflict (Database constraint)
      handleDatabaseError(data);
      break;
      
    case 413: // File too large
      handleFileSizeError(data);
      break;
      
    case 415: // Unsupported Media Type
      showErrorToast('Unsupported file type. Please check the file format.');
      break;
      
    case 500: // Internal Server Error
    case 502: // Bad Gateway
    case 503: // Service Unavailable
      handleServerError(data);
      break;
      
    default:
      showErrorToast(data.message || 'An error occurred');
      console.error(`HTTP ${status}:`, data);
  }
  
  // Call custom error callback if provided
  if (options.onError) {
    options.onError(error);
  }
}

// Example usage with axios interceptor
axios.interceptors.response.use(
  response => response,
  error => {
    handleApiError(error);
    return Promise.reject(error);
  }
);
```

---

## 📦 Example: Form Submission with Error Handling

```javascript
async function submitEventForm(formData) {
  // Show loading state
  showLoadingSpinner();
  
  try {
    const response = await axios.post('/events/attendees', formData);
    
    // Success
    hideLoadingSpinner();
    showSuccessToast('Attendee registered successfully!');
    resetForm();
    
    return response.data;
    
  } catch (error) {
    hideLoadingSpinner();
    
    if (error.response?.status === 400) {
      // Validation errors - show field-level errors
      const errors = error.response.data.result;
      
      if (typeof errors === 'object') {
        Object.keys(errors).forEach(field => {
          showFieldError(field, errors[field]);
        });
        showErrorToast('Please correct the errors and try again');
      } else {
        showErrorToast(error.response.data.message);
      }
      
    } else if (error.response?.status === 409) {
      // Database constraint (e.g., duplicate entry)
      const errorRef = error.response.data.result?.errorReference;
      showErrorDialog({
        title: 'Duplicate Entry',
        message: error.response.data.message,
        details: errorRef ? `Error Reference: ${errorRef}` : null,
        actionText: 'OK'
      });
      
    } else if (error.response?.status === 500) {
      // Server error
      const errorRef = error.response.data.result?.errorReference;
      showErrorDialog({
        title: 'Server Error',
        message: 'An unexpected error occurred. Please try again later.',
        details: errorRef ? `Please quote this reference when contacting support: ${errorRef}` : null,
        actions: [
          { text: 'Try Again', onClick: () => submitEventForm(formData) },
          { text: 'Contact Support', onClick: () => openSupportForm(errorRef) }
        ]
      });
      
    } else if (!error.response) {
      // Network error
      showErrorDialog({
        title: 'Network Error',
        message: 'Unable to connect to the server. Please check your internet connection.',
        actionText: 'Retry',
        onAction: () => submitEventForm(formData)
      });
      
    } else {
      // Other errors
      showErrorToast('An error occurred. Please try again.');
    }
    
    // Log for debugging (without sensitive data)
    console.error('Form submission error:', {
      status: error.response?.status,
      errorRef: error.response?.data?.result?.errorReference,
      timestamp: error.response?.data?.result?.timestamp
    });
    
    throw error; // Re-throw if caller needs to handle it
  }
}
```

---

## 🎨 UI Components for Error Display

### 1. Toast Notification (For Simple Errors)

```javascript
function showErrorToast(message, duration = 5000) {
  const toast = document.createElement('div');
  toast.className = 'toast toast-error';
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: #dc3545;
    color: white;
    padding: 15px 20px;
    border-radius: 5px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
  `;
  
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

function showSuccessToast(message, duration = 3000) {
  const toast = document.createElement('div');
  toast.className = 'toast toast-success';
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: #28a745;
    color: white;
    padding: 15px 20px;
    border-radius: 5px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
  `;
  
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}
```

---

### 2. Error Dialog (For Important Errors)

```javascript
function showErrorDialog({ title, message, details, actions, actionText, onAction }) {
  // Remove existing dialog if any
  const existingDialog = document.getElementById('error-dialog');
  if (existingDialog) existingDialog.remove();
  
  // Create dialog
  const dialog = document.createElement('div');
  dialog.id = 'error-dialog';
  dialog.innerHTML = `
    <div class="modal-overlay">
      <div class="modal-content error-modal">
        <div class="modal-header">
          <h3>${title || 'Error'}</h3>
          <button class="close-btn" onclick="this.closest('.modal-overlay').remove()">×</button>
        </div>
        <div class="modal-body">
          <p class="error-message">${message}</p>
          ${details ? `<div class="error-details">${details}</div>` : ''}
        </div>
        <div class="modal-footer">
          ${actions ? 
            actions.map(action => 
              `<button class="btn btn-${action.type || 'primary'}" data-action="${action.text}">${action.text}</button>`
            ).join('') :
            `<button class="btn btn-primary" data-action="ok">${actionText || 'OK'}</button>`
          }
        </div>
      </div>
    </div>
  `;
  
  dialog.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 10000;
  `;
  
  document.body.appendChild(dialog);
  
  // Handle action buttons
  dialog.querySelectorAll('[data-action]').forEach(btn => {
    btn.addEventListener('click', () => {
      const actionName = btn.dataset.action;
      if (actions) {
        const action = actions.find(a => a.text === actionName);
        if (action?.onClick) action.onClick();
      } else if (onAction) {
        onAction();
      }
      dialog.remove();
    });
  });
}
```

---

### 3. Field Error Display

```javascript
function showFieldError(fieldName, message) {
  const fieldElement = document.querySelector(`[name="${fieldName}"]`);
  if (!fieldElement) return;
  
  // Add error class
  fieldElement.classList.add('is-invalid');
  
  // Find or create error message element
  let errorElement = document.getElementById(`${fieldName}-error`);
  if (!errorElement) {
    errorElement = document.createElement('div');
    errorElement.id = `${fieldName}-error`;
    errorElement.className = 'invalid-feedback';
    fieldElement.parentNode.appendChild(errorElement);
  }
  
  errorElement.textContent = message;
  errorElement.style.display = 'block';
  
  // Remove error on input change
  fieldElement.addEventListener('input', function clearError() {
    fieldElement.classList.remove('is-invalid');
    errorElement.style.display = 'none';
    fieldElement.removeEventListener('input', clearError);
  }, { once: true });
}

function clearAllFieldErrors() {
  document.querySelectorAll('.is-invalid').forEach(el => {
    el.classList.remove('is-invalid');
  });
  document.querySelectorAll('.invalid-feedback').forEach(el => {
    el.style.display = 'none';
  });
}
```

---

## 📊 Error Reference Tracking

When users report errors, they should provide the error reference ID:

```javascript
function openSupportForm(errorReference) {
  const supportUrl = '/contact-support';
  const subject = errorReference ? 
    `Error Report - ${errorReference}` : 
    'Error Report';
  
  // Pre-fill support form with error reference
  window.location.href = `${supportUrl}?subject=${encodeURIComponent(subject)}&ref=${errorReference || ''}`;
}

function copyErrorReference(errorRef) {
  navigator.clipboard.writeText(errorRef).then(() => {
    showSuccessToast('Error reference copied to clipboard');
  }).catch(err => {
    console.error('Failed to copy:', err);
    // Fallback: show in alert
    alert(`Error Reference: ${errorRef}`);
  });
}
```

---

## ✅ Migration Checklist

### For Each API Call in Your Application:

- [ ] Remove any code that displays stack traces
- [ ] Remove any code that shows raw error.message to users
- [ ] Update error handling to use status codes (400, 409, 500, etc.)
- [ ] Display field-level validation errors from `result` object
- [ ] Show error reference ID for server errors (500)
- [ ] Provide "Contact Support" option with error reference
- [ ] Log error details to console for debugging (but not to user)
- [ ] Test error scenarios to ensure proper handling

---

## 🧪 Testing Error Handling

### Test Validation Errors:
```javascript
// Submit form with invalid phone number
await axios.post('/events/attendees', {
  eventId: 'EVT123',
  name: 'Test User',
  phone: '12345'  // Invalid
});

// Expected: 400 status with validation error
// Should display: "Invalid phone number. Must be 10 digits starting with 6, 7, 8, or 9"
```

### Test Server Errors:
```javascript
// Trigger an error on server (e.g., invalid event code)
await axios.post('/events/updateSaleOfAnEvent', {
  eventCode: 'INVALID_CODE',
  sale: 5000
});

// Expected: 500 status with generic message and error reference
// Should display: Generic error message + error reference (ERR-XXXXXXXX)
```

### Test Network Errors:
```javascript
// Disconnect internet and try API call
await axios.post('/events/login', loginData);

// Expected: Network error (no response)
// Should display: "Unable to connect to the server"
```

---

## 📞 Support Integration

When users contact support with an error reference:

1. **User provides**: Error reference (e.g., `ERR-A3B7C9D2`)
2. **Support team**: Searches server logs for that reference
3. **Logs contain**: Full stack trace, SQL errors, and all technical details
4. **Support resolves**: Issue without exposing technical details to user

**Example Support Interaction:**

**User**: "I'm getting an error ERR-A3B7C9D2 when trying to register an attendee."

**Support** (after checking logs):
- Finds: `DataIntegrityViolationException: Duplicate entry for uk_phone_event`
- Responds: "It looks like this phone number is already registered for this event. Would you like to check if they're already on the list, or use a different phone number?"

**✅ Support helped without saying**: "You got a DataIntegrityViolationException..."

---

## 🎓 Best Practices

### DO ✅

- **Show user-friendly error messages**
- **Display validation errors next to fields**
- **Provide error reference IDs in dialogs**
- **Offer retry options for server errors**
- **Log errors to console for debugging**
- **Clear field errors when user starts typing**
- **Provide "Contact Support" option**

### DON'T ❌

- **Don't show stack traces to users**
- **Don't display raw error.message**
- **Don't show SQL errors or Java exceptions**
- **Don't hide all errors silently**
- **Don't make users guess what went wrong**
- **Don't forget to log for debugging**

---

## 🔗 Related Documentation

- Backend Implementation: `ERROR_HANDLING_IMPLEMENTATION.md`
- Input Validation Changes: `../Input-Validation/FRONTEND_CHANGES.md`
- API Documentation: (Update with error response formats)

---

## 📝 Summary

**What You Need to Do:**

1. ✅ Update all API error handlers to check `status` field in response
2. ✅ Display validation errors from `result` object
3. ✅ Show error reference IDs for server errors
4. ✅ Implement user-friendly error dialogs
5. ✅ Provide support contact option with error reference
6. ✅ Remove any code showing stack traces or raw errors
7. ✅ Test all error scenarios

**What Users Will See:**

- ✅ Clear, actionable error messages
- ✅ Field-level validation feedback
- ✅ Error reference IDs for support
- ✅ Professional error handling
- ❌ NO stack traces
- ❌ NO SQL errors
- ❌ NO technical jargon

---

**Implementation Date**: March 4, 2026  
**Status**: ✅ READY FOR FRONTEND INTEGRATION  
**Priority**: HIGH - Update before next deployment


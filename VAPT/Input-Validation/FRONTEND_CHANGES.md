# Input Validation - Frontend Changes Required

**Date**: March 4, 2026  
**Impact**: MEDIUM - API Request Format Changes  
**Action Required**: Update API calls for 11 endpoints

---

## Overview

The backend now has comprehensive input validation. Some endpoints changed from **form data/request parameters** to **JSON request body** for better validation support. Frontend needs to update these API calls.

---

## 🔴 CRITICAL: Endpoints Changed from @RequestParam to @RequestBody

These endpoints **MUST** be updated in the frontend to send JSON body instead of form data or query parameters.

---

### 1. Change Password API

**Endpoint**: `POST /events/changePassword`

#### OLD Format (Form Data / URL Parameters):
```javascript
// Using axios with params
axios.post('/events/changePassword', null, {
  params: {
    storeCode: 'STORE123',
    oldPassword: 'old123',
    newPassword: 'new123',
    confirmPassword: 'new123'
  }
});

// OR using FormData
const formData = new FormData();
formData.append('storeCode', 'STORE123');
formData.append('oldPassword', 'old123');
// ...
axios.post('/events/changePassword', formData);
```

#### NEW Format (JSON Body):
```javascript
axios.post('/events/changePassword', {
  storeCode: 'STORE123',
  oldPassword: 'old123',
  newPassword: 'new123',
  confirmPassword: 'new123'
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

---

### 2. ABM Login API

**Endpoint**: `POST /events/abm_login`

#### OLD Format:
```javascript
axios.post('/events/abm_login', {
  username: 'abm_user',
  password: 'pass123'
});
```

#### NEW Format (ALREADY CORRECT - Just ensure it's JSON):
```javascript
axios.post('/events/abm_login', {
  username: 'abm_user',
  password: 'pass123'
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: Username (3-100 chars), Password (4-100 chars) - both required

---

### 3. RBM Login API

**Endpoint**: `POST /events/rbm_login`

Same as ABM Login - ensure JSON format with Content-Type header.

---

### 4. CEE Login API

**Endpoint**: `POST /events/cee_login`

Same as ABM Login - ensure JSON format with Content-Type header.

---

### 5. Corporate Login API

**Endpoint**: `POST /events/corporate_login`

Same as ABM Login - ensure JSON format with Content-Type header.

---

### 6. Update Event Sale

**Endpoint**: `POST /events/updateSaleOfAnEvent`

#### OLD Format:
```javascript
axios.post('/events/updateSaleOfAnEvent', null, {
  params: {
    eventCode: 'EVT123',
    sale: '50000'
  }
});
```

#### NEW Format:
```javascript
axios.post('/events/updateSaleOfAnEvent', {
  eventCode: 'EVT123',
  sale: 50000  // Send as number, not string
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: eventCode required, sale must be number ≥ 0

---

### 7. Update Event Advance

**Endpoint**: `POST /events/updateAdvanceOfAnEvent`

#### NEW Format:
```javascript
axios.post('/events/updateAdvanceOfAnEvent', {
  eventCode: 'EVT123',
  advance: 20000  // Send as number, not string
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: eventCode required, advance must be number ≥ 0

---

### 8. Update Event GHS/RGA

**Endpoint**: `POST /events/updateGhsRgaOfAnEvent`

#### NEW Format:
```javascript
axios.post('/events/updateGhsRgaOfAnEvent', {
  eventCode: 'EVT123',
  ghsRga: 5  // Send as number, not string
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: eventCode required, ghsRga must be number ≥ 0

---

### 9. Update Event GMB

**Endpoint**: `POST /events/updateGmbOfAnEvent`

#### NEW Format:
```javascript
axios.post('/events/updateGmbOfAnEvent', {
  eventCode: 'EVT123',
  gmb: 3  // Send as number, not string
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: eventCode required, gmb must be number ≥ 0

---

### 10. Get Invited Members

**Endpoint**: `POST /events/getinvitedmember`

#### OLD Format:
```javascript
axios.post('/events/getinvitedmember', null, {
  params: { eventCode: 'EVT123' }
});
```

#### NEW Format:
```javascript
axios.post('/events/getinvitedmember', {
  eventCode: 'EVT123'
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

---

### 11. Bride Details Submission

**Endpoint**: `POST /tanishq/selfie/brideDetails`

#### OLD Format (Form Data):
```javascript
const formData = new FormData();
formData.append('brideType', 'Bride');
formData.append('brideEvent', 'Wedding');
formData.append('brideName', 'Jane Doe');
formData.append('phone', '9876543210');
formData.append('date', '2026-04-01');
formData.append('email', 'jane@example.com');
formData.append('zipCode', '560001');

axios.post('/tanishq/selfie/brideDetails', formData);
```

#### NEW Format (JSON):
```javascript
axios.post('/tanishq/selfie/brideDetails', {
  brideType: 'Bride',
  brideEvent: 'Wedding',
  brideName: 'Jane Doe',
  phone: '9876543210',
  date: '2026-04-01',
  email: 'jane@example.com',
  zipCode: '560001'
}, {
  headers: { 'Content-Type': 'application/json' }
});
```

**Validation**: All fields required except zipCode. Phone must be 10 digits (6-9 start), email must be valid format.

---

## ✅ No Changes Required (Already Use JSON Body)

These endpoints already use JSON and just need validation awareness:

- `POST /events/login` - Already uses LoginDTO
- `POST /events/getevents` - Already uses storeCodeDataDTO
- `POST /rivaah/shareDetails` - Already uses RivaahDTO

---

## 🟢 No Changes Required (Still Use Form Data)

These endpoints still accept form data/multipart:

- `POST /events/upload` - Multipart form (file upload)
- `POST /events/attendees` - Request parameters
- `POST /tanishq/selfie/upload` - Multipart form (file upload)
- `POST /tanishq/selfie/brideImage` - Multipart form (file upload)

---

## Client-Side Validation (Keep Existing)

**IMPORTANT**: Keep existing client-side validation for better UX. Backend validation is an additional security layer, not a replacement.

### Best Practice:
```javascript
function validatePhone(phone) {
  const pattern = /^[6-9]\d{9}$/;
  return pattern.test(phone);
}

async function submitForm(data) {
  // Client-side validation (fast feedback)
  if (!validatePhone(data.phone)) {
    showError('Invalid phone number');
    return;
  }
  
  try {
    // Backend will also validate (security)
    const response = await axios.post('/api/endpoint', data);
    showSuccess('Data saved successfully');
  } catch (error) {
    if (error.response?.status === 400) {
      // Show backend validation errors
      const errors = error.response.data.result;
      showValidationErrors(errors);
    }
  }
}
```

---

## Error Handling

Update your error handling to display backend validation errors:

```javascript
axios.post('/events/login', loginData)
  .then(response => {
    // Success
    console.log('Login successful');
  })
  .catch(error => {
    if (error.response?.status === 400) {
      // Validation error
      const validationErrors = error.response.data.result;
      
      if (typeof validationErrors === 'object') {
        // Multiple field errors
        Object.keys(validationErrors).forEach(field => {
          console.error(`${field}: ${validationErrors[field]}`);
          // Display error next to the field in UI
          showFieldError(field, validationErrors[field]);
        });
      } else {
        // Single error message
        console.error(error.response.data.message);
        showError(error.response.data.message);
      }
    } else if (error.response?.status === 401) {
      // Authentication error
      showError('Invalid credentials');
    } else {
      // Other errors
      showError('An error occurred. Please try again.');
    }
  });
```

---

## Quick Checklist for Frontend Developers

### For Each Changed Endpoint:

1. ✅ Change request from query params/form data to JSON body
2. ✅ Add `Content-Type: application/json` header
3. ✅ Send numbers as numbers (not strings) for numeric fields
4. ✅ Handle 400 Bad Request validation errors
5. ✅ Display field-level error messages to users
6. ✅ Keep existing client-side validation for UX
7. ✅ Test with invalid inputs to verify error handling

---

## Testing Changes

### Test Invalid Phone:
```javascript
// Should return 400 with validation error
axios.post('/events/attendees', null, {
  params: {
    eventId: 'EVT123',
    name: 'John Doe',
    phone: '123456'  // Invalid - too short
  }
});
```

### Test Missing Required Field:
```javascript
// Should return 400 with validation error
axios.post('/events/login', {
  code: '',  // Empty - validation should fail
  password: 'test123'
});
```

### Test Oversized Input:
```javascript
// Should return 400 with validation error
axios.post('/events/login', {
  code: 'A'.repeat(100),  // Too long - exceeds 50 chars
  password: 'test123'
});
```

---

## Summary of Required Changes

| Endpoint | Change Type | Priority |
|----------|-------------|----------|
| `/events/changePassword` | @RequestParam → JSON Body | HIGH |
| `/events/updateSaleOfAnEvent` | @RequestParam → JSON Body | HIGH |
| `/events/updateAdvanceOfAnEvent` | @RequestParam → JSON Body | HIGH |
| `/events/updateGhsRgaOfAnEvent` | @RequestParam → JSON Body | HIGH |
| `/events/updateGmbOfAnEvent` | @RequestParam → JSON Body | HIGH |
| `/events/getinvitedmember` | @RequestParam → JSON Body | MEDIUM |
| `/tanishq/selfie/brideDetails` | Form Data → JSON Body | HIGH |
| `/events/abm_login` | Verify JSON format | LOW |
| `/events/rbm_login` | Verify JSON format | LOW |
| `/events/cee_login` | Verify JSON format | LOW |
| `/events/corporate_login` | Verify JSON format | LOW |

**Total Changes Required**: 11 endpoints

---

## Validation Error Examples

### Phone Number Error:
```json
{
  "status": false,
  "message": "Validation failed",
  "result": {
    "phone": "Invalid phone number. Must be 10 digits starting with 6, 7, 8, or 9"
  }
}
```

### Multiple Field Errors:
```json
{
  "status": false,
  "message": "Validation failed",
  "result": {
    "code": "Code is required",
    "password": "Password must be between 4 and 100 characters",
    "email": "Invalid email format"
  }
}
```

---

## Contact

For questions about these changes, contact the backend development team.


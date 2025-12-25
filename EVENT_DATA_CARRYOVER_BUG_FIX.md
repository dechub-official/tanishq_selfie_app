# Event Data Carryover Bug - Fix Guide

## Issue Description
When creating a new event by clicking "Create Event", data from the previous event (sale, advance, scheme/GHS/RGA, GMB data, and potentially attendee information) is being carried over to the new event form instead of starting with a clean slate.

### Example:
- Day 1: Created event with Sale=1000, Advance=500, GHS/RGA=200, GMB=300
- Day 2: Click "Create Event" - the form pre-fills with Day 1's data (Sale=1000, etc.)
- Day 2 data updates incorrectly capture Day 1's values
- This cascades: Day 1 → Day 2 → Day 3 → Day 4, etc.

## Root Cause
The frontend React application is not properly resetting the form state when creating a new event. The form state object that holds sale, advance, ghsOrRga, gmb, and other event details is being reused without clearing previous values.

## Analysis of Current Code

### Backend (Working Correctly)
Location: `EventsController.java` - `/upload` endpoint

The backend correctly accepts the parameters:
```java
@RequestParam(value = "sale", required = false) Integer sale,
@RequestParam(value = "advance", required = false) Integer advance,
@RequestParam(value = "ghsOrRga", required = false) Integer ghsOrRga,
@RequestParam(value = "gmb", required = false) Integer gmb,
```

The backend stores whatever values are sent from the frontend - **the backend is not causing this issue**.

### Frontend (Source of Bug)
Location: Compiled JavaScript in `/static/assets/index-CLJQELnM.js`

The form state object includes:
```javascript
{
  eventType: "",
  eventSubType: "",
  date: "",
  RSO: "",
  Community: "",
  time: "",
  location: "",
  eventName: "",
  description: "",
  customerName: "",
  customerContact: "",
  sale: "", // or numeric value
  advance: "", // or numeric value
  ghsOrRga: "", // or numeric value
  gmb: "" // or numeric value
}
```

## Required Fixes

### Fix 1: Reset Form State on Event Creation Success

**File**: React Dashboard Component (source file not available, needs to be located)

**Current Behavior**: After successfully creating an event, the form state is not cleared.

**Required Change**: When the event is successfully created (after receiving success response from `/upload` endpoint), completely reset the form state to initial values.

**Implementation**:

```javascript
// Define initial state as a constant
const INITIAL_FORM_STATE = {
  eventType: "",
  eventSubType: "",
  date: "",
  RSO: "",
  Community: "",
  time: "",
  location: "",
  eventName: "",
  description: "",
  customerName: "",
  customerContact: "",
  sale: null,  // or 0 or ""
  advance: null,  // or 0 or ""
  ghsOrRga: null,  // or 0 or ""
  gmb: null,  // or 0 or ""
  diamondAwareness: false,
  ghsFlag: false
};

// In the component
const [formData, setFormData] = useState(INITIAL_FORM_STATE);

// After successful event creation:
const handleCreateEvent = async () => {
  try {
    const response = await axios.post('/events/upload', formData);
    if (response.data.status) {
      // Success - reset form
      setFormData({...INITIAL_FORM_STATE}); // Create new object reference
      
      // Also reset file input
      setSelectedFile(null);
      setFileName("");
      
      // Reset checkboxes
      setCustomInvite(false);
      setSingleCustomer(false);
      setDiamondAwareness(false);
      setGhsFlag(false);
      
      // Show success message and refresh events
      showSuccess();
      getEvents();
    }
  } catch (error) {
    console.error("Error creating event:", error);
  }
};
```

### Fix 2: Clear Form on "Create Event" Button Click

**Alternative/Additional Fix**: Add a "Clear Form" or "New Event" button, or automatically clear the form when the user returns to the create event section.

```javascript
const handleNewEventClick = () => {
  // Reset all form fields
  setFormData({...INITIAL_FORM_STATE});
  setSelectedFile(null);
  setFileName("");
  setCustomInvite(false);
  setSingleCustomer(false);
  setDiamondAwareness(false);
  setGhsFlag(false);
  setErrors({});
};
```

### Fix 3: Ensure Numeric Fields Are Not String-Concatenated

**Issue**: If numeric fields (sale, advance, ghsOrRga, gmb) are being stored as strings, they might concatenate instead of replace.

**Fix**: Ensure these fields are properly typed:

```javascript
// When handling input change for numeric fields
const handleNumericInput = (fieldName, value) => {
  const numericValue = value === "" ? null : parseInt(value, 10);
  setFormData({
    ...formData,
    [fieldName]: numericValue
  });
};

// In the input field
<input
  type="number"
  name="sale"
  value={formData.sale || ""}
  onChange={(e) => handleNumericInput("sale", e.target.value)}
  placeholder="Sale Amount"
/>
```

### Fix 4: View Data Modal - Ensure Correct Event ID

**Issue**: When clicking "View" button, ensure the modal is fetching data for the correct event ID, not using cached data.

**Current Code** (from compiled JS):
```javascript
const viewData = async (eventId) => {
  const response = await axios.post('/events/getinvitedmember?eventCode=' + eventId);
  setModalData(response.data.result);
};
```

**Fix**: Ensure the modal state is cleared before fetching new data:

```javascript
const handleViewClick = async (eventId) => {
  // Clear previous data first
  setModalData(null);
  setModalVisible(true);
  setModalEventId(eventId);
  
  try {
    const response = await axios.post('/events/getinvitedmember?eventCode=' + eventId);
    setModalData(response.data.result);
  } catch (error) {
    console.error("Error fetching event data:", error);
    setModalData([]);
  }
};
```

## Testing Checklist

After implementing the fixes, test the following scenarios:

### Test 1: Create Multiple Events
1. ✅ Create Event 1 with Sale=1000, Advance=500, GHS/RGA=200, GMB=300
2. ✅ Verify Event 1 is saved correctly
3. ✅ Click "Create Event" again
4. ✅ **Verify all fields are EMPTY (sale, advance, GHS/RGA, GMB should be 0 or empty)**
5. ✅ Create Event 2 with Sale=2000, Advance=1000, GHS/RGA=400, GMB=600
6. ✅ Verify Event 2 has ONLY its own data, not Event 1's data
7. ✅ Check database to confirm Event 1 and Event 2 have different values

### Test 2: View Data Modal
1. ✅ Create Event A with 5 attendees
2. ✅ Create Event B with 10 attendees
3. ✅ Click "View" on Event A
4. ✅ **Verify modal shows only Event A's 5 attendees**
5. ✅ Close modal
6. ✅ Click "View" on Event B
7. ✅ **Verify modal shows only Event B's 10 attendees** (not 15 attendees)

### Test 3: Numeric Field Updates
1. ✅ Create an event with Sale=0, Advance=0
2. ✅ Edit Sale to 1000 (inline editing)
3. ✅ Save the change
4. ✅ Refresh the page
5. ✅ **Verify Sale shows 1000, not "01000" or any concatenated value**

### Test 4: Form Reset on Success
1. ✅ Fill in ALL form fields including sale, advance, GHS/RGA, GMB
2. ✅ Click "Create Event"
3. ✅ After success message appears
4. ✅ **Verify ALL fields are cleared, including:**
   - Event Type dropdown
   - Event Sub-Type dropdown
   - Date field
   - Location dropdown
   - Sale, Advance, GHS/RGA, GMB inputs
   - Checkboxes (Diamond Awareness, GHS Flag)
   - Customer name/contact (if visible)
   - File upload field

## Implementation Priority

### High Priority (Fix Immediately)
1. ✅ Fix form state reset after successful event creation
2. ✅ Ensure numeric fields are not string-concatenated

### Medium Priority
3. ✅ Add form validation to prevent submitting with old data
4. ✅ Fix view modal to always fetch fresh data

### Low Priority (Enhancement)
5. Add a "Clear Form" button for manual reset
6. Add confirmation dialog: "You have unsaved changes. Do you want to clear the form?"

## Database Verification Query

To check if events have incorrect carry-over data:

```sql
-- Check for suspicious patterns where consecutive events have identical sale/advance values
SELECT 
    e1.id as event1_id,
    e1.start_date as event1_date,
    e1.event_type as event1_type,
    e1.sale as event1_sale,
    e1.advance as event1_advance,
    e2.id as event2_id,
    e2.start_date as event2_date,
    e2.event_type as event2_type,
    e2.sale as event2_sale,
    e2.advance as event2_advance
FROM events e1
JOIN events e2 ON e1.store_id = e2.store_id
WHERE e1.start_date < e2.start_date
AND e1.sale = e2.sale
AND e1.advance = e2.advance
AND e1.ghs_or_rga = e2.ghs_or_rga
AND e1.gmb = e2.gmb
AND (e1.sale > 0 OR e1.advance > 0)  -- Exclude zero values
ORDER BY e1.start_date DESC
LIMIT 50;
```

## Additional Notes

1. **Session Storage**: Check if form data is being stored in browser localStorage or sessionStorage and not being cleared
2. **State Management**: If using Redux or Context API, ensure the global state is also reset
3. **React DevTools**: Use React DevTools to inspect component state before and after form submission
4. **Network Tab**: Monitor the network request payload to `/upload` endpoint to see what values are actually being sent

## Contact for Questions

If you need clarification or have questions about this fix:
- Review the Event Creation workflow in the Dashboard component
- Check the form submit handler
- Verify the state reset logic after successful API calls

---

**Created**: December 17, 2025
**Status**: Bug Identified - Fix Required
**Severity**: High - Data Integrity Issue
**Impact**: All stores using the event creation feature


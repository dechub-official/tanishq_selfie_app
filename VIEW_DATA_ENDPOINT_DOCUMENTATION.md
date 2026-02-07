# View Data Endpoint Documentation

## Endpoint URL
```
POST https://celebrations.tanishq.co.in/events/getinvitedmember?eventCode={eventCode}
```

## Purpose
This endpoint returns a list of **attendees** (people who actually attended) for a specific event based on the event code.

> **Note:** Despite the endpoint name containing "invitedmember", it now returns **attendees** data, not invitees.

---

## Data Flow Architecture

### 1. **Controller Layer**
**File:** `EventsController.java` (Line 223-230)

```java
@PostMapping("/getinvitedmember")
public ResponseEntity<ResponseDataDTO> getInvitedMember(@RequestParam String eventCode) throws Exception {
    List<?> list = tanishqPageService.getInvitedMember(eventCode);
    ResponseDataDTO response = new ResponseDataDTO();
    response.setStatus(true);
    response.setResult(list);
    return ResponseEntity.ok(response);
}
```

**What it does:**
- Accepts HTTP POST request with `eventCode` as a request parameter
- Calls the service layer to fetch invitees
- Wraps the result in a `ResponseDataDTO` object
- Returns HTTP 200 OK with the response

---

### 2. **Service Layer**
**File:** `TanishqPageService.java` (Line 681-695)

```java
public List<?> getInvitedMember(String eventId) {
    try {
        List<Attendee> attendees = attendeeRepository.findByEventId(eventId);
        return attendees.stream().map(attendee -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", attendee.getName());
            map.put("contact", attendee.getPhone());
            map.put("like", attendee.getLike());
            map.put("firstTimeAtTanishq", attendee.getFirstTimeAtTanishq());
            return map;
        }).collect(Collectors.toList());
    } catch (Exception e) {
        return new ArrayList<>();
    }
}
```

**What it does:**
- Queries the database using `AttendeeRepository.findByEventId(eventId)`
- Transforms each `Attendee` entity into a simplified Map with:
  - `name` - The attendee's name
  - `contact` - The attendee's phone number
  - `like` - Whether they liked the event (Yes/No)
  - `firstTimeAtTanishq` - Whether this is their first time at Tanishq (true/false)
- Returns an empty list if any exception occurs

---

### 3. **Repository Layer**
**File:** `AttendeeRepository.java`

```java
@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    List<Attendee> findByEventId(String eventId);
    long countByEventId(String eventId);
}
```

**What it does:**
- Spring Data JPA automatically implements this method
- Queries the `attendees` table where `event_id` matches the provided `eventId`

---

### 4. **Entity Layer**
**File:** `Attendee.java`

```java
@Entity
@Table(name = "attendees")
public class Attendee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String phone;
    @Column(name = "`like`")
    private String like;
    private Boolean firstTimeAtTanishq;
    private LocalDateTime createdAt;
    private Boolean isUploadedFromExcel;
    private String rsoName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id", 
                nullable = false, columnDefinition = "VARCHAR(255)")
    private Event event;
    
    // ... getters and setters
}
```

**Fields:**
- `id` - Auto-generated primary key
- `name` - Attendee's name
- `phone` - Attendee's phone number
- `like` - Whether the attendee liked the event (Yes/No)
- `firstTimeAtTanishq` - Whether this is their first visit to Tanishq
- `createdAt` - Timestamp when the attendee was added
- `isUploadedFromExcel` - Flag to indicate if data came from Excel upload
- `rsoName` - RSO (Regional Sales Officer) name
- `event` - Many-to-One relationship with Event entity

---

### 5. **Database Table**
**Table Name:** `attendees`

**Schema:**
```sql
CREATE TABLE `attendees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `like` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `first_time_at_tanishq` tinyint(1) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_uploaded_from_excel` tinyint(1) DEFAULT NULL,
  `rso_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `event_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKattendees_event_id` (`event_id`),
  CONSTRAINT `FKattendees_event_id` 
    FOREIGN KEY (`event_id`) REFERENCES `events` (`id`)
) ENGINE=InnoDB;
```

**Columns:**
- `id` - Primary key (bigint, auto-increment)
- `name` - Attendee name (varchar 255)
- `phone` - Phone number (varchar 255)
- `like` - Whether they liked the event, Yes/No (varchar 255)
- `first_time_at_tanishq` - First time visitor flag (boolean/tinyint)
- `created_at` - Creation timestamp (datetime with microseconds)
- `is_uploaded_from_excel` - Excel upload flag (boolean/tinyint)
- `rso_name` - RSO name (varchar 255)
- `event_id` - Foreign key to events table (varchar 255, NOT NULL)

---

## Example Request & Response

### Request
```http
POST /events/getinvitedmember?eventCode=HIS_bc298a53-215f-4deb-967b-08a83578418e
Content-Type: application/json
```

### Response
```json
{
  "status": true,
  "result": [
    {
      "name": "anju",
      "contact": "9876543210",
      "like": "No",
      "firstTimeAtTanishq": false
    },
    {
      "name": "parmila",
      "contact": "9876543211",
      "like": "No",
      "firstTimeAtTanishq": false
    },
    {
      "name": "bhumi",
      "contact": "9876543212",
      "like": "Yes",
      "firstTimeAtTanishq": true
    }
    // ... more attendees
  ]
}
```

---

## Data Source Summary

**The data displayed in the "View Data" popup comes from:**

1. **Database Table:** `attendees`
2. **Query:** `SELECT * FROM attendees WHERE event_id = ?`
3. **Filtered by:** The event code passed as a parameter (e.g., `HIS_bc298a53-215f-4deb-967b-08a83578418e`)
4. **Returns:** `name`, `contact` (phone), `like`, and `firstTimeAtTanishq` fields for each attendee
5. **Data Origin:** 
   - Attendees are people who actually attended the event (not just invited)
   - Data can be uploaded via Excel import functionality
   - Attendees can be added individually through the "Upload Attended Customer List" feature
   - Contains information about whether they liked the event and if it's their first time at Tanishq

---

## Key Points

1. **Event Code Mapping:** The `eventCode` parameter directly maps to the `event_id` foreign key in the `attendees` table
2. **Data Transformation:** The full `Attendee` entity contains more fields (id, createdAt, isUploadedFromExcel, rsoName, event relationship), but only `name`, `contact`, `like`, and `firstTimeAtTanishq` are returned to the frontend
3. **Error Handling:** If any exception occurs during data fetching, an empty list is returned instead of throwing an error
4. **Relationship:** Each attendee belongs to exactly one event (Many-to-One relationship)
5. **Attendees vs Invitees:** This endpoint shows people who **actually attended** the event, not just those who were invited
6. **Additional Information:** The response now includes:
   - Whether the attendee liked the event (`like` field: "Yes"/"No")
   - Whether this was their first visit to Tanishq (`firstTimeAtTanishq` field: true/false)

---

## Frontend Display

The frontend receives this data and displays it in a modal/popup showing:
- **Serial Number (Sno)** - Generated by frontend for display
- **Customer Name** - From `attendees.name`
- **Customer Phone** - From `attendees.phone`
- **Like** - From `attendees.like` (Yes/No)
- **First Time At Tanishq** - From `attendees.firstTimeAtTanishq` (displayed as "No" in your screenshot)

The popup you see in your screenshot shows this list of **attendees** (people who actually attended) for the selected event, not just those who were invited.


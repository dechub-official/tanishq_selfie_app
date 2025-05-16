////package com.dechub.tanishq.controller;
////
////import com.dechub.tanishq.dto.eventsDto.*;
////import com.dechub.tanishq.gdrive.GoogleDriveService;
////import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
////import com.dechub.tanishq.service.TanishqPageService;
////import com.dechub.tanishq.util.APIResponseBuilder;
////import com.dechub.tanishq.util.ResponseDataDTO;
////import com.opencsv.CSVWriter;
////import lombok.extern.log4j.Log4j;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.format.annotation.DateTimeFormat;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.multipart.MultipartFile;
////
////import javax.servlet.http.HttpServletResponse;
////import java.io.IOException;
////import java.io.OutputStreamWriter;
////import java.time.LocalDate;
////import java.time.format.DateTimeFormatter;
////import java.util.*;
////import java.util.concurrent.CompletableFuture;
////import java.util.concurrent.ExecutorService;
////import java.util.concurrent.Executors;
////import java.util.stream.Collectors;
////
////@RestController
////@RequestMapping("events")
////public class EventsController {
////    @Autowired
////    private GoogleDriveService googleServiceUtil;
////    @Autowired
////    private TanishqPageService tanishqPageService;
////
////    @Autowired
////    private GSheetUserDetailsUtil gSheetUserDetailsUtil;
////
////
////
////    @PostMapping("/login")
////    public EventsLoginResponseDTO eventsLogin(@RequestBody LoginDTO loginDTO) throws Exception {
////        return tanishqPageService.eventsLogin(loginDTO.getCode(),loginDTO.getPassword());
////    }
////
////    @GetMapping("/dowload-qr/{id}")
////    private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
////        QrResponseDTO qrResponseDTO = new QrResponseDTO();
////        String imageResponse = gSheetUserDetailsUtil.generateQrCode(eventId);
////
////        if (imageResponse.equals("error")) {
////            qrResponseDTO.setStatus(false);
////            return qrResponseDTO;
////        }
////        qrResponseDTO.setStatus(true);
////        qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
////        return qrResponseDTO;
////    }
////
////    @PostMapping(path = "/upload", produces = "application/json")
////    public QrResponseDTO storeEventsDetails(
////            @RequestParam(value = "code",required = false) String code,
////            @RequestParam(value = "file",required = false) MultipartFile file,
////            @RequestParam(value ="description",required = false) String description,
////            @RequestParam(value ="singalInvite",required = false) boolean isSingleCustomer,
////            @RequestParam(value ="eventName",required = false) String eventName,
////            @RequestParam(value ="eventType",required = false) String eventType,
////            @RequestParam(value ="eventSubType",required = false) String eventSubType,
////            @RequestParam(value ="RSO",required = false) String rso,
////            @RequestParam(value ="date",required = false) String startDate,
////            @RequestParam(value ="time",required = false) String startTime,
////            @RequestParam(value ="image",required = false) String image,
////            @RequestParam(value ="location",required = false) String location,
////            @RequestParam(value ="Community",required = false) String community,
////            @RequestParam(value ="customerName",required = false) String name,
////            @RequestParam(value ="customerContact",required = false) String contact
////
////
////
////    ) {
////        EventsDetailDTO eventsDetailDTO = new EventsDetailDTO();
////        if(file==null||file.isEmpty()){
////            eventsDetailDTO.setSingleCustomer(true);
////        }else{
////            eventsDetailDTO.setSingleCustomer(isSingleCustomer);
////
////        }
////        eventsDetailDTO.setStoreCode(code);
////        eventsDetailDTO.setDescription(description);
////        eventsDetailDTO.setFile(file);
////        eventsDetailDTO.setEventName(eventName);
////        eventsDetailDTO.setEventType(eventType);
////        eventsDetailDTO.setEventSubType(eventSubType);
////        eventsDetailDTO.setCommunity(community);
////        eventsDetailDTO.setImage(image);
////        eventsDetailDTO.setRso(rso);
////        eventsDetailDTO.setLocation(location);
////        eventsDetailDTO.setStartDate(startDate);
////        eventsDetailDTO.setStartTime(startTime);
////        eventsDetailDTO.setName(name);
////        eventsDetailDTO.setContact(contact);
////        return tanishqPageService.storeEventsDetails(eventsDetailDTO);
////    }
////
////    @PostMapping("/attendees")
////    public ResponseDataDTO storeAttendeesData(@RequestParam(name = "eventId",required = false) String eventId,
////                                              @RequestParam(name="name",required = false) String name,
////                                              @RequestParam(name="phone",required = false) String phone,
////                                              @RequestParam(name="like",required = false) String like,
////                                              @RequestParam(name="firstTimeAtTanishq",required = false) boolean firstTimeAtTanishq,
////                                              @RequestParam(name="file",required = false) MultipartFile file){
////
////        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
////        attendeesDetailDTO.setId(eventId);
////        attendeesDetailDTO.setLike(like);
////        attendeesDetailDTO.setPhone(phone);
////        attendeesDetailDTO.setName(name);
////        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
////        attendeesDetailDTO.setFile(file);
////        System.out.println(attendeesDetailDTO);
////        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
////    }
////
////
////    @PostMapping("/getevents")
////    public CompletedEventsResponseDTO getAllCompletedEvents(@RequestBody storeCodeDataDTO storeCodeDataDTO){
////        return tanishqPageService.getAllCompletedEvents(storeCodeDataDTO.getStoreCode());
////    }
////
////    @PostMapping("/getinvitedmember")
////    public ResponseEntity<ResponseDataDTO> getInvitedMember(@RequestParam String eventCode) throws Exception {
////        List<?> list = tanishqPageService.getInvitedMember(eventCode);
////        ResponseDataDTO response = new ResponseDataDTO();
////        response.setStatus(true);
////        response.setResult(list);
////        return ResponseEntity.ok(response);
////    }
////
////    @PostMapping("/uploadCompletedEvents")
////    public ResponseDataDTO uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("eventId") String eventId) {
////        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
////        ExecutorService executor = Executors.newFixedThreadPool(Math.min(files.size(), 10)); // Limit concurrent threads
////        List<CompletableFuture<Boolean>> uploadFutures = new ArrayList<>();
////
////        try {
////            for (MultipartFile file : files) {
////                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
////                    try {
////                        // Validate file type
////                        if (!isAllowedFileType(file.getOriginalFilename())) {
////                            return false;
////                        }
////
////                        // Save file to a temporary location
////                        java.io.File tempFile = java.io.File.createTempFile("upload-", file.getOriginalFilename());
////                        file.transferTo(tempFile);
////
////                        // Upload file to Google Drive
////                        String fileLink = googleServiceUtil.uploadFileToDrive(tempFile, eventId, file.getContentType());
////
////                        // Clean up temporary file
////                        tempFile.delete();
////
////                        // Update Google Sheet with file link
////                        return gSheetUserDetailsUtil.updateDrivelink(eventId, fileLink);
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        return false;
////                    }
////                }, executor);
////
////                uploadFutures.add(future);
////            }
////
////            // Wait for all uploads to complete
////            List<Boolean> results = uploadFutures.stream()
////                    .map(CompletableFuture::join)
////                    .collect(Collectors.toList());
////
////            // Check if all files were uploaded successfully
////            boolean allSuccess = results.stream().allMatch(result -> result);
////            responseDataDTO.setStatus(allSuccess);
////            responseDataDTO.setMessage(allSuccess ? "All files uploaded successfully." : "Some files failed to upload.");
////        } catch (Exception e) {
////            e.printStackTrace();
////            responseDataDTO.setStatus(false);
////            responseDataDTO.setMessage("Error occurred: " + e.getMessage());
////        } finally {
////            executor.shutdown();
////        }
////
////        return responseDataDTO;
////    }
////
////    private boolean isAllowedFileType(String originalFilename) {
////        if (originalFilename == null || originalFilename.isEmpty()) {
////            return false;
////        }
////        String[] blacklist = {".php",".html",".htaccess",".pgif",".inc",".phar",".ctp",".module",".pht",".phtm",".asp",".ashx", ".asmx", ".aspq", ".axd", ".cshtm", ".cshtml"," .rem", ".soap", ".vbhtm"," .vbhtml", ".asa"," .cer", ".jsp", ".jspx", ".jsw", ".jsv", ".jspf","cfm", ".cfml", ".cfc", ".dbm"," .pl", ".cgi"};
////        for (String blacklistedWord : blacklist) {
////            if (originalFilename.toLowerCase().contains(blacklistedWord)) {
////                return false;
////            }
////        }
////
////        return true;
////    }
////
////    private String getFileExtension(String filename) {
////        int lastDotIndex = filename.lastIndexOf('.');
////        if (lastDotIndex == -1) {
////            return "";  // No file extension found
////        }
////        return filename.substring(lastDotIndex + 1).toLowerCase();
////    }
////
//////    @PostMapping("/getinvitedmember")
//////    public CompletedEventsResponseDTO getInviteeMember(@RequestParam("eventCode") String code){
//////
//////    }
//
//    package com.dechub.tanishq.controller;
//
//import com.dechub.tanishq.config.StoreSummaryCache;
//import com.dechub.tanishq.dto.eventsDto.*;
//import com.dechub.tanishq.gdrive.GoogleDriveService;
//import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
//import com.dechub.tanishq.service.TanishqPageService;
//import com.dechub.tanishq.util.APIResponseBuilder;
//import com.dechub.tanishq.util.ResponseDataDTO;
//import com.opencsv.CSVWriter;
//import lombok.extern.log4j.Log4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//
//    @RestController
//    @RequestMapping("events")
//    public class EventsController {
//        @Autowired
//        private GoogleDriveService googleServiceUtil;
//        @Autowired
//        private TanishqPageService tanishqPageService;
//
//        @Autowired
//        private GSheetUserDetailsUtil gSheetUserDetailsUtil;
//
//        @Autowired
//        private StoreSummaryCache storeSummaryCache;
//
//
//        @PostMapping("/login")
//        public EventsLoginResponseDTO eventsLogin(@RequestBody LoginDTO loginDTO) throws Exception {
//            return tanishqPageService.eventsLogin(loginDTO.getCode(),loginDTO.getPassword());
//        }
//
//        @GetMapping("/dowload-qr/{id}")
//        private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
//            QrResponseDTO qrResponseDTO = new QrResponseDTO();
//            String imageResponse = gSheetUserDetailsUtil.generateQrCode(eventId);
//
//            if (imageResponse.equals("error")) {
//                qrResponseDTO.setStatus(false);
//                return qrResponseDTO;
//            }
//            qrResponseDTO.setStatus(true);
//            qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
//            return qrResponseDTO;
//        }
//
//        @PostMapping(path = "/upload", produces = "application/json")
//        public QrResponseDTO storeEventsDetails(
//                @RequestParam(value = "code",required = false) String code,
//                @RequestParam(value = "file",required = false) MultipartFile file,
//                @RequestParam(value ="description",required = false) String description,
//                @RequestParam(value ="singalInvite",required = false) boolean isSingleCustomer,
//                @RequestParam(value ="eventName",required = false) String eventName,
//                @RequestParam(value ="eventType",required = false) String eventType,
//                @RequestParam(value ="eventSubType",required = false) String eventSubType,
//                @RequestParam(value ="RSO",required = false) String rso,
//                @RequestParam(value ="date",required = false) String startDate,
//                @RequestParam(value ="time",required = false) String startTime,
//                @RequestParam(value ="image",required = false) String image,
//                @RequestParam(value ="location",required = false) String location,
//                @RequestParam(value ="Community",required = false) String community,
//                @RequestParam(value ="customerName",required = false) String name,
//                @RequestParam(value ="customerContact",required = false) String contact,
//                @RequestParam(value = "region", required = false) String region,
//                @RequestParam(value = "sale", required = false) Integer sale,
//                @RequestParam(value = "advance", required = false) Integer advance,
//                @RequestParam(value = "ghsOrRga", required = false) Integer ghsOrRga,
//                @RequestParam(value = "gmb", required = false) Integer gmb
//
//        ) {
//            EventsDetailDTO eventsDetailDTO = new EventsDetailDTO();
//            if(file==null||file.isEmpty()){
//                eventsDetailDTO.setSingleCustomer(true);
//            }else{
//                eventsDetailDTO.setSingleCustomer(isSingleCustomer);
//
//            }
//            eventsDetailDTO.setStoreCode(code);
//            eventsDetailDTO.setDescription(description);
//            eventsDetailDTO.setFile(file);
//            eventsDetailDTO.setEventName(eventName);
//            eventsDetailDTO.setEventType(eventType);
//            eventsDetailDTO.setEventSubType(eventSubType);
//            eventsDetailDTO.setCommunity(community);
//            eventsDetailDTO.setImage(image);
//            eventsDetailDTO.setRso(rso);
//            eventsDetailDTO.setLocation(location);
//            eventsDetailDTO.setStartDate(startDate);
//            eventsDetailDTO.setStartTime(startTime);
//            eventsDetailDTO.setName(name);
//            eventsDetailDTO.setContact(contact);
//
//            eventsDetailDTO.setRegion(region);
//            eventsDetailDTO.setSale(sale);
//            eventsDetailDTO.setAdvance(advance);
//            eventsDetailDTO.setGhsOrRga(ghsOrRga);
//            eventsDetailDTO.setGmb(gmb);
//
//            return tanishqPageService.storeEventsDetails(eventsDetailDTO);
//        }
//
////    @PostMapping("/attendees")
////    public ResponseDataDTO storeAttendeesData(@RequestParam(name = "eventId",required = false) String eventId,
////                                              @RequestParam(name="name",required = false) String name,
////                                              @RequestParam(name="phone",required = false) String phone,
////                                              @RequestParam(name="like",required = false) String like,
////                                              @RequestParam(name="firstTimeAtTanishq",required = false) boolean firstTimeAtTanishq,
////                                              @RequestParam(name="file",required = false) MultipartFile file){
////
////        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
////        attendeesDetailDTO.setId(eventId);
////        attendeesDetailDTO.setLike(like);
////        attendeesDetailDTO.setPhone(phone);
////        attendeesDetailDTO.setName(name);
////        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
////        attendeesDetailDTO.setFile(file);
////        System.out.println(attendeesDetailDTO);
////        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
////    }
//
//
////
////    @PostMapping("/attendees")
////    public ResponseEntity<ApiResponse<String>> storeAttendeesData(
////            @RequestParam(name = "id", required = false) String id,
////            @RequestParam(name = "eventId", required = false) String eventId,
////            @RequestParam(name = "eventType", required = false) String eventType,
////            @RequestParam(name = "name", required = false) String name,
////            @RequestParam(name = "storeCode", required = false) String storeCode,
////            @RequestParam(name = "region", required = false) String region,
////            @RequestParam(name = "rsoName", required = false) String rsoName,
////            @RequestParam(name = "phone", required = false) String phone,
////            @RequestParam(name = "like", required = false) String like,
////            @RequestParam(name = "createdAt", required = false) String createdAt,
////            @RequestParam(name = "firstTimeAtTanishq", required = false) boolean firstTimeAtTanishq,
////            @RequestParam(name = "file", required = false) MultipartFile file) {
////
////        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
////        attendeesDetailDTO.setId(id);
////        attendeesDetailDTO.setEventId(eventId);
////        attendeesDetailDTO.setEventType(eventType);
////        attendeesDetailDTO.setName(name);
////        attendeesDetailDTO.setStoreCode(storeCode);
////        attendeesDetailDTO.setRegion(region);
////        attendeesDetailDTO.setRsoName(rsoName);
////        attendeesDetailDTO.setPhone(phone);
////        attendeesDetailDTO.setLike(like);
////        attendeesDetailDTO.setCreatedAt(createdAt);
////        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
////        attendeesDetailDTO.setFile(file);
////
////        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
////    }
//
//        @PostMapping("/attendees")
//        public ResponseEntity<ApiResponse<String>> storeAttendeesData(
//                @RequestParam(name = "id", required = false) String id,
//                @RequestParam(name = "eventId", required = false) String eventId,
//                @RequestParam(name = "eventType", required = false) String eventType,
//                @RequestParam(name = "name", required = false) String name,
//                @RequestParam(name = "storeCode", required = false) String storeCode,
//                @RequestParam(name = "region", required = false) String region,
//                @RequestParam(name = "rsoName", required = false) String rsoName,
//                @RequestParam(name = "phone", required = false) String phone,
//                @RequestParam(name = "like", required = false) String like,
//                @RequestParam(name = "createdAt", required = false) String createdAt,
//                @RequestParam(name = "firstTimeAtTanishq", required = false) boolean firstTimeAtTanishq,
//                @RequestParam(name = "file", required = false) MultipartFile file) {
//
//            try {
//                AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
//                attendeesDetailDTO.setId(id);
//                attendeesDetailDTO.setEventId(eventId);
//                attendeesDetailDTO.setEventType(eventType);
//                attendeesDetailDTO.setName(name);
//                attendeesDetailDTO.setStoreCode(storeCode);
//                attendeesDetailDTO.setRegion(region);
//                attendeesDetailDTO.setRsoName(rsoName);
//                attendeesDetailDTO.setPhone(phone);
//                attendeesDetailDTO.setLike(like);
//                attendeesDetailDTO.setCreatedAt(createdAt);
//                attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
//                attendeesDetailDTO.setFile(file);
//
//                return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                ApiResponse<String> response = new ApiResponse<>(200, "Something went wrong, but request handled gracefully", null);
//                return ResponseEntity.ok(response);
//            }
//        }
//
//
//
//
//
//        @PostMapping("/getevents")
//        public CompletedEventsResponseDTO getAllCompletedEvents(@RequestBody storeCodeDataDTO storeCodeDataDTO){
//            return tanishqPageService.getAllCompletedEvents(storeCodeDataDTO.getStoreCode());
//        }
//
//        @GetMapping("/getStoresByRegion/{region}")
//        public List<storeCodeDataDTO> getStoresByRegion(@PathVariable String region) {
//            return tanishqPageService.getStoresByRegion(region);
//        }
//
//        @PostMapping("/updateSaleOfAnEvent")
//        public ResponseDataDTO updateSaleOfAnEvent(@RequestParam String eventCode,@RequestParam String sale){
//            return tanishqPageService.updateSaleOfAnEvent(eventCode,sale);
//        }
//        @PostMapping("/updateAdvanceOfAnEvent")
//        public ResponseDataDTO updateAdvanceOfAnEvent(@RequestParam String eventCode,@RequestParam String advance){
//            return tanishqPageService.updateAdvanceOfAnEvent(eventCode,advance);
//        }
//        @PostMapping("/updateGhsRgaOfAnEvent")
//        public ResponseDataDTO updateGhsRgaOfAnEvent(@RequestParam String eventCode,@RequestParam String ghsRga){
//            return tanishqPageService.updateGhsRgaOfAnEvent(eventCode,ghsRga);
//        }
//        @PostMapping("/updateGmbOfAnEvent")
//        public ResponseDataDTO updateGmbOfAnEvent(@RequestParam String eventCode,@RequestParam String gmb){
//            return tanishqPageService.updateGmbOfAnEvent(eventCode,gmb);
//        }
//
//        @PostMapping("/getinvitedmember")
//        public ResponseEntity<ResponseDataDTO> getInvitedMember(@RequestParam String eventCode) throws Exception {
//            List<?> list = tanishqPageService.getInvitedMember(eventCode);
//            ResponseDataDTO response = new ResponseDataDTO();
//            response.setStatus(true);
//            response.setResult(list);
//            return ResponseEntity.ok(response);
//        }
//
//        @PostMapping("/uploadCompletedEvents")
//        public ResponseDataDTO uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("eventId") String eventId) {
//            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//            ExecutorService executor = Executors.newFixedThreadPool(Math.min(files.size(), 10)); // Limit concurrent threads
//            List<CompletableFuture<Boolean>> uploadFutures = new ArrayList<>();
//
//            try {
//                for (MultipartFile file : files) {
//                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
//                        try {
//                            // Validate file type
//                            if (!isAllowedFileType(file.getOriginalFilename())) {
//                                return false;
//                            }
//
//                            // Save file to a temporary location
//                            java.io.File tempFile = java.io.File.createTempFile("upload-", file.getOriginalFilename());
//                            file.transferTo(tempFile);
//
//                            // Upload file to Google Drive
//                            String fileLink = googleServiceUtil.uploadFileToDrive(tempFile, eventId, file.getContentType());
//
//                            // Clean up temporary file
//                            tempFile.delete();
//
//                            // Update Google Sheet with file link
//                            return gSheetUserDetailsUtil.updateDrivelink(eventId, fileLink);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            return false;
//                        }
//                    }, executor);
//
//                    uploadFutures.add(future);
//                }
//
//                // Wait for all uploads to complete
//                List<Boolean> results = uploadFutures.stream()
//                        .map(CompletableFuture::join)
//                        .collect(Collectors.toList());
//
//                // Check if all files were uploaded successfully
//                boolean allSuccess = results.stream().allMatch(result -> result);
//                responseDataDTO.setStatus(allSuccess);
//                responseDataDTO.setMessage(allSuccess ? "All files uploaded successfully." : "Some files failed to upload.");
//            } catch (Exception e) {
//                e.printStackTrace();
//                responseDataDTO.setStatus(false);
//                responseDataDTO.setMessage("Error occurred: " + e.getMessage());
//            } finally {
//                executor.shutdown();
//            }
//
//            return responseDataDTO;
//        }
//
//        private boolean isAllowedFileType(String originalFilename) {
//            if (originalFilename == null || originalFilename.isEmpty()) {
//                return false;
//            }
//            String[] blacklist = {".php",".html",".htaccess",".pgif",".inc",".phar",".ctp",".module",".pht",".phtm",".asp",".ashx", ".asmx", ".aspq", ".axd", ".cshtm", ".cshtml"," .rem", ".soap", ".vbhtm"," .vbhtml", ".asa"," .cer", ".jsp", ".jspx", ".jsw", ".jsv", ".jspf","cfm", ".cfml", ".cfc", ".dbm"," .pl", ".cgi"};
//            for (String blacklistedWord : blacklist) {
//                if (originalFilename.toLowerCase().contains(blacklistedWord)) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//
//        private String getFileExtension(String filename) {
//            int lastDotIndex = filename.lastIndexOf('.');
//            if (lastDotIndex == -1) {
//                return "";  // No file extension found
//            }
//            return filename.substring(lastDotIndex + 1).toLowerCase();
//        }
//
//        @PostMapping("/changePassword")
//        private ResponseDataDTO changePassword(@RequestParam String storeCode,@RequestParam String oldPassword,@RequestParam String newPassword){
//            return tanishqPageService.changePasswordForEventManager(storeCode,oldPassword,newPassword);
//        }
//
////    @PostMapping("/abm_login")
////    public ResponseEntity<String> loginAbm(@RequestBody Map<String, String> credentials) {
////        String username = credentials.get("username");
////        String password = credentials.get("password");
////
////        if (username == null || password == null) {
////            return ResponseEntity.badRequest().body("Username or password missing");
////        }
////
////        boolean isValid = tanishqPageService.authenticateAbm(username, password);
////
////        if (isValid) {
////            return ResponseEntity.ok("Login successful");
////        } else {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
////        }
////    }
//
//        @PostMapping("/abm_login")
//        public ResponseEntity<ApiResponse<LoginResponseDTO>> loginAbm(@RequestBody Map<String, String> credentials) {
//            String username = credentials.get("username");
//            String password = credentials.get("password");
//
//            if (username == null || password == null) {
//                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
//            }
//
//            Optional<LoginResponseDTO> user = tanishqPageService.authenticateAbm(username, password);
//
//            if (user.isPresent()) {
//                return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse<>(401, "Invalid credentials", null));
//            }
//        }
//
//
////    @PostMapping("/rbm_login")
////    public ResponseEntity<String> loginRbm(@RequestBody Map<String, String> credentials) {
////        String username = credentials.get("username");
////        String password = credentials.get("password");
////
////        if (username == null || password == null) {
////            return ResponseEntity.badRequest().body("Username or password missing");
////        }
////
////        boolean isValid = tanishqPageService.authenticateRbm(username, password);
////
////        if (isValid) {
////            return ResponseEntity.ok("Login successful");
////        } else {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
////        }
////    }
//
//        @PostMapping("/rbm_login")
//        public ResponseEntity<ApiResponse<LoginResponseDTO>> loginRbm(@RequestBody Map<String, String> credentials) {
//            String username = credentials.get("username");
//            String password = credentials.get("password");
//
//            if (username == null || password == null) {
//                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
//            }
//
//            Optional<LoginResponseDTO> user = tanishqPageService.authenticateRbm(username, password);
//
//            if (user.isPresent()) {
//                return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse<>(401, "Invalid credentials", null));
//            }
//        }
//
//
//        @PostMapping("/cee_login")
//        public ResponseEntity<ApiResponse<LoginResponseDTO>> loginCee(@RequestBody Map<String, String> credentials) {
//            String username = credentials.get("username");
//            String password = credentials.get("password");
//
//            if (username == null || password == null) {
//                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
//            }
//
//            Optional<LoginResponseDTO> user = tanishqPageService.authenticateCee(username, password);
//
//            if (user.isPresent()) {
//                return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse<>(401, "Invalid credentials", null));
//            }
//        }
//
//
////    @GetMapping("/rbmStores")
////    public ResponseEntity<?> getStoresByRbm(@RequestParam String rbmUsername) {
////        try {
////            List<String> stores = tanishqPageService.fetchStoresByRbm(rbmUsername);
////            return ResponseEntity.ok(stores);
////        } catch (IOException e) {
////            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
////        } catch (Exception e) {
////            throw new RuntimeException(e);
////        }
////    }
//
////    @GetMapping("/rbmStores")
////    public ResponseEntity<?> getStoresByRbm(
////            @RequestParam String rbmUsername,
////            @RequestParam(required = false) String startDate,
////            @RequestParam(required = false) String endDate) {
////
////        try {
////            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
////            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
////
////            List<StoreEventSummaryDTO> storeSummaries = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
////            return ResponseEntity.ok(storeSummaries);
////        } catch (IOException e) {
////            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
////        } catch (Exception e) {
////            return ResponseEntity.internalServerError().body("Unexpected error");
////        }
////    }
//
////    @GetMapping("/rbmStores")
////    public ResponseEntity<?> getStoresByRbm(
////            @RequestParam String rbmUsername,
////            @RequestParam(required = false) String startDate,
////            @RequestParam(required = false) String endDate) {
////
////        try {
////            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
////            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
////
////            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
////            return ResponseEntity.ok(result);
////
////        } catch (IOException e) {
////            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
////        } catch (Exception e) {
////            return ResponseEntity.internalServerError().body("Unexpected error");
////        }
////    }
//
////    @GetMapping("/rbmStores")
////    public ResponseEntity<?> getStoresByRbm(
////            @RequestParam String rbmUsername,
////            @RequestParam(required = false) String startDate,
////            @RequestParam(required = false) String endDate) {
////
////        try {
////            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
////            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
////
////            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
////            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
////            return ResponseEntity.ok(response);
////
////        } catch (IOException e) {
////            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
////        } catch (Exception e) {
////            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
////        }
////    }
//
//
//        @GetMapping("/rbmStores")
//        public ResponseEntity<?> getStoresByRbm(
//                @RequestParam String rbmUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate) {
//
//            try {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//                LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//                // If dates are not given, use cached data
//                if (start == null && end == null) {
//                    StoreSummaryWrapperDTO cached = storeSummaryCache.get(rbmUsername);
//                    if (cached != null) {
//                        return ResponseEntity.ok(new ApiResponse<>(200, "Fetched from cache", cached));
//                    }
//                }
//
//                // Else compute fresh
//                StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
//
//                // Cache only full summaries (i.e., without date filter)
//                if (start == null && end == null) {
//                    storeSummaryCache.put(rbmUsername, result);
//                }
//
//                return ResponseEntity.ok(new ApiResponse<>(200, "Fetched freshly", result));
//
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(new ApiResponse<>(500, "Error fetching data from sheet", null));
//            } catch (Exception e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(new ApiResponse<>(500, "Unexpected error occurred", null));
//            }
//        }
//
//
//
//
//
////    @GetMapping("/abmStores")
////    public ResponseEntity<?> getStoresByAbm(@RequestParam String abmUsername) {
////        try {
////            List<String> stores = tanishqPageService.fetchStoresByAbm(abmUsername);
////            return ResponseEntity.ok(stores);
////        } catch (IOException e) {
////            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
////        } catch (Exception e) {
////            throw new RuntimeException(e);
////        }
////    }
//
//        @GetMapping("/abmStores")
//        public ResponseEntity<?> getStoresByAbm(
//                @RequestParam String abmUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate) {
//
//            try {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//                LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//                StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByAbmParallel(abmUsername, start, end);
//                ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
//                return ResponseEntity.ok(response);
//
//            } catch (IOException e) {
//                ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//            } catch (Exception e) {
//                ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//            }
//        }
//
////    @GetMapping("/ceeStores")
////    public ResponseEntity<?> getStoresByCee(@RequestParam String ceeUsername) {
////        try {
////            List<String> stores = tanishqPageService.fetchStoresByCee(ceeUsername);
////            return ResponseEntity.ok(stores);
////        } catch (IOException e) {
////            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
////        } catch (Exception e) {
////            throw new RuntimeException(e);
////        }
////    }
//
//        @GetMapping("/ceeStores")
//        public ResponseEntity<?> getStoresByCee(
//                @RequestParam String ceeUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate) {
//
//            try {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//                LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//                StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByCeeParallel(ceeUsername, start, end);
//                ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
//                return ResponseEntity.ok(response);
//
//            } catch (IOException e) {
//                ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//            } catch (Exception e) {
//                ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//            }
//        }
//
//        @GetMapping("/rbm/events")
//        public ResponseEntity<?> getEventsByRbmUsername(@RequestParam String rbmUsername) {
//            try {
//                List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
//                List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
//                return ResponseEntity.ok(events);
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body("Failed to fetch events for RBM user.");
//            }
//        }
//
//        @GetMapping("/abm/events")
//        public ResponseEntity<?> getEventsByAbmUsername(@RequestParam String abmUsername) {
//            try {
//                List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
//                List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
//                return ResponseEntity.ok(events);
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body("Failed to fetch events for ABM user.");
//            }
//        }
//
//        @GetMapping("/cee/events")
//        public ResponseEntity<?> getEventsByCeeUsername(@RequestParam String ceeUsername) {
//            try {
//                List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
//                List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
//                return ResponseEntity.ok(events);
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body("Failed to fetch events for CEE user.");
//            }
//        }
//
////    @GetMapping("/abm/events/download")
////    public void downloadAbmEventsAsCsv(
////            @RequestParam String abmUsername,
////            @RequestParam(required = false) String startDate,
////            @RequestParam(required = false) String endDate,
////            HttpServletResponse response
////    ) throws Exception {
////        List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
////        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
////
////        List<Map<String, Object>> filteredEvents;
////        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
////            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
////        } else {
////            filteredEvents = allEvents; // No filtering
////        }
////
////        response.setContentType("text/csv");
////        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
////
////        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
////
////        if (!filteredEvents.isEmpty()) {
////            Set<String> headers = filteredEvents.get(0).keySet();
////            writer.writeNext(headers.toArray(new String[0]));
////
////            for (Map<String, Object> row : filteredEvents) {
////                List<String> rowData = headers.stream()
////                        .map(h -> row.get(h) != null ? row.get(h).toString() : "")
////                        .collect(Collectors.toList());
////                writer.writeNext(rowData.toArray(new String[0]));
////            }
////        }
////
////        writer.flush();
////        writer.close();
////    }
//
//        @GetMapping("/store/events/download")
//        public void downloadStoreEventsAsCsv(
//                @RequestParam String storeCode,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate,
//                HttpServletResponse response
//        ) throws Exception {
//            List<String> storeCodes = new ArrayList<>();
//            storeCodes.add(storeCode);
//            List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
//            List<Map<String, Object>> filteredEvents;
//            if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//                filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
//            } else {
//                filteredEvents = allEvents;
//            }
//
//            response.setContentType("text/csv");
//            response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
//
//            CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
//
//            // Define the exact column order
//            List<String> headers = Arrays.asList(
//                    "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
//                    "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
//                    "createdAt", "completedEvent", "Community", "location",
//                    "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
//            );
//
//            writer.writeNext(headers.toArray(new String[0]));
//
//            for (Map<String, Object> row : filteredEvents) {
//                List<String> rowData = headers.stream()
//                        .map(h -> row.getOrDefault(h, "").toString())
//                        .collect(Collectors.toList());
//                writer.writeNext(rowData.toArray(new String[0]));
//            }
//
//            writer.flush();
//            writer.close();
//        }
//
//        @GetMapping("/abm/events/download")
//        public void downloadAbmEventsAsCsv(
//                @RequestParam String abmUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate,
//                HttpServletResponse response
//        ) throws Exception {
//            List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
//            List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
//
//            List<Map<String, Object>> filteredEvents;
//            if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//                filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
//            } else {
//                filteredEvents = allEvents;
//            }
//
//            response.setContentType("text/csv");
//            response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
//
//            CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
//
//            // Define the exact column order
//            List<String> headers = Arrays.asList(
//                    "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
//                    "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
//                    "createdAt", "completedEvent", "Community", "location",
//                    "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
//            );
//
//            writer.writeNext(headers.toArray(new String[0]));
//
//            for (Map<String, Object> row : filteredEvents) {
//                List<String> rowData = headers.stream()
//                        .map(h -> row.getOrDefault(h, "").toString())
//                        .collect(Collectors.toList());
//                writer.writeNext(rowData.toArray(new String[0]));
//            }
//
//            writer.flush();
//            writer.close();
//        }
//
//        @GetMapping("/rbm/events/download")
//        public void downloadRbmEventsAsCsv(
//                @RequestParam String rbmUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate,
//                HttpServletResponse response
//        ) throws Exception {
//            List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
//            List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
//
//            List<Map<String, Object>> filteredEvents;
//            if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//                filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
//            } else {
//                filteredEvents = allEvents;
//            }
//
//            response.setContentType("text/csv");
//            response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
//
//            CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
//
//            // Define the exact column order
//            List<String> headers = Arrays.asList(
//                    "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
//                    "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
//                    "createdAt", "completedEvent", "Community", "location",
//                    "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
//            );
//
//            writer.writeNext(headers.toArray(new String[0]));
//
//            for (Map<String, Object> row : filteredEvents) {
//                List<String> rowData = headers.stream()
//                        .map(h -> row.getOrDefault(h, "").toString())
//                        .collect(Collectors.toList());
//                writer.writeNext(rowData.toArray(new String[0]));
//            }
//
//            writer.flush();
//            writer.close();
//        }
//
//
//        @GetMapping("/cee/events/download")
//        public void downloadCeeEventsAsCsv(
//                @RequestParam String ceeUsername,
//                @RequestParam(required = false) String startDate,
//                @RequestParam(required = false) String endDate,
//                HttpServletResponse response
//        ) throws Exception {
//            List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
//            List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
//
//            List<Map<String, Object>> filteredEvents;
//            if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//                filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
//            } else {
//                filteredEvents = allEvents;
//            }
//
//            response.setContentType("text/csv");
//            response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
//
//            CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
//
//            // Define the exact column order
//            List<String> headers = Arrays.asList(
//                    "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
//                    "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
//                    "createdAt", "completedEvent", "Community", "location",
//                    "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
//            );
//
//            writer.writeNext(headers.toArray(new String[0]));
//
//            for (Map<String, Object> row : filteredEvents) {
//                List<String> rowData = headers.stream()
//                        .map(h -> row.getOrDefault(h, "").toString())
//                        .collect(Collectors.toList());
//                writer.writeNext(rowData.toArray(new String[0]));
//            }
//
//            writer.flush();
//            writer.close();
//        }
//        @GetMapping("/store-summary")
//        public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getStoreSummary(
//                @RequestParam String storeCode,
//                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//            try {
//                StoreEventSummaryDTO summary = tanishqPageService.processSingleStoreCode(storeCode, startDate, endDate);
//                ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", summary);
//                return ResponseEntity.ok(response);
//            } catch (Exception e) {
//                ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching store summary: " + e.getMessage(), null);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//            }
//        }
//
//
//
//
//    }
//
//
//}




package com.dechub.tanishq.controller;

import com.dechub.tanishq.config.StoreSummaryCache;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.gdrive.GoogleDriveService;
import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.ResponseDataDTO;
import com.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("events")
public class EventsController {
    @Autowired
    private GoogleDriveService googleServiceUtil;
    @Autowired
    private TanishqPageService tanishqPageService;

    @Autowired
    private GSheetUserDetailsUtil gSheetUserDetailsUtil;

    @Autowired
    private StoreSummaryCache storeSummaryCache;


    @PostMapping("/login")
    public EventsLoginResponseDTO eventsLogin(@RequestBody LoginDTO loginDTO) throws Exception {
        return tanishqPageService.eventsLogin(loginDTO.getCode(),loginDTO.getPassword());
    }

    @GetMapping("/dowload-qr/{id}")
    private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
        QrResponseDTO qrResponseDTO = new QrResponseDTO();
        String imageResponse = gSheetUserDetailsUtil.generateQrCode(eventId);

        if (imageResponse.equals("error")) {
            qrResponseDTO.setStatus(false);
            return qrResponseDTO;
        }
        qrResponseDTO.setStatus(true);
        qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
        return qrResponseDTO;
    }

    @PostMapping(path = "/upload", produces = "application/json")
    public QrResponseDTO storeEventsDetails(
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "file",required = false) MultipartFile file,
            @RequestParam(value ="description",required = false) String description,
            @RequestParam(value ="singalInvite",required = false) boolean isSingleCustomer,
            @RequestParam(value ="eventName",required = false) String eventName,
            @RequestParam(value ="eventType",required = false) String eventType,
            @RequestParam(value ="eventSubType",required = false) String eventSubType,
            @RequestParam(value ="RSO",required = false) String rso,
            @RequestParam(value ="date",required = false) String startDate,
            @RequestParam(value ="time",required = false) String startTime,
            @RequestParam(value ="image",required = false) String image,
            @RequestParam(value ="location",required = false) String location,
            @RequestParam(value ="Community",required = false) String community,
            @RequestParam(value ="customerName",required = false) String name,
            @RequestParam(value ="customerContact",required = false) String contact,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "sale", required = false) Integer sale,
            @RequestParam(value = "advance", required = false) Integer advance,
            @RequestParam(value = "ghsOrRga", required = false) Integer ghsOrRga,
            @RequestParam(value = "gmb", required = false) Integer gmb

    ) {
        EventsDetailDTO eventsDetailDTO = new EventsDetailDTO();
        if(file==null||file.isEmpty()){
            eventsDetailDTO.setSingleCustomer(true);
        }else{
            eventsDetailDTO.setSingleCustomer(isSingleCustomer);

        }
        eventsDetailDTO.setStoreCode(code);
        eventsDetailDTO.setDescription(description);
        eventsDetailDTO.setFile(file);
        eventsDetailDTO.setEventName(eventName);
        eventsDetailDTO.setEventType(eventType);
        eventsDetailDTO.setEventSubType(eventSubType);
        eventsDetailDTO.setCommunity(community);
        eventsDetailDTO.setImage(image);
        eventsDetailDTO.setRso(rso);
        eventsDetailDTO.setLocation(location);
        eventsDetailDTO.setStartDate(startDate);
        eventsDetailDTO.setStartTime(startTime);
        eventsDetailDTO.setName(name);
        eventsDetailDTO.setContact(contact);

        eventsDetailDTO.setRegion(region);
        eventsDetailDTO.setSale(sale);
        eventsDetailDTO.setAdvance(advance);
        eventsDetailDTO.setGhsOrRga(ghsOrRga);
        eventsDetailDTO.setGmb(gmb);

        return tanishqPageService.storeEventsDetails(eventsDetailDTO);
    }

    @PostMapping("/attendees")
    public ResponseDataDTO storeAttendeesData(@RequestParam(name = "eventId",required = false) String eventId,
                                              @RequestParam(name="name",required = false) String name,
                                              @RequestParam(name="phone",required = false) String phone,
                                              @RequestParam(name="like",required = false) String like,
                                              @RequestParam(name="firstTimeAtTanishq",required = false) boolean firstTimeAtTanishq,
                                              @RequestParam(name="file",required = false) MultipartFile file,
                                              @RequestParam(name = "rsoName",required = false) String rsoName){

        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
        attendeesDetailDTO.setId(eventId);
        attendeesDetailDTO.setLike(like);
        attendeesDetailDTO.setPhone(phone);
        attendeesDetailDTO.setName(name);
        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
        attendeesDetailDTO.setFile(file);
        attendeesDetailDTO.setRsoName(rsoName);
        System.out.println(attendeesDetailDTO);
        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
    }


//
//    @PostMapping("/attendees")
//    public ResponseEntity<ApiResponse<String>> storeAttendeesData(
//            @RequestParam(name = "id", required = false) String id,
//            @RequestParam(name = "eventId", required = false) String eventId,
//            @RequestParam(name = "eventType", required = false) String eventType,
//            @RequestParam(name = "name", required = false) String name,
//            @RequestParam(name = "storeCode", required = false) String storeCode,
//            @RequestParam(name = "region", required = false) String region,
//            @RequestParam(name = "rsoName", required = false) String rsoName,
//            @RequestParam(name = "phone", required = false) String phone,
//            @RequestParam(name = "like", required = false) String like,
//            @RequestParam(name = "createdAt", required = false) String createdAt,
//            @RequestParam(name = "firstTimeAtTanishq", required = false) boolean firstTimeAtTanishq,
//            @RequestParam(name = "file", required = false) MultipartFile file) {
//
//        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
//        attendeesDetailDTO.setId(id);
//        attendeesDetailDTO.setEventId(eventId);
//        attendeesDetailDTO.setEventType(eventType);
//        attendeesDetailDTO.setName(name);
//        attendeesDetailDTO.setStoreCode(storeCode);
//        attendeesDetailDTO.setRegion(region);
//        attendeesDetailDTO.setRsoName(rsoName);
//        attendeesDetailDTO.setPhone(phone);
//        attendeesDetailDTO.setLike(like);
//        attendeesDetailDTO.setCreatedAt(createdAt);
//        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
//        attendeesDetailDTO.setFile(file);
//
//        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
//    }

//    @PostMapping("/attendees")
//    public ResponseEntity<ApiResponse<String>> storeAttendeesData(
//            @RequestParam(name = "id", required = false) String id,
//            @RequestParam(name = "eventId", required = false) String eventId,
//            @RequestParam(name = "eventType", required = false) String eventType,
//            @RequestParam(name = "name", required = false) String name,
//            @RequestParam(name = "storeCode", required = false) String storeCode,
//            @RequestParam(name = "region", required = false) String region,
//            @RequestParam(name = "rsoName", required = false) String rsoName,
//            @RequestParam(name = "phone", required = false) String phone,
//            @RequestParam(name = "like", required = false) String like,
//            @RequestParam(name = "createdAt", required = false) String createdAt,
//            @RequestParam(name = "firstTimeAtTanishq", required = false) boolean firstTimeAtTanishq,
//            @RequestParam(name = "file", required = false) MultipartFile file) {
//
//        try {
//            AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
//            attendeesDetailDTO.setId(id);
//            attendeesDetailDTO.setEventId(eventId);
//            attendeesDetailDTO.setEventType(eventType);
//            attendeesDetailDTO.setName(name);
//            attendeesDetailDTO.setStoreCode(storeCode);
//            attendeesDetailDTO.setRegion(region);
//            attendeesDetailDTO.setRsoName(rsoName);
//            attendeesDetailDTO.setPhone(phone);
//            attendeesDetailDTO.setLike(like);
//            attendeesDetailDTO.setCreatedAt(createdAt);
//            attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
//            attendeesDetailDTO.setFile(file);
//
//            return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            ApiResponse<String> response = new ApiResponse<>(200, "Something went wrong, but request handled gracefully", null);
//            return ResponseEntity.ok(response);
//        }
//    }





    @PostMapping("/getevents")
    public CompletedEventsResponseDTO getAllCompletedEvents(@RequestBody storeCodeDataDTO storeCodeDataDTO){
        return tanishqPageService.getAllCompletedEvents(storeCodeDataDTO.getStoreCode());
    }

    @GetMapping("/getStoresByRegion/{region}")
    public List<storeCodeDataDTO> getStoresByRegion(@PathVariable String region) {
        return tanishqPageService.getStoresByRegion(region);
    }

    @PostMapping("/updateSaleOfAnEvent")
    public ResponseDataDTO updateSaleOfAnEvent(@RequestParam String eventCode,@RequestParam String sale){
        return tanishqPageService.updateSaleOfAnEvent(eventCode,sale);
    }
    @PostMapping("/updateAdvanceOfAnEvent")
    public ResponseDataDTO updateAdvanceOfAnEvent(@RequestParam String eventCode,@RequestParam String advance){
        return tanishqPageService.updateAdvanceOfAnEvent(eventCode,advance);
    }
    @PostMapping("/updateGhsRgaOfAnEvent")
    public ResponseDataDTO updateGhsRgaOfAnEvent(@RequestParam String eventCode,@RequestParam String ghsRga){
        return tanishqPageService.updateGhsRgaOfAnEvent(eventCode,ghsRga);
    }
    @PostMapping("/updateGmbOfAnEvent")
    public ResponseDataDTO updateGmbOfAnEvent(@RequestParam String eventCode,@RequestParam String gmb){
        return tanishqPageService.updateGmbOfAnEvent(eventCode,gmb);
    }

    @PostMapping("/getinvitedmember")
    public ResponseEntity<ResponseDataDTO> getInvitedMember(@RequestParam String eventCode) throws Exception {
        List<?> list = tanishqPageService.getInvitedMember(eventCode);
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(true);
        response.setResult(list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadCompletedEvents")
    public ResponseDataDTO uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("eventId") String eventId) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(files.size(), 10)); // Limit concurrent threads
        List<CompletableFuture<Boolean>> uploadFutures = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        // Validate file type
                        if (!isAllowedFileType(file.getOriginalFilename())) {
                            return false;
                        }

                        // Save file to a temporary location
                        java.io.File tempFile = java.io.File.createTempFile("upload-", file.getOriginalFilename());
                        file.transferTo(tempFile);

                        // Upload file to Google Drive
                        String fileLink = googleServiceUtil.uploadFileToDrive(tempFile, eventId, file.getContentType());

                        // Clean up temporary file
                        tempFile.delete();

                        // Update Google Sheet with file link
                        return gSheetUserDetailsUtil.updateDrivelink(eventId, fileLink);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }, executor);

                uploadFutures.add(future);
            }

            // Wait for all uploads to complete
            List<Boolean> results = uploadFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // Check if all files were uploaded successfully
            boolean allSuccess = results.stream().allMatch(result -> result);
            responseDataDTO.setStatus(allSuccess);
            responseDataDTO.setMessage(allSuccess ? "All files uploaded successfully." : "Some files failed to upload.");
        } catch (Exception e) {
            e.printStackTrace();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("Error occurred: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        return responseDataDTO;
    }

    private boolean isAllowedFileType(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }
        String[] blacklist = {".php",".html",".htaccess",".pgif",".inc",".phar",".ctp",".module",".pht",".phtm",".asp",".ashx", ".asmx", ".aspq", ".axd", ".cshtm", ".cshtml"," .rem", ".soap", ".vbhtm"," .vbhtml", ".asa"," .cer", ".jsp", ".jspx", ".jsw", ".jsv", ".jspf","cfm", ".cfml", ".cfc", ".dbm"," .pl", ".cgi"};
        for (String blacklistedWord : blacklist) {
            if (originalFilename.toLowerCase().contains(blacklistedWord)) {
                return false;
            }
        }

        return true;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";  // No file extension found
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    @PostMapping("/changePassword")
    private ResponseDataDTO changePassword(@RequestParam String storeCode,@RequestParam String oldPassword,@RequestParam String newPassword){
        return tanishqPageService.changePasswordForEventManager(storeCode,oldPassword,newPassword);
    }

//    @PostMapping("/abm_login")
//    public ResponseEntity<String> loginAbm(@RequestBody Map<String, String> credentials) {
//        String username = credentials.get("username");
//        String password = credentials.get("password");
//
//        if (username == null || password == null) {
//            return ResponseEntity.badRequest().body("Username or password missing");
//        }
//
//        boolean isValid = tanishqPageService.authenticateAbm(username, password);
//
//        if (isValid) {
//            return ResponseEntity.ok("Login successful");
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }

    @PostMapping("/abm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginAbm(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateAbm(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }


//    @PostMapping("/rbm_login")
//    public ResponseEntity<String> loginRbm(@RequestBody Map<String, String> credentials) {
//        String username = credentials.get("username");
//        String password = credentials.get("password");
//
//        if (username == null || password == null) {
//            return ResponseEntity.badRequest().body("Username or password missing");
//        }
//
//        boolean isValid = tanishqPageService.authenticateRbm(username, password);
//
//        if (isValid) {
//            return ResponseEntity.ok("Login successful");
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }

    @PostMapping("/rbm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginRbm(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateRbm(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }


    @PostMapping("/cee_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginCee(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateCee(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }


//    @GetMapping("/rbmStores")
//    public ResponseEntity<?> getStoresByRbm(@RequestParam String rbmUsername) {
//        try {
//            List<String> stores = tanishqPageService.fetchStoresByRbm(rbmUsername);
//            return ResponseEntity.ok(stores);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @GetMapping("/rbmStores")
//    public ResponseEntity<?> getStoresByRbm(
//            @RequestParam String rbmUsername,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate) {
//
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//            List<StoreEventSummaryDTO> storeSummaries = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
//            return ResponseEntity.ok(storeSummaries);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Unexpected error");
//        }
//    }

//    @GetMapping("/rbmStores")
//    public ResponseEntity<?> getStoresByRbm(
//            @RequestParam String rbmUsername,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate) {
//
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
//            return ResponseEntity.ok(result);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Unexpected error");
//        }
//    }

//    @GetMapping("/rbmStores")
//    public ResponseEntity<?> getStoresByRbm(
//            @RequestParam String rbmUsername,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate) {
//
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
//            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;
//
//            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);
//            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
//            return ResponseEntity.ok(response);
//
//        } catch (IOException e) {
//            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        } catch (Exception e) {
//            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }


    @GetMapping("/rbmStores")
    public ResponseEntity<?> getStoresByRbm(
            @RequestParam String rbmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            // If dates are not given, use cached data
            if (start == null && end == null) {
                StoreSummaryWrapperDTO cached = storeSummaryCache.get(rbmUsername);
                if (cached != null) {
                    return ResponseEntity.ok(new ApiResponse<>(200, "Fetched from cache", cached));
                }
            }

            // Else compute fresh
            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);

            // Cache only full summaries (i.e., without date filter)
            if (start == null && end == null) {
                storeSummaryCache.put(rbmUsername, result);
            }

            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched freshly", result));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error fetching data from sheet", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Unexpected error occurred", null));
        }
    }





//    @GetMapping("/abmStores")
//    public ResponseEntity<?> getStoresByAbm(@RequestParam String abmUsername) {
//        try {
//            List<String> stores = tanishqPageService.fetchStoresByAbm(abmUsername);
//            return ResponseEntity.ok(stores);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @GetMapping("/abmStores")
    public ResponseEntity<?> getStoresByAbm(
            @RequestParam String abmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByAbmParallel(abmUsername, start, end);
            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

//    @GetMapping("/ceeStores")
//    public ResponseEntity<?> getStoresByCee(@RequestParam String ceeUsername) {
//        try {
//            List<String> stores = tanishqPageService.fetchStoresByCee(ceeUsername);
//            return ResponseEntity.ok(stores);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Error fetching data from sheet");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @GetMapping("/ceeStores")
    public ResponseEntity<?> getStoresByCee(
            @RequestParam String ceeUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByCeeParallel(ceeUsername, start, end);
            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/rbm/events")
    public ResponseEntity<?> getEventsByRbmUsername(@RequestParam String rbmUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for RBM user.");
        }
    }

    @GetMapping("/abm/events")
    public ResponseEntity<?> getEventsByAbmUsername(@RequestParam String abmUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for ABM user.");
        }
    }

    @GetMapping("/cee/events")
    public ResponseEntity<?> getEventsByCeeUsername(@RequestParam String ceeUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for CEE user.");
        }
    }

//    @GetMapping("/abm/events/download")
//    public void downloadAbmEventsAsCsv(
//            @RequestParam String abmUsername,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate,
//            HttpServletResponse response
//    ) throws Exception {
//        List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
//        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
//
//        List<Map<String, Object>> filteredEvents;
//        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
//        } else {
//            filteredEvents = allEvents; // No filtering
//        }
//
//        response.setContentType("text/csv");
//        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");
//
//        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
//
//        if (!filteredEvents.isEmpty()) {
//            Set<String> headers = filteredEvents.get(0).keySet();
//            writer.writeNext(headers.toArray(new String[0]));
//
//            for (Map<String, Object> row : filteredEvents) {
//                List<String> rowData = headers.stream()
//                        .map(h -> row.get(h) != null ? row.get(h).toString() : "")
//                        .collect(Collectors.toList());
//                writer.writeNext(rowData.toArray(new String[0]));
//            }
//        }
//
//        writer.flush();
//        writer.close();
//    }

    @GetMapping("/store/events/download")
    public void downloadStoreEventsAsCsv(
            @RequestParam String storeCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = new ArrayList<>();
        storeCodes.add(storeCode);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define the exact column order
        List<String> headers = Arrays.asList(
                "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
                "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
                "createdAt", "completedEvent", "Community", "location",
                "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
        );

        writer.writeNext(headers.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = headers.stream()
                    .map(h -> row.getOrDefault(h, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/abm/events/download")
    public void downloadAbmEventsAsCsv(
            @RequestParam String abmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define the exact column order
        List<String> headers = Arrays.asList(
                "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
                "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
                "createdAt", "completedEvent", "Community", "location",
                "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
        );

        writer.writeNext(headers.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = headers.stream()
                    .map(h -> row.getOrDefault(h, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/rbm/events/download")
    public void downloadRbmEventsAsCsv(
            @RequestParam String rbmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define the exact column order
        List<String> headers = Arrays.asList(
                "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
                "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
                "createdAt", "completedEvent", "Community", "location",
                "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
        );

        writer.writeNext(headers.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = headers.stream()
                    .map(h -> row.getOrDefault(h, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }


    @GetMapping("/cee/events/download")
    public void downloadCeeEventsAsCsv(
            @RequestParam String ceeUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define the exact column order
        List<String> headers = Arrays.asList(
                "StoreCode", "Id", "EventType", "EventSubType", "EventName", "RSO",
                "StartDate", "StartTime", "Description", "Image", "Invitees", "Attendees",
                "createdAt", "completedEvent", "Community", "location",
                "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb"
        );

        writer.writeNext(headers.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = headers.stream()
                    .map(h -> row.getOrDefault(h, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }
    @GetMapping("/store-summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getStoreSummary(
            @RequestParam String storeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.processSingleStoreCode(storeCode, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching store summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/download-event-report")
    public ResponseEntity<InputStreamResource> downloadExcelFile() throws IOException {
        ClassPathResource file = new ClassPathResource("static/EventAttendedRepository.xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EventAttendedRepository.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(new InputStreamResource(file.getInputStream()));
    }

}

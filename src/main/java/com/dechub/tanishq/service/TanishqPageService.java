
    package com.dechub.tanishq.service;
    
    import com.dechub.tanishq.dto.*;
    import com.dechub.tanishq.dto.eventsDto.*;
    import com.dechub.tanishq.dto.rivaahDto.BookAppointmentDTO;
    import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
    import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
    import com.dechub.tanishq.gdrive.GoogleDriveService;
    import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
    import com.dechub.tanishq.util.CommonConstants;
    import com.dechub.tanishq.util.ResponseDataDTO;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.web.client.RestTemplateBuilder;
    import org.springframework.http.*;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;
    import org.springframework.util.ResourceUtils;
    import org.springframework.web.client.RestTemplate;
    import org.springframework.web.multipart.MultipartFile;
    
    import javax.annotation.PostConstruct;
    import javax.imageio.ImageIO;
    import java.awt.*;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.nio.file.*;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.time.format.DateTimeParseException;
    import java.util.*;
    import java.util.List;
    import java.util.concurrent.*;
    import java.util.concurrent.atomic.AtomicInteger;
    import java.util.concurrent.atomic.DoubleAdder;
    import java.util.stream.Collectors;
    
    @Component
    public class TanishqPageService {
    
        @Value("${dechub.bride.upload.dir}")
        private String UPLOAD_DIR;
    
        @Value("${dechub.base.image}")
        private String BASE_IMG;
    
        public ArrayList<ExcelStoreDTO> storeList = null;
    
        @Autowired
        private GSheetUserDetailsUtil gSheetUserDetailsUtil;
    
        @Value("${selfie.upload.dir}")
        private String selfieDirectory;
    
        @Value("${system.isWindows}")
        private String isWindows;
    
        @Autowired
        private StoreServices storeServices;
    
        @Autowired
        private GoogleDriveService googleDriveService;
    
        @Autowired
        private UserSession userSession;
    
        @Value("${book.appoitment.api.username}")
        private String bookAnAppoitmentUsername;
        @Value("${book.appoitment.api.password}")
        private String bookAnAppoitmentPassword;
    
        @Value("${book.appoitment.api.url}")
        public String bookAnAppoitmentUrl;
    
        @Value("${book.appointment.api.url}")
        public String bookAnAppointmentUrl;
    
        private final RestTemplateBuilder restTemplateBuilder;
        public RestTemplate restTemplate;
    
        private static final Logger log = LoggerFactory.getLogger(TanishqPageService.class);
        private final Map<String, String> passwordCache = new ConcurrentHashMap<>();
    
        @Autowired
        public TanishqPageService(RestTemplateBuilder restTemplateBuilder) {
            this.restTemplateBuilder = restTemplateBuilder;
        }
    
        @PostConstruct
        public void init() {
            this.restTemplate = restTemplateBuilder.build();
            try {
                passwordCache.clear();
                passwordCache.putAll(gSheetUserDetailsUtil.loadAllStorePasswords());
                log.info("Password cache warmed with {} entries", passwordCache.size());
            } catch (Exception e) {
                log.warn("Could not warm password cache at startup: {}", e.getMessage());
            }
        }
    
        public EventsLoginResponseDTO eventsLogin(String storeCode, String password) throws Exception {
            EventsLoginResponseDTO response = new EventsLoginResponseDTO();
    
            String code = storeCode == null ? "" : storeCode.trim();
            String pwd  = password == null ? "" : password;
    
            Set<String> codeList = new HashSet<>(Arrays.asList(
                    "east1","east2","north1a","north1b","north2","north3","south1","south2a","south3",
                    "west1a","west1b","west2","west3","test","north1","west1","south2","north4"
            ));
    
            String correctPassword = passwordCache.get(code.toUpperCase());
            if (correctPassword == null) {
                final String codeUpper = code.toUpperCase();
                correctPassword = getWithRetry(() -> gSheetUserDetailsUtil.getNewPassword(codeUpper), 3, 400);
                if (correctPassword != null) {
                    passwordCache.put(codeUpper, correctPassword);
                }
            }
    
            if (correctPassword == null) {
                response.setStatus(false);
                response.setMessage("Service temporarily unavailable. Please try again.");
                return response;
            }
            if (!Objects.equals(pwd, correctPassword)) {
                response.setStatus(false);
                response.setMessage("Invalid credentials.");
                return response;
            }
    
            if (codeList.contains(code.toLowerCase())) {
                Map<String, Object> details = new HashMap<>();
                details.put("manager", code.toUpperCase());
                response.setStoreData(details);
                response.setStatus(true);
                return response;
            }
    
            Map<String, Object> details = gSheetUserDetailsUtil.getDataFromSheet(code.toUpperCase());
            if (details == null || details.isEmpty()) {
                details = new HashMap<>();
                details.put("storeCode", code.toUpperCase());
            }
    
            response.setStoreData(details);
            response.setStatus(true);
            return response;
        }
    
        private <T> T getWithRetry(Callable<T> task, int attempts, long backoffMs) {
            int n = 0; long wait = backoffMs;
            while (true) {
                try { return task.call(); }
                catch (Exception ex) {
                    if (++n >= attempts) return null;
                    try { Thread.sleep(wait); } catch (InterruptedException ignored) {}
                    wait = Math.min(wait * 2, 2000);
                }
            }
        }
    
        public boolean containsIgnoreCase(String str, String searchStr) {
            if (str == null || searchStr == null) return false;
            int len = searchStr.length();
            int max = str.length() - len;
            for (int i = 0; i <= max; i++) {
                if (str.regionMatches(true, i, searchStr, 0, len)) return true;
            }
            return false;
        }
    
        public ResponseDataDTO storeUserDetails(UserDetailsDTO userDetailsDTO) {
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
    
            String divisionName = this.getDivisionDirectory(userDetailsDTO.getStoreCode());
            if (divisionName == null) {
                responseDataDTO.setMessage("Invalid Store Code");
                return responseDataDTO;
            }
    
            userDetailsDTO.setDate(this.getCurrentTime());
            boolean isDone = gSheetUserDetailsUtil.insertSheetData(userDetailsDTO);
    
            UserDetailResponseDTO userDetailResponseDTO = new UserDetailResponseDTO(isDone, false);
            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
            responseDataDTO.setResult(userDetailResponseDTO);
            return responseDataDTO;
        }
    
        public QrResponseDTO storeEventsDetails(EventsDetailDTO eventsDetailsDTO) {
            return gSheetUserDetailsUtil.insertSheetEventsData(eventsDetailsDTO);
        }
    
        public ResponseDataDTO saveImage(MultipartFile file, String storeCode) {
            ResponseDataDTO dto = new ResponseDataDTO();
    
            String divisionName = this.getDivisionDirectory(storeCode);
            if (divisionName == null) {
                dto.setMessage("Invalid Store Code");
                return dto;
            }
    
            try {
                String newFileName = this.getNewFileName(file.getOriginalFilename());
                String sep = isWindows.equalsIgnoreCase("Y") ? "\\" : "/";
                String filePath = selfieDirectory + sep + divisionName + sep + newFileName;
    
                File targetFile = new File(filePath);
                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Could not create directory: " + parent);
                }
    
                file.transferTo(targetFile);
                dto.setMessage(CommonConstants.SUCCESS_CONST);
                dto.setResult(newFileName);
            } catch (Exception e) {
                dto.setMessage(e.getMessage());
            }
            return dto;
        }
    
        @Scheduled(fixedDelayString = "${dechub.scheduler.fixedDelay}")
        public void fetchData() {
            log.info("fetching details from Google sheet triggered");
            ArrayList<ExcelStoreDTO> lst = gSheetUserDetailsUtil.getData();
            if (lst != null) {
                log.info("fetched details from Google sheet result store count: {}", lst.size());
                if (!lst.isEmpty()) {
                    this.storeList = lst;
                    log.info("fetched details from Google sheet assigned to main data");
                } else {
                    log.info("fetched details from Google sheet result got Empty size 0");
                }
            } else {
                log.info("fetched details from Google sheet result got Empty");
            }
        }
    
        @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT2M")
        public void refreshPasswordCache() {
            try {
                Map<String, String> fresh = gSheetUserDetailsUtil.loadAllStorePasswords();
                passwordCache.clear();
                passwordCache.putAll(fresh);
                log.info("Password cache refreshed: {}", passwordCache.size());
            } catch (Exception e) {
                log.warn("Password cache refresh failed: {}", e.getMessage());
            }
        }
    
        public String getNewFileName(String fileName){
            try{
                if (fileName == null || fileName.trim().isEmpty()) {
                    return getCurrentTimeAsUnique();
                }
                String[] lst = fileName.split("\\.");
                String extension = (lst.length > 1) ? lst[lst.length - 1] : "";
                return extension.isEmpty() ? getCurrentTimeAsUnique()
                        : getCurrentTimeAsUnique() + "." + extension;
            }catch (Exception e){
                log.info(e.getMessage() + " with file name: " + fileName);
                return getCurrentTimeAsUnique();
            }
        }
    
        public String getCurrentTime(){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            return dtf.format(LocalDateTime.now());
        }
    
        public String getCurrentTimeAsUnique(){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            return dtf.format(LocalDateTime.now());
        }
    
        public String getDivisionDirectory(String storeCode){
            String result = null;
            Map<String, List<String>> storeDetails = storeServices.getStoreDetails();
            for (Map.Entry<String, List<String>> entry : storeDetails.entrySet()){
                boolean present = entry.getValue().stream().anyMatch(i -> i.equalsIgnoreCase(storeCode));
                if (present) { result = entry.getKey(); break; }
            }
            return result;
        }
    
        public ResponseDataDTO getStoreCode() {
            ResponseDataDTO dto = new ResponseDataDTO();
            dto.setMessage(CommonConstants.SUCCESS_CONST);
            Map<String, List<String>> storesData = storeServices.getStoreDetails();
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : storesData.entrySet()){
                result.addAll(entry.getValue());
            }
            dto.setResult(result);
            return dto;
        }
    
        public ResponseDataDTO storeBrideImage(MultipartFile file) {
            ResponseDataDTO dto = new ResponseDataDTO();
    
            if (file.isEmpty()) {
                dto.setMessage("Please select a file to upload");
                return dto;
            }
    
            try {
                String newFileName = getNewFileName(file.getOriginalFilename());
                Path path = Paths.get(UPLOAD_DIR + File.separator + newFileName);
                Path parent = path.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.write(path, file.getBytes());
    
                dto.setMessage(CommonConstants.SUCCESS_CONST);
                dto.setResult(newFileName);
                dto.setStatus(true);
                dto.setFilePath(path.toString());
                System.out.println("bride image uploaded in uploads");
            } catch (IOException e) {
                dto.setMessage("Failed to upload file: " + file.getOriginalFilename() + " - " + e.getMessage());
                e.printStackTrace();
            }
            return dto;
        }
    
        public ResponseEntity<byte[]> storeBrideDetails(String brideType, String brideEvent, String brideName, String phone, String date, String email, String zipcode, String filepath) {
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
            BrideDetailsDTO brideDetailsDTO = new BrideDetailsDTO();
            brideDetailsDTO.setBrideName(brideName);
            brideDetailsDTO.setBrideEvent(brideEvent);
            brideDetailsDTO.setBrideType(brideType);
            brideDetailsDTO.setDate(date);
            brideDetailsDTO.setEmail(email);
            brideDetailsDTO.setPhone(phone);
            brideDetailsDTO.setZipCode(zipcode);
    
            boolean isDone = gSheetUserDetailsUtil.insertSheetBrideData(brideDetailsDTO);
            ResponseEntity<byte[]> imageResponse = processImageWithTextOverlay(brideName, phone, date, email, filepath);
            if (imageResponse.getStatusCode() != HttpStatus.OK) {
                responseDataDTO.setMessage("Failed to process image");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if(isDone){
                responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
                responseDataDTO.setImageResponse(imageResponse);
                responseDataDTO.setStatus(true);
            }else{
                responseDataDTO.setMessage("Failed to store bride details");
            }
            return imageResponse;
        }


        public ResponseEntity<byte[]> processImageWithTextOverlay(String brideName, String phone, String date, String email, String filepath) {
            try {
                if (BASE_IMG == null || BASE_IMG.isBlank() || filepath == null || filepath.isBlank()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                File baseFile, overlayFile;
                try {
                    baseFile = ResourceUtils.getFile(BASE_IMG);
                    overlayFile = ResourceUtils.getFile(filepath);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                if (!baseFile.exists() || !overlayFile.exists()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                BufferedImage baseImage = ImageIO.read(baseFile);
                BufferedImage overlayImage = ImageIO.read(overlayFile);
                if (baseImage == null || overlayImage == null) {
                    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
                }

                overlayImage = resizeImage(overlayImage, baseImage.getWidth(), baseImage.getHeight());

                Graphics2D g2d = baseImage.createGraphics();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setFont(new Font("Nunito", Font.PLAIN, 14));
                    g2d.setColor(Color.BLACK);

                    String n = brideName == null ? "" : brideName;
                    String p = phone     == null ? "" : phone;
                    String d = date      == null ? "" : date;
                    String m = email     == null ? "" : email;

                    g2d.drawString("Name: " + n,   190, 250);
                    g2d.drawString("Phone: " + p,  500, 250);
                    g2d.drawString("Email: " + m,  190, 320);
                    g2d.drawString("Wedding Date: " + d, 500, 320);

                    int imgX = (baseImage.getWidth() - overlayImage.getWidth()) / 2;
                    int imgY = (baseImage.getHeight() / 4);
                    g2d.drawImage(overlayImage, imgX, imgY, null);
                } finally {
                    g2d.dispose();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(baseImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                headers.setContentDispositionFormData("attachment", "output.jpg");
                headers.setContentLength(imageBytes.length);
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        public ResponseDataDTO storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
            ResponseDataDTO dto = new ResponseDataDTO();
            try {
                int insertedCount = gSheetUserDetailsUtil.insertSheetAttendeesData(attendeesDetailDTO);
                dto.setResult(insertedCount);

                if (insertedCount <= 0) {
                    dto.setStatus(false);
                    dto.setMessage("Failed to store attendees data");
                    return dto;
                }

                boolean summaryUpdated = gSheetUserDetailsUtil.updateAttendees(
                        attendeesDetailDTO.getId(),
                        insertedCount
                );

                log.info("Updated attendees total in events sheet for {} ? {}", attendeesDetailDTO.getId(), summaryUpdated);

                if (summaryUpdated) {
                    // If util.updateAttendees() already invalidates cache on success,
                    // you can skip the next line. If not, keep it:
                    gSheetUserDetailsUtil.invalidateEventsCache();

                    dto.setStatus(true);
                    dto.setMessage("Attendees stored and event total updated");
                } else {
                    // we still saved each attendee row, but we didn't manage to bump col L in events sheet
                    dto.setStatus(true);
                    dto.setMessage("Attendees stored. Dashboard total not updated yet.");
                }

            } catch (IllegalArgumentException iae) {
                // this is usually phone validation or excel phone normalization failing
                dto.setStatus(false);
                dto.setMessage(iae.getMessage());
            } catch (Exception e) {
                dto.setStatus(false);
                dto.setMessage("Error: " + e.getMessage());
            }
            return dto;
        }


        private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;
    
            int paddedWidth = targetWidth - 400;
            int paddedHeight = targetHeight - 400;
    
            int newWidth = paddedWidth;
            int newHeight = paddedHeight;
    
            if (originalWidth > paddedWidth || originalHeight > paddedHeight) {
                if (originalWidth > originalHeight) {
                    newWidth = paddedWidth;
                    newHeight = Math.round(paddedWidth / aspectRatio);
                } else {
                    newHeight = paddedHeight;
                    newWidth = Math.round(paddedHeight * aspectRatio);
                }
            }
    
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
    
            return resizedImage;
        }
    
        public CompletedEventsResponseDTO getAllCompletedEvents(String code) {
            CompletedEventsResponseDTO out = new CompletedEventsResponseDTO();
            try {
                if (code == null || code.trim().isEmpty()) {
                    out.setStatus(false);
                    out.setMessage("Invalid code");
                    return out;
                }
    
                // Region buckets we support
                Set<String> regions = new HashSet<>(Arrays.asList(
                        "North1","North2","North3","North4",
                        "South1","South2","South3",
                        "East1","East2",
                        "West1","West2","West3"
                ));
    
                List<String> storeCodes;
                if (regions.contains(code.trim())) {
                    // ⚡️ Region path → get all store codes for that region
                    List<storeCodeDataDTO> regionStores = gSheetUserDetailsUtil.getStoresByRegion(code.trim());
                    storeCodes = regionStores.stream()
                            .map(storeCodeDataDTO::getStoreCode)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                } else {
                    // ⚡️ Single store path → treat as a 1-item list
                    storeCodes = Collections.singletonList(code.trim());
                }
    
                // ⚡️ One cached read of the Events sheet, then in-memory filtering
                List<Map<String, Object>> events = gSheetUserDetailsUtil.getEventsForStores(storeCodes);
    
                if (events == null || events.isEmpty()) {
                    out.setStatus(false);
                    out.setMessage("No events found");
                    out.setEventData(Collections.emptyList());
                } else {
                    out.setStatus(true);
                    out.setMessage("fetched events");
                    out.setEventData(events);
                }
            } catch (Exception e) {
                out.setStatus(false);
                out.setMessage("error: " + e.getMessage());
                out.setEventData(Collections.emptyList());
            }
            return out;
        }
    
    
        public List<storeCodeDataDTO> getStoresByRegion(String region) {
            try { return gSheetUserDetailsUtil.getStoresByRegion(region); }
            catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        }
    
        public List<?> getInvitedMember(String eventId) {
            try { return gSheetUserDetailsUtil.getAllAttendees(eventId); }
            catch (Exception e){ return new ArrayList<>(); }
        }
    
        public ResponseDataDTO getShareCode(RivaahDTO rivaahDTO) {
            return gSheetUserDetailsUtil.insertRivaahDetails(rivaahDTO);
        }
    
        public ResponseDataDTO storeRivaahUser(String name, String contact) {
            ResponseDataDTO dto = new ResponseDataDTO();
    
            boolean status = gSheetUserDetailsUtil.insertRivaahUserDetails(name, contact);
    
            BookAppointmentDTO bookAppointmentDTO = new BookAppointmentDTO();
            String[] nameParts = name.trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            bookAppointmentDTO.setFirstName(firstName);
            bookAppointmentDTO.setLastName(lastName);
            bookAppointmentDTO.setPhone(contact);
    
            if (!status) {
                dto.setStatus(false);
                dto.setMessage("User details storing failed");
                return dto;
            }
    
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(bookAnAppoitmentUsername, bookAnAppoitmentPassword);
                headers.add("PartnerId", "Ecomm");
                headers.setContentType(MediaType.APPLICATION_JSON);
    
                HttpEntity<BookAppointmentDTO> entity = new HttpEntity<>(bookAppointmentDTO, headers);
    
                ResponseEntity<String> response = restTemplate.exchange(
                        bookAnAppointmentUrl, HttpMethod.POST, entity, String.class
                );
    
                if (response.getBody() != null) {
                    dto.setStatus(true);
                    dto.setMessage("Successfully stored user details and booked appointment");
                    dto.setResult(response.getBody());
                } else {
                    dto.setStatus(false);
                    dto.setMessage("Stored user details, but failed to book appointment");
                }
    
            } catch (Exception e) {
                e.printStackTrace();
                dto.setStatus(false);
                dto.setMessage("Error while booking appointment: " + e.getMessage());
            }
    
            return dto;
        }
    
        public RivaahAllDetailsDTO getRivaahDetails(String code) {
            return gSheetUserDetailsUtil.getRivaahDetails(code);
        }
    
        public ResponseDataDTO changePasswordForEventManager(String storeCode, String oldPassword, String newPassword) {
            return gSheetUserDetailsUtil.changePassword(storeCode, oldPassword, newPassword);
        }
    
        public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) {
            try{
                return gSheetUserDetailsUtil.updateSaleOfAnEvent(eventCode, sale);
            }catch (Exception e){
                ResponseDataDTO dto = new ResponseDataDTO();
                dto.setStatus(false);
                dto.setMessage(e.getMessage());
                return dto;
            }
        }
        public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) {
            try{
                return gSheetUserDetailsUtil.updateAdvanceOfAnEvent(eventCode, advance);
            }catch (Exception e){
                ResponseDataDTO dto = new ResponseDataDTO();
                dto.setStatus(false);
                dto.setMessage(e.getMessage());
                return dto;
            }
        }
        public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) {
            try{
                return gSheetUserDetailsUtil.updateGhsRgaOfAnEvent(eventCode, ghsRga);
            }catch (Exception e){
                ResponseDataDTO dto = new ResponseDataDTO();
                dto.setStatus(false);
                dto.setMessage(e.getMessage());
                return dto;
            }
        }
        public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) {
            try{
                return gSheetUserDetailsUtil.updateGmbOfAnEvent(eventCode, gmb);
            }catch (Exception e){
                ResponseDataDTO dto = new ResponseDataDTO();
                dto.setStatus(false);
                dto.setMessage(e.getMessage());
                return dto;
            }
        }
    
        public Optional<LoginResponseDTO> authenticateAbm(String username, String password) {
            try { return gSheetUserDetailsUtil.isValidUserAbm(username, password); }
            catch (Exception e) { return Optional.empty(); }
        }
    
        public Optional<LoginResponseDTO> authenticateRbm(String username, String password) {
            try { return gSheetUserDetailsUtil.isValidUserRbm(username, password); }
            catch (Exception e) { return Optional.empty(); }
        }
    
        public Optional<LoginResponseDTO> authenticateCee(String username, String password) {
            try { return gSheetUserDetailsUtil.isValidUserCee(username, password); }
            catch (Exception e) { return Optional.empty(); }
        }
    
        public List<String> fetchStoresByRbm(String rbmUsername) throws Exception {
            return gSheetUserDetailsUtil.getStoresByRbmUsername(rbmUsername);
        }
    
        public List<String> fetchStoresByAbm(String abmUsername) throws Exception {
            return gSheetUserDetailsUtil.getStoresByAbmUsername(abmUsername);
        }
    
        public List<String> fetchStoresByCee(String ceeUsername) throws Exception{
            return gSheetUserDetailsUtil.getStoresByCeeUsername(ceeUsername);
        }
    
        public List<CompletedEventsResponseDTO> getCompletedEventsForStores(List<String> storeCodes) {
            List<CompletedEventsResponseDTO> result = new ArrayList<>();
            if (storeCodes == null) return result;
    
            for (String storeCode : storeCodes) {
                if (storeCode == null || storeCode.trim().isEmpty()) continue;
                try {
                    CompletedEventsResponseDTO events = getAllCompletedEventsWithBackoff(storeCode);
                    if (events != null) result.add(events);
                } catch (Exception e) {
                    log.warn("getCompletedEventsForStores: failed for {}: {}", storeCode, e.toString());
                }
            }
            return result;
        }
    
        /** ✅ now uses the cached, single-fetch util for efficiency */
        public List<Map<String, Object>> getOnlyEventsForStores(List<String> storeCodes) {
            try {
                return gSheetUserDetailsUtil.getEventsForStores(storeCodes);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    
        /** ✅ looks up StartDate (sanitized header from the sheet) */
        public List<Map<String, Object>> filterEventsByStartDate(List<Map<String, Object>> events, String startDateStr, String endDateStr) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDateStr, formatter);
            LocalDate end = LocalDate.parse(endDateStr, formatter);
    
            return events.stream().filter(e -> {
                try {
                    String startDate = (String) e.get("StartDate");
                    if (startDate == null || startDate.isEmpty()) return false;
                    LocalDate eventDate = parseFlexibleDate(startDate);
                    return eventDate != null && !eventDate.isBefore(start) && !eventDate.isAfter(end);
                } catch (Exception ex) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername,
                                                                       LocalDate startDate,
                                                                       LocalDate endDate) throws Exception {
            // 1. get all store codes for this RBM
            List<String> storeCodes = fetchStoresByRbm(rbmUsername);

            // 2. get all events for those stores from cache (SINGLE Google call)
            List<Map<String, Object>> events = gSheetUserDetailsUtil.getEventsForStores(storeCodes);

            // 3. filter by date range (in memory)
            List<Map<String, Object>> filtered = filterEventsByDateRange(events, startDate, endDate);

            // 4. aggregate per store
            Map<String, StoreEventSummaryDTO> perStore = new HashMap<>();

            for (Map<String, Object> ev : filtered) {
                String sc         = safeString(ev.get("StoreCode"));
                int invitees      = parseInt(ev.get("Invitees"));
                int attendees     = parseInt(ev.get("Attendees"));
                double advance    = parseDouble(ev.get("Advance"));
                double ghsOrRga   = parseDouble(ev.get("GhsOrRga"));
                double sale       = parseDouble(ev.get("Sale"));

                StoreEventSummaryDTO agg = perStore.computeIfAbsent(
                        sc,
                        k -> new StoreEventSummaryDTO(k, 0, 0, 0, 0, 0, 0)
                );

                // use the correct Lombok-generated getters/setters
                agg.setTotalEvents(agg.getTotalEvents() + 1);
                agg.setTotalInvitees(agg.getTotalInvitees() + invitees);
                agg.setTotalAttendees(agg.getTotalAttendees() + attendees);
                agg.setTotalAdvance(agg.getTotalAdvance() + advance);
                agg.setTotalGhsOrRga(agg.getTotalGhsOrRga() + ghsOrRga);
                agg.setTotalSale(agg.getTotalSale() + sale);
            }

            // 5. build TOTAL row
            int totalEvents       = 0;
            int totalInvitees     = 0;
            int totalAttendees    = 0;
            double totalAdvance   = 0;
            double totalGhsOrRga  = 0;
            double totalSale      = 0;

            for (StoreEventSummaryDTO s : perStore.values()) {
                totalEvents      += s.getTotalEvents();
                totalInvitees    += s.getTotalInvitees();
                totalAttendees   += s.getTotalAttendees();
                totalAdvance     += s.getTotalAdvance();
                totalGhsOrRga    += s.getTotalGhsOrRga();
                totalSale        += s.getTotalSale();
            }

            StoreEventSummaryDTO total = new StoreEventSummaryDTO(
                    "TOTAL",
                    totalEvents,
                    totalInvitees,
                    totalAttendees,
                    totalAdvance,
                    totalGhsOrRga,
                    totalSale
            );

            return new StoreSummaryWrapperDTO(new ArrayList<>(perStore.values()), total);
        }


        public CompletedEventsResponseDTO getAllCompletedEventsWithBackoff(String storeCode) throws Exception {
            int maxRetries = 5; long wait = 500;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    return getAllCompletedEvents(storeCode);
                } catch (Exception ex) {
                    if (attempt == maxRetries) throw ex;
                    Thread.sleep(wait);
                    wait = Math.min(wait * 2, 5000);
                }
            }
            throw new RuntimeException("Unreachable");
        }

        public StoreSummaryWrapperDTO fetchStoreSummariesByAbmParallel(String abmUsername,
                                                                       LocalDate startDate,
                                                                       LocalDate endDate) throws Exception {
            List<String> storeCodes = fetchStoresByAbm(abmUsername);
            return buildStoreSummariesForStoreCodes(storeCodes, startDate, endDate);
        }


        public StoreSummaryWrapperDTO fetchStoreSummariesByCeeParallel(String ceeUsername,
                                                                       LocalDate startDate,
                                                                       LocalDate endDate) throws Exception {
            List<String> storeCodes = fetchStoresByCee(ceeUsername);
            return buildStoreSummariesForStoreCodes(storeCodes, startDate, endDate);
        }

        private StoreSummaryWrapperDTO processStoreCodesInParallel(List<String> storeCodes, LocalDate startDate, LocalDate endDate) throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
            List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
    
            AtomicInteger totalEvents = new AtomicInteger(0);
            AtomicInteger totalInvitees = new AtomicInteger(0);
            AtomicInteger totalAttendees = new AtomicInteger(0);
            DoubleAdder totalAdvance = new DoubleAdder();
            DoubleAdder totalGhsOrRga = new DoubleAdder();
            DoubleAdder totalSale = new DoubleAdder();
    
            int threadPoolSize = Math.min(10, Runtime.getRuntime().availableProcessors() * 2);
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
    
            List<Callable<Void>> tasks = storeCodes.stream().map(storeCode -> (Callable<Void>) () -> {
                try {
                    CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithBackoff(storeCode);
                    Object rawData = eventsResponse.getEventData();
    
                    List<Map<String, Object>> events = new ArrayList<>();
                    if (rawData instanceof List<?>) {
                        events = ((List<?>) rawData).stream()
                                .filter(e -> e instanceof Map)
                                .map(e -> (Map<String, Object>) e)
                                .collect(Collectors.toList());
                    }
    
                    List<Map<String, Object>> filteredEvents = events.stream()
                            .filter(event -> {
                                if (startDate == null || endDate == null) return true;
                                Object dateObj = event.get("StartDate"); // ✅ fixed key
                                if (dateObj == null) return false;
                                try {
                                    LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
                                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                                } catch (Exception ex) {
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
    
                    int storeEventCount = filteredEvents.size();
                    int storeInvitees = 0;
                    int storeAttendees = 0;
                    double storeAdvance = 0;
                    double storeGhsOrRga = 0;
                    double storeSale = 0;
    
                    for (Map<String, Object> event : filteredEvents) {
                        storeInvitees += parseInt(event.get("Invitees"));
                        storeAttendees += parseInt(event.get("Attendees"));
                        storeAdvance += parseDouble(event.get("advance"));
                        storeGhsOrRga += parseDouble(event.get("ghs/rga"));
                        storeSale += parseDouble(event.get("sale"));
                    }
    
                    totalEvents.addAndGet(storeEventCount);
                    totalInvitees.addAndGet(storeInvitees);
                    totalAttendees.addAndGet(storeAttendees);
                    totalAdvance.add(storeAdvance);
                    totalGhsOrRga.add(storeGhsOrRga);
                    totalSale.add(storeSale);
    
                    summaries.add(new StoreEventSummaryDTO(
                            storeCode, storeEventCount, storeInvitees, storeAttendees,
                            storeAdvance, storeGhsOrRga, storeSale
                    ));
                } catch (Exception e) {
                    System.err.println("Error processing store " + storeCode + ": " + e.getMessage());
                    summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
                }
                return null;
            }).collect(Collectors.toList());
    
            executor.invokeAll(tasks);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
    
            StoreEventSummaryDTO total = new StoreEventSummaryDTO(
                    "TOTAL",
                    totalEvents.get(),
                    totalInvitees.get(),
                    totalAttendees.get(),
                    totalAdvance.sum(),
                    totalGhsOrRga.sum(),
                    totalSale.sum()
            );
    
            return new StoreSummaryWrapperDTO(summaries, total);
        }
    
        private int parseInt(Object value) {
            try { return Integer.parseInt(value != null ? value.toString().trim() : "0"); }
            catch (Exception e) { return 0; }
        }
    
        private double parseDouble(Object value) {
            try { return Double.parseDouble(value != null ? value.toString().trim() : "0"); }
            catch (Exception e) { return 0.0; }
        }

        public StoreEventSummaryDTO processSingleStoreCode(String storeCode,
                                                           LocalDate startDate,
                                                           LocalDate endDate) throws Exception {

            List<Map<String, Object>> events =
                    gSheetUserDetailsUtil.getEventsForStores(Collections.singletonList(storeCode));

            List<Map<String, Object>> filtered = filterEventsByDateRange(events, startDate, endDate);

            int eventCount = 0;
            int invitees = 0;
            int attendees = 0;
            double advance = 0;
            double ghsOrRga = 0;
            double sale = 0;

            for (Map<String, Object> ev : filtered) {
                eventCount += 1;
                invitees  += parseInt(ev.get("Invitees"));
                attendees += parseInt(ev.get("Attendees"));
                advance   += parseDouble(ev.get("Advance"));
                ghsOrRga  += parseDouble(ev.get("GhsOrRga"));
                sale      += parseDouble(ev.get("Sale"));
            }

            return new StoreEventSummaryDTO(
                    storeCode,
                    eventCount,
                    invitees,
                    attendees,
                    advance,
                    ghsOrRga,
                    sale
            );
        }

        private LocalDate parseFlexibleDate(String dateStr) {
            try { return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
            catch (DateTimeParseException e1) {
                try { return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")); }
                catch (DateTimeParseException e2) { return null; }
            }
        }
    
        public ResponseDataDTO appointment(BookAppointmentDTO bookAppointmentDTO, boolean isVisitStore) {
            ResponseDataDTO dto = new ResponseDataDTO();
    
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(bookAnAppoitmentUsername, bookAnAppoitmentPassword);
                headers.add("PartnerId", "Ecomm");
                headers.setContentType(MediaType.APPLICATION_JSON);
    
                HttpEntity<BookAppointmentDTO> entity = new HttpEntity<>(bookAppointmentDTO, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        bookAnAppointmentUrl, HttpMethod.POST, entity, String.class
                );
    
                List<Object> row = Arrays.asList(
                        bookAppointmentDTO.getStoreCode(),
                        bookAppointmentDTO.getStoreName(),
                        bookAppointmentDTO.getAppointmentDate(),
                        bookAppointmentDTO.getAppointmentTime(),
                        bookAppointmentDTO.getFirstName(),
                        bookAppointmentDTO.getLastName(),
                        bookAppointmentDTO.getPhone(),
                        bookAppointmentDTO.getEmailId(),
                        bookAppointmentDTO.getTicketType()
                );
                boolean dataAdded = gSheetUserDetailsUtil.insertBAPSheetData(row);
    
                if (response.getBody() != null) {
                    dto.setStatus(true);
                    dto.setMessage(dataAdded ? "Success" : "Success, but failed to save data in sheet");
                    dto.setResult(response.getBody());
                } else {
                    dto.setMessage("Failed to book appointment");
                }
    
            } catch (Exception e) {
                e.printStackTrace();
                dto.setMessage("Error: " + e.getMessage());
            }
    
            return dto;
        }

        @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT30S")
        public void warmEventsCache() {
            try {
                gSheetUserDetailsUtil.warmEntireEventsCache();
                log.info("✅ Global events cache warmed");
            } catch (Exception e) {
                log.warn("⚠️ Failed to warm global events cache: {}", e.getMessage());
            }
        }

        private LocalDate safeToLocalDate(Object obj) {
            if (obj == null) return null;
            String s = obj.toString().trim();
            // try multiple formats just like parseFlexibleDate()
            LocalDate d = parseFlexibleDate(s);
            return d;
        }

        private String safeString(Object obj) {
            return obj == null ? "" : obj.toString();
        }

        private List<Map<String, Object>> filterEventsByDateRange(
                List<Map<String, Object>> events,
                LocalDate startDate,
                LocalDate endDate
        ) {
            if (startDate == null || endDate == null) {
                return events;
            }
            return events.stream()
                    .filter(ev -> {
                        LocalDate eventDate = safeToLocalDate(ev.get("StartDate"));
                        if (eventDate == null) return false;
                        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }


        private StoreSummaryWrapperDTO buildStoreSummariesForStoreCodes(List<String> storeCodes,
                                                                        LocalDate startDate,
                                                                        LocalDate endDate) throws Exception {

            // 1. read all events for those stores from cache
            List<Map<String, Object>> events = gSheetUserDetailsUtil.getEventsForStores(storeCodes);

            // 2. filter by date
            List<Map<String, Object>> filtered = filterEventsByDateRange(events, startDate, endDate);

            // 3. aggregate per store
            Map<String, StoreEventSummaryDTO> perStore = new HashMap<>();

            for (Map<String, Object> ev : filtered) {
                String sc         = safeString(ev.get("StoreCode"));
                int invitees      = parseInt(ev.get("Invitees"));
                int attendees     = parseInt(ev.get("Attendees"));
                double advance    = parseDouble(ev.get("Advance"));
                double ghsOrRga   = parseDouble(ev.get("GhsOrRga"));
                double sale       = parseDouble(ev.get("Sale"));

                StoreEventSummaryDTO agg = perStore.computeIfAbsent(
                        sc,
                        k -> new StoreEventSummaryDTO(k, 0, 0, 0, 0, 0, 0)
                );

                agg.setTotalEvents(agg.getTotalEvents() + 1);
                agg.setTotalInvitees(agg.getTotalInvitees() + invitees);
                agg.setTotalAttendees(agg.getTotalAttendees() + attendees);
                agg.setTotalAdvance(agg.getTotalAdvance() + advance);
                agg.setTotalGhsOrRga(agg.getTotalGhsOrRga() + ghsOrRga);
                agg.setTotalSale(agg.getTotalSale() + sale);
            }

            // 4. roll up grand total row
            int totalEvents       = 0;
            int totalInvitees     = 0;
            int totalAttendees    = 0;
            double totalAdvance   = 0;
            double totalGhsOrRga  = 0;
            double totalSale      = 0;

            for (StoreEventSummaryDTO s : perStore.values()) {
                totalEvents      += s.getTotalEvents();
                totalInvitees    += s.getTotalInvitees();
                totalAttendees   += s.getTotalAttendees();
                totalAdvance     += s.getTotalAdvance();
                totalGhsOrRga    += s.getTotalGhsOrRga();
                totalSale        += s.getTotalSale();
            }

            StoreEventSummaryDTO total = new StoreEventSummaryDTO(
                    "TOTAL",
                    totalEvents,
                    totalInvitees,
                    totalAttendees,
                    totalAdvance,
                    totalGhsOrRga,
                    totalSale
            );

            return new StoreSummaryWrapperDTO(new ArrayList<>(perStore.values()), total);
        }

    }

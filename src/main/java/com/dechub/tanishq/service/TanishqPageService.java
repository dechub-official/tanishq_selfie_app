//package com.dechub.tanishq.service;
//
//
//
//import com.dechub.tanishq.dto.*;
//import com.dechub.tanishq.dto.eventsDto.*;
//import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
//import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
//import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
//import com.dechub.tanishq.gdrive.GoogleDriveService;
//import com.dechub.tanishq.gsheet.*;
//// import com.dechub.tanishq.mail.EmailService;
//import com.dechub.tanishq.util.CommonConstants;
//import com.dechub.tanishq.util.ResponseDataDTO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.DoubleAdder;
//import java.util.stream.Collectors;
//import org.springframework.util.ResourceUtils;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//
//
//@Component
//public class TanishqPageService {
//
//
//    @Value("${dechub.bride.upload.dir}")
//    private String UPLOAD_DIR;
//
//    @Value("${dechub.base.image}")
//    private String BASE_IMG;
//
//    public ArrayList<ExcelStoreDTO> storeList = null;
//
//    @Autowired
//    private GSheetUserDetailsUtil gSheetUserDetailsUtil;
//
//
//
//    // @Autowired
//    // private EmailService emailService;
//
//
//    @Value("${selfie.upload.dir}")
//    private String selfieDirectory;
//
//
//    @Value("${system.isWindows}")
//    private String isWindows;
//
//
//    @Autowired
//    private StoreServices storeServices;
//
//
//    @Autowired
//    private GoogleDriveService googleDriveService;
//
//    @Autowired
//    private UserSession userSession;
//
//
//
//    private static final Logger log = LoggerFactory.getLogger(TanishqPageService.class);
//    @Scheduled(fixedDelayString = "${dechub.scheduler.fixedDelay}")
//    public void fetchData(){
//        log.info("fetching details from Google sheet triggered");
//        ArrayList<ExcelStoreDTO> lst = gSheetUserDetailsUtil.getData();
//        if(lst != null){
//            log.info("fetched details from Google sheet result store count: " + lst.size());
//            if(lst.size() > 0){
//                this.storeList = lst;
//                log.info("fetched details from Google sheet assigned to main data");
//            }else{
//                log.info("fetched details from Google sheet result got Empty size 0");
//            }
//        }else{
//            log.info("fetched details from Google sheet result got Empty");
//        }
//    }
//
//    public EventsLoginResponseDTO eventsLogin(String storeCode, String password) throws Exception {
//        EventsLoginResponseDTO responseDataDTO = new EventsLoginResponseDTO();
//        if(!password.equals("Tanishq@123")){
//            responseDataDTO.setStatus(false);
//            return responseDataDTO;
//        }
//        List<String> codeList = new ArrayList<>(Arrays.asList(
//                "East1", "East2",
//                "North1A", "North1B",
//                "North2", "North3",
//                "South1", "South2A", "South2A", "South3",
//                "West1A", "West1B", "West2", "West3","TEST"
//        ));
//        boolean isCodePresent = codeList.contains(storeCode);
//        if(isCodePresent){
//            System.out.println("login successful");
//
//            Map<String, Object> details = new HashMap<>();
//            details.put("manager", storeCode);
//            responseDataDTO.setStoreData(details);
//            responseDataDTO.setStatus(true);
//            return responseDataDTO;
//        }else{
////            String divisionName = this.getDivisionDirectory(storeCode);
//            List<ExcelStoreDTO> result = this.storeList.stream().filter(i -> i.getStoreCode().toUpperCase().equals(storeCode)).collect(Collectors. toList());
//
//            if( result.get(0).getStoreCode() == null){
//                responseDataDTO.setStatus(false);
//                return responseDataDTO;
//            }
//
//            Map<String, Object> details = gSheetUserDetailsUtil.getDataFromSheet(storeCode);
//            System.out.println("login successful");
//            responseDataDTO.setStoreData(details);
//            responseDataDTO.setStatus(true);
//            return responseDataDTO;
//        }
//
//    }
//    public boolean containsIgnoreCase(String str, String searchStr) {
//        if (str == null || searchStr == null) {
//            return false;
//        }
//        int len = searchStr.length();
//        int max = str.length() - len;
//        for (int i = 0; i <= max; i++) {
//            if (str.regionMatches(true, i, searchStr, 0, len)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public ResponseDataDTO storeUserDetails(UserDetailsDTO userDetailsDTO) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//
//        String divisionName = this.getDivisionDirectory(userDetailsDTO.getStoreCode());
//        if(divisionName == null){
//            responseDataDTO.setMessage("Invalid Store Code");
//            return responseDataDTO;
//        }
//
//        //storing details
//        userDetailsDTO.setDate(this.getCurrentTime());
//        boolean isDone = gSheetUserDetailsUtil.insertSheetData(userDetailsDTO);
//
//        //sending mail
////        String imageNameWithRespectiveDirectory = isWindows.equalsIgnoreCase("Y") ? divisionName + "\\" + userDetailsDTO.getSelfieImageName() : divisionName + "/" + userDetailsDTO.getSelfieImageName();
////        boolean isEmailSent = emailService.sendEmail(userDetailsDTO.getEmailId(), "Test", "This is Test Email", imageNameWithRespectiveDirectory);
//
//        UserDetailResponseDTO userDetailResponseDTO = new UserDetailResponseDTO(isDone, false);
//        responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//        responseDataDTO.setResult(userDetailResponseDTO);
//        return responseDataDTO;
//    }
//
//    public QrResponseDTO storeEventsDetails(EventsDetailDTO eventsDetailsDTO) {
//
//
//
//        QrResponseDTO isDone = gSheetUserDetailsUtil.insertSheetEventsData(eventsDetailsDTO);
//
//
//        return isDone;
//    }
//
//    public ResponseDataDTO saveImage(MultipartFile file, String storeCode) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//
//        String divisionName = this.getDivisionDirectory(storeCode);
//        if(divisionName == null){
//            responseDataDTO.setMessage("Invalid Store Code");
//            return responseDataDTO;
//        }
//
//        try{
//            String newFileName = this.getNewFileName(file.getOriginalFilename());
//            String filePath = null;
//            if(isWindows.equalsIgnoreCase("Y")){
//                filePath = selfieDirectory + "\\" + divisionName + "\\" + newFileName;
//            }else{
//                filePath = selfieDirectory + "/" + divisionName + "/" + newFileName;
//            }
//            File targetFile = new File(filePath);
//            file.transferTo(targetFile);
//            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//            responseDataDTO.setResult(newFileName);
//        }catch (Exception e){
//            responseDataDTO.setMessage(e.getMessage());
//        }
//        return responseDataDTO;
//    }
//
//    // public ResponseDataDTO saveVideo(MultipartFile file) {
//    //     ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//
//    //     try{
//    //         googleDriveService.uploadVideo(file);
//    //         responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//    //     }catch (Exception e){
//    //         responseDataDTO.setMessage(e.getMessage());
//    //     }
//    //     return responseDataDTO;
//    // }
//
//    public String getNewFileName(String fileName){
//        String result = null;
//        try{
//            String[] lst = fileName.split("\\.");
//            String extension = lst[lst.length - 1];
//            result =  getCurrentTimeAsUnique() + "." + extension;
////            result =  getCurrentTimeAsUnique() + selfieExtension;
//        }catch (Exception e){
//            log.info(e.getMessage() + " with file name: " + fileName);
//        }
//        return result;
//    }
//
//    public String getCurrentTime(){
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//        LocalDateTime now = LocalDateTime.now();
//        return dtf.format(now);
//    }
//
//    public String getCurrentTimeAsUnique(){
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
//        LocalDateTime now = LocalDateTime.now();
//        return dtf.format(now);
//    }
//
//    public String getDivisionDirectory(String storeCode){
//        String result = null;
//        Map<String, List<String>> storeDetails = storeServices.getStoreDetails();
//        for(Map.Entry<String, List<String>> entry : storeDetails.entrySet()){
//            List<String> tempVar = entry.getValue().stream().filter(i -> i.equalsIgnoreCase(storeCode)).collect(Collectors.toList());
//            if(tempVar.size() > 0){
//                result = entry.getKey();
//                break;
//            }
//        }
//
//        return result;
//    }
//
//
//    public ResponseDataDTO getStoreCode() {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//        Map<String, List<String>> storesData = storeServices.getStoreDetails();
//        List<String> result = new ArrayList<>();
//        for(Map.Entry<String, List<String>> entry : storesData.entrySet()){
//            result.addAll(entry.getValue());
//        }
//        responseDataDTO.setResult(result);
//        return responseDataDTO;
//    }
//
//
//    public ResponseDataDTO storeBrideImage(MultipartFile file) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//
//        if (file.isEmpty()) {
//            responseDataDTO.setMessage("Please select a file to upload");
//            return responseDataDTO;
//        }
//
//        try {
//            byte[] bytes = file.getBytes();
//            String newFileName = getNewFileName(file.getOriginalFilename());
//            Path path = Paths.get(UPLOAD_DIR + File.separator + newFileName);
//            Files.write(path, bytes);
//
//            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//            responseDataDTO.setResult(newFileName);
//            responseDataDTO.setStatus(true);
//            // Set the full path of the stored file in the response
//            responseDataDTO.setFilePath(path.toString());
//            System.out.println("bride image uploaded in uploads");
//        } catch (IOException e) {
//            responseDataDTO.setMessage("Failed to upload file: " + file.getOriginalFilename() + " - " + e.getMessage());
//            e.printStackTrace();
//            // You can log the exception or handle it based on your application's needs
//        }
//        return responseDataDTO;
//    }
//
//
//    public ResponseEntity<byte[]> storeBrideDetails(String brideType, String brideEvent, String brideName, String phone, String date, String email,String zipcode,String filepath) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        BrideDetailsDTO brideDetailsDTO = new BrideDetailsDTO();
//        brideDetailsDTO.setBrideName(brideName);
//        brideDetailsDTO.setBrideEvent(brideEvent);
//        brideDetailsDTO.setBrideType(brideType);
//        brideDetailsDTO.setDate(date);
//        brideDetailsDTO.setEmail(email);
//        brideDetailsDTO.setPhone(phone);
//        brideDetailsDTO.setZipCode(zipcode);
//        System.out.println(brideDetailsDTO.getZipCode());
//        boolean isDone = gSheetUserDetailsUtil.insertSheetBrideData(brideDetailsDTO);
//        ResponseEntity<byte[]> imageResponse = processImageWithTextOverlay(brideName, phone, date, email, filepath);
//        if (imageResponse.getStatusCode() != HttpStatus.OK) {
//            responseDataDTO.setMessage("Failed to process image");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//        if(isDone){
//            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
//            responseDataDTO.setImageResponse(imageResponse);
//            responseDataDTO.setStatus(true);
//        }else{
//            responseDataDTO.setMessage("Failed to store bride details");
//        }
//        return imageResponse;
//    }
//    public ResponseDataDTO storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO)  {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        int isDone = gSheetUserDetailsUtil.insertSheetAttendeesData(attendeesDetailDTO);
//        if(isDone>0){
//            boolean updated = gSheetUserDetailsUtil.updateAttendees(attendeesDetailDTO.getId(),isDone);
//            if(updated){
//                responseDataDTO.setStatus(true);
//            }
//        }else{
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage("storing failed");
//        }
//        return responseDataDTO;
//    }
//
//
//    public ResponseEntity<byte[]> processImageWithTextOverlay(String brideName, String phone, String date, String email, String filepath) {
//        try {
//            BufferedImage baseImage = ImageIO.read(ResourceUtils.getFile(BASE_IMG));
//            BufferedImage overlayImage = ImageIO.read(ResourceUtils.getFile(filepath));
//
//            // Resize overlay image if larger than base image
//            overlayImage = resizeImage(overlayImage, baseImage.getWidth(), baseImage.getHeight());
//
//            Graphics2D g2d = baseImage.createGraphics();
//            g2d.setFont(new Font("Nunito", Font.PLAIN, 14));
//            g2d.setColor(Color.BLACK);
//
//            g2d.drawString("Name: " + brideName, 190, 250);
//            g2d.drawString("Phone: " + phone, 500, 250);
//            g2d.drawString("Email: " + email, 190, 320);
//            g2d.drawString("Wedding Date: " + date, 500, 320);
//
////            g2d.drawString("Name: " + brideName, 70, 55);
////            g2d.drawString("Phone: " + phone, 170, 55);
////            g2d.drawString("Email: " + email, 70, 76);
////            g2d.drawString("Wedding Date: " + date, 170, 76);
//
//            // Center the overlay image
//            int imgX = (baseImage.getWidth() - overlayImage.getWidth()) / 2;
//            int imgY = (baseImage.getHeight() / 4);
//
//            g2d.drawImage(overlayImage, imgX, imgY, null);
//            g2d.dispose();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(baseImage, "jpg", baos);
//            byte[] imageBytes = baos.toByteArray();
//
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_JPEG);
//            headers.setContentDispositionFormData("attachment", "output.jpg");
//            headers.setContentLength(imageBytes.length);
//
//            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        int originalWidth = originalImage.getWidth();
//        int originalHeight = originalImage.getHeight();
//        float aspectRatio = (float) originalWidth / originalHeight;
//
//        // Apply padding to ensure the image fits well within the target dimensions
//        int paddedWidth = targetWidth - 400; // Adding a padding of 20 pixels
//        int paddedHeight = targetHeight - 400; // Adding a padding of 20 pixels
//
//        int newWidth = paddedWidth;
//        int newHeight = paddedHeight;
//
//        if (originalWidth > paddedWidth || originalHeight > paddedHeight) {
//            if (originalWidth > originalHeight) {
//                newWidth = paddedWidth;
//                newHeight = Math.round(paddedWidth / aspectRatio);
//            } else {
//                newHeight = paddedHeight;
//                newWidth = Math.round(paddedHeight * aspectRatio);
//            }
//        }
//
//        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = resizedImage.createGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
//        g2d.dispose();
//
//        return resizedImage;
//    }
//
//    public CompletedEventsResponseDTO getAllCompletedEvents(String code) {
//        CompletedEventsResponseDTO completedEventsResponseDTO = new CompletedEventsResponseDTO();
//        try{
//            List<Map<String, Object>> completedEventsDataDTOS = gSheetUserDetailsUtil.getCompletedEventDetails(code);
//            if(completedEventsDataDTOS.size()>0){
//                completedEventsResponseDTO.setStatus(true);
//                completedEventsResponseDTO.setMessage("file uploaded");
//                completedEventsResponseDTO.setEventData(completedEventsDataDTOS);
//            }else{
//                completedEventsResponseDTO.setStatus(false);
//                completedEventsResponseDTO.setMessage("file uploaded");
//                completedEventsResponseDTO.setEventData("fetching failed");
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            completedEventsResponseDTO.setStatus(false);
//            completedEventsResponseDTO.setEventData("error"+e.getMessage());
//        }
//        return completedEventsResponseDTO;
//    }
//
//    public List<?> getInvitedMember(String eventId) {
//        try{
//            return  gSheetUserDetailsUtil.getAllAttendees(eventId);
//        }catch (Exception e){
//            return new ArrayList<>();
//        }
//    }
//
//    public ResponseDataDTO getShareCode(RivaahDTO rivaahDTO) {
//        return gSheetUserDetailsUtil.insertRivaahDetails(rivaahDTO);
//    }
//
//    public ResponseDataDTO storeRivaahUser(String name, String contact) {
//        boolean status = gSheetUserDetailsUtil.insertRivaahUserDetails(name, contact);
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        if(status){
//            responseDataDTO.setStatus(true);
//            responseDataDTO.setMessage("Successfully stored user details");
//            return responseDataDTO;
//        }
//        responseDataDTO.setStatus(false);
//        responseDataDTO.setMessage("User details storing failed");
//        return responseDataDTO;
//    }
//
//
//    public RivaahAllDetailsDTO getRivaahDetails(String code) {
//        return gSheetUserDetailsUtil.getRivaahDetails(code);
//    }
//
//    public ResponseDataDTO changePasswordForEventManager(String storeCode, String oldPassword, String newPassword) {
//        return gSheetUserDetailsUtil.changePassword(storeCode,oldPassword,newPassword);
//    }
//
//    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) {
//        try{
//            return gSheetUserDetailsUtil.updateSaleOfAnEvent(eventCode,sale);
//        }catch (Exception e){
//            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage(e.getMessage());
//            return responseDataDTO;
//        }
//    }
//    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) {
//        try{
//            return gSheetUserDetailsUtil.updateAdvanceOfAnEvent(eventCode,advance);
//        }catch (Exception e){
//            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage(e.getMessage());
//            return responseDataDTO;
//        }
//    }
//    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) {
//        try{
//            return gSheetUserDetailsUtil.updateGhsRgaOfAnEvent(eventCode,ghsRga);
//        }catch (Exception e){
//            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage(e.getMessage());
//            return responseDataDTO;
//        }
//    }
//    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) {
//        try{
//            return gSheetUserDetailsUtil.updateGmbOfAnEvent(eventCode,gmb);
//        }catch (Exception e){
//            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage(e.getMessage());
//            return responseDataDTO;
//        }
//    }
//
////    public boolean authenticateAbm(String username, String password) {
////        try {
////            return gSheetUserDetailsUtil.isValidUserAbm(username, password);
////        } catch (Exception e) {
////            // Log the error
////            return false;
////        }
////    }
//
//    public Optional<LoginResponseDTO> authenticateAbm(String username, String password) {
//        try {
//            return gSheetUserDetailsUtil.isValidUserAbm(username, password);
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//    }
//
//
//
//    public Optional<LoginResponseDTO> authenticateRbm(String username, String password) {
//        try {
//            return gSheetUserDetailsUtil.isValidUserRbm(username, password);
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//    }
//
//
//    public Optional<LoginResponseDTO> authenticateCee(String username, String password) {
//        try {
//            return gSheetUserDetailsUtil.isValidUserCee(username, password);
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//    }
//
//
//    public List<String> fetchStoresByRbm(String rbmUsername) throws Exception {
//        return gSheetUserDetailsUtil.getStoresByRbmUsername(rbmUsername);
//    }
//
//    public List<String> fetchStoresByAbm(String abmUsername) throws Exception {
//        return gSheetUserDetailsUtil.getStoresByAbmUsername(abmUsername);
//    }
//
//    public List<String> fetchStoresByCee(String ceeUsername) throws Exception{
//        return gSheetUserDetailsUtil.getStoresByCeeUsername(ceeUsername);
//    }
//
//    public List<CompletedEventsResponseDTO> getCompletedEventsForStores(List<String> storeCodes) {
//        List<CompletedEventsResponseDTO> result = new ArrayList<>();
//        for (String storeCode : storeCodes) {
//            try {
//                CompletedEventsResponseDTO events = getAllCompletedEvents(storeCode);
//                if (events != null) {
//                    result.add(events);
//                }
//            } catch (Exception e) {
//                // Log and skip problematic store
//            }
//        }
//        return result;
//    }
//
//    public List<Map<String, Object>> getOnlyEventsForStores(List<String> storeCodes) {
//        List<Map<String, Object>> allEvents = new ArrayList<>();
//        for (String storeCode : storeCodes) {
//            try {
//                CompletedEventsResponseDTO dto = getAllCompletedEvents(storeCode);
//                if (dto != null && dto.getStatus()==true && dto.getEventData() instanceof List) {
//                    List<Map<String, Object>> events = (List<Map<String, Object>>) dto.getEventData();
//                    if (events != null) {
//                        allEvents.addAll(events);
//                    }
//                }
//            } catch (Exception e) {
//                // Optionally log the error
//            }
//        }
//        return allEvents;
//    }
//
//
//    public List<Map<String, Object>> filterEventsByStartDate(List<Map<String, Object>> events, String startDateStr, String endDateStr) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate start = LocalDate.parse(startDateStr, formatter);
//        LocalDate end = LocalDate.parse(endDateStr, formatter);
//
//        return events.stream()
//                .filter(e -> {
//                    try {
//                        String startDate = (String) e.get("StartDate");
//                        if (startDate == null || startDate.isEmpty()) return false;
//                        LocalDate eventDate = LocalDate.parse(startDate);
//                        return !eventDate.isBefore(start) && !eventDate.isAfter(end);
//                    } catch (Exception ex) {
//                        return false;
//                    }
//                })
//                .collect(Collectors.toList());
//    }
//
////    public List<StoreEventSummaryDTO> fetchStoreSummariesByRbmParallel(String rbmUsername) throws Exception {
////        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
////
////        // Use parallel stream for faster processing
////        return storeCodes.parallelStream().map(storeCode -> {
////            try {
////                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
////                Object rawData = eventsResponse.getEventData();
////
////                List<Map<String, Object>> events = new ArrayList<>();
////
////                if (rawData instanceof List<?>) {
////                    List<?> rawList = (List<?>) rawData;
////                    events = rawList.stream()
////                            .filter(e -> e instanceof Map)
////                            .map(e -> (Map<String, Object>) e)
////                            .collect(Collectors.toList());
////                }
////
////                int totalEvents = events.size();
////                int totalInvitees = 0;
////                int totalAttendees = 0;
////                double totalAdvance = 0;
////                double totalGhsOrRga = 0;
////                double totalSale = 0;
////
////                for (Map<String, Object> event : events) {
////                    totalInvitees += parseInt(event.get("Invitees"));
////                    totalAttendees += parseInt(event.get("Attendees"));
////                    totalAdvance += parseDouble(event.get("advance"));
////                    totalGhsOrRga += parseDouble(event.get("ghs/rga"));
////                    totalSale += parseDouble(event.get("sale"));
////                }
////
////                return new StoreEventSummaryDTO(
////                        storeCode, totalEvents, totalInvitees, totalAttendees,
////                        totalAdvance, totalGhsOrRga, totalSale
////                );
////
////            } catch (Exception e) {
////                // In case of failure, return summary with zeros
////                return new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0);
////            }
////        }).collect(Collectors.toList());
////    }
//
////    public List<StoreEventSummaryDTO> fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
////        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////
////        return storeCodes.parallelStream().map(storeCode -> {
////            try {
////                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
////                Object rawData = eventsResponse.getEventData();
////
////                List<Map<String, Object>> events = new ArrayList<>();
////
////                if (rawData instanceof List<?>) {
////                    List<?> rawList = (List<?>) rawData;
////                    events = rawList.stream()
////                            .filter(e -> e instanceof Map)
////                            .map(e -> (Map<String, Object>) e)
////                            .collect(Collectors.toList());
////                }
////
////                // ✅ Apply date filter ONLY if both dates are provided
////                List<Map<String, Object>> filteredEvents = events.stream()
////                        .filter(event -> {
////                            if (startDate == null || endDate == null) return true;
////
////                            Object dateObj = event.get("eventDate");
////                            if (dateObj == null) return false;
////
////                            try {
////                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
////                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
////                            } catch (Exception e) {
////                                return false;
////                            }
////                        })
////                        .collect(Collectors.toList());
////
////                int totalEvents = filteredEvents.size();
////                int totalInvitees = 0;
////                int totalAttendees = 0;
////                double totalAdvance = 0;
////                double totalGhsOrRga = 0;
////                double totalSale = 0;
////
////                for (Map<String, Object> event : filteredEvents) {
////                    totalInvitees += parseInt(event.get("Invitees"));
////                    totalAttendees += parseInt(event.get("Attendees"));
////                    totalAdvance += parseDouble(event.get("advance"));
////                    totalGhsOrRga += parseDouble(event.get("ghs/rga"));
////                    totalSale += parseDouble(event.get("sale"));
////                }
////
////                return new StoreEventSummaryDTO(
////                        storeCode, totalEvents, totalInvitees, totalAttendees,
////                        totalAdvance, totalGhsOrRga, totalSale
////                );
////
////            } catch (Exception e) {
////                return new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0);
////            }
////        }).collect(Collectors.toList());
////    }
//
//
////    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
////        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////
////        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
////
////        AtomicInteger totalEvents = new AtomicInteger(0);
////        AtomicInteger totalInvitees = new AtomicInteger(0);
////        AtomicInteger totalAttendees = new AtomicInteger(0);
////        DoubleAdder totalAdvance = new DoubleAdder();
////        DoubleAdder totalGhsOrRga = new DoubleAdder();
////        DoubleAdder totalSale = new DoubleAdder();
////
////        storeCodes.parallelStream().forEach(storeCode -> {
////            try {
////                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
////                Object rawData = eventsResponse.getEventData();
////
////                List<Map<String, Object>> events = new ArrayList<>();
////
////                if (rawData instanceof List<?>) {
////                    List<?> rawList = (List<?>) rawData;
////                    events = rawList.stream()
////                            .filter(e -> e instanceof Map)
////                            .map(e -> (Map<String, Object>) e)
////                            .collect(Collectors.toList());
////                }
////
////                List<Map<String, Object>> filteredEvents = events.stream()
////                        .filter(event -> {
////                            if (startDate == null || endDate == null) return true;
////                            Object dateObj = event.get("eventDate");
////                            if (dateObj == null) return false;
////
////                            try {
////                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
////                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
////                            } catch (Exception e) {
////                                return false;
////                            }
////                        })
////                        .collect(Collectors.toList());
////
////                int storeEventCount = filteredEvents.size();
////                int storeInvitees = 0;
////                int storeAttendees = 0;
////                double storeAdvance = 0;
////                double storeGhsOrRga = 0;
////                double storeSale = 0;
////
////                for (Map<String, Object> event : filteredEvents) {
////                    storeInvitees += parseInt(event.get("Invitees"));
////                    storeAttendees += parseInt(event.get("Attendees"));
////                    storeAdvance += parseDouble(event.get("advance"));
////                    storeGhsOrRga += parseDouble(event.get("ghs/rga"));
////                    storeSale += parseDouble(event.get("sale"));
////                }
////
////                // Add to totals (thread-safe)
////                totalEvents.addAndGet(storeEventCount);
////                totalInvitees.addAndGet(storeInvitees);
////                totalAttendees.addAndGet(storeAttendees);
////                totalAdvance.add(storeAdvance);
////                totalGhsOrRga.add(storeGhsOrRga);
////                totalSale.add(storeSale);
////
////                summaries.add(new StoreEventSummaryDTO(
////                        storeCode, storeEventCount, storeInvitees, storeAttendees,
////                        storeAdvance, storeGhsOrRga, storeSale
////                ));
////
////            } catch (Exception e) {
////                summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
////            }
////        });
////
////        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
////                "TOTAL",
////                totalEvents.get(),
////                totalInvitees.get(),
////                totalAttendees.get(),
////                totalAdvance.sum(),
////                totalGhsOrRga.sum(),
////                totalSale.sum()
////        );
////
////        return new StoreSummaryWrapperDTO(summaries, total);
////    }
//
//
////    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
////        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////
////        List<StoreEventSummaryDTO> summaries = new ArrayList<>();
////
////        AtomicInteger totalEvents = new AtomicInteger(0);
////        AtomicInteger totalInvitees = new AtomicInteger(0);
////        AtomicInteger totalAttendees = new AtomicInteger(0);
////        DoubleAdder totalAdvance = new DoubleAdder();
////        DoubleAdder totalGhsOrRga = new DoubleAdder();
////        DoubleAdder totalSale = new DoubleAdder();
////
////        for (String storeCode : storeCodes) {
////            try {
////                CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
////                Object rawData = eventsResponse.getEventData();
////
////                List<Map<String, Object>> events = new ArrayList<>();
////                if (rawData instanceof List<?>) {
////                    events = ((List<?>) rawData).stream()
////                            .filter(e -> e instanceof Map)
////                            .map(e -> (Map<String, Object>) e)
////                            .collect(Collectors.toList());
////                }
////
////                List<Map<String, Object>> filteredEvents = events.stream()
////                        .filter(event -> {
////                            if (startDate == null || endDate == null) return true;
////                            Object dateObj = event.get("eventDate");
////                            if (dateObj == null) return false;
////                            try {
////                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
////                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
////                            } catch (Exception e) {
////                                return false;
////                            }
////                        })
////                        .collect(Collectors.toList());
////
////                int storeEventCount = filteredEvents.size();
////                int storeInvitees = 0;
////                int storeAttendees = 0;
////                double storeAdvance = 0;
////                double storeGhsOrRga = 0;
////                double storeSale = 0;
////
////                for (Map<String, Object> event : filteredEvents) {
////                    storeInvitees += parseInt(event.get("Invitees"));
////                    storeAttendees += parseInt(event.get("Attendees"));
////                    storeAdvance += parseDouble(event.get("advance"));
////                    storeGhsOrRga += parseDouble(event.get("ghs/rga"));
////                    storeSale += parseDouble(event.get("sale"));
////                }
////
////                totalEvents.addAndGet(storeEventCount);
////                totalInvitees.addAndGet(storeInvitees);
////                totalAttendees.addAndGet(storeAttendees);
////                totalAdvance.add(storeAdvance);
////                totalGhsOrRga.add(storeGhsOrRga);
////                totalSale.add(storeSale);
////
////                summaries.add(new StoreEventSummaryDTO(storeCode, storeEventCount, storeInvitees, storeAttendees, storeAdvance, storeGhsOrRga, storeSale));
////
////            } catch (Exception e) {
////                // Log error if needed
////                System.err.println("Failed to fetch for store: " + storeCode + " - " + e.getMessage());
////                summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
////            }
////        }
////
////        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
////                "TOTAL",
////                totalEvents.get(),
////                totalInvitees.get(),
////                totalAttendees.get(),
////                totalAdvance.sum(),
////                totalGhsOrRga.sum(),
////                totalSale.sum()
////        );
////
////        return new StoreSummaryWrapperDTO(summaries, total);
////    }
//
//    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
//
//        AtomicInteger totalEvents = new AtomicInteger(0);
//        AtomicInteger totalInvitees = new AtomicInteger(0);
//        AtomicInteger totalAttendees = new AtomicInteger(0);
//        DoubleAdder totalAdvance = new DoubleAdder();
//        DoubleAdder totalGhsOrRga = new DoubleAdder();
//        DoubleAdder totalSale = new DoubleAdder();
//
//        int maxThreads = 2; // reduced concurrency
//        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
//        List<Future<?>> futures = new ArrayList<>();
//
//        for (String storeCode : storeCodes) {
//            futures.add(executor.submit(() -> {
//                try {
//                    // Rate limiting: sleep before making the request
//                    Thread.sleep(1000); // 1 second delay between each thread's request
//
//                    CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithBackoff(storeCode);
//                    Object rawData = eventsResponse.getEventData();
//
//                    List<Map<String, Object>> events = new ArrayList<>();
//                    if (rawData instanceof List<?>) {
//                        events = ((List<?>) rawData).stream()
//                                .filter(e -> e instanceof Map)
//                                .map(e -> (Map<String, Object>) e)
//                                .collect(Collectors.toList());
//                    }
//
//                    List<Map<String, Object>> filteredEvents = events.stream()
//                            .filter(event -> {
//                                if (startDate == null || endDate == null) return true;
//                                Object dateObj = event.get("eventDate");
//                                if (dateObj == null) return false;
//                                try {
//                                    LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                                } catch (Exception e) {
//                                    return false;
//                                }
//                            })
//                            .collect(Collectors.toList());
//
//                    int storeEventCount = filteredEvents.size();
//                    int storeInvitees = 0;
//                    int storeAttendees = 0;
//                    double storeAdvance = 0;
//                    double storeGhsOrRga = 0;
//                    double storeSale = 0;
//
//                    for (Map<String, Object> event : filteredEvents) {
//                        storeInvitees += parseInt(event.get("Invitees"));
//                        storeAttendees += parseInt(event.get("Attendees"));
//                        storeAdvance += parseDouble(event.get("advance"));
//                        storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//                        storeSale += parseDouble(event.get("sale"));
//                    }
//
//                    totalEvents.addAndGet(storeEventCount);
//                    totalInvitees.addAndGet(storeInvitees);
//                    totalAttendees.addAndGet(storeAttendees);
//                    totalAdvance.add(storeAdvance);
//                    totalGhsOrRga.add(storeGhsOrRga);
//                    totalSale.add(storeSale);
//
//                    summaries.add(new StoreEventSummaryDTO(
//                            storeCode, storeEventCount, storeInvitees, storeAttendees,
//                            storeAdvance, storeGhsOrRga, storeSale
//                    ));
//
//                } catch (Exception e) {
//                    System.err.println("Error processing store " + storeCode + ": " + e.getMessage());
//                    summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
//                }
//            }));
//        }
//
//        for (Future<?> future : futures) {
//            try {
//                future.get();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        executor.shutdown();
//
//        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
//                "TOTAL",
//                totalEvents.get(),
//                totalInvitees.get(),
//                totalAttendees.get(),
//                totalAdvance.sum(),
//                totalGhsOrRga.sum(),
//                totalSale.sum()
//        );
//
//        return new StoreSummaryWrapperDTO(summaries, total);
//    }
//
//    public CompletedEventsResponseDTO getAllCompletedEventsWithBackoff(String storeCode) throws Exception {
//        int maxRetries = 5;
//        int backoff = 1000; // 1 second
//
//        for (int attempt = 1; attempt <= maxRetries; attempt++) {
//            return getAllCompletedEvents(storeCode);
//        }
//
//        throw new RuntimeException("Max retry attempts reached for store: " + storeCode);
//    }
//
//
//    //    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
////        List<String> storeCodes = fetchStoresByAbm(rbmUsername);
////        return processStoreCodesInParallel(storeCodes, startDate, endDate);
////    }
//    public StoreSummaryWrapperDTO fetchStoreSummariesByAbmParallel(String abmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByAbm(abmUsername);
//        return processStoreCodesInParallel(storeCodes, startDate, endDate);
//    }
//
//    public StoreSummaryWrapperDTO fetchStoreSummariesByCeeParallel(String ceeUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByCee(ceeUsername);
//        return processStoreCodesInParallel(storeCodes, startDate, endDate);
//    }
//
////    private StoreSummaryWrapperDTO processStoreCodesInParallel(List<String> storeCodes, LocalDate startDate, LocalDate endDate) throws Exception {
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////
////        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
////
////        AtomicInteger totalEvents = new AtomicInteger(0);
////        AtomicInteger totalInvitees = new AtomicInteger(0);
////        AtomicInteger totalAttendees = new AtomicInteger(0);
////        DoubleAdder totalAdvance = new DoubleAdder();
////        DoubleAdder totalGhsOrRga = new DoubleAdder();
////        DoubleAdder totalSale = new DoubleAdder();
////
////        ExecutorService executor = Executors.newFixedThreadPool(4);
////        List<Future<?>> futures = new ArrayList<>();
////
////        for (String storeCode : storeCodes) {
////            futures.add(executor.submit(() -> {
////                try {
////                    CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
////                    Object rawData = eventsResponse.getEventData();
////
////                    List<Map<String, Object>> events = new ArrayList<>();
////                    if (rawData instanceof List<?>) {
////                        events = ((List<?>) rawData).stream()
////                                .filter(e -> e instanceof Map)
////                                .map(e -> (Map<String, Object>) e)
////                                .collect(Collectors.toList());
////                    }
////
////                    List<Map<String, Object>> filteredEvents = events.stream()
////                            .filter(event -> {
////                                if (startDate == null || endDate == null) return true;
////                                Object dateObj = event.get("eventDate");
////                                if (dateObj == null) return false;
////                                try {
////                                    LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
////                                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
////                                } catch (Exception e) {
////                                    return false;
////                                }
////                            })
////                            .collect(Collectors.toList());
////
////                    int storeEventCount = filteredEvents.size();
////                    int storeInvitees = 0;
////                    int storeAttendees = 0;
////                    double storeAdvance = 0;
////                    double storeGhsOrRga = 0;
////                    double storeSale = 0;
////
////                    for (Map<String, Object> event : filteredEvents) {
////                        storeInvitees += parseInt(event.get("Invitees"));
////                        storeAttendees += parseInt(event.get("Attendees"));
////                        storeAdvance += parseDouble(event.get("advance"));
////                        storeGhsOrRga += parseDouble(event.get("ghs/rga"));
////                        storeSale += parseDouble(event.get("sale"));
////                    }
////
////                    totalEvents.addAndGet(storeEventCount);
////                    totalInvitees.addAndGet(storeInvitees);
////                    totalAttendees.addAndGet(storeAttendees);
////                    totalAdvance.add(storeAdvance);
////                    totalGhsOrRga.add(storeGhsOrRga);
////                    totalSale.add(storeSale);
////
////                    summaries.add(new StoreEventSummaryDTO(
////                            storeCode, storeEventCount, storeInvitees, storeAttendees,
////                            storeAdvance, storeGhsOrRga, storeSale
////                    ));
////
////                } catch (Exception e) {
////                    System.err.println("Error processing store " + storeCode + ": " + e.getMessage());
////                    summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
////                }
////            }));
////        }
////
////        for (Future<?> future : futures) {
////            try {
////                future.get();
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
////
////        executor.shutdown();
////
////        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
////                "TOTAL",
////                totalEvents.get(),
////                totalInvitees.get(),
////                totalAttendees.get(),
////                totalAdvance.sum(),
////                totalGhsOrRga.sum(),
////                totalSale.sum()
////        );
////
////        return new StoreSummaryWrapperDTO(summaries, total);
////    }
//
//    private StoreSummaryWrapperDTO processStoreCodesInParallel(List<String> storeCodes, LocalDate startDate, LocalDate endDate) throws Exception {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
//
//        AtomicInteger totalEvents = new AtomicInteger(0);
//        AtomicInteger totalInvitees = new AtomicInteger(0);
//        AtomicInteger totalAttendees = new AtomicInteger(0);
//        DoubleAdder totalAdvance = new DoubleAdder();
//        DoubleAdder totalGhsOrRga = new DoubleAdder();
//        DoubleAdder totalSale = new DoubleAdder();
//
//        int threadPoolSize = Math.min(10, Runtime.getRuntime().availableProcessors() * 2); // adjustable
//        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
//
//        List<Callable<Void>> tasks = storeCodes.stream().map(storeCode -> (Callable<Void>) () -> {
//            try {
//                CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//                Object rawData = eventsResponse.getEventData();
//
//                List<Map<String, Object>> events = new ArrayList<>();
//                if (rawData instanceof List<?>) {
//                    events = ((List<?>) rawData).stream()
//                            .filter(e -> e instanceof Map)
//                            .map(e -> (Map<String, Object>) e)
//                            .collect(Collectors.toList());
//                }
//
//                List<Map<String, Object>> filteredEvents = events.stream()
//                        .filter(event -> {
//                            if (startDate == null || endDate == null) return true;
//                            Object dateObj = event.get("eventDate");
//                            if (dateObj == null) return false;
//                            try {
//                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                            } catch (Exception ex) {
//                                return false;
//                            }
//                        })
//                        .collect(Collectors.toList());
//
//                int storeEventCount = filteredEvents.size();
//                int storeInvitees = 0;
//                int storeAttendees = 0;
//                double storeAdvance = 0;
//                double storeGhsOrRga = 0;
//                double storeSale = 0;
//
//                for (Map<String, Object> event : filteredEvents) {
//                    storeInvitees += parseInt(event.get("Invitees"));
//                    storeAttendees += parseInt(event.get("Attendees"));
//                    storeAdvance += parseDouble(event.get("advance"));
//                    storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//                    storeSale += parseDouble(event.get("sale"));
//                }
//
//                totalEvents.addAndGet(storeEventCount);
//                totalInvitees.addAndGet(storeInvitees);
//                totalAttendees.addAndGet(storeAttendees);
//                totalAdvance.add(storeAdvance);
//                totalGhsOrRga.add(storeGhsOrRga);
//                totalSale.add(storeSale);
//
//                summaries.add(new StoreEventSummaryDTO(
//                        storeCode, storeEventCount, storeInvitees, storeAttendees,
//                        storeAdvance, storeGhsOrRga, storeSale
//                ));
//            } catch (Exception e) {
//                System.err.println("Error processing store " + storeCode + ": " + e.getMessage());
//                summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
//            }
//            return null;
//        }).collect(Collectors.toList());
//
//        // Submit all tasks and wait for completion
//        executor.invokeAll(tasks);
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.MINUTES);
//
//        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
//                "TOTAL",
//                totalEvents.get(),
//                totalInvitees.get(),
//                totalAttendees.get(),
//                totalAdvance.sum(),
//                totalGhsOrRga.sum(),
//                totalSale.sum()
//        );
//
//        return new StoreSummaryWrapperDTO(summaries, total);
//    }
//
//
//
//
//    private CompletedEventsResponseDTO getAllCompletedEventsWithRetry(String storeCode) throws Exception {
//        int retries = 3;
//        int delay = 2000; // 2 seconds
//        for (int i = 0; i < retries; i++) {
//            return getAllCompletedEvents(storeCode);
//        }
//        throw new RuntimeException("Failed to fetch after retries for storeCode: " + storeCode);
//    }
//
//
//
//
//
//
//    private int parseInt(Object value) {
//        try {
//            return Integer.parseInt(value != null ? value.toString().trim() : "0");
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//
//    private double parseDouble(Object value) {
//        try {
//            return Double.parseDouble(value != null ? value.toString().trim() : "0");
//        } catch (Exception e) {
//            return 0.0;
//        }
//    }
//
//
//    public StoreEventSummaryDTO processSingleStoreCode(String storeCode, LocalDate startDate, LocalDate endDate) throws Exception {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//        Object rawData = eventsResponse.getEventData();
//
//        List<Map<String, Object>> events = new ArrayList<>();
//        if (rawData instanceof List<?>) {
//            events = ((List<?>) rawData).stream()
//                    .filter(e -> e instanceof Map)
//                    .map(e -> (Map<String, Object>) e)
//                    .collect(Collectors.toList());
//        }
//
//        List<Map<String, Object>> filteredEvents = events.stream()
//                .filter(event -> {
//                    if (startDate == null || endDate == null) return true;
//                    Object dateObj = event.get("eventDate");
//                    if (dateObj == null) return false;
//                    try {
//                        LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                    } catch (Exception ex) {
//                        return false;
//                    }
//                })
//                .collect(Collectors.toList());
//
//        int storeEventCount = filteredEvents.size();
//        int storeInvitees = 0;
//        int storeAttendees = 0;
//        double storeAdvance = 0;
//        double storeGhsOrRga = 0;
//        double storeSale = 0;
//
//        for (Map<String, Object> event : filteredEvents) {
//            storeInvitees += parseInt(event.get("Invitees"));
//            storeAttendees += parseInt(event.get("Attendees"));
//            storeAdvance += parseDouble(event.get("advance"));
//            storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//            storeSale += parseDouble(event.get("sale"));
//        }
//
//        return new StoreEventSummaryDTO(
//                storeCode, storeEventCount, storeInvitees, storeAttendees,
//                storeAdvance, storeGhsOrRga, storeSale
//        );
//    }
//}




package com.dechub.tanishq.service;



import com.dechub.tanishq.dto.*;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.dto.rivaahDto.BookAppointmentDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
import com.dechub.tanishq.gdrive.GoogleDriveService;
import com.dechub.tanishq.gsheet.*;
// import com.dechub.tanishq.mail.EmailService;
import com.dechub.tanishq.util.CommonConstants;
import com.dechub.tanishq.util.ResponseDataDTO;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


@Component
public class TanishqPageService {


    @Value("${dechub.bride.upload.dir}")
    private String UPLOAD_DIR;

    @Value("${dechub.base.image}")
    private String BASE_IMG;

    public ArrayList<ExcelStoreDTO> storeList = null;

    @Autowired
    private GSheetUserDetailsUtil gSheetUserDetailsUtil;



    // @Autowired
    // private EmailService emailService;


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

    @Autowired
    public TanishqPageService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }


    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder
                .build();
    }



    @Scheduled(fixedDelayString = "${dechub.scheduler.fixedDelay}")
    public void fetchData(){
        log.info("fetching details from Google sheet triggered");
        ArrayList<ExcelStoreDTO> lst = gSheetUserDetailsUtil.getData();
        if(lst != null){
            log.info("fetched details from Google sheet result store count: " + lst.size());
            if(lst.size() > 0){
                this.storeList = lst;
                log.info("fetched details from Google sheet assigned to main data");
            }else{
                log.info("fetched details from Google sheet result got Empty size 0");
            }
        }else{
            log.info("fetched details from Google sheet result got Empty");
        }
    }

    public EventsLoginResponseDTO eventsLogin(String storeCode, String password) throws Exception {
        EventsLoginResponseDTO responseDataDTO = new EventsLoginResponseDTO();
        String correctPassword = gSheetUserDetailsUtil.getNewPassword(storeCode);

        List<String> codeList = new ArrayList<>(Arrays.asList(
                "east1", "east2",
                "north1a", "north1b",
                "north2", "north3",
                "south1", "south2a", "south3",
                "west1a", "west1b", "west2", "west3","test","north1","west1","south2","north4"
        ));

        boolean isCodePresent = codeList.contains(storeCode.toLowerCase());
        if(!password.equals(correctPassword)){
            responseDataDTO.setStatus(false);
            return responseDataDTO;
        }
        if(isCodePresent && password.equals(correctPassword)){
            System.out.println("login successful");
            Map<String, Object> details = new HashMap<>();
            details.put("manager", storeCode.toUpperCase());
            responseDataDTO.setStoreData(details);
            responseDataDTO.setStatus(true);
            return responseDataDTO;
        }else{
//            String divisionName = this.getDivisionDirectory(storeCode);
            List<ExcelStoreDTO> result = this.storeList.stream().filter(i -> i.getStoreCode().toUpperCase().equals(storeCode)).collect(Collectors. toList());

            if( result.get(0).getStoreCode() == null){
                responseDataDTO.setStatus(false);
                return responseDataDTO;
            }

            Map<String, Object> details = gSheetUserDetailsUtil.getDataFromSheet(storeCode);
            System.out.println("login successful");
            responseDataDTO.setStoreData(details);
            responseDataDTO.setStatus(true);
            return responseDataDTO;
        }

    }
    public boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        int len = searchStr.length();
        int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }

    public ResponseDataDTO storeUserDetails(UserDetailsDTO userDetailsDTO) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        String divisionName = this.getDivisionDirectory(userDetailsDTO.getStoreCode());
        if(divisionName == null){
            responseDataDTO.setMessage("Invalid Store Code");
            return responseDataDTO;
        }

        //storing details
        userDetailsDTO.setDate(this.getCurrentTime());
        boolean isDone = gSheetUserDetailsUtil.insertSheetData(userDetailsDTO);

        //sending mail
//        String imageNameWithRespectiveDirectory = isWindows.equalsIgnoreCase("Y") ? divisionName + "\\" + userDetailsDTO.getSelfieImageName() : divisionName + "/" + userDetailsDTO.getSelfieImageName();
//        boolean isEmailSent = emailService.sendEmail(userDetailsDTO.getEmailId(), "Test", "This is Test Email", imageNameWithRespectiveDirectory);

        UserDetailResponseDTO userDetailResponseDTO = new UserDetailResponseDTO(isDone, false);
        responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
        responseDataDTO.setResult(userDetailResponseDTO);
        return responseDataDTO;
    }

    public QrResponseDTO storeEventsDetails(EventsDetailDTO eventsDetailsDTO) {



        QrResponseDTO isDone = gSheetUserDetailsUtil.insertSheetEventsData(eventsDetailsDTO);


        return isDone;
    }

    public ResponseDataDTO saveImage(MultipartFile file, String storeCode) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        String divisionName = this.getDivisionDirectory(storeCode);
        if(divisionName == null){
            responseDataDTO.setMessage("Invalid Store Code");
            return responseDataDTO;
        }

        try{
            String newFileName = this.getNewFileName(file.getOriginalFilename());
            String filePath = null;
            if(isWindows.equalsIgnoreCase("Y")){
                filePath = selfieDirectory + "\\" + divisionName + "\\" + newFileName;
            }else{
                filePath = selfieDirectory + "/" + divisionName + "/" + newFileName;
            }
            File targetFile = new File(filePath);
            file.transferTo(targetFile);
            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
            responseDataDTO.setResult(newFileName);
        }catch (Exception e){
            responseDataDTO.setMessage(e.getMessage());
        }
        return responseDataDTO;
    }

    // public ResponseDataDTO saveVideo(MultipartFile file) {
    //     ResponseDataDTO responseDataDTO = new ResponseDataDTO();

    //     try{
    //         googleDriveService.uploadVideo(file);
    //         responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
    //     }catch (Exception e){
    //         responseDataDTO.setMessage(e.getMessage());
    //     }
    //     return responseDataDTO;
    // }

    public String getNewFileName(String fileName){
        String result = null;
        try{
            String[] lst = fileName.split("\\.");
            String extension = lst[lst.length - 1];
            result =  getCurrentTimeAsUnique() + "." + extension;
//            result =  getCurrentTimeAsUnique() + selfieExtension;
        }catch (Exception e){
            log.info(e.getMessage() + " with file name: " + fileName);
        }
        return result;
    }

    public String getCurrentTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getCurrentTimeAsUnique(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getDivisionDirectory(String storeCode){
        String result = null;
        Map<String, List<String>> storeDetails = storeServices.getStoreDetails();
        for(Map.Entry<String, List<String>> entry : storeDetails.entrySet()){
            List<String> tempVar = entry.getValue().stream().filter(i -> i.equalsIgnoreCase(storeCode)).collect(Collectors.toList());
            if(tempVar.size() > 0){
                result = entry.getKey();
                break;
            }
        }

        return result;
    }


    public ResponseDataDTO getStoreCode() {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
        Map<String, List<String>> storesData = storeServices.getStoreDetails();
        List<String> result = new ArrayList<>();
        for(Map.Entry<String, List<String>> entry : storesData.entrySet()){
            result.addAll(entry.getValue());
        }
        responseDataDTO.setResult(result);
        return responseDataDTO;
    }


    public ResponseDataDTO storeBrideImage(MultipartFile file) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        if (file.isEmpty()) {
            responseDataDTO.setMessage("Please select a file to upload");
            return responseDataDTO;
        }

        try {
            byte[] bytes = file.getBytes();
            String newFileName = getNewFileName(file.getOriginalFilename());
            Path path = Paths.get(UPLOAD_DIR + File.separator + newFileName);
            Files.write(path, bytes);

            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
            responseDataDTO.setResult(newFileName);
            responseDataDTO.setStatus(true);
            // Set the full path of the stored file in the response
            responseDataDTO.setFilePath(path.toString());
            System.out.println("bride image uploaded in uploads");
        } catch (IOException e) {
            responseDataDTO.setMessage("Failed to upload file: " + file.getOriginalFilename() + " - " + e.getMessage());
            e.printStackTrace();
            // You can log the exception or handle it based on your application's needs
        }
        return responseDataDTO;
    }


    public ResponseEntity<byte[]> storeBrideDetails(String brideType, String brideEvent, String brideName, String phone, String date, String email,String zipcode,String filepath) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        BrideDetailsDTO brideDetailsDTO = new BrideDetailsDTO();
        brideDetailsDTO.setBrideName(brideName);
        brideDetailsDTO.setBrideEvent(brideEvent);
        brideDetailsDTO.setBrideType(brideType);
        brideDetailsDTO.setDate(date);
        brideDetailsDTO.setEmail(email);
        brideDetailsDTO.setPhone(phone);
        brideDetailsDTO.setZipCode(zipcode);
        System.out.println(brideDetailsDTO.getZipCode());
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
    public ResponseDataDTO storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO)  {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        int isDone = gSheetUserDetailsUtil.insertSheetAttendeesData(attendeesDetailDTO);
        if(isDone>0){
            boolean updated = gSheetUserDetailsUtil.updateAttendees(attendeesDetailDTO.getId(),isDone);
            if(updated){
                responseDataDTO.setStatus(true);
            }
        }else{
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("storing failed");
        }
        return responseDataDTO;
    }

//    public ResponseEntity<ApiResponse<String>> storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
//        try {
//            int isDone = gSheetUserDetailsUtil.insertSheetAttendeesData(attendeesDetailDTO);
//            if (isDone > 0) {
//                boolean updated = gSheetUserDetailsUtil.updateAttendees(attendeesDetailDTO.getId(), isDone);
//                if (updated) {
//                    return ResponseEntity.ok(new ApiResponse<>(200, "Data stored successfully", null));
//                } else {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(new ApiResponse<>(500, "Data inserted in sheet but update failed", null));
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new ApiResponse<>(400, "Failed to store data in sheet", null));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse<>(500, "An error occurred: " + e.getMessage(), null));
//        }
//    }



    public ResponseEntity<byte[]> processImageWithTextOverlay(String brideName, String phone, String date, String email, String filepath) {
        try {
            BufferedImage baseImage = ImageIO.read(ResourceUtils.getFile(BASE_IMG));
            BufferedImage overlayImage = ImageIO.read(ResourceUtils.getFile(filepath));

            // Resize overlay image if larger than base image
            overlayImage = resizeImage(overlayImage, baseImage.getWidth(), baseImage.getHeight());

            Graphics2D g2d = baseImage.createGraphics();
            g2d.setFont(new Font("Nunito", Font.PLAIN, 14));
            g2d.setColor(Color.BLACK);

            g2d.drawString("Name: " + brideName, 190, 250);
            g2d.drawString("Phone: " + phone, 500, 250);
            g2d.drawString("Email: " + email, 190, 320);
            g2d.drawString("Wedding Date: " + date, 500, 320);

//            g2d.drawString("Name: " + brideName, 70, 55);
//            g2d.drawString("Phone: " + phone, 170, 55);
//            g2d.drawString("Email: " + email, 70, 76);
//            g2d.drawString("Wedding Date: " + date, 170, 76);

            // Center the overlay image
            int imgX = (baseImage.getWidth() - overlayImage.getWidth()) / 2;
            int imgY = (baseImage.getHeight() / 4);

            g2d.drawImage(overlayImage, imgX, imgY, null);
            g2d.dispose();

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

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        float aspectRatio = (float) originalWidth / originalHeight;

        // Apply padding to ensure the image fits well within the target dimensions
        int paddedWidth = targetWidth - 400; // Adding a padding of 20 pixels
        int paddedHeight = targetHeight - 400; // Adding a padding of 20 pixels

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

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    public CompletedEventsResponseDTO getAllCompletedEvents(String code) {
        CompletedEventsResponseDTO completedEventsResponseDTO = new CompletedEventsResponseDTO();
        try{
            if(code.equalsIgnoreCase("North1")||
                    code.equalsIgnoreCase("North2")||
                    code.equalsIgnoreCase("North3")||
                    code.equalsIgnoreCase("North4")||
                    code.equalsIgnoreCase("South1")||
                    code.equalsIgnoreCase("South2")||
                    code.equalsIgnoreCase("South3")||
                    code.equalsIgnoreCase("East1")||
                    code.equalsIgnoreCase("East2")||
                    code.equalsIgnoreCase("West1")||
                    code.equalsIgnoreCase("West2")||
                    code.equalsIgnoreCase("West3")){

                List<storeCodeDataDTO> codes = gSheetUserDetailsUtil.getStoresByRegion(code);
                List<Map<String, Object>> combinedEvents = new ArrayList<>();

                for(storeCodeDataDTO store : codes){
                    List<Map<String, Object>> events = gSheetUserDetailsUtil.getCompletedEventDetails(store.getStoreCode());
                    combinedEvents.addAll(events);
                }

                if(!combinedEvents.isEmpty()){
                    completedEventsResponseDTO.setStatus(true);
                    completedEventsResponseDTO.setMessage("fetched events");
                    completedEventsResponseDTO.setEventData(combinedEvents);
                    return completedEventsResponseDTO;
                } else {
                    completedEventsResponseDTO.setStatus(false);
                    completedEventsResponseDTO.setMessage("fetching failed");
                    return completedEventsResponseDTO;
                }
            }
            List<Map<String, Object>> completedEventsDataDTOS = gSheetUserDetailsUtil.getCompletedEventDetails(code);
            if(completedEventsDataDTOS.size()>0){
                completedEventsResponseDTO.setStatus(true);
                completedEventsResponseDTO.setMessage("fetched events");
                completedEventsResponseDTO.setEventData(completedEventsDataDTOS);
            }else{
                completedEventsResponseDTO.setStatus(false);
                completedEventsResponseDTO.setMessage("fetching failed");

            }
        }catch (Exception e){
            e.printStackTrace();
            completedEventsResponseDTO.setStatus(false);
            completedEventsResponseDTO.setEventData("error"+e.getMessage());
        }
        return completedEventsResponseDTO;
    }


    public List<storeCodeDataDTO> getStoresByRegion(String region) {
        try {
            return gSheetUserDetailsUtil.getStoresByRegion(region);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<?> getInvitedMember(String eventId) {
        try{
            return  gSheetUserDetailsUtil.getAllAttendees(eventId);
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    public ResponseDataDTO getShareCode(RivaahDTO rivaahDTO) {
        return gSheetUserDetailsUtil.insertRivaahDetails(rivaahDTO);
    }

    public ResponseDataDTO storeRivaahUser(String name, String contact) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        boolean status = gSheetUserDetailsUtil.insertRivaahUserDetails(name, contact);

        // Create minimal DTO to call appointment()
        BookAppointmentDTO bookAppointmentDTO = new BookAppointmentDTO();
        String[] nameParts = name.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        bookAppointmentDTO.setFirstName(firstName);
        bookAppointmentDTO.setLastName(lastName);
        bookAppointmentDTO.setPhone(contact);


        if (!status) {
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("User details storing failed");
            return responseDataDTO;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(bookAnAppoitmentUsername, bookAnAppoitmentPassword);
            headers.add("PartnerId", "Ecomm");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BookAppointmentDTO> entity = new HttpEntity<>(bookAppointmentDTO, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    bookAnAppointmentUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getBody() != null) {
                responseDataDTO.setStatus(true);
                responseDataDTO.setMessage("Successfully stored user details and booked appointment");
                responseDataDTO.setResult(response.getBody());
            } else {
                responseDataDTO.setStatus(false);
                responseDataDTO.setMessage("Stored user details, but failed to book appointment");
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("Error while booking appointment: " + e.getMessage());
        }

        return responseDataDTO;
    }

    public RivaahAllDetailsDTO getRivaahDetails(String code) {
        return gSheetUserDetailsUtil.getRivaahDetails(code);
    }

    public ResponseDataDTO changePasswordForEventManager(String storeCode, String oldPassword, String newPassword) {
        return gSheetUserDetailsUtil.changePassword(storeCode,oldPassword,newPassword);
    }

    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) {
        try{
            return gSheetUserDetailsUtil.updateSaleOfAnEvent(eventCode,sale);
        }catch (Exception e){
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage(e.getMessage());
            return responseDataDTO;
        }
    }
    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) {
        try{
            return gSheetUserDetailsUtil.updateAdvanceOfAnEvent(eventCode,advance);
        }catch (Exception e){
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage(e.getMessage());
            return responseDataDTO;
        }
    }
    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) {
        try{
            return gSheetUserDetailsUtil.updateGhsRgaOfAnEvent(eventCode,ghsRga);
        }catch (Exception e){
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage(e.getMessage());
            return responseDataDTO;
        }
    }
    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) {
        try{
            return gSheetUserDetailsUtil.updateGmbOfAnEvent(eventCode,gmb);
        }catch (Exception e){
            ResponseDataDTO responseDataDTO = new ResponseDataDTO();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage(e.getMessage());
            return responseDataDTO;
        }
    }

//    public boolean authenticateAbm(String username, String password) {
//        try {
//            return gSheetUserDetailsUtil.isValidUserAbm(username, password);
//        } catch (Exception e) {
//            // Log the error
//            return false;
//        }
//    }

    public Optional<LoginResponseDTO> authenticateAbm(String username, String password) {
        try {
            return gSheetUserDetailsUtil.isValidUserAbm(username, password);
        } catch (Exception e) {
            return Optional.empty();
        }
    }



    public Optional<LoginResponseDTO> authenticateRbm(String username, String password) {
        try {
            return gSheetUserDetailsUtil.isValidUserRbm(username, password);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public Optional<LoginResponseDTO> authenticateCee(String username, String password) {
        try {
            return gSheetUserDetailsUtil.isValidUserCee(username, password);
        } catch (Exception e) {
            return Optional.empty();
        }
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
        for (String storeCode : storeCodes) {
            try {
                CompletedEventsResponseDTO events = getAllCompletedEvents(storeCode);
                if (events != null) {
                    result.add(events);
                }
            } catch (Exception e) {
                // Log and skip problematic store
            }
        }
        return result;
    }

    public List<Map<String, Object>> getOnlyEventsForStores(List<String> storeCodes) {
        List<Map<String, Object>> allEvents = new ArrayList<>();
        for (String storeCode : storeCodes) {
            try {
                CompletedEventsResponseDTO dto = getAllCompletedEvents(storeCode);
                if (dto != null && dto.getStatus()==true && dto.getEventData() instanceof List) {
                    List<Map<String, Object>> events = (List<Map<String, Object>>) dto.getEventData();
                    if (events != null) {
                        allEvents.addAll(events);
                    }
                }
            } catch (Exception e) {
                // Optionally log the error
            }
        }
        return allEvents;
    }


    public List<Map<String, Object>> filterEventsByStartDate(List<Map<String, Object>> events, String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDateStr, formatter);
        LocalDate end = LocalDate.parse(endDateStr, formatter);

        return events.stream()
                .filter(e -> {
                    try {
                        String startDate = (String) e.get("StartDate");
                        if (startDate == null || startDate.isEmpty()) return false;
                        LocalDate eventDate = LocalDate.parse(startDate);
                        return !eventDate.isBefore(start) && !eventDate.isAfter(end);
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

//    public List<StoreEventSummaryDTO> fetchStoreSummariesByRbmParallel(String rbmUsername) throws Exception {
//        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
//
//        // Use parallel stream for faster processing
//        return storeCodes.parallelStream().map(storeCode -> {
//            try {
//                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
//                Object rawData = eventsResponse.getEventData();
//
//                List<Map<String, Object>> events = new ArrayList<>();
//
//                if (rawData instanceof List<?>) {
//                    List<?> rawList = (List<?>) rawData;
//                    events = rawList.stream()
//                            .filter(e -> e instanceof Map)
//                            .map(e -> (Map<String, Object>) e)
//                            .collect(Collectors.toList());
//                }
//
//                int totalEvents = events.size();
//                int totalInvitees = 0;
//                int totalAttendees = 0;
//                double totalAdvance = 0;
//                double totalGhsOrRga = 0;
//                double totalSale = 0;
//
//                for (Map<String, Object> event : events) {
//                    totalInvitees += parseInt(event.get("Invitees"));
//                    totalAttendees += parseInt(event.get("Attendees"));
//                    totalAdvance += parseDouble(event.get("advance"));
//                    totalGhsOrRga += parseDouble(event.get("ghs/rga"));
//                    totalSale += parseDouble(event.get("sale"));
//                }
//
//                return new StoreEventSummaryDTO(
//                        storeCode, totalEvents, totalInvitees, totalAttendees,
//                        totalAdvance, totalGhsOrRga, totalSale
//                );
//
//            } catch (Exception e) {
//                // In case of failure, return summary with zeros
//                return new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0);
//            }
//        }).collect(Collectors.toList());
//    }

//    public List<StoreEventSummaryDTO> fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        return storeCodes.parallelStream().map(storeCode -> {
//            try {
//                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
//                Object rawData = eventsResponse.getEventData();
//
//                List<Map<String, Object>> events = new ArrayList<>();
//
//                if (rawData instanceof List<?>) {
//                    List<?> rawList = (List<?>) rawData;
//                    events = rawList.stream()
//                            .filter(e -> e instanceof Map)
//                            .map(e -> (Map<String, Object>) e)
//                            .collect(Collectors.toList());
//                }
//
//                // ✅ Apply date filter ONLY if both dates are provided
//                List<Map<String, Object>> filteredEvents = events.stream()
//                        .filter(event -> {
//                            if (startDate == null || endDate == null) return true;
//
//                            Object dateObj = event.get("eventDate");
//                            if (dateObj == null) return false;
//
//                            try {
//                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                            } catch (Exception e) {
//                                return false;
//                            }
//                        })
//                        .collect(Collectors.toList());
//
//                int totalEvents = filteredEvents.size();
//                int totalInvitees = 0;
//                int totalAttendees = 0;
//                double totalAdvance = 0;
//                double totalGhsOrRga = 0;
//                double totalSale = 0;
//
//                for (Map<String, Object> event : filteredEvents) {
//                    totalInvitees += parseInt(event.get("Invitees"));
//                    totalAttendees += parseInt(event.get("Attendees"));
//                    totalAdvance += parseDouble(event.get("advance"));
//                    totalGhsOrRga += parseDouble(event.get("ghs/rga"));
//                    totalSale += parseDouble(event.get("sale"));
//                }
//
//                return new StoreEventSummaryDTO(
//                        storeCode, totalEvents, totalInvitees, totalAttendees,
//                        totalAdvance, totalGhsOrRga, totalSale
//                );
//
//            } catch (Exception e) {
//                return new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0);
//            }
//        }).collect(Collectors.toList());
//    }


//    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
//
//        AtomicInteger totalEvents = new AtomicInteger(0);
//        AtomicInteger totalInvitees = new AtomicInteger(0);
//        AtomicInteger totalAttendees = new AtomicInteger(0);
//        DoubleAdder totalAdvance = new DoubleAdder();
//        DoubleAdder totalGhsOrRga = new DoubleAdder();
//        DoubleAdder totalSale = new DoubleAdder();
//
//        storeCodes.parallelStream().forEach(storeCode -> {
//            try {
//                CompletedEventsResponseDTO eventsResponse = getAllCompletedEvents(storeCode);
//                Object rawData = eventsResponse.getEventData();
//
//                List<Map<String, Object>> events = new ArrayList<>();
//
//                if (rawData instanceof List<?>) {
//                    List<?> rawList = (List<?>) rawData;
//                    events = rawList.stream()
//                            .filter(e -> e instanceof Map)
//                            .map(e -> (Map<String, Object>) e)
//                            .collect(Collectors.toList());
//                }
//
//                List<Map<String, Object>> filteredEvents = events.stream()
//                        .filter(event -> {
//                            if (startDate == null || endDate == null) return true;
//                            Object dateObj = event.get("eventDate");
//                            if (dateObj == null) return false;
//
//                            try {
//                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                            } catch (Exception e) {
//                                return false;
//                            }
//                        })
//                        .collect(Collectors.toList());
//
//                int storeEventCount = filteredEvents.size();
//                int storeInvitees = 0;
//                int storeAttendees = 0;
//                double storeAdvance = 0;
//                double storeGhsOrRga = 0;
//                double storeSale = 0;
//
//                for (Map<String, Object> event : filteredEvents) {
//                    storeInvitees += parseInt(event.get("Invitees"));
//                    storeAttendees += parseInt(event.get("Attendees"));
//                    storeAdvance += parseDouble(event.get("advance"));
//                    storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//                    storeSale += parseDouble(event.get("sale"));
//                }
//
//                // Add to totals (thread-safe)
//                totalEvents.addAndGet(storeEventCount);
//                totalInvitees.addAndGet(storeInvitees);
//                totalAttendees.addAndGet(storeAttendees);
//                totalAdvance.add(storeAdvance);
//                totalGhsOrRga.add(storeGhsOrRga);
//                totalSale.add(storeSale);
//
//                summaries.add(new StoreEventSummaryDTO(
//                        storeCode, storeEventCount, storeInvitees, storeAttendees,
//                        storeAdvance, storeGhsOrRga, storeSale
//                ));
//
//            } catch (Exception e) {
//                summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
//            }
//        });
//
//        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
//                "TOTAL",
//                totalEvents.get(),
//                totalInvitees.get(),
//                totalAttendees.get(),
//                totalAdvance.sum(),
//                totalGhsOrRga.sum(),
//                totalSale.sum()
//        );
//
//        return new StoreSummaryWrapperDTO(summaries, total);
//    }


//    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<StoreEventSummaryDTO> summaries = new ArrayList<>();
//
//        AtomicInteger totalEvents = new AtomicInteger(0);
//        AtomicInteger totalInvitees = new AtomicInteger(0);
//        AtomicInteger totalAttendees = new AtomicInteger(0);
//        DoubleAdder totalAdvance = new DoubleAdder();
//        DoubleAdder totalGhsOrRga = new DoubleAdder();
//        DoubleAdder totalSale = new DoubleAdder();
//
//        for (String storeCode : storeCodes) {
//            try {
//                CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//                Object rawData = eventsResponse.getEventData();
//
//                List<Map<String, Object>> events = new ArrayList<>();
//                if (rawData instanceof List<?>) {
//                    events = ((List<?>) rawData).stream()
//                            .filter(e -> e instanceof Map)
//                            .map(e -> (Map<String, Object>) e)
//                            .collect(Collectors.toList());
//                }
//
//                List<Map<String, Object>> filteredEvents = events.stream()
//                        .filter(event -> {
//                            if (startDate == null || endDate == null) return true;
//                            Object dateObj = event.get("eventDate");
//                            if (dateObj == null) return false;
//                            try {
//                                LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                            } catch (Exception e) {
//                                return false;
//                            }
//                        })
//                        .collect(Collectors.toList());
//
//                int storeEventCount = filteredEvents.size();
//                int storeInvitees = 0;
//                int storeAttendees = 0;
//                double storeAdvance = 0;
//                double storeGhsOrRga = 0;
//                double storeSale = 0;
//
//                for (Map<String, Object> event : filteredEvents) {
//                    storeInvitees += parseInt(event.get("Invitees"));
//                    storeAttendees += parseInt(event.get("Attendees"));
//                    storeAdvance += parseDouble(event.get("advance"));
//                    storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//                    storeSale += parseDouble(event.get("sale"));
//                }
//
//                totalEvents.addAndGet(storeEventCount);
//                totalInvitees.addAndGet(storeInvitees);
//                totalAttendees.addAndGet(storeAttendees);
//                totalAdvance.add(storeAdvance);
//                totalGhsOrRga.add(storeGhsOrRga);
//                totalSale.add(storeSale);
//
//                summaries.add(new StoreEventSummaryDTO(storeCode, storeEventCount, storeInvitees, storeAttendees, storeAdvance, storeGhsOrRga, storeSale));
//
//            } catch (Exception e) {
//                // Log error if needed
//                System.err.println("Failed to fetch for store: " + storeCode + " - " + e.getMessage());
//                summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
//            }
//        }
//
//        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
//                "TOTAL",
//                totalEvents.get(),
//                totalInvitees.get(),
//                totalAttendees.get(),
//                totalAdvance.sum(),
//                totalGhsOrRga.sum(),
//                totalSale.sum()
//        );
//
//        return new StoreSummaryWrapperDTO(summaries, total);
//    }

    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger totalEvents = new AtomicInteger(0);
        AtomicInteger totalInvitees = new AtomicInteger(0);
        AtomicInteger totalAttendees = new AtomicInteger(0);
        DoubleAdder totalAdvance = new DoubleAdder();
        DoubleAdder totalGhsOrRga = new DoubleAdder();
        DoubleAdder totalSale = new DoubleAdder();

        int maxThreads = 2; // reduced concurrency
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (String storeCode : storeCodes) {
            futures.add(executor.submit(() -> {
                try {
                    // Rate limiting: sleep before making the request
                    Thread.sleep(1000); // 1 second delay between each thread's request

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
                                Object dateObj = event.get("eventDate");
                                if (dateObj == null) return false;
                                try {
                                    LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
                                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                                } catch (Exception e) {
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
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

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

    public CompletedEventsResponseDTO getAllCompletedEventsWithBackoff(String storeCode) throws Exception {
        int maxRetries = 5;
        int backoff = 1000; // 1 second

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            return getAllCompletedEvents(storeCode);
        }

        throw new RuntimeException("Max retry attempts reached for store: " + storeCode);
    }


    //    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
//        List<String> storeCodes = fetchStoresByAbm(rbmUsername);
//        return processStoreCodesInParallel(storeCodes, startDate, endDate);
//    }
    public StoreSummaryWrapperDTO fetchStoreSummariesByAbmParallel(String abmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByAbm(abmUsername);
        return processStoreCodesInParallel(storeCodes, startDate, endDate);
    }

    public StoreSummaryWrapperDTO fetchStoreSummariesByCeeParallel(String ceeUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByCee(ceeUsername);
        return processStoreCodesInParallel(storeCodes, startDate, endDate);
    }

//    private StoreSummaryWrapperDTO processStoreCodesInParallel(List<String> storeCodes, LocalDate startDate, LocalDate endDate) throws Exception {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());
//
//        AtomicInteger totalEvents = new AtomicInteger(0);
//        AtomicInteger totalInvitees = new AtomicInteger(0);
//        AtomicInteger totalAttendees = new AtomicInteger(0);
//        DoubleAdder totalAdvance = new DoubleAdder();
//        DoubleAdder totalGhsOrRga = new DoubleAdder();
//        DoubleAdder totalSale = new DoubleAdder();
//
//        ExecutorService executor = Executors.newFixedThreadPool(4);
//        List<Future<?>> futures = new ArrayList<>();
//
//        for (String storeCode : storeCodes) {
//            futures.add(executor.submit(() -> {
//                try {
//                    CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//                    Object rawData = eventsResponse.getEventData();
//
//                    List<Map<String, Object>> events = new ArrayList<>();
//                    if (rawData instanceof List<?>) {
//                        events = ((List<?>) rawData).stream()
//                                .filter(e -> e instanceof Map)
//                                .map(e -> (Map<String, Object>) e)
//                                .collect(Collectors.toList());
//                    }
//
//                    List<Map<String, Object>> filteredEvents = events.stream()
//                            .filter(event -> {
//                                if (startDate == null || endDate == null) return true;
//                                Object dateObj = event.get("eventDate");
//                                if (dateObj == null) return false;
//                                try {
//                                    LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                                } catch (Exception e) {
//                                    return false;
//                                }
//                            })
//                            .collect(Collectors.toList());
//
//                    int storeEventCount = filteredEvents.size();
//                    int storeInvitees = 0;
//                    int storeAttendees = 0;
//                    double storeAdvance = 0;
//                    double storeGhsOrRga = 0;
//                    double storeSale = 0;
//
//                    for (Map<String, Object> event : filteredEvents) {
//                        storeInvitees += parseInt(event.get("Invitees"));
//                        storeAttendees += parseInt(event.get("Attendees"));
//                        storeAdvance += parseDouble(event.get("advance"));
//                        storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//                        storeSale += parseDouble(event.get("sale"));
//                    }
//
//                    totalEvents.addAndGet(storeEventCount);
//                    totalInvitees.addAndGet(storeInvitees);
//                    totalAttendees.addAndGet(storeAttendees);
//                    totalAdvance.add(storeAdvance);
//                    totalGhsOrRga.add(storeGhsOrRga);
//                    totalSale.add(storeSale);
//
//                    summaries.add(new StoreEventSummaryDTO(
//                            storeCode, storeEventCount, storeInvitees, storeAttendees,
//                            storeAdvance, storeGhsOrRga, storeSale
//                    ));
//
//                } catch (Exception e) {
//                    System.err.println("Error processing store " + storeCode + ": " + e.getMessage());
//                    summaries.add(new StoreEventSummaryDTO(storeCode, 0, 0, 0, 0, 0, 0));
//                }
//            }));
//        }
//
//        for (Future<?> future : futures) {
//            try {
//                future.get();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        executor.shutdown();
//
//        StoreEventSummaryDTO total = new StoreEventSummaryDTO(
//                "TOTAL",
//                totalEvents.get(),
//                totalInvitees.get(),
//                totalAttendees.get(),
//                totalAdvance.sum(),
//                totalGhsOrRga.sum(),
//                totalSale.sum()
//        );
//
//        return new StoreSummaryWrapperDTO(summaries, total);
//    }

    private StoreSummaryWrapperDTO processStoreCodesInParallel(List<String> storeCodes, LocalDate startDate, LocalDate endDate) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<StoreEventSummaryDTO> summaries = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger totalEvents = new AtomicInteger(0);
        AtomicInteger totalInvitees = new AtomicInteger(0);
        AtomicInteger totalAttendees = new AtomicInteger(0);
        DoubleAdder totalAdvance = new DoubleAdder();
        DoubleAdder totalGhsOrRga = new DoubleAdder();
        DoubleAdder totalSale = new DoubleAdder();

        int threadPoolSize = Math.min(10, Runtime.getRuntime().availableProcessors() * 2); // adjustable
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        List<Callable<Void>> tasks = storeCodes.stream().map(storeCode -> (Callable<Void>) () -> {
            try {
                CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
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
                            Object dateObj = event.get("eventDate");
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

        // Submit all tasks and wait for completion
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




    private CompletedEventsResponseDTO getAllCompletedEventsWithRetry(String storeCode) throws Exception {
        int retries = 3;
        int delay = 2000; // 2 seconds
        for (int i = 0; i < retries; i++) {
            return getAllCompletedEvents(storeCode);
        }
        throw new RuntimeException("Failed to fetch after retries for storeCode: " + storeCode);
    }






    private int parseInt(Object value) {
        try {
            return Integer.parseInt(value != null ? value.toString().trim() : "0");
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(Object value) {
        try {
            return Double.parseDouble(value != null ? value.toString().trim() : "0");
        } catch (Exception e) {
            return 0.0;
        }
    }


//    public StoreEventSummaryDTO processSingleStoreCode(String storeCode, LocalDate startDate, LocalDate endDate) throws Exception {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//        Object rawData = eventsResponse.getEventData();
//
//        List<Map<String, Object>> events = new ArrayList<>();
//        if (rawData instanceof List<?>) {
//            events = ((List<?>) rawData).stream()
//                    .filter(e -> e instanceof Map)
//                    .map(e -> (Map<String, Object>) e)
//                    .collect(Collectors.toList());
//        }
//
//        List<Map<String, Object>> filteredEvents = events.stream()
//                .filter(event -> {
//                    if (startDate == null || endDate == null) return true;
//                    Object dateObj = event.get("eventDate");
//                    if (dateObj == null) return false;
//                    try {
//                        LocalDate eventDate = LocalDate.parse(dateObj.toString(), formatter);
//                        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
//                    } catch (Exception ex) {
//                        return false;
//                    }
//                })
//                .collect(Collectors.toList());
//
//        int storeEventCount = filteredEvents.size();
//        int storeInvitees = 0;
//        int storeAttendees = 0;
//        double storeAdvance = 0;
//        double storeGhsOrRga = 0;
//        double storeSale = 0;
//
//        for (Map<String, Object> event : filteredEvents) {
//            storeInvitees += parseInt(event.get("Invitees"));
//            storeAttendees += parseInt(event.get("Attendees"));
//            storeAdvance += parseDouble(event.get("advance"));
//            storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//            storeSale += parseDouble(event.get("sale"));
//        }
//
//        return new StoreEventSummaryDTO(
//                storeCode, storeEventCount, storeInvitees, storeAttendees,
//                storeAdvance, storeGhsOrRga, storeSale
//        );
//    }

//    public StoreEventSummaryDTO processSingleStoreCode(String storeCode, LocalDate startDate, LocalDate endDate) throws Exception {
//        DateTimeFormatter fallbackFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
//        Object rawData = eventsResponse.getEventData();
//
//        List<Map<String, Object>> events = new ArrayList<>();
//        if (rawData instanceof List<?>) {
//            events = ((List<?>) rawData).stream()
//                    .filter(e -> e instanceof Map)
//                    .map(e -> (Map<String, Object>) e)
//                    .collect(Collectors.toList());
//        }
//
//        List<Map<String, Object>> filteredEvents = events.stream()
//                .filter(event -> {
//                    Object dateObj = event.get("eventDate");
//                    if (dateObj == null) {
//                        System.out.println("Missing eventDate in event: " + event);
//                        return false;
//                    }
//
//                    try {
//                        String dateStr = dateObj.toString();
//                        LocalDate eventDate;
//
//                        if (dateStr.contains("T")) {
//                            eventDate = LocalDateTime.parse(dateStr).toLocalDate(); // ISO_LOCAL_DATE_TIME
//                        } else {
//                            eventDate = LocalDate.parse(dateStr, fallbackFormatter);
//                        }
//
//                        return (startDate == null || !eventDate.isBefore(startDate)) &&
//                                (endDate == null || !eventDate.isAfter(endDate));
//
//                    } catch (Exception ex) {
//                        System.out.println("Failed to parse eventDate: " + dateObj + " | Event: " + event);
//                        return false;
//                    }
//                })
//                .collect(Collectors.toList());
//
//        int storeEventCount = filteredEvents.size();
//        int storeInvitees = 0;
//        int storeAttendees = 0;
//        double storeAdvance = 0;
//        double storeGhsOrRga = 0;
//        double storeSale = 0;
//
//        for (Map<String, Object> event : filteredEvents) {
//            storeInvitees += parseInt(event.get("Invitees"));
//            storeAttendees += parseInt(event.get("Attendees"));
//            storeAdvance += parseDouble(event.get("advance"));
//            storeGhsOrRga += parseDouble(event.get("ghs/rga"));
//            storeSale += parseDouble(event.get("sale"));
//        }
//
//        return new StoreEventSummaryDTO(
//                storeCode,
//                storeEventCount,
//                storeInvitees,
//                storeAttendees,
//                storeAdvance,
//                storeGhsOrRga,
//                storeSale
//        );
//    }

    public StoreEventSummaryDTO processSingleStoreCode(String storeCode, LocalDate startDate, LocalDate endDate) throws Exception {
        CompletedEventsResponseDTO eventsResponse = getAllCompletedEventsWithRetry(storeCode);
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
                    Object dateObj = event.get("StartDate"); // Use StartDate instead of eventDate
                    if (dateObj == null) return false;
                    try {
                        LocalDate eventDate = parseFlexibleDate(dateObj.toString());
                        return eventDate != null && !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
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

        return new StoreEventSummaryDTO(
                storeCode, storeEventCount, storeInvitees, storeAttendees,
                storeAdvance, storeGhsOrRga, storeSale
        );
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }


//    public ResponseDataDTO appointment(BookAppointmentDTO bookAppointmentDTO,boolean isVisitStore) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        bookAppointmentDTO.setBrand("Tanishq");
//
//        try {
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBasicAuth(bookAnAppoitmentUsername,bookAnAppoitmentPassword);
//            headers.add("PartnerId", "Ecomm");
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<BookAppointmentDTO> entity = new HttpEntity<>(bookAppointmentDTO, headers);
//            ResponseEntity<String> response = restTemplate.exchange(bookAnAppointmentUrl, HttpMethod.POST, entity, String.class);
//
//            if (response.getBody() != null) {
//                responseDataDTO.setStatus(true);
//                responseDataDTO.setMessage("Success");
//                responseDataDTO.setResult(response.getBody());
//            } else {
//                responseDataDTO.setMessage("Failed to book appointment");
//            }
//
//        } catch (Exception e) {
//            responseDataDTO.setMessage("Error: " + e.getMessage());
//        }
//        return responseDataDTO;
//    }

    public ResponseDataDTO appointment(BookAppointmentDTO bookAppointmentDTO, boolean isVisitStore) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(bookAnAppoitmentUsername, bookAnAppoitmentPassword);
            headers.add("PartnerId", "Ecomm");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BookAppointmentDTO> entity = new HttpEntity<>(bookAppointmentDTO, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    bookAnAppointmentUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Save to Google Sheet
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
                responseDataDTO.setStatus(true);
                if (dataAdded) {
                    responseDataDTO.setMessage("Success");
                } else {
                    responseDataDTO.setMessage("Success, but failed to save data in sheet");
                }
                responseDataDTO.setResult(response.getBody());
            } else {
                responseDataDTO.setMessage("Failed to book appointment");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Recommended during debugging
            responseDataDTO.setMessage("Error: " + e.getMessage());
        }

        return responseDataDTO;
    }
}


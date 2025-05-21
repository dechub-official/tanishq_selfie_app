//package com.dechub.tanishq.gsheet;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.UUID;
//import java.time.format.DateTimeFormatter;
//import java.time.LocalDateTime;
//import com.dechub.tanishq.dto.BrideDetailsDTO;
//import com.dechub.tanishq.dto.ExcelStoreDTO;
//import com.dechub.tanishq.dto.eventsDto.*;
//import com.dechub.tanishq.dto.UserDetailsDTO;
//import com.dechub.tanishq.dto.rivaahDto.ProductDetailDTO;
//import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
//import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
//import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
//import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
//import com.dechub.tanishq.util.APIResponseBuilder;
//import com.dechub.tanishq.util.CommonConstants;
//import com.dechub.tanishq.util.ResponseDataDTO;
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.sheets.v4.Sheets;
//import com.google.api.services.sheets.v4.SheetsScopes;
//import com.google.api.services.sheets.v4.model.AppendValuesResponse;
//import com.google.api.services.sheets.v4.model.ValueRange;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import java.io.File;
//import java.util.*;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//
//import java.io.ByteArrayOutputStream;
//import java.util.stream.Collectors;
//
//
//@Component
//
//public class GSheetUserDetailsUtil {
//
//    @Value("${google.sheet.dechub.user.details.id}")
//    private String sheetId;
//
//    @Value("${google.sheet.dechub.bride.details.id}")
//    private String sheetId2;
//
//    @Value("${google.sheet.dechub.range}")
//    private String sheetRange;
//
//    @Value("${google.sheet.dechub.store.details.id}")
//    private String sheetId3;
//
//    @Value("${google.sheet.dechub.events.details.id}")
//    private String sheetId4;
//
//    @Value("${google.sheet.dechub.events_attendees.details.id}")
//    private String sheetId5;
//
//    @Value("${google.sheet.dechub.events_invitees.details.id}")
//    private String sheetId6;
//
//    @Value("${google.sheet.dechub.checklist_products.id}")
//    private String sheetId7;
//
//    @Value("${google.sheet.dechub.rivaah.details.id}")
//    private String sheetId8;
//
//    @Value("${google.sheet.dechub.rivaah.user.details.id}")
//    private String sheetId9;
//
//    @Value("${dechub.tanishq.key.filepath}")
//    private String filePath;
//    @Value("${dechub.tanishq.google.service.account}")
//    private String serviceAccount;
//
//    private static final Logger log = LoggerFactory.getLogger(APIResponseBuilder.class);
//
//
//    @Autowired
//    private UserSession userSession;
//
//    @Autowired
//    private ExcelEventsUtil excelEventsUtil;
//
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
//
//    public ArrayList<ExcelStoreDTO> getData(){
//        try{
//            ArrayList<ExcelStoreDTO> result = new ArrayList<>();
//            List<List<Object>> res = this.getSheetData();
//            for(List<Object> store: res){
//
//                if(store.get(0) == null){
//                    break;
//                }
//                ExcelStoreDTO item = new ExcelStoreDTO();
//
//                item.setStoreCode(this.formInput(store,0));
//                item.setStoreName(this.formInput(store,3));
//                item.setStoreAddress(this.formInput(store,4));
//                item.setStoreCity(this.formInput(store,5));
//                item.setStoreState(this.formInput(store,6));
//                item.setStoreCountry(this.formInput(store,7));
//                item.setStoreZipCode(this.formInput(store,8));
//                item.setStorePhoneNoOne(this.formInput(store,9));
//                item.setStorePhoneNoTwo(this.formInput(store,10));
//                item.setStoreEmailId(this.formInput(store,11));
//                item.setStoreLatitude(this.formInput(store,12));
//                item.setStoreLongitude(this.formInput(store,13));
//                item.setStoreDateOfOpening(this.formInput(store,14));
//                item.setStoreType(this.formInput(store,15));
//                item.setStoreOpeningTime(this.formInput(store,16));
//                item.setStoreClosingTime(this.formInput(store,17));
//                item.setStoreManagerName(this.formInput(store,18));
//                item.setStoreManagerNo(this.formInput(store,19));
//                item.setStoreManagerEmail(this.formInput(store,20));
//                item.setStoreLocationLink(this.formInput(store,21));
//
//                item.setLanguages(this.formInputForLanguages(store,22));
//                item.setParking(this.formInputForParking(store,23));
//                item.setPayment(this.formInputForPayment(store,24));
//                item.setKakatiyaStore(this.formInput(store,25));
//                item.setCelesteStore(this.formInput(store,26));
//                item.setRating(this.formInput(store,27));
//                item.setNumberOfRatings(this.formInput(store,28));
//                item.setIsCollection(this.formInput(store,29));
//
//                result.add(item);
//            }
//            return result;
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }
//    private List<List<Object>> getSheetData() throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//        ValueRange response = service.spreadsheets().values().get(sheetId3, sheetRange).execute();
//        return response.getValues();
//
//    }
//    private Object formInputForLanguages(List<Object> store, int index){
//        ArrayList<String> res = new ArrayList<>();
//        try{
//            String data =  (String) store.get(index);
//            String[] comaSepVal = data.split(",");
//
//            for(String i : comaSepVal){
//                String[] andSepVal = i.split("and");
//                for(String j : andSepVal){
//                    res.add(j);
//                }
//            }
////            for(String i : comaSepVal){
////                res.add(i);
////            }
//            if(res.size() == 0){
//                res.add(data);
//            }
//
//        }catch (Exception e){
//            //return res;
//        }
//
//        if(res.size() > 0){
//            List<String> finalRes = new ArrayList<>();
//            for(String i : res){
//                if(this.containsIgnoreCase(i, "Telugu")){
//                    finalRes.add("Telugu");
//                }
//                if(this.containsIgnoreCase(i, "Urudu")){
//                    finalRes.add("Urudu");
//                }
//                if(this.containsIgnoreCase(i, "Malayalam")){
//                    finalRes.add("Malayalam");
//                }
//                if(this.containsIgnoreCase(i, "Hindi")){
//                    finalRes.add("Hindi");
//                }
//                if(this.containsIgnoreCase(i, "English")){
//                    finalRes.add("English");
//                }
//                if(this.containsIgnoreCase(i, "Tamil")){
//                    finalRes.add("Tamil");
//                }
//                if(this.containsIgnoreCase(i, "Marathi")){
//                    finalRes.add("Marathi");
//                }
//                if(this.containsIgnoreCase(i, "Gujarati")){
//                    finalRes.add("Gujarati");
//                }
//                if(this.containsIgnoreCase(i, "Rajasthani")){
//                    finalRes.add("Rajasthani");
//                }
//                if(this.containsIgnoreCase(i, "Punjabi")){
//                    finalRes.add("Punjabi");
//                }
//                if(this.containsIgnoreCase(i, "Bengali")){
//                    finalRes.add("Bengali");
//                }
//                if(this.containsIgnoreCase(i, "Local Languages")){
//                    finalRes.add("Local Languages");
//                }
//                if(this.containsIgnoreCase(i, "Odiya")){
//                    finalRes.add("Odiya");
//                }
//                if(this.containsIgnoreCase(i, "Bhojpuri")){
//                    finalRes.add("Bhojpuri");
//                }
//                if(this.containsIgnoreCase(i, "Marvadi")){
//                    finalRes.add("Marvadi");
//                }
//                if(this.containsIgnoreCase(i, "Angika")){
//                    finalRes.add("Angika");
//                }
//                if(this.containsIgnoreCase(i, "Kannada")){
//                    finalRes.add("Kannada");
//                }
//                if(this.containsIgnoreCase(i, "Coorgi")){
//                    finalRes.add("Coorgi");
//                }
//                if(this.containsIgnoreCase(i, "Nagamese")){
//                    finalRes.add("Nagamese");
//                }
//                if(this.containsIgnoreCase(i, "Konkani")){
//                    finalRes.add("Konkani");
//                }
//            }
//            return finalRes.stream().distinct().collect(Collectors. toList());
//        }
//        else{
//            return res;
//        }
//
////        return  res.stream().distinct().collect(Collectors. toList());
////        return res;
//    }
//
//    private Object formInputForPayment(List<Object> store, int index){
//        ArrayList<String> res = new ArrayList<>();
//        try{
//            String data =  (String) store.get(index);
//            String[] comaSepVal = data.split(",");
//            for(String i : comaSepVal){
//                String[] andSepVal = i.split("&");
//                for(String j : andSepVal){
//                    res.add(j);
//                }
//            }
//            if(res.size() == 0){
//                res.add(data);
//            }
//
//        }catch (Exception e){
//            //return res;
//        }
//
//        if(res.size() > 0){
//            List<String> finalRes = new ArrayList<>();
//            for(String i : res){
//                if(this.containsIgnoreCase(i, "Cheque")){
//                    finalRes.add("Cheque");
//                }
//                if(this.containsIgnoreCase(i, "Airpay")){
//                    finalRes.add("Airpay");
//                }
//                if(this.containsIgnoreCase(i, "Old Gold exchange")){
//                    finalRes.add("Old Gold exchange");
//                }
//                if(this.containsIgnoreCase(i, "Net Banking")){
//                    finalRes.add("Net Banking");
//                }
//                if(this.containsIgnoreCase(i, "UPI")){
//                    finalRes.add("UPI");
//                }
//                if(this.containsIgnoreCase(i, "Credit Card")){
//                    finalRes.add("Credit Card");
//                }
//                if(this.containsIgnoreCase(i, "Debit Card")){
//                    finalRes.add("Debit Card");
//                }
//                if(this.containsIgnoreCase(i, "Cash")){
//                    finalRes.add("Cash");
//                }
//            }
//            return finalRes.stream().distinct().collect(Collectors. toList());
//        }
//        else{
//            return res;
//        }
//    }
//
//    private Object formInputForParking(List<Object> store, int index){
//        ArrayList<String> res = new ArrayList<>();
//        try{
//            String data =  (String) store.get(index);
//            String[] comaSepVal = data.split(",");
//            for(String i : comaSepVal){
//                res.add(i);
//            }
//            if(res.size() == 0){
//                res.add(data);
//            }
//
//        }catch (Exception e){
//            //return res;
//        }
//        return  res.stream().distinct().collect(Collectors. toList());
//    }
//    private String formInput(List<Object> store, int index){
//        try{
//            return (String) store.get(index);
//        }catch (Exception e){
//            return "";
//        }
//    }
//
//    public List<Map<String, Object>> getCompletedEventDetails(String code) throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//// Define the range to fetch all columns
//        String range = "Sheet1!A:Q"; // Assuming columns A to L contain the necessary data
//
//// Fetch data from the sheet
//        ValueRange response = service.spreadsheets().values()
//                .get(sheetId4, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        List<Map<String, Object>> events = new ArrayList<>();
//
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//            return Collections.singletonList(Map.of("status", false));
//        }
//
//// Get the header row
//        List<String> headers = values.get(0).stream()
//                .map(Object::toString)
//                .map(this::sanitizeHeader)
//                .collect(Collectors.toList());
//
//// Iterate over rows and extract required columns
//        for (int i = 1; i < values.size(); i++) { // Start from 1 to skip headers
//            List<Object> row = values.get(i);
//            if (row.size() > 0 && code.equalsIgnoreCase(row.get(0).toString())) {
//                Map<String, Object> event = new HashMap<>();
//
//                for (int j = 0; j < headers.size(); j++) {
//                    if (j < row.size()) {
//                        event.put(headers.get(j).toString(), row.get(j));
//
//                    } else {
//                        event.put(headers.get(j).toString(), ""); // Fill missing columns with empty string
//
//                    }
//                }
//                // Check if "Completed Events" is not empty and set completedEvent key
////                int completedEventsIndex = headers.indexOf("completedEvents");
////                if (completedEventsIndex != -1 && completedEventsIndex < row.size()) {
////                    String completedEventsValue = row.get(completedEventsIndex).toString().trim();
////                    event.put("completedEvent", !completedEventsValue.isEmpty());
////                } else {
////                    event.put("completedEvent", false); // Default to false if column is missing or empty
////                }
//
//                event.put("completedEvent", event.get("completedEvents").toString().trim().length()>1);
//
//                events.add(event);
//            }
//        }
//
//        return events;
//    }
//
//    public List<storeCodeDataDTO> getStoresByRegion(String regionSheetName) throws Exception {
//        List<storeCodeDataDTO> stores = new ArrayList<>();
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//
//        String range = regionSheetName + "!A2:A"; // Only first column (Store Code), skip header
//        ValueRange response = service.spreadsheets().values()
//                .get(sheetId3, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        if (values != null && !values.isEmpty()) {
//            for (List<Object> row : values) {
//                if (!row.isEmpty() && row.get(0) != null && !row.get(0).toString().trim().isEmpty()) {
//                    storeCodeDataDTO dto = new storeCodeDataDTO();
//                    dto.setStoreCode(row.get(0).toString().trim());
//                    stores.add(dto);
//                }
//            }
//        }
//        return stores;
//    }
//
//    public Map<String, Object> getDataFromSheet(String storeCode) throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//        String range = "stores"; // Adjust sheet name if needed
//
//        ValueRange response = service.spreadsheets().values()
//                .get(sheetId3, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//
//        if (values == null || values.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        // Assume the first row contains headers
//        List<String> headers = values.get(0).stream()
//                .map(Object::toString)
//                .map(this::sanitizeHeader)
//                .collect(Collectors.toList());
//
//        for (int i = 1; i < values.size(); i++) {
//            List<Object> row = values.get(i);
//            if (row.size() > 0 && row.get(0).equals(storeCode)) {
//                return createDataMap(headers, row);
//            }
//        }
//
//        return Collections.emptyMap();
//    }
//    public RivaahImagesDTO getImagesByTags(List<String> tags) throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Set<String> categories = new HashSet<>();
//        List<ProductDetailDTO> productDetails = new ArrayList<>();
//        Sheets service = getSheetService(httpTransport);
//        String range = "Sheet1!A:E"; // Adjust sheet name if needed
//
//        // Fetch data from Google Sheets
//        ValueRange response = service.spreadsheets().values()
//                .get(sheetId7, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//
//        if (values != null && !values.isEmpty()) {
//            // Step through each row of the sheet data
//            for (List<Object> row : values) {
//                String id = row.get(0).toString();
//                String category = row.get(1).toString();   // Category is in the first column
//                String imageLink = row.get(2).toString();  // Image link in the second column
//                String productLink = row.get(3).toString();// Product link in the third column
//
//                // Check if the category contains any of the tags in the list
//                for (String tag : tags) {
//                    if (category.equalsIgnoreCase(tag)) {
//                        categories.add(category);  // Add category to Set to avoid duplicates
//                        productDetails.add(new ProductDetailDTO(id,imageLink, productLink));
//                        break;  // Once a tag matches, no need to check other tags for this row
//                    }
//                }
//            }
//        }
//
//        Collections.shuffle(productDetails);
//
//        // Convert the set of categories to a list and create the response object
//        return new RivaahImagesDTO(new ArrayList<>(categories), productDetails);
//    }
//
//    public ResponseEntity<String> incrementLikeCount( Long imageId,boolean increase) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            // Fetch data from the sheet
//            String range = "Sheet1" + "!A:E";
//            ValueRange response = service.spreadsheets().values().get(sheetId7, range).execute();
//            List<List<Object>> values = response.getValues();
//
//            if (values == null || values.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found in the sheet.");
//            }
//
//            // Locate the row with the matching imageId
//            for (int i = 1; i < values.size(); i++) { // Start from 1 to skip the header row
//                List<Object> row = values.get(i);
//
//                if (row.size() > 0 && row.get(0).toString().equals(String.valueOf(imageId))) {
//                    // Increment the like count in column E
//                    int currentLikes = row.size() > 4 && !row.get(4).toString().isEmpty()
//                            ? Integer.parseInt(row.get(4).toString())
//                            : 0;
//                    if(increase) currentLikes++;
//                    if(!increase) currentLikes--;
//
//                    // Update the sheet
//                    String updateRange = "Sheet1" + "!E" + (i + 1);
//                    ValueRange body = new ValueRange().setValues(List.of(List.of(currentLikes)));
//                    service.spreadsheets().values()
//                            .update(sheetId7, updateRange, body)
//                            .setValueInputOption("RAW")
//                            .execute();
//                    if(increase) return ResponseEntity.ok("Like count incremented successfully.");
//                    return ResponseEntity.ok("Like count decremented successfully.");
//                }
//            }
//
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with imageId not found.");
//
//        }
//        catch (IOException | NumberFormatException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating the like count.");
//        }catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
//        }
//    }
//
//    private String sanitizeHeader(String header) {
//        return header.replaceAll("\\s+", "");
//    }
//
//    private Map<String, Object> createDataMap(List<String> headers, List<Object> row) {
//        Map<String, Object> dataMap = new HashMap<>();
//        for (int i = 0; i < Math.min(headers.size(), row.size()); i++) {
//            dataMap.put(headers.get(i), row.get(i));
//        }
//        return dataMap;
//    }
//    public boolean insertSheetData(UserDetailsDTO userDetailsDTO) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            ValueRange body = new ValueRange()
//                    .setValues(this.formInputData(userDetailsDTO));
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId, "A2", body)
//                    .setValueInputOption("RAW").execute();
//            int resSize = appendValuesResponse.size();
//            if (resSize > 0) {
//                System.out.println("user details excel updated successfully");
//                return true;
//            } else {
//                System.out.println("user details excel updated failed");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
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
//    public boolean insertSheetBrideData(BrideDetailsDTO brideDetailsDTO) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            ValueRange body = new ValueRange()
//                    .setValues(this.formInputBrideData(brideDetailsDTO));
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId2, "A2", body)
//                    .setValueInputOption("RAW").execute();
//            int resSize = appendValuesResponse.size();
//            if (resSize > 0) {
//                System.out.println("bride details excel updated successfully");
//                return true;
//            } else {
//                System.out.println("bride details excel updated failed");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//
//    public QrResponseDTO insertSheetEventsData(EventsDetailDTO eventsDetailDTO) {
//        QrResponseDTO qrResponseDTO = new QrResponseDTO();
//        try {
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            eventsDetailDTO.setId(eventsDetailDTO.getStoreCode() + "_" + UUID.randomUUID().toString());
//            if (!eventsDetailDTO.getSingleCustomer()) {
//                int inviteesCount = excelEventsUtil.uploadExcelFile(eventsDetailDTO.getFile());
//                eventsDetailDTO.setInvitees(inviteesCount);
//                boolean inviteesUpdated = uploadXlsxToGoogleSheet(eventsDetailDTO.getFile(),eventsDetailDTO.getId(),sheetId6);
//                if(!inviteesUpdated){
//                    qrResponseDTO.setStatus(false);
//                    qrResponseDTO.setQrData("Invitees sheet update failed");
//                    return qrResponseDTO;
//                }
//            } else {
//                eventsDetailDTO.setInvitees(1);
//                InviteesDetailDTO inviteesDetailDTO = new InviteesDetailDTO();
//                inviteesDetailDTO.setName(eventsDetailDTO.getName());
//                inviteesDetailDTO.setEventId(eventsDetailDTO.getId());
//                inviteesDetailDTO.setContact(eventsDetailDTO.getContact());
//                boolean inviteesUpdated = insertSheetInviteesData(inviteesDetailDTO);
//                if(!inviteesUpdated){
//                    qrResponseDTO.setStatus(false);
//                    qrResponseDTO.setQrData("Invitees sheet update failed");
//                    return qrResponseDTO;
//                }
//            }
//
//            // Initialize row data with empty values for each column
//            int totalColumns = 20; // Adjust this based on the total number of columns
//            List<Object> rowData = new ArrayList<>(Collections.nCopies(totalColumns, ""));
//
//            // Column mapping according to the sheet
//            rowData.set(0, dtf.format(now)); // createdAt
//            rowData.set(1, eventsDetailDTO.getStoreCode() != null ? eventsDetailDTO.getStoreCode() : "");
//            rowData.set(2, eventsDetailDTO.getRegion() != null ? eventsDetailDTO.getRegion() : "");
//            rowData.set(3, eventsDetailDTO.getId() != null ? eventsDetailDTO.getId() : "");
//            rowData.set(4, eventsDetailDTO.getEventType() != null ? eventsDetailDTO.getEventType() : "");
//            rowData.set(5, eventsDetailDTO.getEventSubType() != null ? eventsDetailDTO.getEventSubType() : "");
//            rowData.set(6, eventsDetailDTO.getEventName() != null ? eventsDetailDTO.getEventName() : "");
//            rowData.set(7, eventsDetailDTO.getRso() != null ? eventsDetailDTO.getRso() : "");
//            rowData.set(8, eventsDetailDTO.getStartDate() != null ? eventsDetailDTO.getStartDate() : "");
//            rowData.set(9, eventsDetailDTO.getImage() != null ? eventsDetailDTO.getImage() : "");
//            rowData.set(10, eventsDetailDTO.getInvitees());
//            rowData.set(11, eventsDetailDTO.getAttendees());
//            rowData.set(12, eventsDetailDTO.getCompletedEventsDriveLink() != null ? eventsDetailDTO.getCompletedEventsDriveLink() : "");
//            rowData.set(13, eventsDetailDTO.getCommunity() != null ? eventsDetailDTO.getCommunity() : "");
//            rowData.set(14, eventsDetailDTO.getLocation() != null ? eventsDetailDTO.getLocation() : "");
//            rowData.set(15, eventsDetailDTO.isAttendeesUploaded());
//            rowData.set(16, eventsDetailDTO.getSale() != null ? eventsDetailDTO.getSale() : 0);
//            rowData.set(17, eventsDetailDTO.getAdvance() != null ? eventsDetailDTO.getAdvance() : 0);
//            rowData.set(18, eventsDetailDTO.getGhsOrRga() != null ? eventsDetailDTO.getGhsOrRga() : 0);
//            rowData.set(19, eventsDetailDTO.getGmb() != null ? eventsDetailDTO.getGmb() : 0);
//
//
//
//            List<List<Object>> values = Collections.singletonList(rowData);
//
//            ValueRange body = new ValueRange()
//                    .setValues(values);
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
//                    .append(SheetId11, "Sheet1" + "!A2", body)
//                    .setValueInputOption("USER_ENTERED")
//                    .execute();
//
//            int updatedRows = appendValuesResponse.getUpdates().getUpdatedRows();
//            if (updatedRows > 0) {
//
//                String imageResponse = generateQrCode(eventsDetailDTO.getId());
//                if (imageResponse.equals("error")) {
//                    qrResponseDTO.setStatus(false);
//                    qrResponseDTO.setQrData("error in generating qr code");
//                    return qrResponseDTO;
//                }
//                qrResponseDTO.setStatus(true);
//                qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
//                return qrResponseDTO;
//            } else {
//                qrResponseDTO.setStatus(false);
//                qrResponseDTO.setQrData("Events sheet update failed");
//                return qrResponseDTO;
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            qrResponseDTO.setStatus(false);
//            qrResponseDTO.setQrData("error:"+e.getMessage());
//            return qrResponseDTO;
//        }
//    }
//    public String generateQrCode(String eventId)  {
//        try {
//            // Generate QR code
//            String qrCodeText = "https://celebrations.tanishq.co.in/events/customer/" + eventId;
//            BufferedImage qrCodeImage = generateQRCodeImage(qrCodeText);
//
//            // Convert QR code to byte array
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(qrCodeImage, "png", baos);
//            byte[] qrCodeBytes = baos.toByteArray();
//
//            // Encode the QR code data to Base64
//            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);
//
//            // Set headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            return base64QrCode;
//        } catch (Exception e) {
//            return "error";
//        }
//
//    }
//    private BufferedImage generateQRCodeImage(String text) throws WriterException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
//        return MatrixToImageWriter.toBufferedImage(bitMatrix);
//    }
//
//
//
//    private Credential getCred(NetHttpTransport transport) throws Exception {
//        File file = new File(filePath);
//        if (!file.exists()) {
//            throw new Exception("Keys file Not found");
//        }
//
//        Credential credential = new GoogleCredential.Builder()
//                .setTransport(transport)
//                .setJsonFactory(JSON_FACTORY)
//                .setServiceAccountId(serviceAccount)
//                .setServiceAccountPrivateKeyFromP12File(file)
//                .setServiceAccountScopes(SCOPES).build();
//        return credential;
//    }
//
//    private Sheets getSheetService(NetHttpTransport transport) throws Exception {
//        return new Sheets.Builder(transport, JSON_FACTORY, this.getCred(transport)).build();
//    }
//
//    private List<List<Object>> formInputData(UserDetailsDTO input) {
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(input.getName());
//        item.add(input.getReason());
//        item.add(input.getRsoName());
//        item.add(input.getStoreCode());
//        item.add(input.getDate());
//        item.add(input.getMyFirstDiamond());
//        res.add(item);
//        return res;
//    }
//    private List<List<Object>> formInputBrideData(BrideDetailsDTO input) {
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(input.getBrideName());
//        item.add(input.getBrideEvent());
//        item.add(input.getEmail());
//        item.add(input.getPhone());
//        item.add(input.getDate());
//        item.add(input.getBrideType());
//        item.add(input.getZipCode());
//        res.add(item);
//        return res;
//    }
//
//
//    public int insertSheetAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
//        try {
//            if(attendeesDetailDTO.getFile()!=null&&!attendeesDetailDTO.getFile().isEmpty()){
//                int attendeesCount = excelEventsUtil.uploadExcelFile(attendeesDetailDTO.getFile());
//                log.info("attendeesCount "+attendeesCount);
//                boolean attendeesUpdated = uploadXlsxToGoogleSheet(attendeesDetailDTO.getFile(),attendeesDetailDTO.getId(),sheetId5);
//                return attendeesCount;
//            }else{
//                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//                Sheets service = getSheetService(httpTransport);
//
//                ValueRange body = new ValueRange()
//                        .setValues(this.formInputAttendeesData(attendeesDetailDTO));
//
//                AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId5, "A2", body)
//                        .setValueInputOption("RAW").execute();
//                int resSize = appendValuesResponse.size();
//                if (resSize > 0) {
//                    System.out.println("attendees details excel updated successfully");
//                    return 1;
//                } else {
//                    System.out.println("attendees details excel updated failed");
//                    return 0;
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//    public boolean insertSheetInviteesData(InviteesDetailDTO inviteesDetailDTO) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            ValueRange body = new ValueRange()
//                    .setValues(this.formInputInviteesData(inviteesDetailDTO));
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId6, "A2", body)
//                    .setValueInputOption("RAW").execute();
//            int resSize = appendValuesResponse.size();
//            if (resSize > 0) {
//                System.out.println("invitees details excel updated successfully");
//                return true;
//            } else {
//                System.out.println("invitees details excel updated failed");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//    private List<List<Object>> formInputInviteesData(InviteesDetailDTO inviteesDetailDTO) {
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(inviteesDetailDTO.getName());
//        item.add(inviteesDetailDTO.getContact());
//        item.add(dtf.format(now));
//        item.add(inviteesDetailDTO.getEventId());
//        res.add(item);
//        return res;
//    }
//    private List<List<Object>> formInputAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(attendeesDetailDTO.getId());
//        item.add(attendeesDetailDTO.getName());
//        item.add(attendeesDetailDTO.getPhone());
//        item.add(attendeesDetailDTO.getLike());
//        item.add(attendeesDetailDTO.isFirstTimeAtTanishq());
//        item.add(dtf.format(now));
//        item.add(false);
//        res.add(item);
//        return res;
//    }
//
//    public boolean updateAttendees(String eventId,int count) {
//       try{
//           // Get the values in the Id column
//           String range = "Sheet1" + "!" + "B" + ":" + "B";
//           final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//           Sheets service = getSheetService(httpTransport);
//           ValueRange response = service.spreadsheets().values()
//                   .get(sheetId4, range)
//                   .execute();
//
//           List<List<Object>> values = response.getValues();
//           if (values == null || values.isEmpty()) {
//               System.out.println("No data found.");
//               return false;
//           }
//
//           // Search for the eventId in the column
//           int rowIndex = -1;
//           for (int i = 0; i < values.size(); i++) {
//               if (values.get(i).get(0).equals(eventId)) {
//                   rowIndex = i + 1; // +1 because rows are 1-indexed in Google Sheets
//                   break;
//               }
//           }
//
//           if (rowIndex == -1) {
//               System.out.println(eventId);
//               System.out.println("Event ID not found.");
//               return false;
//           }
//
//           /// Get the current value of the Attendees column
//           String attendeesCell = "Sheet1" + "!" + "L" + rowIndex;
//           ValueRange attendeesResponse = service.spreadsheets().values()
//                   .get(sheetId4, attendeesCell)
//                   .execute();
//           List<List<Object>> attendeesValues = attendeesResponse.getValues();
//
//           int currentAttendees = 0;
//           if (attendeesValues != null && !attendeesValues.isEmpty() && !attendeesValues.get(0).isEmpty()) {
//               currentAttendees = Integer.parseInt(attendeesValues.get(0).get(0).toString());
//           }
//
//// Increment the number of attendees by 1
//           currentAttendees += count;
//
//// Update the Attendees column with the new value
//           ValueRange attendeesBody = new ValueRange()
//                   .setValues(Arrays.asList(Arrays.asList(currentAttendees)));
//           BatchUpdateValuesRequest attendeesUpdateRequest = new BatchUpdateValuesRequest()
//                   .setValueInputOption("RAW")
//                   .setData(Arrays.asList(
//                           new ValueRange().setRange(attendeesCell).setValues(Arrays.asList(Arrays.asList(currentAttendees)))
//                   ));
//           service.spreadsheets().values()
//                   .batchUpdate(sheetId4, attendeesUpdateRequest)
//                   .execute();
//
//// Set isDone to true if value > 1, otherwise false
//           String isDoneCell = "Sheet1" + "!" + "Q" + rowIndex;
//
//
//           boolean isDone = count > 1;
//
//// Update Column P with true/false based on isDoneValue
//           ValueRange isDoneBody = new ValueRange()
//                   .setValues(Arrays.asList(Arrays.asList(isDone)));
//           BatchUpdateValuesRequest isDoneUpdateRequest = new BatchUpdateValuesRequest()
//                   .setValueInputOption("RAW")
//                   .setData(Arrays.asList(
//                           new ValueRange().setRange(isDoneCell).setValues(Arrays.asList(Arrays.asList(isDone)))
//                   ));
//           service.spreadsheets().values()
//                   .batchUpdate(sheetId4, isDoneUpdateRequest)
//                   .execute();
//
//           System.out.println("Row updated successfully.");
//           return true;
//
//       }catch (Exception e){
//           System.out.println(e.getMessage());
//           System.out.println("Row updation failed");
//           return false;
//       }
//    }
//    public boolean updateDrivelink(String eventId, String link) {
//        try {
//            // Get the values in the Id column
//            String range = "Sheet1" + "!" + "B" + ":" + "B";
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//            ValueRange response = service.spreadsheets().values()
//                    .get(sheetId4, range)
//                    .execute();
//
//            List<List<Object>> values = response.getValues();
//            if (values == null || values.isEmpty()) {
//                System.out.println("No data found.");
//                return false;
//            }
//
//            // Search for the eventId in the column
//            int rowIndex = -1;
//            for (int i = 0; i < values.size(); i++) {
//                if (values.get(i).get(0).equals(eventId)) {
//                    rowIndex = i + 1; // +1 because rows are 1-indexed in Google Sheets
//                    break;
//                }
//            }
//
//            if (rowIndex == -1) {
//                System.out.println("Event ID not found.");
//                return false;
//            }
//
//            // Update the Link column with the new value
//            String linkCell = "Sheet1" + "!" + "N" + rowIndex;
//            ValueRange body = new ValueRange()
//                    .setValues(Arrays.asList(Arrays.asList(link)));
//            BatchUpdateValuesRequest batchUpdateValuesRequest = new BatchUpdateValuesRequest()
//                    .setValueInputOption("RAW")
//                    .setData(Arrays.asList(
//                            new ValueRange().setRange(linkCell).setValues(Arrays.asList(Arrays.asList(link)))
//                    ));
//
//            service.spreadsheets().values()
//                    .batchUpdate(sheetId4, batchUpdateValuesRequest)
//                    .execute();
//
//            System.out.println("Row updated successfully.");
//            return true;
//        } catch (Exception e) {
//            System.out.println("Row update failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//
//    public boolean uploadXlsxToGoogleSheet(MultipartFile file, String eventId,String sheetId) throws Exception {
//        // Read XLSX file
//        List<List<Object>> data = readXlsxFile(file);
//        if(sheetId.equals(sheetId5)){
//            // Add current date and time, and eventId to each row
//            data = addDateTimeAndEventIdToData(data, eventId,true);
//        }else{
//            // Add current date and time, and eventId to each row
//            data = addDateTimeAndEventIdToData(data, eventId,false);
//        }
//
//        // Upload to Google Sheets
//        return uploadToGoogleSheets(data, sheetId);
//    }
//
//    private List<List<Object>> readXlsxFile(MultipartFile file) throws IOException {
//        List<List<Object>> data = new ArrayList<>();
//        Workbook workbook = new XSSFWorkbook(file.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
//
//        // Start from the second row (index 1) to skip the header
//        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//            Row row = sheet.getRow(i);
//            List<Object> rowData = new ArrayList<>();
//            for (Cell cell : row) {
//                switch (cell.getCellType()) {
//                    case STRING:
//                        rowData.add(cell.getStringCellValue());
//                        break;
//                    case NUMERIC:
//                        rowData.add(cell.getNumericCellValue());
//                        break;
//                    case BOOLEAN: // Handle boolean cells
//                        rowData.add(cell.getBooleanCellValue());
//                        break;
//                    // Add other cases as needed
//                    default:
//                        rowData.add("");
//                }
//            }
//            data.add(rowData);
//        }
//
//        workbook.close();
//        return data;
//    }
//
//    private List<List<Object>> addDateTimeAndEventIdToData(List<List<Object>> data, String eventId,boolean attendees) {
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String dateTime = now.format(formatter);
//
//        List<List<Object>> newData = new ArrayList<>();
//        if(attendees){
//            // Add data rows with the required columns and default values
//            for (List<Object> row : data) {
//                List<Object> newRow = new ArrayList<>();
//                newRow.add(eventId); // Event ID
//                newRow.add(row.size() > 0 ? row.get(0) : ""); // Name
//                newRow.add(row.size() > 1 ? row.get(1) : ""); // Contact
//                newRow.add(row.size() > 2 ? row.get(2) : ""); // Like
//                newRow.add(row.size() > 3 ? row.get(3) : ""); // First time
//                newRow.add(dateTime); // Created At
//                newRow.add(true); // isUploadedFromExcel
//                newData.add(newRow);
//            }
//        }else{
//        // Add data rows with timestamp and eventId
//        for (List<Object> row : data) {
//            List<Object> newRow = new ArrayList<>(row);
//            newRow.add(dateTime);
//            newRow.add(eventId);
//            newData.add(newRow);
//        }
//        }
//
//
//        return newData;
//    }
//
//    private boolean uploadToGoogleSheets(List<List<Object>> data, String sheetId) throws Exception {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(HTTP_TRANSPORT);
//
//        ValueRange body = new ValueRange()
//                .setValues(data);
//
//        AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
//                .append(sheetId, "A2", body)
//                .setValueInputOption("RAW")
//                .execute();
//        int resSize = appendValuesResponse.getUpdates().getUpdatedRows();
//        if (resSize > 0) {
//            System.out.println("invitees details excel updated successfully");
//            return true;
//        } else {
//            System.out.println("invitees details excel update failed");
//            return false;
//        }
//    }
//
//    public ResponseDataDTO insertRivaahDetails(RivaahDTO rivaahDTO) {
//        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//            String code = String.format("%010d", (long) (Math.random() * 1_000_000_0000L));
//            ValueRange body = new ValueRange()
//                    .setValues(this.formInputRivaahData(code,rivaahDTO));
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId8, "A2", body)
//                    .setValueInputOption("RAW").execute();
//            int resSize = appendValuesResponse.size();
//            if (resSize > 0) {
//                System.out.println("rivaah details excel updated successfully");
//                responseDataDTO.setResult(code);
//                responseDataDTO.setStatus(true);
//                responseDataDTO.setMessage("Rivaah details stored successfully");
//            } else {
//                System.out.println("rivaah details excel updated failed");
//                responseDataDTO.setStatus(false);
//                responseDataDTO.setMessage("rivaah details excel updated failed");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            responseDataDTO.setStatus(false);
//            responseDataDTO.setMessage("rivaah details excel updated failed");
//        }
//        return responseDataDTO;
//    }
//    private List<List<Object>> formInputRivaahData(String code, RivaahDTO rivaahDTO) {
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(code);
//        System.out.println(rivaahDTO.getBride());
//        item.add(rivaahDTO.getBride());
//        item.add(rivaahDTO.getEvent());
//        item.add(rivaahDTO.getClothing_type());
//
//        // Join the tags with comma, removing spaces between tags
//        String formattedTags = String.join(",", rivaahDTO.getTags());
//        item.add(formattedTags);
//
//        res.add(item);
//        return res;
//    }
//
//    public boolean insertRivaahUserDetails(String name,String contact) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            ValueRange body = new ValueRange()
//                    .setValues(this.formInputRivaahUserData(name,contact));
//
//            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId9, "A2", body)
//                    .setValueInputOption("RAW").execute();
//            int resSize = appendValuesResponse.size();
//            if (resSize > 0) {
//                System.out.println("rivaah user details excel updated successfully");
//                return true;
//            } else {
//                System.out.println("rivaah user details excel updated failed");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//    private List<List<Object>> formInputRivaahUserData(String name,String contact) {
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(name);
//        item.add(contact);
//        res.add(item);
//        return res;
//    }
//
//    public RivaahAllDetailsDTO getRivaahDetails(String code) {
//        RivaahAllDetailsDTO rivaahAllDetailsDTO = new RivaahAllDetailsDTO();
//        rivaahAllDetailsDTO.setStatus(false);
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            // Step 1: Fetch data from sheetId8 where code matches
//            ValueRange response8 = service.spreadsheets().values()
//                    .get(sheetId8, "Sheet1!A:E")
//                    .execute();
//
//            List<List<Object>> values8 = response8.getValues();
//
//            if (values8 == null || values8.isEmpty()) {
//                rivaahAllDetailsDTO.setMessage("No data found in the sheet");
//                return rivaahAllDetailsDTO; // No data found in the sheet
//            }
//
//            List<Object> matchingRow = null;
//
//            // Step 2: Loop through the rows and find the matching code
//            for (List<Object> row : values8) {
//                if (row.size() > 0 && row.get(0).equals(code)) {  // Assuming code is in the first column
//                    matchingRow = row;
//                    break;
//                }
//            }
//
//            if (matchingRow == null) {
//                rivaahAllDetailsDTO.setMessage("No matching code found in the sheet");
//                return rivaahAllDetailsDTO; // No matching code found
//            }
//
//            // Extract bride, event, clothing_type, tags (assuming the structure of columns)
//            String tagsStr = (String) matchingRow.get(4);
//            rivaahAllDetailsDTO.setBride(matchingRow.get(1).toString());
//            rivaahAllDetailsDTO.setEvent(matchingRow.get(2).toString());
//            rivaahAllDetailsDTO.setCode(code);
//            rivaahAllDetailsDTO.setClothing_type( matchingRow.get(3).toString());
//
//            // Step 3: Extract list of tags by removing the brackets and splitting by comma
//            tagsStr = tagsStr.trim();  // Trim any leading or trailing spaces
//            List<String> tags = List.of(tagsStr.split("\\s*,\\s*"));
//            System.out.println(tags);
//            rivaahAllDetailsDTO.setTags(tags);
//            RivaahImagesDTO rivaahImagesDTO = getImagesByTags(tags);
//            System.out.println(rivaahImagesDTO);
//            rivaahAllDetailsDTO.setImages(rivaahImagesDTO);
//            rivaahAllDetailsDTO.setStatus(true);
//            rivaahAllDetailsDTO.setMessage("Details successfully fetched from the sheet.");
//            return rivaahAllDetailsDTO;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public List<AttendeesDetailDTO> getAllAttendees(String eventId) throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets sheetsService = getSheetService(httpTransport);
//
//        String range = "Sheet1!A:F"; // Adjust the range as per your sheet structure
//        ValueRange response = sheetsService.spreadsheets().values()
//                .get(sheetId5, range)
//                .execute();
//
//        List<List<Object>> rows = response.getValues();
//        List<AttendeesDetailDTO> attendeesList = new ArrayList<>();
//
//        if (rows != null && !rows.isEmpty()) {
//            for (List<Object> row : rows) {
//                // Skip if the row does not have the required columns
//                if (row.size() < 6) continue;
//
//                // Check if the first column matches the eventId
//                if (eventId.equals(row.get(0).toString())) {
//                    AttendeesDetailDTO dto = new AttendeesDetailDTO();
//                    dto.setName(row.get(1).toString());
//                    dto.setPhone(row.get(2).toString());
//                    dto.setLike(row.get(3).toString());
//                    dto.setFirstTimeAtTanishq(Boolean.parseBoolean(row.get(4).toString().toLowerCase()));
//                    attendeesList.add(dto);
//                }
//            }
//        }
//        return attendeesList;
//
//    }
//}





package com.dechub.tanishq.gsheet;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import com.dechub.tanishq.dto.BrideDetailsDTO;
import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.dto.UserDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.ProductDetailDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.CommonConstants;
import com.dechub.tanishq.util.ResponseDataDTO;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;


@Component

public class GSheetUserDetailsUtil {

    @Value("${google.sheet.dechub.user.details.id}")
    private String sheetId;

    @Value("${google.sheet.dechub.bride.details.id}")
    private String sheetId2;

    @Value("${google.sheet.dechub.range}")
    private String sheetRange;

    @Value("${google.sheet.dechub.store.details.id}")
    private String sheetId3;

    @Value("${google.sheet.dechub.events.details.id}")
    private String sheetId4;


    @Value("${google.sheet.dechub.events_attendees.details.id}")
    private String sheetId5;

    @Value("${google.sheet.dechub.events_invitees.details.id}")
    private String sheetId6;

    @Value("${google.sheet.dechub.checklist_products.id}")
    private String sheetId7;

    @Value("${google.sheet.dechub.rivaah.details.id}")
    private String sheetId8;

    @Value("${google.sheet.dechub.rivaah.user.details.id}")
    private String sheetId9;

    @Value("${google.sheet.dechub.events.credential.sheet.id}")
    private String sheetId10;
    @Value("${google.sheet.dechub.events.details.sheet.id}")
    private String SheetId11;
    @Value("${google.sheet.dechub.events.attendees.sheet.id}")
    private String sheetId12;

    @Value("${dechub.tanishq.key.filepath}")
    private String filePath;
    @Value("${dechub.tanishq.google.service.account}")
    private String serviceAccount;

    private static final Logger log = LoggerFactory.getLogger(APIResponseBuilder.class);


    @Autowired
    private UserSession userSession;

    @Autowired
    private ExcelEventsUtil excelEventsUtil;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    public ArrayList<ExcelStoreDTO> getData(){
        try{
            ArrayList<ExcelStoreDTO> result = new ArrayList<>();
            List<List<Object>> res = this.getSheetData();
            for(List<Object> store: res){

                if(store.get(0) == null){
                    break;
                }
                ExcelStoreDTO item = new ExcelStoreDTO();

                item.setStoreCode(this.formInput(store,0));
                item.setStoreName(this.formInput(store,3));
                item.setStoreAddress(this.formInput(store,4));
                item.setStoreCity(this.formInput(store,5));
                item.setStoreState(this.formInput(store,6));
                item.setStoreCountry(this.formInput(store,7));
                item.setStoreZipCode(this.formInput(store,8));
                item.setStorePhoneNoOne(this.formInput(store,9));
                item.setStorePhoneNoTwo(this.formInput(store,10));
                item.setStoreEmailId(this.formInput(store,11));
                item.setStoreLatitude(this.formInput(store,12));
                item.setStoreLongitude(this.formInput(store,13));
                item.setStoreDateOfOpening(this.formInput(store,14));
                item.setStoreType(this.formInput(store,15));
                item.setStoreOpeningTime(this.formInput(store,16));
                item.setStoreClosingTime(this.formInput(store,17));
                item.setStoreManagerName(this.formInput(store,18));
                item.setStoreManagerNo(this.formInput(store,19));
                item.setStoreManagerEmail(this.formInput(store,20));
                item.setStoreLocationLink(this.formInput(store,21));

                item.setLanguages(this.formInputForLanguages(store,22));
                item.setParking(this.formInputForParking(store,23));
                item.setPayment(this.formInputForPayment(store,24));
                item.setKakatiyaStore(this.formInput(store,25));
                item.setCelesteStore(this.formInput(store,26));
                item.setRating(this.formInput(store,27));
                item.setNumberOfRatings(this.formInput(store,28));
                item.setIsCollection(this.formInput(store,29));

                result.add(item);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private List<List<Object>> getSheetData() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        ValueRange response = service.spreadsheets().values().get(sheetId3, sheetRange).execute();
        return response.getValues();

    }
    private Object formInputForLanguages(List<Object> store, int index){
        ArrayList<String> res = new ArrayList<>();
        try{
            String data =  (String) store.get(index);
            String[] comaSepVal = data.split(",");

            for(String i : comaSepVal){
                String[] andSepVal = i.split("and");
                for(String j : andSepVal){
                    res.add(j);
                }
            }
//            for(String i : comaSepVal){
//                res.add(i);
//            }
            if(res.size() == 0){
                res.add(data);
            }

        }catch (Exception e){
            //return res;
        }

        if(res.size() > 0){
            List<String> finalRes = new ArrayList<>();
            for(String i : res){
                if(this.containsIgnoreCase(i, "Telugu")){
                    finalRes.add("Telugu");
                }
                if(this.containsIgnoreCase(i, "Urudu")){
                    finalRes.add("Urudu");
                }
                if(this.containsIgnoreCase(i, "Malayalam")){
                    finalRes.add("Malayalam");
                }
                if(this.containsIgnoreCase(i, "Hindi")){
                    finalRes.add("Hindi");
                }
                if(this.containsIgnoreCase(i, "English")){
                    finalRes.add("English");
                }
                if(this.containsIgnoreCase(i, "Tamil")){
                    finalRes.add("Tamil");
                }
                if(this.containsIgnoreCase(i, "Marathi")){
                    finalRes.add("Marathi");
                }
                if(this.containsIgnoreCase(i, "Gujarati")){
                    finalRes.add("Gujarati");
                }
                if(this.containsIgnoreCase(i, "Rajasthani")){
                    finalRes.add("Rajasthani");
                }
                if(this.containsIgnoreCase(i, "Punjabi")){
                    finalRes.add("Punjabi");
                }
                if(this.containsIgnoreCase(i, "Bengali")){
                    finalRes.add("Bengali");
                }
                if(this.containsIgnoreCase(i, "Local Languages")){
                    finalRes.add("Local Languages");
                }
                if(this.containsIgnoreCase(i, "Odiya")){
                    finalRes.add("Odiya");
                }
                if(this.containsIgnoreCase(i, "Bhojpuri")){
                    finalRes.add("Bhojpuri");
                }
                if(this.containsIgnoreCase(i, "Marvadi")){
                    finalRes.add("Marvadi");
                }
                if(this.containsIgnoreCase(i, "Angika")){
                    finalRes.add("Angika");
                }
                if(this.containsIgnoreCase(i, "Kannada")){
                    finalRes.add("Kannada");
                }
                if(this.containsIgnoreCase(i, "Coorgi")){
                    finalRes.add("Coorgi");
                }
                if(this.containsIgnoreCase(i, "Nagamese")){
                    finalRes.add("Nagamese");
                }
                if(this.containsIgnoreCase(i, "Konkani")){
                    finalRes.add("Konkani");
                }
            }
            return finalRes.stream().distinct().collect(Collectors. toList());
        }
        else{
            return res;
        }

//        return  res.stream().distinct().collect(Collectors. toList());
//        return res;
    }

    private Object formInputForPayment(List<Object> store, int index){
        ArrayList<String> res = new ArrayList<>();
        try{
            String data =  (String) store.get(index);
            String[] comaSepVal = data.split(",");
            for(String i : comaSepVal){
                String[] andSepVal = i.split("&");
                for(String j : andSepVal){
                    res.add(j);
                }
            }
            if(res.size() == 0){
                res.add(data);
            }

        }catch (Exception e){
            //return res;
        }

        if(res.size() > 0){
            List<String> finalRes = new ArrayList<>();
            for(String i : res){
                if(this.containsIgnoreCase(i, "Cheque")){
                    finalRes.add("Cheque");
                }
                if(this.containsIgnoreCase(i, "Airpay")){
                    finalRes.add("Airpay");
                }
                if(this.containsIgnoreCase(i, "Old Gold exchange")){
                    finalRes.add("Old Gold exchange");
                }
                if(this.containsIgnoreCase(i, "Net Banking")){
                    finalRes.add("Net Banking");
                }
                if(this.containsIgnoreCase(i, "UPI")){
                    finalRes.add("UPI");
                }
                if(this.containsIgnoreCase(i, "Credit Card")){
                    finalRes.add("Credit Card");
                }
                if(this.containsIgnoreCase(i, "Debit Card")){
                    finalRes.add("Debit Card");
                }
                if(this.containsIgnoreCase(i, "Cash")){
                    finalRes.add("Cash");
                }
            }
            return finalRes.stream().distinct().collect(Collectors. toList());
        }
        else{
            return res;
        }
    }

    private Object formInputForParking(List<Object> store, int index){
        ArrayList<String> res = new ArrayList<>();
        try{
            String data =  (String) store.get(index);
            String[] comaSepVal = data.split(",");
            for(String i : comaSepVal){
                res.add(i);
            }
            if(res.size() == 0){
                res.add(data);
            }

        }catch (Exception e){
            //return res;
        }
        return  res.stream().distinct().collect(Collectors. toList());
    }
    private String formInput(List<Object> store, int index){
        try{
            return (String) store.get(index);
        }catch (Exception e){
            return "";
        }
    }

//    public List<Map<String, Object>> getCompletedEventDetails(String code) throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//// Define the range to fetch all columns
//        String range = "Sheet1!A:U"; // Assuming columns A to L contain the necessary data
//
//// Fetch data from the sheet
//        ValueRange response = service.spreadsheets().values()
//                .get(sheetId4, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        List<Map<String, Object>> events = new ArrayList<>();
//
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//            return Collections.singletonList(Map.of("status", false));
//        }
//
//// Get the header row
//        List<String> headers = values.get(0).stream()
//                .map(Object::toString)
//                .map(this::sanitizeHeader)
//                .collect(Collectors.toList());
//
//// Iterate over rows and extract required columns
//        for (int i = 1; i < values.size(); i++) { // Start from 1 to skip headers
//            List<Object> row = values.get(i);
//            if (row.size() > 0 && code.equalsIgnoreCase(row.get(0).toString())) {
//                Map<String, Object> event = new HashMap<>();
//
//                for (int j = 0; j < headers.size(); j++) {
//                    if (j < row.size()) {
//                        event.put(headers.get(j).toString(), row.get(j));
//                    } else {
//                        event.put(headers.get(j).toString(), ""); // Fill missing columns with empty string
//                    }
//                }
//
//                event.put("completedEvent", event.get("completedEvents").toString().trim().length() > 1);
//
//                events.add(event);
//            }
//        }
//        System.out.println(events);
//        return events;
//    }


public List<Map<String, Object>> getCompletedEventDetails(String storeCode) throws Exception {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service = getSheetService(httpTransport);

    // Define the range assuming data is in columns A to T (20 columns)
    String range = "Sheet1!A:T";

    // Fetch data
    ValueRange response = service.spreadsheets().values()
            .get(SheetId11, range)
            .execute();

    List<List<Object>> values = response.getValues();
    List<Map<String, Object>> events = new ArrayList<>();

    if (values == null || values.isEmpty()) {
        System.out.println("No data found.");
        return Collections.singletonList(Map.of("status", false));
    }

    // Extract and sanitize headers
    List<String> headers = values.get(0).stream()
            .map(Object::toString)
            .map(this::sanitizeHeader) // if you want to clean headers like replacing spaces, etc.
            .collect(Collectors.toList());

    for (int i = 1; i < values.size(); i++) {
        List<Object> row = values.get(i);
        if (row.size() > 1 && storeCode.equalsIgnoreCase(row.get(1).toString())) { // Store Code is at index 1
            Map<String, Object> event = new HashMap<>();

            for (int j = 0; j < headers.size(); j++) {
                event.put(headers.get(j), j < row.size() ? row.get(j) : "");
            }

            // Optional: derived field
            Object completedEvents = event.getOrDefault("completedEvents", "");
            event.put("completedEvent", completedEvents.toString().trim().length() > 1);

            events.add(event);
        }
    }

    System.out.println(events);
    return events;
}


    public List<storeCodeDataDTO> getStoresByRegion(String regionSheetName) throws Exception {
        List<storeCodeDataDTO> stores = new ArrayList<>();
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);

        String range = regionSheetName + "!A2:A"; // Only first column (Store Code), skip header
        ValueRange response = service.spreadsheets().values()
                .get(sheetId3, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values != null && !values.isEmpty()) {
            for (List<Object> row : values) {
                if (!row.isEmpty() && row.get(0) != null && !row.get(0).toString().trim().isEmpty()) {
                    storeCodeDataDTO dto = new storeCodeDataDTO();
                    dto.setStoreCode(row.get(0).toString().trim());
                    stores.add(dto);
                }
            }
        }
        return stores;
    }
    public Map<String, Object> getDataFromSheet(String storeCode) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        String range = "stores"; // Adjust sheet name if needed

        ValueRange response = service.spreadsheets().values()
                .get(sheetId3, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }

        // Assume the first row contains headers
        List<String> headers = values.get(0).stream()
                .map(Object::toString)
                .map(this::sanitizeHeader)
                .collect(Collectors.toList());

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && row.get(0).equals(storeCode)) {
                return createDataMap(headers, row);
            }
        }

        return Collections.emptyMap();
    }
    public RivaahImagesDTO getImagesByTags(List<String> tags) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Set<String> categories = new HashSet<>();
        List<ProductDetailDTO> productDetails = new ArrayList<>();
        Sheets service = getSheetService(httpTransport);
        String range = "Sheet1!A:E"; // Adjust sheet name if needed

        // Fetch data from Google Sheets
        ValueRange response = service.spreadsheets().values()
                .get(sheetId7, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values != null && !values.isEmpty()) {
            // Step through each row of the sheet data
            for (List<Object> row : values) {
                String id = row.get(0).toString();
                String category = row.get(1).toString();   // Category is in the first column
                String imageLink = row.get(2).toString();  // Image link in the second column
                String productLink = row.get(3).toString();// Product link in the third column

                // Check if the category contains any of the tags in the list
                for (String tag : tags) {
                    if (category.equalsIgnoreCase(tag)) {
                        categories.add(category);  // Add category to Set to avoid duplicates
                        productDetails.add(new ProductDetailDTO(id,imageLink, productLink));
                        break;  // Once a tag matches, no need to check other tags for this row
                    }
                }
            }
        }

        Collections.shuffle(productDetails);

        // Convert the set of categories to a list and create the response object
        return new RivaahImagesDTO(new ArrayList<>(categories), productDetails);
    }

    public ResponseEntity<String> incrementLikeCount( Long imageId,boolean increase) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            // Fetch data from the sheet
            String range = "Sheet1" + "!A:E";
            ValueRange response = service.spreadsheets().values().get(sheetId7, range).execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found in the sheet.");
            }

            // Locate the row with the matching imageId
            for (int i = 1; i < values.size(); i++) { // Start from 1 to skip the header row
                List<Object> row = values.get(i);

                if (row.size() > 0 && row.get(0).toString().equals(String.valueOf(imageId))) {
                    // Increment the like count in column E
                    int currentLikes = row.size() > 4 && !row.get(4).toString().isEmpty()
                            ? Integer.parseInt(row.get(4).toString())
                            : 0;
                    if(increase) currentLikes++;
                    if(!increase) currentLikes--;

                    // Update the sheet
                    String updateRange = "Sheet1" + "!E" + (i + 1);
                    ValueRange body = new ValueRange().setValues(List.of(List.of(currentLikes)));
                    service.spreadsheets().values()
                            .update(sheetId7, updateRange, body)
                            .setValueInputOption("RAW")
                            .execute();
                    if(increase) return ResponseEntity.ok("Like count incremented successfully.");
                    return ResponseEntity.ok("Like count decremented successfully.");
                }
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with imageId not found.");

        }
        catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating the like count.");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
    }

    private String sanitizeHeader(String header) {
        return header.replaceAll("\\s+", "");
    }

    private Map<String, Object> createDataMap(List<String> headers, List<Object> row) {
        Map<String, Object> dataMap = new HashMap<>();
        for (int i = 0; i < Math.min(headers.size(), row.size()); i++) {
            dataMap.put(headers.get(i), row.get(i));
        }
        return dataMap;
    }
    public boolean insertSheetData(UserDetailsDTO userDetailsDTO) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange body = new ValueRange()
                    .setValues(this.formInputData(userDetailsDTO));

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId, "A2", body)
                    .setValueInputOption("RAW").execute();
            int resSize = appendValuesResponse.size();
            if (resSize > 0) {
                System.out.println("user details excel updated successfully");
                return true;
            } else {
                System.out.println("user details excel updated failed");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
    public boolean insertSheetBrideData(BrideDetailsDTO brideDetailsDTO) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange body = new ValueRange()
                    .setValues(this.formInputBrideData(brideDetailsDTO));

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId2, "A2", body)
                    .setValueInputOption("RAW").execute();
            int resSize = appendValuesResponse.size();
            if (resSize > 0) {
                System.out.println("bride details excel updated successfully");
                return true;
            } else {
                System.out.println("bride details excel updated failed");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


//public QrResponseDTO insertSheetEventsData(EventsDetailDTO eventsDetailDTO) {
//    QrResponseDTO qrResponseDTO = new QrResponseDTO();
//    try {
//
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = getSheetService(httpTransport);
//
//        eventsDetailDTO.setId(eventsDetailDTO.getStoreCode() + "_" + UUID.randomUUID().toString());
//        if (!eventsDetailDTO.getSingleCustomer()) {
//            int inviteesCount = excelEventsUtil.uploadExcelFile(eventsDetailDTO.getFile());
//            eventsDetailDTO.setInvitees(inviteesCount);
//            boolean inviteesUpdated = uploadXlsxToGoogleSheet(eventsDetailDTO.getFile(),eventsDetailDTO.getId(),sheetId6);
//            if(!inviteesUpdated){
//                qrResponseDTO.setStatus(false);
//                qrResponseDTO.setQrData("Invitees sheet update failed");
//                return qrResponseDTO;
//            }
//        } else {
//            eventsDetailDTO.setInvitees(1);
//            InviteesDetailDTO inviteesDetailDTO = new InviteesDetailDTO();
//            inviteesDetailDTO.setName(eventsDetailDTO.getName());
//            inviteesDetailDTO.setEventId(eventsDetailDTO.getId());
//            inviteesDetailDTO.setContact(eventsDetailDTO.getContact());
//            boolean inviteesUpdated = insertSheetInviteesData(inviteesDetailDTO);
//            if(!inviteesUpdated){
//                qrResponseDTO.setStatus(false);
//                qrResponseDTO.setQrData("Invitees sheet update failed");
//                return qrResponseDTO;
//            }
//        }
//
//        // Initialize row data with empty values for each column
//        int totalColumns = 16; // Adjust this based on the total number of columns
//        List<Object> rowData = new ArrayList<>(Collections.nCopies(totalColumns, ""));
//
//        // Populate row data based on column indices
//        rowData.set(0, eventsDetailDTO.getStoreCode()); // Column 1: Store Code
//        rowData.set(1, eventsDetailDTO.getId()); // Column 2: Id
//        rowData.set(2, eventsDetailDTO.getEventType() != null ? eventsDetailDTO.getEventType() : ""); // Column 5: Event Type
//        rowData.set(3, eventsDetailDTO.getEventSubType() != null ? eventsDetailDTO.getEventSubType() : ""); // Column 6: Event Sub Type
//        rowData.set(4, eventsDetailDTO.getEventName() != null ? eventsDetailDTO.getEventName() : ""); // Column 7: Event Name
//        rowData.set(5, eventsDetailDTO.getRso() != null ? eventsDetailDTO.getRso() : ""); // Column 7: Event Name
//
//        rowData.set(6, eventsDetailDTO.getStartDate() != null ? eventsDetailDTO.getStartDate() : ""); // Column 8: Start Date
//        rowData.set(7, eventsDetailDTO.getStartTime() != null ? eventsDetailDTO.getStartTime() : ""); // Column 9: Start Time
//        rowData.set(8, eventsDetailDTO.getDescription()!=null? eventsDetailDTO.getDescription():""); // Column 3: Description
////        rowData.set(4, eventsDetailDTO.getLocation() != null ? eventsDetailDTO.getLocation() : ""); // Column 4: Event Location
//        rowData.set(9, eventsDetailDTO.getImage() != null ? eventsDetailDTO.getImage() : ""); // Column 10: Image
//        rowData.set(10, eventsDetailDTO.getInvitees()); // Column 0: Invitees
//        rowData.set(11, eventsDetailDTO.getAttendees());
//        rowData.set(12, dtf.format(now));
    ////        rowData.set(13, eventsDetailDTO.getImage() != null ? eventsDetailDTO.getImage() : "");
//        rowData.set(14, eventsDetailDTO.getCommunity() != null ? eventsDetailDTO.getCommunity() : "");
//        rowData.set(15, eventsDetailDTO.getLocation() != null ? eventsDetailDTO.getLocation() : "");
//
//
//
//        List<List<Object>> values = Collections.singletonList(rowData);
//
//        ValueRange body = new ValueRange()
//                .setValues(values);
//
//        AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
//                .append(sheetId4, "Sheet1" + "!A2", body)
//                .setValueInputOption("USER_ENTERED")
//                .execute();
//
//        int updatedRows = appendValuesResponse.getUpdates().getUpdatedRows();
//        if (updatedRows > 0) {
//
//            String imageResponse = generateQrCode(eventsDetailDTO.getId());
//            if (imageResponse.equals("error")) {
//                qrResponseDTO.setStatus(false);
//                qrResponseDTO.setQrData("error in generating qr code");
//                return qrResponseDTO;
//            }
//            qrResponseDTO.setStatus(true);
//            qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
//            return qrResponseDTO;
//        } else {
//            qrResponseDTO.setStatus(false);
//            qrResponseDTO.setQrData("Events sheet update failed");
//            return qrResponseDTO;
//
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//        qrResponseDTO.setStatus(false);
//        qrResponseDTO.setQrData("error:"+e.getMessage());
//        return qrResponseDTO;
//    }
//}

    public QrResponseDTO insertSheetEventsData(EventsDetailDTO eventsDetailDTO) {
        QrResponseDTO qrResponseDTO = new QrResponseDTO();
        try {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            eventsDetailDTO.setId(eventsDetailDTO.getStoreCode() + "_" + UUID.randomUUID().toString());
            if (!eventsDetailDTO.getSingleCustomer()) {
                int inviteesCount = excelEventsUtil.uploadExcelFile(eventsDetailDTO.getFile());
                eventsDetailDTO.setInvitees(inviteesCount);
                boolean inviteesUpdated = uploadXlsxToGoogleSheet(eventsDetailDTO.getFile(),eventsDetailDTO.getId(),sheetId6);
                if(!inviteesUpdated){
                    qrResponseDTO.setStatus(false);
                    qrResponseDTO.setQrData("Invitees sheet update failed");
                    return qrResponseDTO;
                }
            } else {
                eventsDetailDTO.setInvitees(1);
                InviteesDetailDTO inviteesDetailDTO = new InviteesDetailDTO();
                inviteesDetailDTO.setName(eventsDetailDTO.getName());
                inviteesDetailDTO.setEventId(eventsDetailDTO.getId());
                inviteesDetailDTO.setContact(eventsDetailDTO.getContact());
                boolean inviteesUpdated = insertSheetInviteesData(inviteesDetailDTO);
                if(!inviteesUpdated){
                    qrResponseDTO.setStatus(false);
                    qrResponseDTO.setQrData("Invitees sheet update failed");
                    return qrResponseDTO;
                }
            }

            // Initialize row data with empty values for each column
            int totalColumns = 20; // Adjust this based on the total number of columns
            List<Object> rowData = new ArrayList<>(Collections.nCopies(totalColumns, ""));

            // Column mapping according to the sheet
            rowData.set(0, dtf.format(now)); // createdAt
            rowData.set(1, eventsDetailDTO.getStoreCode() != null ? eventsDetailDTO.getStoreCode() : "");
            rowData.set(2, eventsDetailDTO.getRegion() != null ? eventsDetailDTO.getRegion() : "");
            rowData.set(3, eventsDetailDTO.getId() != null ? eventsDetailDTO.getId() : "");
            rowData.set(4, eventsDetailDTO.getEventType() != null ? eventsDetailDTO.getEventType() : "");
            rowData.set(5, eventsDetailDTO.getEventSubType() != null ? eventsDetailDTO.getEventSubType() : "");
            rowData.set(6, eventsDetailDTO.getEventName() != null ? eventsDetailDTO.getEventName() : "");
            rowData.set(7, eventsDetailDTO.getRso() != null ? eventsDetailDTO.getRso() : "");
            rowData.set(8, eventsDetailDTO.getStartDate() != null ? eventsDetailDTO.getStartDate() : "");
            rowData.set(9, eventsDetailDTO.getImage() != null ? eventsDetailDTO.getImage() : "");
            rowData.set(10, eventsDetailDTO.getInvitees());
            rowData.set(11, eventsDetailDTO.getAttendees());
            rowData.set(12, eventsDetailDTO.getCompletedEventsDriveLink() != null ? eventsDetailDTO.getCompletedEventsDriveLink() : "");
            rowData.set(13, eventsDetailDTO.getCommunity() != null ? eventsDetailDTO.getCommunity() : "");
            rowData.set(14, eventsDetailDTO.getLocation() != null ? eventsDetailDTO.getLocation() : "");
            rowData.set(15, eventsDetailDTO.isAttendeesUploaded());
            rowData.set(16, eventsDetailDTO.getSale() != null ? eventsDetailDTO.getSale() : 0);
            rowData.set(17, eventsDetailDTO.getAdvance() != null ? eventsDetailDTO.getAdvance() : 0);
            rowData.set(18, eventsDetailDTO.getGhsOrRga() != null ? eventsDetailDTO.getGhsOrRga() : 0);
            rowData.set(19, eventsDetailDTO.getGmb() != null ? eventsDetailDTO.getGmb() : 0);



            List<List<Object>> values = Collections.singletonList(rowData);

            ValueRange body = new ValueRange()
                    .setValues(values);

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
                    .append(SheetId11, "Sheet1" + "!A2", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            int updatedRows = appendValuesResponse.getUpdates().getUpdatedRows();
            if (updatedRows > 0) {
                System.out.println("id"+eventsDetailDTO.getId());
                System.out.println("type "+eventsDetailDTO.getEventType());
                String imageResponse = generateQrCode(eventsDetailDTO.getId());
                if (imageResponse.equals("error")) {
                    qrResponseDTO.setStatus(false);
                    qrResponseDTO.setQrData("error in generating qr code");
                    return qrResponseDTO;
                }
                qrResponseDTO.setStatus(true);
                qrResponseDTO.setQrData("data:image/png;base64,"+imageResponse);
                return qrResponseDTO;
            } else {
                qrResponseDTO.setStatus(false);
                qrResponseDTO.setQrData("Events sheet update failed");
                return qrResponseDTO;

            }
        } catch (Exception e) {
            e.printStackTrace();
            qrResponseDTO.setStatus(false);
            qrResponseDTO.setQrData("error:"+e.getMessage());
            return qrResponseDTO;
        }
    }
    public String generateQrCode(String eventId)  {
        try {
            // Generate QR code
            String qrCodeText = "https://celebrations.tanishq.co.in/events/customer/" + eventId;

            BufferedImage qrCodeImage = generateQRCodeImage(qrCodeText);

            // Convert QR code to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrCodeImage, "png", baos);
            byte[] qrCodeBytes = baos.toByteArray();

            // Encode the QR code data to Base64
            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return base64QrCode;
        } catch (Exception e) {
            return "error";
        }

    }
//    public String generateQrCodeWithEventLocation(String eventId, String eventLocation)  {
//        try {
//            // Generate QR code
//            String qrCodeText;
//            if (eventLocation.equalsIgnoreCase("customer's house")){
//                qrCodeText = "https://celebrations.tanishq.co.in/events/customer/" + eventId +"?ishome=true";
//            }
//            else {
//                qrCodeText = "https://celebrations.tanishq.co.in/events/customer/" + eventId;
//            }
//            System.out.println("qr text "+qrCodeText);
//            BufferedImage qrCodeImage = generateQRCodeImage(qrCodeText);
//
//            // Convert QR code to byte array
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(qrCodeImage, "png", baos);
//            byte[] qrCodeBytes = baos.toByteArray();
//
//            // Encode the QR code data to Base64
//            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);
//
//            // Set headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            return base64QrCode;
//        } catch (Exception e) {
//            return "error";
//        }
//
//    }
    private BufferedImage generateQRCodeImage(String text) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }



    private Credential getCred(NetHttpTransport transport) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("Keys file Not found");
        }

        Credential credential = new GoogleCredential.Builder()
                .setTransport(transport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccount)
                .setServiceAccountPrivateKeyFromP12File(file)
                .setServiceAccountScopes(SCOPES).build();
        return credential;
    }

    private Sheets getSheetService(NetHttpTransport transport) throws Exception {
        return new Sheets.Builder(transport, JSON_FACTORY, this.getCred(transport)).build();
    }

    private List<List<Object>> formInputData(UserDetailsDTO input) {
        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(input.getName());
        item.add(input.getReason());
        item.add(input.getRsoName());
        item.add(input.getStoreCode());
        item.add(input.getDate());
        item.add(input.getMyFirstDiamond());
        res.add(item);
        return res;
    }
    private List<List<Object>> formInputBrideData(BrideDetailsDTO input) {
        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(input.getBrideName());
        item.add(input.getBrideEvent());
        item.add(input.getEmail());
        item.add(input.getPhone());
        item.add(input.getDate());
        item.add(input.getBrideType());
        item.add(input.getZipCode());
        res.add(item);
        return res;
    }


    public int insertSheetAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
        try {
            if(attendeesDetailDTO.getFile()!=null&&!attendeesDetailDTO.getFile().isEmpty()){
                int attendeesCount = excelEventsUtil.uploadExcelFile(attendeesDetailDTO.getFile());
                log.info("attendeesCount "+attendeesCount);
                boolean attendeesUpdated = uploadXlsxToGoogleSheet(attendeesDetailDTO.getFile(),attendeesDetailDTO.getId(),sheetId5);
                return attendeesCount;
            }else{
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                Sheets service = getSheetService(httpTransport);

                ValueRange body = new ValueRange()
                        .setValues(this.formInputAttendeesData(attendeesDetailDTO));

//                AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId5, "A2", body)
//                        .setValueInputOption("RAW").execute();

                AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
                        .append(sheetId5, "A2:H2", body) // <-- updated range
                        .setValueInputOption("RAW")
                        .execute();

                int resSize = appendValuesResponse.size();
                if (resSize > 0) {
                    System.out.println("attendees details excel updated successfully");
                    return 1;
                } else {
                    System.out.println("attendees details excel updated failed");
                    return 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public boolean insertSheetInviteesData(InviteesDetailDTO inviteesDetailDTO) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange body = new ValueRange()
                    .setValues(this.formInputInviteesData(inviteesDetailDTO));

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId6, "A2", body)
                    .setValueInputOption("RAW").execute();
            int resSize = appendValuesResponse.size();
            if (resSize > 0) {
                System.out.println("invitees details excel updated successfully");
                return true;
            } else {
                System.out.println("invitees details excel updated failed");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private List<List<Object>> formInputInviteesData(InviteesDetailDTO inviteesDetailDTO) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(inviteesDetailDTO.getName());
        item.add(inviteesDetailDTO.getContact());
        item.add(dtf.format(now));
        item.add(inviteesDetailDTO.getEventId());
        res.add(item);
        return res;
    }
//    private List<List<Object>> formInputAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//
//        List<List<Object>> res = new ArrayList<>();
//        List<Object> item = new ArrayList<>();
//        item.add(attendeesDetailDTO.getId());
//        item.add(attendeesDetailDTO.getName());
//        item.add(attendeesDetailDTO.getPhone());
//        item.add(attendeesDetailDTO.getLike());
//        item.add(attendeesDetailDTO.isFirstTimeAtTanishq());
//        item.add(dtf.format(now));
//        item.add(false);
//        item.add(attendeesDetailDTO.getRsoName());
//        res.add(item);
//        return res;
//    }

    private List<List<Object>> formInputAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();

        // Ensure non-null values in exact column order
        item.add(attendeesDetailDTO.getId() != null ? attendeesDetailDTO.getId() : "");                      // 1. Event Id
        item.add(attendeesDetailDTO.getName() != null ? attendeesDetailDTO.getName() : "");                  // 2. Name
        item.add(attendeesDetailDTO.getPhone() != null ? attendeesDetailDTO.getPhone() : "");                // 3. Contact
        item.add(attendeesDetailDTO.getLike() != null ? attendeesDetailDTO.getLike() : "");                  // 4. Like
        item.add(attendeesDetailDTO.isFirstTimeAtTanishq());                                                 // 5. First Time (boolean defaults to false)
        item.add(dtf.format(now));                                                                            // 6. Created At
        item.add(false);                                                                                      // 7. isUploadedFromExcel
        item.add(attendeesDetailDTO.getRsoName() != null ? attendeesDetailDTO.getRsoName() : "");            // 8. Rso Name

        System.out.println("Final DATA ROW: " + item);  // Debug log

        res.add(item);
        return res;
    }


//    private List<List<Object>> formInputAttendeesData(AttendeesDetailDTO dto) {
//        List<List<Object>> values = new ArrayList<>();
//        List<Object> row = Arrays.asList(
//                nullToEmpty(dto.getRegion()),              // region
//                nullToEmpty(dto.getStoreCode()),           // store code
//                nullToEmpty(dto.getEventType()),           // event type
//                nullToEmpty(dto.getEventId()),             // Event Id
//                nullToEmpty(dto.getName()),                // Name
//                nullToEmpty(dto.getPhone()),               // Contact
//                nullToEmpty(dto.getLike()),                // Like
//                dto.isFirstTimeAtTanishq(),                // first Time (true/false)
//                nullToEmpty(dto.getCreatedAt()),           // Created At
//                false,                                      // isUploadedFromExcel = false for manual input
//                nullToEmpty(dto.getRsoName())              // rso name
//        );
//        values.add(row);
//        return values;
//    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }


//    public boolean updateAttendees(String eventId,int count) {
//       try{
//           System.out.println("Fetching row from events");
//           // Get the values in the Id column
//           String range = "Sheet1" + "!" + "B" + ":" + "B";
//           final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//           Sheets service = getSheetService(httpTransport);
//           ValueRange response = service.spreadsheets().values()
//                   .get(sheetId4, range)
//                   .execute();
//
//           List<List<Object>> values = response.getValues();
//           System.out.println("values: "+values.size());
//           if (values == null || values.isEmpty()) {
//               System.out.println("No data found.");
//               return false;
//           }
//
//           // Search for the eventId in the column
//           int rowIndex = -1;
//           for (int i = 0; i < values.size(); i++) {
//               if (values.get(i)!=null&&!values.get(i).isEmpty() && values.get(i).get(0).equals(eventId)) {
//                   System.out.println("Row Found in events");
//                   rowIndex = i + 1; // +1 because rows are 1-indexed in Google Sheets
//                   break;
//               }
//           }
//
//           if (rowIndex == -1) {
//               System.out.println(eventId);
//               System.out.println("Event ID not found.");
//               return false;
//           }
//
//           /// Get the current value of the Attendees column
//           String attendeesCell = "Sheet1" + "!" + "L" + rowIndex;
//           ValueRange attendeesResponse = service.spreadsheets().values()
//                   .get(sheetId4, attendeesCell)
//                   .execute();
//           List<List<Object>> attendeesValues = attendeesResponse.getValues();
//           int currentAttendees = 0;
//           if (attendeesValues != null && !attendeesValues.isEmpty() && !attendeesValues.get(0).isEmpty()) {
//               currentAttendees = Integer.parseInt(attendeesValues.get(0).get(0).toString());
//           }
//
//// Increment the number of attendees by 1
//           currentAttendees += count;
//
//// Update the Attendees column with the new value
//           ValueRange attendeesBody = new ValueRange()
//                   .setValues(Arrays.asList(Arrays.asList(currentAttendees)));
//           BatchUpdateValuesRequest attendeesUpdateRequest = new BatchUpdateValuesRequest()
//                   .setValueInputOption("RAW")
//                   .setData(Arrays.asList(
//                           new ValueRange().setRange(attendeesCell).setValues(Arrays.asList(Arrays.asList(currentAttendees)))
//                   ));
//           service.spreadsheets().values()
//                   .batchUpdate(sheetId4, attendeesUpdateRequest)
//                   .execute();
//
//// Set isDone to true if value > 1, otherwise false
//           String isDoneCell = "Sheet1" + "!" + "Q" + rowIndex;
//
//
//           boolean isDone = count > 1;
//
    //// Update Column P with true/false based on isDoneValue
//           ValueRange isDoneBody = new ValueRange()
//                   .setValues(Arrays.asList(Arrays.asList(isDone)));
//           BatchUpdateValuesRequest isDoneUpdateRequest = new BatchUpdateValuesRequest()
//                   .setValueInputOption("RAW")
//                   .setData(Arrays.asList(
//                           new ValueRange().setRange(isDoneCell).setValues(Arrays.asList(Arrays.asList(isDone)))
//                   ));
//           service.spreadsheets().values()
//                   .batchUpdate(sheetId4, isDoneUpdateRequest)
//                   .execute();
//
//           System.out.println("Row updated successfully.");
//           return true;
//
//       }catch (Exception e){
//           System.out.println(e.getMessage());
//           System.out.println("Row updation failed");
//           return false;
//       }
//    }

//    public boolean updateAttendees(String eventId, int count) {
//        try {
//            System.out.println("Fetching row from events");
//
//            String range = "Sheet1!B:B"; // Column B
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//
//            ValueRange response = service.spreadsheets().values()
//                    .get(sheetId4, range)
//                    .execute();
//
//            List<List<Object>> values = response.getValues();
//            if (values == null || values.isEmpty()) {
//                System.out.println("No data found in Sheet1!B:B");
//                return true; // Don't fail the request
//            }
//
//            int rowIndex = -1;
//            for (int i = 0; i < values.size(); i++) {
//                if (values.get(i) != null && !values.get(i).isEmpty()) {
//                    String cellValue = values.get(i).get(0).toString().trim();
//                    if (cellValue.equalsIgnoreCase(eventId.trim())) {
//                        rowIndex = i + 1; // 1-indexed
//                        break;
//                    }
//                }
//            }
//
//            if (rowIndex == -1) {
//                System.out.println("Event ID not found in sheet: " + eventId);
//                return true; // Don't throw 500; let frontend proceed
//            }
//
//            // Fetch existing count
//            String attendeesCell = "Sheet1!L" + rowIndex;
//            int currentAttendees = 0;
//            try {
//                ValueRange attendeesResponse = service.spreadsheets().values()
//                        .get(sheetId4, attendeesCell)
//                        .execute();
//                List<List<Object>> attendeesValues = attendeesResponse.getValues();
//                if (attendeesValues != null && !attendeesValues.isEmpty() && !attendeesValues.get(0).isEmpty()) {
//                    currentAttendees = Integer.parseInt(attendeesValues.get(0).get(0).toString().trim());
//                }
//            } catch (Exception e) {
//                System.out.println("Failed to parse existing attendee count. Defaulting to 0.");
//            }
//
//            currentAttendees += count;
//
//            // Update attendees
//            ValueRange attendeesBody = new ValueRange()
//                    .setValues(Arrays.asList(Arrays.asList(currentAttendees)));
//            BatchUpdateValuesRequest attendeesUpdateRequest = new BatchUpdateValuesRequest()
//                    .setValueInputOption("RAW")
//                    .setData(Arrays.asList(
//                            new ValueRange().setRange(attendeesCell).setValues(attendeesBody.getValues())
//                    ));
//            service.spreadsheets().values()
//                    .batchUpdate(sheetId4, attendeesUpdateRequest)
//                    .execute();
//
//            // Update isDone in column Q
//            String isDoneCell = "Sheet1!Q" + rowIndex;
//            boolean isDone = count > 1;
//
//            ValueRange isDoneBody = new ValueRange()
//                    .setValues(Arrays.asList(Arrays.asList(isDone)));
//            BatchUpdateValuesRequest isDoneUpdateRequest = new BatchUpdateValuesRequest()
//                    .setValueInputOption("RAW")
//                    .setData(Arrays.asList(
//                            new ValueRange().setRange(isDoneCell).setValues(isDoneBody.getValues())
//                    ));
//            service.spreadsheets().values()
//                    .batchUpdate(sheetId4, isDoneUpdateRequest)
//                    .execute();
//
//            System.out.println("Row updated successfully.");
//            return true;
//
//        } catch (Exception e) {
//            e.printStackTrace(); // full stack trace for backend debugging
//            System.out.println("Row update failed gracefully. Event ID: " + eventId);
//            return true; // ✅ don't throw 500 — let frontend proceed
//        }
//    }


    public boolean updateAttendees(String eventId, int count) {
        try {
            System.out.println("Fetching row from events");

            String range = "Sheet1!D:D"; // Column D for eventId
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange response = service.spreadsheets().values()
                    .get(SheetId11, range) // Updated to new sheet ID
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found in Sheet1!D:D");
                return true;
            }

            int rowIndex = -1;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) != null && !values.get(i).isEmpty()) {
                    String cellValue = values.get(i).get(0).toString().trim();
                    if (cellValue.equalsIgnoreCase(eventId.trim())) {
                        rowIndex = i + 1; // Google Sheets rows are 1-indexed
                        break;
                    }
                }
            }

            if (rowIndex == -1) {
                System.out.println("Event ID not found in sheet: " + eventId);
                return true;
            }

            // Attendees column (L)
            String attendeesCell = "Sheet1!L" + rowIndex;
            int currentAttendees = 0;
            try {
                ValueRange attendeesResponse = service.spreadsheets().values()
                        .get(SheetId11, attendeesCell)
                        .execute();
                List<List<Object>> attendeesValues = attendeesResponse.getValues();
                if (attendeesValues != null && !attendeesValues.isEmpty() && !attendeesValues.get(0).isEmpty()) {
                    currentAttendees = Integer.parseInt(attendeesValues.get(0).get(0).toString().trim());
                }
            } catch (Exception e) {
                System.out.println("Failed to parse existing attendee count. Defaulting to 0.");
            }

            currentAttendees += count;

            // Update attendees value
            ValueRange attendeesBody = new ValueRange()
                    .setValues(Arrays.asList(Arrays.asList(currentAttendees)));
            BatchUpdateValuesRequest attendeesUpdateRequest = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW")
                    .setData(Arrays.asList(
                            new ValueRange().setRange(attendeesCell).setValues(attendeesBody.getValues())
                    ));
            service.spreadsheets().values()
                    .batchUpdate(SheetId11, attendeesUpdateRequest)
                    .execute();

            System.out.println("Row updated successfully.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Row update failed gracefully. Event ID: " + eventId);
            return true;
        }
    }


//    public boolean updateDrivelink(String eventId, String link) {
//        try {
//            // Get the values in the Id column
//            String range = "Sheet1" + "!" + "B" + ":" + "B";
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Sheets service = getSheetService(httpTransport);
//            ValueRange response = service.spreadsheets().values()
//                    .get(sheetId4, range)
//                    .execute();
//
//            List<List<Object>> values = response.getValues();
//            if (values == null || values.isEmpty()) {
//                System.out.println("No data found.");
//                return false;
//            }
//
//            // Search for the eventId in the column
//            int rowIndex = -1;
//            for (int i = 0; i < values.size(); i++) {
//                if (values.get(i).size() >0 && values.get(i).get(0).equals(eventId)) {
//                    rowIndex = i + 1; // +1 because rows are 1-indexed in Google Sheets
//                    break;
//                }
//            }
//
//            if (rowIndex == -1) {
//                System.out.println("Event ID not found.");
//                return false;
//            }
//            System.out.println("Row Index in sheet: "+rowIndex);
//            // Update the Link column with the new value
//            String linkCell = "Sheet1" + "!" + "N" + rowIndex;
//            ValueRange body = new ValueRange()
//                    .setValues(Arrays.asList(Arrays.asList(link)));
//            BatchUpdateValuesRequest batchUpdateValuesRequest = new BatchUpdateValuesRequest()
//                    .setValueInputOption("RAW")
//                    .setData(Arrays.asList(
//                            new ValueRange().setRange(linkCell).setValues(Arrays.asList(Arrays.asList(link)))
//                    ));
//
//            service.spreadsheets().values()
//                    .batchUpdate(sheetId4, batchUpdateValuesRequest)
//                    .execute();
//
//            System.out.println("Row updated successfully.");
//            return true;
//        } catch (Exception e) {
//            System.out.println("Row update failed: " + e.getMessage());
//            return false;
//        }
//    }

    public boolean updateDrivelink(String eventId, String link) {
        try {
            // Get the values in the Id column (now column D)
            String range = "Sheet1!D:D"; // Column D for event ID
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange response = service.spreadsheets().values()
                    .get(SheetId11, range) // Use the new sheet ID
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found in Id column.");
                return false;
            }

            // Search for the eventId in the Id column
            int rowIndex = -1;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).size() > 0 && values.get(i).get(0).toString().trim().equalsIgnoreCase(eventId.trim())) {
                    rowIndex = i + 1; // 1-based index for Sheets
                    break;
                }
            }

            if (rowIndex == -1) {
                System.out.println("Event ID not found.");
                return false;
            }

            System.out.println("Row Index in sheet: " + rowIndex);

            // Update the Drive link in column U
            String driveLinkCell = "Sheet1!M" + rowIndex;
            ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(Arrays.asList(link)));

            BatchUpdateValuesRequest batchUpdateValuesRequest = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW")
                    .setData(Arrays.asList(
                            new ValueRange().setRange(driveLinkCell).setValues(body.getValues())
                    ));

            service.spreadsheets().values()
                    .batchUpdate(SheetId11, batchUpdateValuesRequest)
                    .execute();

            System.out.println("Drive link updated successfully.");
            return true;

        } catch (Exception e) {
            System.out.println("Drive link update failed: " + e.getMessage());
            return false;
        }
    }



    public boolean uploadXlsxToGoogleSheet(MultipartFile file, String eventId,String sheetId) throws Exception {
        // Read XLSX file
        List<List<Object>> data = readXlsxFile(file);
        if(sheetId.equals(sheetId5)){
            // Add current date and time, and eventId to each row
            data = addDateTimeAndEventIdToData(data, eventId,true);
        }else{
            // Add current date and time, and eventId to each row
            data = addDateTimeAndEventIdToData(data, eventId,false);
        }

        // Upload to Google Sheets
        return uploadToGoogleSheets(data, sheetId);
    }

    private List<List<Object>> readXlsxFile(MultipartFile file) throws IOException {
        List<List<Object>> data = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Start from the second row (index 1) to skip the header
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            List<Object> rowData = new ArrayList<>();
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        rowData.add(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        rowData.add(cell.getNumericCellValue());
                        break;
                    case BOOLEAN: // Handle boolean cells
                        rowData.add(cell.getBooleanCellValue());
                        break;
                    // Add other cases as needed
                    default:
                        rowData.add("");
                }
            }
            data.add(rowData);
        }

        workbook.close();
        return data;
    }

    private List<List<Object>> addDateTimeAndEventIdToData(List<List<Object>> data, String eventId,boolean attendees) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = now.format(formatter);

        List<List<Object>> newData = new ArrayList<>();
        if(attendees){
            // Add data rows with the required columns and default values
            for (List<Object> row : data) {
                List<Object> newRow = new ArrayList<>();
                newRow.add(eventId); // Event ID
                newRow.add(row.size() > 0 ? row.get(0) : ""); // Name
                newRow.add(row.size() > 1 ? row.get(1) : ""); // Contact
                newRow.add(row.size() > 2 ? row.get(2) : ""); // Like
                newRow.add(row.size() > 3 ? row.get(3) : ""); // First time
                newRow.add(dateTime); // Created At
                newRow.add(true); // isUploadedFromExcel
                newData.add(newRow);
            }
        }else{
            // Add data rows with timestamp and eventId
            for (List<Object> row : data) {
                List<Object> newRow = new ArrayList<>(row);
                newRow.add(dateTime);
                newRow.add(eventId);
                newData.add(newRow);
            }
        }


        return newData;
    }

    private boolean uploadToGoogleSheets(List<List<Object>> data, String sheetId) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(HTTP_TRANSPORT);

        ValueRange body = new ValueRange()
                .setValues(data);

        AppendValuesResponse appendValuesResponse = service.spreadsheets().values()
                .append(sheetId, "A2", body)
                .setValueInputOption("RAW")
                .execute();
        int resSize = appendValuesResponse.getUpdates().getUpdatedRows();
        if (resSize > 0) {
            System.out.println("invitees details excel updated successfully");
            return true;
        } else {
            System.out.println("invitees details excel update failed");
            return false;
        }
    }

    public ResponseDataDTO insertRivaahDetails(RivaahDTO rivaahDTO) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);
            String code = String.format("%010d", (long) (Math.random() * 1_000_000_0000L));
            ValueRange body = new ValueRange()
                    .setValues(this.formInputRivaahData(code,rivaahDTO));

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId8, "A2", body)
                    .setValueInputOption("RAW").execute();
            int resSize = appendValuesResponse.size();
            if (resSize > 0) {
                System.out.println("rivaah details excel updated successfully");
                responseDataDTO.setResult(code);
                responseDataDTO.setStatus(true);
                responseDataDTO.setMessage("Rivaah details stored successfully");
            } else {
                System.out.println("rivaah details excel updated failed");
                responseDataDTO.setStatus(false);
                responseDataDTO.setMessage("rivaah details excel updated failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("rivaah details excel updated failed");
        }
        return responseDataDTO;
    }
    private List<List<Object>> formInputRivaahData(String code, RivaahDTO rivaahDTO) {
        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(code);
        System.out.println(rivaahDTO.getBride());
        item.add(rivaahDTO.getBride());
        item.add(rivaahDTO.getEvent());
        item.add(rivaahDTO.getClothing_type());

        // Join the tags with comma, removing spaces between tags
        String formattedTags = String.join(",", rivaahDTO.getTags());
        item.add(formattedTags);

        res.add(item);
        return res;
    }

    public boolean insertRivaahUserDetails(String name,String contact) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange body = new ValueRange()
                    .setValues(this.formInputRivaahUserData(name,contact));

            AppendValuesResponse appendValuesResponse = service.spreadsheets().values().append(sheetId9, "A2", body)
                    .setValueInputOption("RAW").execute();
            int resSize = appendValuesResponse.size();
            if (resSize > 0) {
                System.out.println("rivaah user details excel updated successfully");
                return true;
            } else {
                System.out.println("rivaah user details excel updated failed");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private List<List<Object>> formInputRivaahUserData(String name,String contact) {
        List<List<Object>> res = new ArrayList<>();
        List<Object> item = new ArrayList<>();
        item.add(name);
        item.add(contact);
        res.add(item);
        return res;
    }

    public RivaahAllDetailsDTO getRivaahDetails(String code) {
        RivaahAllDetailsDTO rivaahAllDetailsDTO = new RivaahAllDetailsDTO();
        rivaahAllDetailsDTO.setStatus(false);
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            // Step 1: Fetch data from sheetId8 where code matches
            ValueRange response8 = service.spreadsheets().values()
                    .get(sheetId8, "Sheet1!A:E")
                    .execute();

            List<List<Object>> values8 = response8.getValues();

            if (values8 == null || values8.isEmpty()) {
                rivaahAllDetailsDTO.setMessage("No data found in the sheet");
                return rivaahAllDetailsDTO; // No data found in the sheet
            }

            List<Object> matchingRow = null;

            // Step 2: Loop through the rows and find the matching code
            for (List<Object> row : values8) {
                if (row.size() > 0 && row.get(0).equals(code)) {  // Assuming code is in the first column
                    matchingRow = row;
                    break;
                }
            }

            if (matchingRow == null) {
                rivaahAllDetailsDTO.setMessage("No matching code found in the sheet");
                return rivaahAllDetailsDTO; // No matching code found
            }

            // Extract bride, event, clothing_type, tags (assuming the structure of columns)
            String tagsStr = (String) matchingRow.get(4);
            rivaahAllDetailsDTO.setBride(matchingRow.get(1).toString());
            rivaahAllDetailsDTO.setEvent(matchingRow.get(2).toString());
            rivaahAllDetailsDTO.setCode(code);
            rivaahAllDetailsDTO.setClothing_type( matchingRow.get(3).toString());

            // Step 3: Extract list of tags by removing the brackets and splitting by comma
            tagsStr = tagsStr.trim();  // Trim any leading or trailing spaces
            List<String> tags = List.of(tagsStr.split("\\s*,\\s*"));
            System.out.println(tags);
            rivaahAllDetailsDTO.setTags(tags);
            RivaahImagesDTO rivaahImagesDTO = getImagesByTags(tags);
            System.out.println(rivaahImagesDTO);
            rivaahAllDetailsDTO.setImages(rivaahImagesDTO);
            rivaahAllDetailsDTO.setStatus(true);
            rivaahAllDetailsDTO.setMessage("Details successfully fetched from the sheet.");
            return rivaahAllDetailsDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AttendeesDetailDTO> getAllAttendees(String eventId) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(httpTransport);

        String range = "Sheet1!A:F"; // Adjust the range as per your sheet structure
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId5, range)
                .execute();

        List<List<Object>> rows = response.getValues();
        List<AttendeesDetailDTO> attendeesList = new ArrayList<>();

        if (rows != null && !rows.isEmpty()) {
            for (List<Object> row : rows) {
                // Skip if the row does not have the required columns
                if (row.size() < 6) continue;

                // Check if the first column matches the eventId
                if (eventId.equals(row.get(0).toString())) {
                    AttendeesDetailDTO dto = new AttendeesDetailDTO();
                    dto.setName(row.get(1).toString());
                    dto.setPhone(row.get(2).toString());
                    dto.setLike(row.get(3).toString());
                    dto.setFirstTimeAtTanishq(Boolean.parseBoolean(row.get(4).toString().toLowerCase()));
                    attendeesList.add(dto);
                }
            }
        }
        return attendeesList;

    }

    public ResponseDataDTO changePassword(String storeCode, String oldPassword, String newPassword) {
        String sheetName = "Sheet3";
        String range = sheetName + "!A:D";
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets sheetsService = getSheetService(httpTransport);
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(sheetId3, range)
                    .execute();
            String correctPassword = this.getNewPassword(storeCode);
            if(!oldPassword.equals(correctPassword)){
                responseDataDTO.setStatus(false);
                responseDataDTO.setMessage("Old Password does not match");
                return responseDataDTO;
            }
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                responseDataDTO.setStatus(false);
                responseDataDTO.setMessage("No Data Found");
                return responseDataDTO;
            }

            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() > 1 && row.get(0).toString().equals(storeCode)) {
//                    System.out.println();
                    if (row.get(2).toString().equals(oldPassword)) {
                        // Update the password and timestamp
                        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        List<Object> newRow = List.of(storeCode, oldPassword, newPassword, dateTime);
                        ValueRange newValue = new ValueRange().setValues(List.of(newRow));
                        String updateRange = sheetName + "!A" + (i + 1) + ":D" + (i + 1);

                        sheetsService.spreadsheets().values()
                                .update(sheetId3, updateRange, newValue)
                                .setValueInputOption("RAW")
                                .execute();

                        responseDataDTO.setStatus(true);
                        responseDataDTO.setMessage("Password updated successfully");
                        responseDataDTO.setResult(newPassword);
                        return responseDataDTO;
                    } else {
                        responseDataDTO.setStatus(false);
                        responseDataDTO.setMessage("Incorrect old password");
                        return responseDataDTO;
                    }
                }
            }
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("Store code not found");
            return responseDataDTO;
        } catch (Exception e) {
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("Error: " + e.getMessage());
            return responseDataDTO;
        }
    }

    public String getNewPassword(String storeCode) {
        String sheetName = "Sheet3";
        String range = sheetName + "!A:D";

        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets sheetsService = getSheetService(httpTransport);
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(sheetId3, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return null;
            }

            for (List<Object> row : values) {
                if (row.size() > 2 && row.get(0).toString().equalsIgnoreCase(storeCode)) {
                    return row.get(2).toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        String range = "Sheet1!D:R"; // Adjust if your sheet name is different

        // Fetch existing data
        ValueRange response = service.spreadsheets().values()
                .get(SheetId11, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            throw new Exception("No data found in the sheet.");
        }

        // Find row with matching eventCode in column B
        int rowIndex = -1;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 1 && eventCode.equalsIgnoreCase(values.get(i).get(0).toString())) { // Column B = index 1
                rowIndex = i + 1; // Google Sheets is 1-based index
                break;
            }
        }

        if (rowIndex == -1) {
            throw new Exception("Event code not found.");
        }

        // Update sale value in column R (index 17)
        String updateRange = "Sheet1!Q" + rowIndex;
        ValueRange body = new ValueRange().setValues(List.of(List.of(sale)));

        service.spreadsheets().values()
                .update(SheetId11, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(true);
        responseDataDTO.setMessage("Sale updated successfully");
        return responseDataDTO;
    }
    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        String range = "Sheet1!B:R"; // Adjust if your sheet name is different

        // Fetch existing data
        ValueRange response = service.spreadsheets().values()
                .get(SheetId11, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            throw new Exception("No data found in the sheet.");
        }

        // Find row with matching eventCode in column B
        int rowIndex = -1;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 1 && eventCode.equalsIgnoreCase(values.get(i).get(0).toString())) { // Column B = index 1
                rowIndex = i + 1; // Google Sheets is 1-based index
                break;
            }
        }

        if (rowIndex == -1) {
            throw new Exception("Event code not found.");
        }

        // Update sale value in column R
        String updateRange = "Sheet1!R" + rowIndex;
        ValueRange body = new ValueRange().setValues(List.of(List.of(advance)));

        service.spreadsheets().values()
                .update(SheetId11, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(true);
        responseDataDTO.setMessage("Advance updated successfully");
        return responseDataDTO;
    }
    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        String range = "Sheet1!B:S"; // Adjust if your sheet name is different

        // Fetch existing data
        ValueRange response = service.spreadsheets().values()
                .get(SheetId11, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            throw new Exception("No data found in the sheet.");
        }

        // Find row with matching eventCode in column B
        int rowIndex = -1;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 1 && eventCode.equalsIgnoreCase(values.get(i).get(0).toString())) { // Column B = index 1
                rowIndex = i + 1; // Google Sheets is 1-based index
                break;
            }
        }

        if (rowIndex == -1) {
            throw new Exception("Event code not found.");
        }

        // Update sale value in column
        String updateRange = "Sheet1!S" + rowIndex;
        ValueRange body = new ValueRange().setValues(List.of(List.of(ghsRga)));

        service.spreadsheets().values()
                .update(SheetId11, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(true);
        responseDataDTO.setMessage("GHS/RGA updated successfully");
        return responseDataDTO;
    }

    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(httpTransport);
        String range = "Sheet1!B:T"; // Adjust if your sheet name is different

        // Fetch existing data
        ValueRange response = service.spreadsheets().values()
                .get(SheetId11, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            throw new Exception("No data found in the sheet.");
        }

        // Find row with matching eventCode in column B
        int rowIndex = -1;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).size() > 1 && eventCode.equalsIgnoreCase(values.get(i).get(0).toString())) { // Column B = index 1
                rowIndex = i + 1; // Google Sheets is 1-based index
                break;
            }
        }

        if (rowIndex == -1) {
            throw new Exception("Event code not found.");
        }

        // Update sale value in column U
        String updateRange = "Sheet1!T" + rowIndex;
        ValueRange body = new ValueRange().setValues(List.of(List.of(gmb)));

        service.spreadsheets().values()
                .update(SheetId11, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(true);
        responseDataDTO.setMessage("GMB updated successfully");
        return responseDataDTO;
    }

//    public boolean isValidUserAbm(String username, String password) throws Exception {
//        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets sheetsService = getSheetService(transport);
//
//        String range = "abm_login!B2:D"; // username = B, password = D
//        ValueRange response = sheetsService.spreadsheets().values()
//                .get(sheetId10, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) return false;
//
//        for (List<Object> row : values) {
//            if (row.size() >= 3) {
//                String sheetUsername = String.valueOf(row.get(0));
//                String sheetPassword = String.valueOf(row.get(2)); // index 2 = column D
//                if (username.equals(sheetUsername) && password.equals(sheetPassword)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public Optional<LoginResponseDTO> isValidUserAbm(String username, String password) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);

        String range = "abm_login!A2:D"; // A=name, B=userId, D=newPassword
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) return Optional.empty();

        for (List<Object> row : values) {
            if (row.size() >= 4) {
                String sheetName = String.valueOf(row.get(0));      // Column A
                String sheetUserId = String.valueOf(row.get(1));    // Column B
                String sheetPassword = String.valueOf(row.get(3));  // Column D

                if (username.equals(sheetUserId) && password.equals(sheetPassword)) {
                    return Optional.of(new LoginResponseDTO(sheetUserId, sheetName));
                }
            }
        }
        return Optional.empty();
    }



//    public boolean isValidUserRbm(String username, String password) throws Exception {
//        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets sheetsService = getSheetService(transport);
//
//        String range = "rbm_login!B2:D"; // username = B, password = D
//        ValueRange response = sheetsService.spreadsheets().values()
//                .get(sheetId10, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) return false;
//
//        for (List<Object> row : values) {
//            if (row.size() >= 3) {
//                String sheetUsername = String.valueOf(row.get(0));
//                String sheetPassword = String.valueOf(row.get(2)); // index 2 = column D
//                if (username.equals(sheetUsername) && password.equals(sheetPassword)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public Optional<LoginResponseDTO> isValidUserRbm(String username, String password) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);

        String range = "rbm_login!A2:D"; // A=name, B=userId, D=newPassword
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) return Optional.empty();

        for (List<Object> row : values) {
            if (row.size() >= 4) {
                String sheetName = String.valueOf(row.get(0));      // Column A
                String sheetUserId = String.valueOf(row.get(1));    // Column B
                String sheetPassword = String.valueOf(row.get(3));  // Column D

                if (username.equals(sheetUserId) && password.equals(sheetPassword)) {
                    return Optional.of(new LoginResponseDTO(sheetUserId, sheetName));
                }
            }
        }
        return Optional.empty();
    }


//    public boolean isValidUserCee(String username, String password) throws Exception {
//        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets sheetsService = getSheetService(transport);
//
//        String range = "cee_login!B2:D"; // username = B, password = D
//        ValueRange response = sheetsService.spreadsheets().values()
//                .get(sheetId10, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) return false;
//
//        for (List<Object> row : values) {
//            if (row.size() >= 3) {
//                String sheetUsername = String.valueOf(row.get(0));
//                String sheetPassword = String.valueOf(row.get(2)); // index 2 = column D
//                if (username.equals(sheetUsername) && password.equals(sheetPassword)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


    public Optional<LoginResponseDTO> isValidUserCee(String username, String password) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);

        String range = "cee_login!A2:D"; // A=name, B=userId, D=newPassword
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) return Optional.empty();

        for (List<Object> row : values) {
            if (row.size() >= 4) {
                String sheetName = String.valueOf(row.get(0));      // Column A
                String sheetUserId = String.valueOf(row.get(1));    // Column B
                String sheetPassword = String.valueOf(row.get(3));  // Column D

                if (username.equals(sheetUserId) && password.equals(sheetPassword)) {
                    return Optional.of(new LoginResponseDTO(sheetUserId, sheetName));
                }
            }
        }
        return Optional.empty();
    }


    public List<String> getStoresByRbmUsername(String rbmUsername) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);
        String range = "main_sheet!A2:T"; // A = Store ID, T = RBM Username
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<String> storeIds = new ArrayList<>();

        if (values == null || values.isEmpty()) return storeIds;

        for (List<Object> row : values) {
            if (row.size() >= 20) {
                String storeId = String.valueOf(row.get(0));
                String rbmName = String.valueOf(row.get(19));

                if (rbmUsername.equalsIgnoreCase(rbmName)) {
                    storeIds.add(storeId);
                }
            }
        }

        return storeIds;
    }

    public List<String> getStoresByAbmUsername(String abmUsername) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);
        String range = "main_sheet!A2:Z"; // A = Store ID, Z = ABM Username
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<String> storeIds = new ArrayList<>();

        if (values == null || values.isEmpty()) return storeIds;

        for (List<Object> row : values) {
            if (row.size() >= 26) { // Z column = index 25
                String storeId = String.valueOf(row.get(0));
                String abmName = String.valueOf(row.get(25));

                if (abmUsername.equalsIgnoreCase(abmName)) {
                    storeIds.add(storeId);
                }
            }
        }

        return storeIds;
    }

    public List<String> getStoresByCeeUsername(String ceeUsername) throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = getSheetService(transport);
        String range = "main_sheet!A2:Z"; // A = Store ID, Z = ABM Username
        ValueRange response = sheetsService.spreadsheets().values()
                .get(sheetId10, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<String> storeIds = new ArrayList<>();

        if (values == null || values.isEmpty()) return storeIds;

        for (List<Object> row : values) {
            if (row.size() >= 23) { // W = index 22
                String storeId = String.valueOf(row.get(0));
                String ceeName = String.valueOf(row.get(22));

                if (ceeUsername.equalsIgnoreCase(ceeName)) {
                    storeIds.add(storeId);
                }
            }
        }

        return storeIds;
    }

    public boolean insertBAPSheetData(List<Object> row) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = getSheetService(httpTransport);

            ValueRange valueRange = new ValueRange().setValues(Collections.singletonList(row));
            AppendValuesResponse response = service.spreadsheets().values()
                    .append(sheetId4, "Sheet1", valueRange)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();

            if (response.getUpdates() != null && response.getUpdates().getUpdatedRows() > 0) {
                System.out.println("BAP excel updated successfully");
                return true;
            } else {
                System.out.println("BAP excel update failed");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

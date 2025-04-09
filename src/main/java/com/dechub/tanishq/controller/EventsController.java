package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.gdrive.GoogleDriveService;
import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.ResponseDataDTO;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
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
            @RequestParam(value ="customerContact",required = false) String contact
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
        return tanishqPageService.storeEventsDetails(eventsDetailDTO);
    }

    @PostMapping("/attendees")
    public ResponseDataDTO storeAttendeesData(@RequestParam(name = "eventId",required = false) String eventId,
                                              @RequestParam(name="name",required = false) String name,
                                              @RequestParam(name="phone",required = false) String phone,
                                              @RequestParam(name="like",required = false) String like,
                                              @RequestParam(name="firstTimeAtTanishq",required = false) boolean firstTimeAtTanishq,
                                              @RequestParam(name="file",required = false) MultipartFile file){

        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
        attendeesDetailDTO.setId(eventId);
        attendeesDetailDTO.setLike(like);
        attendeesDetailDTO.setPhone(phone);
        attendeesDetailDTO.setName(name);
        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
        attendeesDetailDTO.setFile(file);
        System.out.println(attendeesDetailDTO);
        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
    }


    @PostMapping("/getevents")
    public CompletedEventsResponseDTO getAllCompletedEvents(@RequestBody storeCodeDataDTO storeCodeDataDTO){
        return tanishqPageService.getAllCompletedEvents(storeCodeDataDTO.getStoreCode());
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

}

package com.dechub.tanishq.mail;


import com.dechub.tanishq.service.TanishqPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EmailService {


    private final JavaMailSender emailSender;

    @Value("${system.isWindows}")
    private String isWindows;


    @Value("${selfie.upload.dir}")
    private String selfieDirectory;


    @Value("${spring.mail.username}")
    private String fromMailId;


    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);


    public boolean sendEmail(String toMailId, String subject, String text, String fileName)  {
        //throws  IOException, MessagingException
        try{
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);


//            helper.setFrom("tanishqcelebrations@titan.co.in");
            helper.setFrom(fromMailId);



            helper.setTo(toMailId);
            helper.setSubject(subject);
            helper.setText(text);

            String filePath =  null;
            if(isWindows.equalsIgnoreCase("Y")){
                filePath = selfieDirectory + "\\" + fileName;
            }else{
                filePath = selfieDirectory + "/" + fileName;
            }

            Path pathFile = Paths.get(filePath);
            byte[] fileBytes = Files.readAllBytes(pathFile);
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes);
            helper.addAttachment(fileName, fileResource);

            emailSender.send(message);
            log.info("Email Send Success For toMailId: "  + toMailId + " for FileName: " + fileName);
            return true;
        } catch (Exception exception){
            exception.printStackTrace();
            log.info("Error sending Email: " + exception.getMessage() + "\nFor toMailId: " + toMailId + " for FileName: " + fileName);
            return false;
        }
    }



//old code
//    public boolean sendEmailWithAttachment(String to, String subject, String text, String imagePath, String videoPath)  {
//        //throws  IOException, MessagingException
//        try{
//            MimeMessage message = emailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(text);
//            // Attach image
//            Path imagePathFile = Paths.get(imagePath);
//            byte[] imageBytes = Files.readAllBytes(imagePathFile);
//            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
//            helper.addAttachment("image.png", imageResource);
//
//            // Attach video
//            Path videoPathFile = Paths.get(videoPath);
//            byte[] videoBytes = Files.readAllBytes(videoPathFile);
//            ByteArrayResource videoResource = new ByteArrayResource(videoBytes);
//            helper.addAttachment("video.mp4", videoResource);
//            emailSender.send(message);
//        } catch (Exception exception){
//
//        }
//        return false;
//
//    }

}

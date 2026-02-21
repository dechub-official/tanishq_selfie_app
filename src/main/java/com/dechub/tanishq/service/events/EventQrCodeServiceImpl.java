package com.dechub.tanishq.service.events;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Event QR Code Service Implementation
 *
 * This is a DEDICATED service for Events QR code generation ONLY.
 * It replicates the exact logic from the original Google Sheets implementation
 * (GSheetUserDetailsUtil.generateQrCode method).
 *
 * IMPORTANT: This is completely separate from GreetingController's QR functionality.
 *
 * Flow:
 * 1. Manager creates event -> QR code generated with event customer URL
 * 2. Customer scans QR -> Redirects to /events/customer/{eventId}
 * 3. Customer fills attendee form -> Saved to attendees table
 * 4. Attendee count auto-incremented in events table
 */
@Slf4j
@Service
public class EventQrCodeServiceImpl implements EventQrCodeService {

    /**
     * Base URL for event customer registration
     * This should point to: https://celebrations.tanishq.co.in/events/customer/
     * Format: {baseUrl}{eventId}
     */
    @Value("${events.qr.base.url:http://localhost:8130/events/customer/}")
    private String eventsQrBaseUrl;

    /**
     * QR Code image size (width and height in pixels)
     * Same as original Google Sheets implementation
     */
    private static final int QR_CODE_SIZE = 300;

    /**
     * Generate QR code for event - EXACT replica of Google Sheets implementation
     *
     * Original Logic (from GSheetUserDetailsUtil):
     * String qrUrl = "https://celebrations.tanishq.co.in/events/customer/" + eventId;
     * QRCodeWriter qrCodeWriter = new QRCodeWriter();
     * BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 300, 300);
     * BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
     * ByteArrayOutputStream baos = new ByteArrayOutputStream();
     * ImageIO.write(qrImage, "png", baos);
     * byte[] qrCodeBytes = baos.toByteArray();
     * return Base64.getEncoder().encodeToString(qrCodeBytes);
     *
     * @param eventId Event ID (e.g., STORE123_uuid)
     * @return Base64-encoded PNG image string
     * @throws Exception if generation fails
     */
    @Override
    public String generateEventQrCode(String eventId) throws Exception {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.error("Event ID is null or empty");
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }

        try {
            // Step 1: Construct QR code URL
            String qrUrl = eventsQrBaseUrl + eventId.trim();
            log.info("Generating Event QR code for URL: {}", qrUrl);

            // Step 2: Create QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Step 3: Encode URL into QR code matrix
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            // Step 4: Convert matrix to BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Step 5: Convert BufferedImage to PNG byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] qrCodeBytes = baos.toByteArray();

            // Step 6: Encode to Base64
            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);

            log.info("Successfully generated Event QR code for event: {}", eventId);
            return base64QrCode;

        } catch (WriterException e) {
            log.error("Failed to encode QR code for event: {}", eventId, e);
            throw new Exception("QR code encoding failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to generate QR code for event: {}", eventId, e);
            throw new Exception("QR code generation failed: " + e.getMessage(), e);
        }
    }
}


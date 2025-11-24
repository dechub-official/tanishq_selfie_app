package com.dechub.tanishq.service.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Implementation of QrCodeService
 * Handles all QR code generation and processing operations
 */
@Slf4j
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final String QR_URL_BASE = "https://celebrations.tanishq.co.in/events/customer/";
    private static final int DEFAULT_QR_SIZE = 300;

    @Override
    public String generateQrCodeBase64(String text, int width, int height) throws Exception {
        try {
            BufferedImage qrImage = generateQRCodeImage(text, width, height);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] qrCodeBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (Exception e) {
            log.error("Failed to generate QR code for text: {}", text, e);
            throw new Exception("QR code generation failed", e);
        }
    }

    @Override
    public byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        BufferedImage image = generateQRCodeImage(text, width, height);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }

    @Override
    public BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("QR code text cannot be null or empty");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }



    @Override
    public String generateEventQrCode(String eventId) throws Exception {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }

        String qrUrl = QR_URL_BASE + eventId.trim();
        log.debug("Generating QR code for event URL: {}", qrUrl);

        return generateQrCodeBase64(qrUrl, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }

    @Override
    public boolean validateQrCodeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // Check for reasonable length (URLs can be long, but not excessively so)
        if (text.length() > 2048) {
            return false;
        }

        // Basic validation for URLs if it looks like one
        if (text.startsWith("http://") || text.startsWith("https://")) {
            try {
                new java.net.URL(text);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // For non-URLs, just ensure it's not empty and contains valid characters
        return text.matches("[\\x20-\\x7E]+");
    }
}

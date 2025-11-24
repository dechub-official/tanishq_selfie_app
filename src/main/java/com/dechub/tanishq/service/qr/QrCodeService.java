package com.dechub.tanishq.service.qr;

import java.awt.image.BufferedImage;

/**
 * Interface for QR code operations
 * Single Responsibility: Handle all QR code generation and processing
 */
public interface QrCodeService {

    /**
     * Generate QR code as base64 string
     */
    String generateQrCodeBase64(String text, int width, int height) throws Exception;

    /**
     * Generate QR code as byte array
     */
    byte[] generateQrCodeImage(String text, int width, int height) throws Exception;

    /**
     * Generate QR code as BufferedImage
     */
    BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception;

    /**
     * Generate QR code for event with URL
     */
    String generateEventQrCode(String eventId) throws Exception;

    /**
     * Validate QR code text parameters
     */
    boolean validateQrCodeText(String text);
}

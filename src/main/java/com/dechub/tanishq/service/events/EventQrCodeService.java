package com.dechub.tanishq.service.events;

/**
 * Dedicated QR Code Service for Events ONLY
 * Replicates the original Google Sheets QR code generation logic
 * This service is completely separate from GreetingController's QR code functionality
 *
 * Original Google Sheets Implementation:
 * - URL Format: https://celebrations.tanishq.co.in/events/customer/{eventId}
 * - QR Generation: QRCodeWriter -> BitMatrix -> PNG -> Base64
 * - Response: data:image/png;base64,{base64Data}
 */
public interface EventQrCodeService {

    /**
     * Generate QR code for event attendee registration
     * This is the ONLY method needed for Events QR code generation
     *
     * @param eventId The event ID
     * @return Base64-encoded PNG image string (WITHOUT data:image prefix)
     * @throws Exception if QR code generation fails
     */
    String generateEventQrCode(String eventId) throws Exception;
}


package com.dechub.tanishq.service;

import com.dechub.tanishq.dto.qrcode.ShareInfo;
import com.dechub.tanishq.entity.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for generating platform-specific share URLs
 * Handles URL encoding and platform-specific formatting
 */
@Slf4j
@Service
public class ShareService {

    @Value("${greeting.share.text:Watch my special Tanishq greeting! 💝✨}")
    private String defaultShareText;

    @Value("${greeting.qr.base.url:https://celebrationsite-preprod.tanishq.co.in/greetings/}")
    private String greetingBaseUrl;

    @Value("${greeting.share.enabled:true}")
    private boolean shareEnabled;

    /**
     * Generate share information for a greeting
     *
     * @param greeting The greeting entity
     * @return ShareInfo with all platform-specific URLs
     */
    public ShareInfo generateShareInfo(Greeting greeting) {
        if (!shareEnabled) {
            log.warn("Share feature is disabled");
            throw new IllegalStateException("Share feature is disabled");
        }

        String uniqueId = greeting.getUniqueId();
        String greetingName = greeting.getGreetingText() != null ? greeting.getGreetingText() : "Anonymous";

        // Construct the view URL
        String viewUrl = greetingBaseUrl + uniqueId + "/view";

        // Prepare share text
        String shareText = defaultShareText;

        // Generate platform-specific URLs
        String whatsappUrl = generateWhatsAppUrl(shareText, viewUrl);
        String facebookUrl = generateFacebookUrl(viewUrl);
        String twitterUrl = generateTwitterUrl(shareText, viewUrl);
        String linkedinUrl = generateLinkedInUrl(viewUrl);
        String emailUrl = generateEmailUrl(shareText, viewUrl, greetingName);
        String smsUrl = generateSmsUrl(shareText, viewUrl);

        // Create metadata
        ShareInfo.ShareMetadata metadata = new ShareInfo.ShareMetadata(
            uniqueId,
            greeting.getUploaded() != null && greeting.getUploaded(),
            greetingName,
            System.currentTimeMillis()
        );

        log.info("Generated share info for greeting: {}", uniqueId);

        return new ShareInfo(
            shareText,
            viewUrl,
            whatsappUrl,
            facebookUrl,
            twitterUrl,
            linkedinUrl,
            emailUrl,
            smsUrl,
            viewUrl,  // copyUrl - same as viewUrl for copy to clipboard
            metadata
        );
    }

    /**
     * Generate WhatsApp share URL
     * Format: https://wa.me/?text=MESSAGE
     */
    private String generateWhatsAppUrl(String text, String url) {
        String message = text + " " + url;
        return "https://wa.me/?text=" + urlEncode(message);
    }

    /**
     * Generate Facebook share URL
     * Format: https://www.facebook.com/sharer/sharer.php?u=URL
     */
    private String generateFacebookUrl(String url) {
        return "https://www.facebook.com/sharer/sharer.php?u=" + urlEncode(url);
    }

    /**
     * Generate Twitter/X share URL
     * Format: https://twitter.com/intent/tweet?text=TEXT&url=URL
     */
    private String generateTwitterUrl(String text, String url) {
        return "https://twitter.com/intent/tweet?text=" + urlEncode(text) + "&url=" + urlEncode(url);
    }

    /**
     * Generate LinkedIn share URL
     * Format: https://www.linkedin.com/sharing/share-offsite/?url=URL
     */
    private String generateLinkedInUrl(String url) {
        return "https://www.linkedin.com/sharing/share-offsite/?url=" + urlEncode(url);
    }

    /**
     * Generate Email share URL
     * Format: mailto:?subject=SUBJECT&body=BODY
     */
    private String generateEmailUrl(String text, String url, String greetingName) {
        String subject = "Special Tanishq Greeting from " + greetingName;
        String body = text + "\n\n" + url + "\n\n";

        return "mailto:?subject=" + urlEncode(subject) + "&body=" + urlEncode(body);
    }

    /**
     * Generate SMS share URL
     * Format: sms:?body=MESSAGE
     */
    private String generateSmsUrl(String text, String url) {
        String message = text + " " + url;
        return "sms:?body=" + urlEncode(message);
    }

    /**
     * URL encode a string using UTF-8
     *
     * @param value String to encode
     * @return URL-encoded string
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to URL encode value: {}", value, e);
            // Fallback to no encoding
            return value;
        }
    }

    /**
     * Check if share feature is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isShareEnabled() {
        return shareEnabled;
    }
}


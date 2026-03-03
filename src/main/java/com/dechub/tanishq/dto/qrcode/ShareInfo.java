package com.dechub.tanishq.dto.qrcode;

/**
 * DTO for share information
 * Contains platform-specific share URLs and metadata
 */
public class ShareInfo {
    private String shareText;
    private String viewUrl;
    private String whatsappUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String linkedinUrl;
    private String emailUrl;
    private String smsUrl;
    private String copyUrl;
    private ShareMetadata metadata;

    public ShareInfo(String shareText, String viewUrl, String whatsappUrl,
                     String facebookUrl, String twitterUrl, String linkedinUrl,
                     String emailUrl, String smsUrl, String copyUrl,
                     ShareMetadata metadata) {
        this.shareText = shareText;
        this.viewUrl = viewUrl;
        this.whatsappUrl = whatsappUrl;
        this.facebookUrl = facebookUrl;
        this.twitterUrl = twitterUrl;
        this.linkedinUrl = linkedinUrl;
        this.emailUrl = emailUrl;
        this.smsUrl = smsUrl;
        this.copyUrl = copyUrl;
        this.metadata = metadata;
    }

    // Getters
    public String getShareText() { return shareText; }
    public String getViewUrl() { return viewUrl; }
    public String getWhatsappUrl() { return whatsappUrl; }
    public String getFacebookUrl() { return facebookUrl; }
    public String getTwitterUrl() { return twitterUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getEmailUrl() { return emailUrl; }
    public String getSmsUrl() { return smsUrl; }
    public String getCopyUrl() { return copyUrl; }
    public ShareMetadata getMetadata() { return metadata; }

    /**
     * Nested class for share metadata
     */
    public static class ShareMetadata {
        private String uniqueId;
        private boolean hasVideo;
        private String greetingName;
        private long timestamp;

        public ShareMetadata(String uniqueId, boolean hasVideo, String greetingName, long timestamp) {
            this.uniqueId = uniqueId;
            this.hasVideo = hasVideo;
            this.greetingName = greetingName;
            this.timestamp = timestamp;
        }

        // Getters
        public String getUniqueId() { return uniqueId; }
        public boolean isHasVideo() { return hasVideo; }
        public String getGreetingName() { return greetingName; }
        public long getTimestamp() { return timestamp; }
    }
}


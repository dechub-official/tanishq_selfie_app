package com.dechub.tanishq.dto.qrcode;

public class GreetingInfo {
    private boolean hasVideo;           // derived (status + fileId)
    private String status;              // B
    private String driveFileId;         // C
    private String videoPlaybackUrl;    // D
    private String submissionTimestamp; // E
    private String name;                // F
    private String message;             // G

    public GreetingInfo(boolean hasVideo, String status, String driveFileId,
                        String videoPlaybackUrl, String submissionTimestamp,
                        String name, String message) {
        this.hasVideo = hasVideo;
        this.status = status;
        this.driveFileId = driveFileId;
        this.videoPlaybackUrl = videoPlaybackUrl;
        this.submissionTimestamp = submissionTimestamp;
        this.name = name;
        this.message = message;
    }

    public boolean getHasVideo() { return hasVideo; }
    public String getStatus() {
        return status; }
    public String getDriveFileId() {
        return driveFileId; }
    public String getVideoPlaybackUrl() { return videoPlaybackUrl; }
    public String getSubmissionTimestamp() { return submissionTimestamp; }
    public String getName() { return name; }
    public String getMessage() { return message; }
}

package com.dechub.tanishq.util;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDataDTO {
    private boolean status;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    private Object FileResponseDataDto;

    /**
     * The message.
     */
    private String message;

    /**
     * The result.
     */
    private Object result;
    private String filePath;
    private ResponseEntity<byte[]> imageResponse;

    public Object getFileResponseDataDto() {
        return FileResponseDataDto;
    }

    public void setFileResponseDataDto(Object fileResponseDataDto) {
        FileResponseDataDto = fileResponseDataDto;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Sets the result.
     *
     * @param result the new result
     */
    public void setResult(final Object result) {
        this.result = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public ResponseEntity<byte[]> getImageResponse() {
        return imageResponse;
    }

    public void setImageResponse(ResponseEntity<byte[]> imageResponse) {
        this.imageResponse = imageResponse;
    }

    @Override
    public String toString() {
        return "ResponseDataDTO{" +
                "FileResponseDataDto=" + FileResponseDataDto +
                ", message='" + message + '\'' +
                ", result=" + result +
                ", filePath='" + filePath + '\'' +
                ", imageResponse=" + imageResponse +
                '}';
    }

}

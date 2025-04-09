package com.dechub.tanishq.dto;

public class UserDetailResponseDTO {


    private boolean mailSend;
    private boolean detailStored;


    public UserDetailResponseDTO(boolean detailStored, boolean mailSend) {
        this.mailSend = mailSend;
        this.detailStored = detailStored;
    }


    public UserDetailResponseDTO() {
    }

    public boolean isMailSend() {
        return mailSend;
    }

    public void setMailSend(boolean mailSend) {
        this.mailSend = mailSend;
    }

    public boolean isDetailStored() {
        return detailStored;
    }

    public void setDetailStored(boolean detailStored) {
        this.detailStored = detailStored;
    }


    @Override
    public String toString() {
        return "UserDetailResponseDTO{" +
                "mailSend=" + mailSend +
                ", detailStored=" + detailStored +
                '}';
    }
}

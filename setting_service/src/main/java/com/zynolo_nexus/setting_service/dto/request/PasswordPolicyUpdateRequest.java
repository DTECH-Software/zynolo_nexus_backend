package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class PasswordPolicyUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Integer minUpperCase;
    private Integer minLowerCase;
    private Integer minNumbers;
    private Integer minSpecialCharacters;
    private Integer minLength;
    private Integer maxLength;
    private Integer passwordHistory;
    private Integer attemptExceedCount;
    private Integer otpExceedCount;
}

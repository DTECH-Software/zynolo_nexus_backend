package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicyDto {

    private Long id;
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

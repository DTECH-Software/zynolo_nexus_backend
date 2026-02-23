package com.zynolo_nexus.setting_service.dto.response;

import com.zynolo_nexus.setting_service.enums.BankStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {

    private Long id;
    private String code;
    private String name;
    private BankStatus status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}

package com.zynolo_nexus.cheque_service.dto.response;

import com.zynolo_nexus.cheque_service.enums.ChequeBankStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeBankDto {

    private Long id;
    private String code;
    private String name;
    private ChequeBankStatus status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}

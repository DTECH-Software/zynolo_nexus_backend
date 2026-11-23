package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.BankStatus;
import lombok.Data;

@Data
public class BankStatusUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long id;
    private BankStatus status;
}

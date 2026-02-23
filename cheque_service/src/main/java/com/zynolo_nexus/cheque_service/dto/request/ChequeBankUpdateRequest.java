package com.zynolo_nexus.cheque_service.dto.request;

import com.zynolo_nexus.cheque_service.enums.ChequeBankStatus;
import lombok.Data;

@Data
public class ChequeBankUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long id;
    private String code;
    private String name;
    private ChequeBankStatus status;
}

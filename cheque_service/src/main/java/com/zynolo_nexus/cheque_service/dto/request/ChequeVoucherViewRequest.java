package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeVoucherViewRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
}

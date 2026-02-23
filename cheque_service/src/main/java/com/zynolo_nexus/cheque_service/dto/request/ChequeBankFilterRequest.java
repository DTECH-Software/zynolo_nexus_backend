package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeBankFilterRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Integer page;
    private Integer size;
    private String sortColumn;
    private String sortDirection;
    private ChequeBankFilterSearch search;
}

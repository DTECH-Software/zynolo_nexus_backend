package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.contracts.pages.PageStatus;
import lombok.Data;

@Data
public class PageStatusUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String code;
    private PageStatus status;
}

package com.zynolo_nexus.contracts.pages;

import lombok.Data;

@Data
public class PageStatusRequest {

    private PageStatus status;
    private String username;
}

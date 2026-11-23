package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class BankFilterSearch {

    private String code;
    private String name;
    private String status;
}

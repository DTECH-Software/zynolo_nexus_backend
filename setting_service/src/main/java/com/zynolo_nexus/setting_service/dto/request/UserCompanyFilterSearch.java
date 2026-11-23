package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class UserCompanyFilterSearch {

    private String username;
    private String companyCode;
    private String roleCode;
    private String status;
}

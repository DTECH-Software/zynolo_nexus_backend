package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class CompanyModuleFilterSearch {

    private String companyCode;
    private String companyDescription;
    private String moduleCode;
    private String moduleDescription;
    private String status;
}

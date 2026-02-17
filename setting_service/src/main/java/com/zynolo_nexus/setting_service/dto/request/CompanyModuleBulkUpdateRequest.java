package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CompanyModuleBulkUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String companyCode;
    private List<ModuleAccess> modules;

    @Data
    public static class ModuleAccess {
        private String moduleCode;
        private Boolean allowed;
    }
}


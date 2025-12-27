package com.zynolo_nexus.contracts.pages;

import lombok.Data;

import java.util.List;

@Data
public class RolePageTaskAccessUpdateRequest {

    private String roleCode;
    private List<PageTaskPermission> tasks;

    @Data
    public static class PageTaskPermission {
        private String pageCode;
        private String taskCode;
        private boolean canAccess;
    }
}

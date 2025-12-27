package com.zynolo_nexus.contracts.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePageTaskAccessDto {

    private String roleCode;
    private List<PagePermissionItem> pages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagePermissionItem {
        private String moduleCode;
        private String moduleName;
        private String sectionCode;
        private String sectionName;
        private String pageCode;
        private String pageName;
        private List<TaskPermissionItem> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskPermissionItem {
        private String taskCode;
        private String taskName;
        private boolean canAccess;
    }
}

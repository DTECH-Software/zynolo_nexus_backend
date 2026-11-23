package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModulePrivilegesDto {

    private boolean add;
    private boolean update;
    private boolean view;
    private boolean search;
    private boolean delete;
}

package com.zynolo_nexus.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagePermissionDto {

    private String code;
    private String name;
    private List<PageTaskPermissionDto> tasks;
}

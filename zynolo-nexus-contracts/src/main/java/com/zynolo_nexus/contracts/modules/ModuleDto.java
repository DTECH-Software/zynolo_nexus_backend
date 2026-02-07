package com.zynolo_nexus.contracts.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String url;
    private ModuleStatus status;
    private String statusDescription;
    private Integer sortOrder;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}

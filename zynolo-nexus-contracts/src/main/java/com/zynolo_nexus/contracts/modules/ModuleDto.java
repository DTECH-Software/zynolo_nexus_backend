package com.zynolo_nexus.contracts.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private Integer sortOrder;
}

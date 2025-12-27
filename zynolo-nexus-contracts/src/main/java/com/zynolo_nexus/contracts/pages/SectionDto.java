package com.zynolo_nexus.contracts.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {

    private Long id;
    private String moduleCode;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private Integer sortOrder;
}

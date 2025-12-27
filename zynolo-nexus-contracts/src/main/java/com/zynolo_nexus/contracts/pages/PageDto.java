package com.zynolo_nexus.contracts.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {

    private Long id;
    private String sectionCode;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private Integer sortOrder;
}

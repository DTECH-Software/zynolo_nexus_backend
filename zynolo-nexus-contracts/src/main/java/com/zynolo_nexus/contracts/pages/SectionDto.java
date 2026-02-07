package com.zynolo_nexus.contracts.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String url;
    private boolean active;
    private Integer sortOrder;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}

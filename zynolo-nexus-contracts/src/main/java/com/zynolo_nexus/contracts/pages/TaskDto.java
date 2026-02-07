package com.zynolo_nexus.contracts.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private Integer sortOrder;
}

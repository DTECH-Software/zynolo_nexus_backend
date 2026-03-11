package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPrivilegesDto {
    private boolean add;
    private boolean update;
    private boolean view;
    private boolean search;
    private boolean delete;
}

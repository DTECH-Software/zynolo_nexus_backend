package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankFilterResultDto {

    private List<BankListItemDto> content;
    private Integer size;
    private Integer totalRecords;
    private Integer page;
    private Integer totalPages;
}

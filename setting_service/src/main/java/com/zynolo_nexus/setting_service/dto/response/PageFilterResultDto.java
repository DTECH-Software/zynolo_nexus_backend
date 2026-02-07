package com.zynolo_nexus.setting_service.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageFilterResultDto {

    private List<PageListItemDto> items;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}

package com.zynolo_nexus.cheque_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeReprintFilterResultDto {

    private List<ChequeReprintRequestDto> content;
    private Integer size;
    private Integer totalRecords;
    private Integer page;
    private Integer totalPages;
}

package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReceiptFilterResultDto {
    private List<InvoiceReceiptListItemDto> content;
    private int size;
    private long totalRecords;
}

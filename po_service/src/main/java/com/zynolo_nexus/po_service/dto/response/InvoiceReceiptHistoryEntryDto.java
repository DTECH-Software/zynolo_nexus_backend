package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReceiptHistoryEntryDto {
    private Long id;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String invoiceRemark;
    private String receivedBy;
    private LocalDateTime createdDate;
    private BigDecimal totalAmount;
    private List<InvoiceReceiptHistoryLineDto> items;
}

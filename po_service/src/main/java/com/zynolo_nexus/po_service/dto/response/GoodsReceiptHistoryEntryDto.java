package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptHistoryEntryDto {
    private Long id;
    private LocalDate receiptDate;
    private String deliveryNoteNo;
    private String receiveRemark;
    private String receivedBy;
    private LocalDateTime createdDate;
    private List<GoodsReceiptHistoryLineDto> items;
}

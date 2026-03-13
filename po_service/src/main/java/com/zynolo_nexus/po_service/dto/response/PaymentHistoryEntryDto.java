package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryEntryDto {
    private Long id;
    private String paymentReferenceNo;
    private String paymentMethod;
    private String chequeNo;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private String paymentRemark;
    private String paidBy;
    private LocalDateTime createdDate;
}

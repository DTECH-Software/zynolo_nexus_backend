package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentPayRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    private String paymentReferenceNo;

    private String paymentMethod = "CHEQUE";

    @NotBlank(message = "chequeNo is required")
    private String chequeNo;

    @NotNull(message = "paymentDate is required")
    private LocalDate paymentDate;

    @NotNull(message = "paidAmount is required")
    @DecimalMin(value = "0.01", message = "paidAmount must be greater than zero")
    private BigDecimal paidAmount;

    private String paymentRemark;
}

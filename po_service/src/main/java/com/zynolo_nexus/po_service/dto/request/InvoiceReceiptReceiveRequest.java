package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InvoiceReceiptReceiveRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "invoiceNo is required")
    private String invoiceNo;

    @NotNull(message = "invoiceDate is required")
    private LocalDate invoiceDate;

    private String invoiceRemark;

    @Valid
    @NotEmpty(message = "items are required")
    private List<InvoiceReceiptReceiveItemRequest> items;
}

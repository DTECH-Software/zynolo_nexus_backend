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
public class PurchaseOrderUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotNull(message = "requiredDate is required")
    private LocalDate requiredDate;

    @NotBlank(message = "justification is required")
    private String justification;

    @Valid
    @NotEmpty(message = "items are required")
    private List<PurchaseOrderItemUpdateRequest> items;
}
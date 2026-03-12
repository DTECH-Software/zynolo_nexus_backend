package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderRequestViewRequest extends AuditRequest {

    @NotNull(message = "requestId is required")
    private Long requestId;
}
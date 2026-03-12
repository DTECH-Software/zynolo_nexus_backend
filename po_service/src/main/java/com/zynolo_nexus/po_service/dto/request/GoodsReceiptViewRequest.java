package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsReceiptViewRequest extends AuditRequest {

    @NotNull(message = "poId is required")
    private Long poId;
}

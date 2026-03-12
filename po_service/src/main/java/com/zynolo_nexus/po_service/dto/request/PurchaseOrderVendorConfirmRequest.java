package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseOrderVendorConfirmRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "status is required")
    private String status;

    private String vendorReferenceNo;
    private LocalDate expectedDeliveryDate;
    private String vendorConfirmationRemark;
}

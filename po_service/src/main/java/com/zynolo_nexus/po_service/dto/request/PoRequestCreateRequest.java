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
public class PoRequestCreateRequest extends AuditRequest {

    @NotBlank(message = "companyCode is required")
    private String companyCode;

    @NotBlank(message = "companyName is required")
    private String companyName;

    @NotBlank(message = "requestType is required")
    private String requestType;

    private String department;

    private String costCenter;

    @NotBlank(message = "currencyCode is required")
    private String currencyCode;

    private String vendorCode;

    private String vendorName;

    @NotNull(message = "requiredDate is required")
    private LocalDate requiredDate;

    @NotBlank(message = "justification is required")
    private String justification;

    @Valid
    @NotEmpty(message = "items are required")
    private List<PoRequestItemRequest> items;
}

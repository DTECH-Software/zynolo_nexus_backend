package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyCreateRequest extends AuditRequest {

    @NotBlank(message = "code is required")
    @Pattern(regexp = "[A-Z]{3}", message = "code must contain exactly 3 uppercase letters")
    private String code;

    @NotBlank(message = "description is required")
    private String description;
}

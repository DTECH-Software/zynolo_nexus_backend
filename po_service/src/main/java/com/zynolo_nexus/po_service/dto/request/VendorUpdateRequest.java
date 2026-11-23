package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "description is required")
    private String description;

    private String street1;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String contactNo;
    private String email;
    private String website;
    private String status;
}

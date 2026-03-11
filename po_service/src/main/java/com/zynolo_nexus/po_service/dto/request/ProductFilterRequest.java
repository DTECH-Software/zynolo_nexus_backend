package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest extends AuditRequest {

    @Min(value = 0, message = "page must be zero or greater")
    private int page = 0;

    @Min(value = 1, message = "size must be greater than zero")
    private int size = 10;

    private String sortColumn = "lastModifiedDate";
    private String sortDirection = "DESC";

    @Valid
    @NotNull(message = "search is required")
    private ProductFilterSearch search;
}

package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingExportRequest extends AuditRequest {

    private String sortColumn = "lastModifiedDate";
    private String sortDirection = "DESC";

    @Valid
    @NotNull(message = "search is required")
    private TrackingFilterSearch search;
}

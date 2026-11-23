package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GoodsReceiptReceiveRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotNull(message = "receiptDate is required")
    private LocalDate receiptDate;

    private String deliveryNoteNo;
    private String receiveRemark;

    @Valid
    @NotEmpty(message = "items are required")
    private List<GoodsReceiptReceiveItemRequest> items;
}

package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptFilterSearch {
    private String receiptNumber;
    private String tripNumber;
    private String requesterUsername;
    private String paymentMethod;
    private String status;
    /** When true, only receipts for the login username's transport requests. */
    private Boolean ownOnly;
}

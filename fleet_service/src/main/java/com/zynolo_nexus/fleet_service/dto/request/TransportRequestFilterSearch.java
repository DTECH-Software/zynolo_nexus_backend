package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransportRequestFilterSearch {
    private String requestNumber;
    private String requesterUsername;
    private String status;
    private String pickupLocation;
    private String dropLocation;
}
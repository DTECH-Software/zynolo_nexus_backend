package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripFareFilterSearch {
    private String fareNumber;
    private String tripNumber;
    private String paymentMethod;
    private String status;
}

package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverFilterSearch {
    private String name;
    private String mobile;
    private String availability;
    private String status;
}
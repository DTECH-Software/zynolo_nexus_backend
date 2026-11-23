package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MyAssignmentFilterSearch {
    private LocalDate scheduleDate;
    private String tripNumber;
    private String status;
}
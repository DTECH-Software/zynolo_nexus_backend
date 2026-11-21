package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OutstandingFilterSearch {
    /** REQUEST_SUBMITTED | TRIP_ASSIGNED | COMPLETED_WITHOUT_FARE. Empty = all types. */
    private String itemType;
    /** Applies to trip types only. Empty = all dates. */
    private LocalDate scheduleDate;
    private String referenceNumber;
}

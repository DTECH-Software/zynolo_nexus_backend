package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayDashboardDto {
    private LocalDate reportDate;

    /** Requests with submittedAt on reportDate. */
    private long requestsSubmitted;

    /** Open queue: status SUBMITTED (all dates). */
    private long requestsPendingQueue;

    /** Trips with scheduledPickupAt on reportDate. */
    private long tripsAssigned;
    private long tripsStarted;
    private long tripsCompleted;
    private long tripsCancelled;
    private long tripsTotal;

    /** Currently live: status STARTED (all dates). */
    private long tripsLive;

    /** Fares with recordedAt on reportDate and status RECORDED. */
    private long faresRecorded;
    private BigDecimal faresAmountTotal;

    /** COMPLETED trips on reportDate with no fare row. */
    private long completedWithoutFare;
}

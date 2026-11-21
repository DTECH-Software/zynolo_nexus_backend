package com.zynolo_nexus.fleet_service.model;

import com.zynolo_nexus.fleet_service.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "fleet_trips",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_fleet_trip_request", columnNames = {"transport_request_id"})
        }
)
public class FleetTrip extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "trip_number", nullable = false, length = 40)
    private String tripNumber;

    @Column(name = "transport_request_id", nullable = false)
    private Long transportRequestId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "scheduled_pickup_at", nullable = false)
    private LocalDateTime scheduledPickupAt;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", nullable = false, length = 50)
    private String assignedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripStatus status;
}
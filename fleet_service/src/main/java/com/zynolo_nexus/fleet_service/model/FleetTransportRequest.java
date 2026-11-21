package com.zynolo_nexus.fleet_service.model;

import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "fleet_transport_requests")
public class FleetTransportRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "request_number", nullable = false, length = 40)
    private String requestNumber;

    @Column(name = "requester_username", nullable = false, length = 50)
    private String requesterUsername;

    @Column(name = "pickup_location", nullable = false, length = 255)
    private String pickupLocation;

    @Column(name = "drop_location", nullable = false, length = 255)
    private String dropLocation;

    @Column(name = "requested_pickup_at", nullable = false)
    private LocalDateTime requestedPickupAt;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "contact_mobile", length = 30)
    private String contactMobile;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransportRequestStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
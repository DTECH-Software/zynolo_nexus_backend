package com.zynolo_nexus.fleet_service.model;

import com.zynolo_nexus.fleet_service.enums.FarePaymentMethod;
import com.zynolo_nexus.fleet_service.enums.FareStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "fleet_trip_fares",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_fleet_trip_fare_trip", columnNames = {"trip_id"}),
                @UniqueConstraint(name = "UK_fleet_trip_fare_number", columnNames = {"company_id", "fare_number"})
        }
)
public class FleetTripFare extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "fare_number", nullable = false, length = 40)
    private String fareNumber;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private FarePaymentMethod paymentMethod;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "recorded_by", nullable = false, length = 50)
    private String recordedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FareStatus status;
}

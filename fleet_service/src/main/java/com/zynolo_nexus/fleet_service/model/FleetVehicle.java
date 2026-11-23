package com.zynolo_nexus.fleet_service.model;

import com.zynolo_nexus.fleet_service.enums.MasterStatus;
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

@Getter
@Setter
@Entity
@Table(
        name = "fleet_vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_fleet_vehicle_company_registration", columnNames = {"company_id", "registration_normalized"})
        }
)
public class FleetVehicle extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "registration", nullable = false, length = 50)
    private String registration;

    @Column(name = "registration_normalized", nullable = false, length = 50)
    private String registrationNormalized;

    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Column(name = "passenger_capacity", nullable = false)
    private Integer passengerCapacity;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MasterStatus status;
}

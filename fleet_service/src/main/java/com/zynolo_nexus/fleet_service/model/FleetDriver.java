package com.zynolo_nexus.fleet_service.model;

import com.zynolo_nexus.fleet_service.enums.DriverAvailability;
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
        name = "fleet_drivers",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_fleet_driver_company_mobile", columnNames = {"company_id", "mobile_normalized"})
        }
)
public class FleetDriver extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "mobile", nullable = false, length = 30)
    private String mobile;

    @Column(name = "mobile_normalized", nullable = false, length = 30)
    private String mobileNormalized;

    @Column(name = "licence_number", length = 50)
    private String licenceNumber;

    @Column(name = "linked_username", length = 50)
    private String linkedUsername;

    @Column(name = "usual_vehicle_id")
    private Long usualVehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false, length = 20)
    private DriverAvailability availability;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MasterStatus status;
}
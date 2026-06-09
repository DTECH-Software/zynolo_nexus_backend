package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingVendorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_vendors", indexes = {
        @Index(name = "idx_meeting_vendors_company", columnList = "company_id"),
        @Index(name = "idx_meeting_vendors_company_code", columnList = "company_id,vendor_code", unique = true),
        @Index(name = "idx_meeting_vendors_company_name", columnList = "company_id,vendor_name", unique = true),
        @Index(name = "idx_meeting_vendors_type", columnList = "vendor_type"),
        @Index(name = "idx_meeting_vendors_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", nullable = false, length = 255)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false, length = 40)
    private MeetingVendorType vendorType;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "email_address", length = 150)
    private String emailAddress;

    @Column(length = 1000)
    private String address;

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String lastModifiedBy;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) {
            createdDate = now;
        }
        if (lastModifiedDate == null) {
            lastModifiedDate = now;
        }
        if (active == null) {
            active = Boolean.TRUE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}

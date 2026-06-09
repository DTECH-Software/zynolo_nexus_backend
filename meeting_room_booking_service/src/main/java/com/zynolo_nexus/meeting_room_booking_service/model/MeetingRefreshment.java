package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_refreshments", indexes = {
        @Index(name = "idx_meeting_refreshments_company", columnList = "company_id"),
        @Index(name = "idx_meeting_refreshments_company_code", columnList = "company_id,refreshment_code", unique = true),
        @Index(name = "idx_meeting_refreshments_category", columnList = "category"),
        @Index(name = "idx_meeting_refreshments_vendor", columnList = "default_vendor_id"),
        @Index(name = "idx_meeting_refreshments_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRefreshment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "refreshment_code", nullable = false, length = 50)
    private String refreshmentCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefreshmentCategory category;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "default_vendor_id")
    private Long defaultVendorId;

    @Column(name = "default_vendor_code", length = 50)
    private String defaultVendorCode;

    @Column(name = "default_vendor_name", length = 255)
    private String defaultVendorName;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 1000)
    private String description;

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

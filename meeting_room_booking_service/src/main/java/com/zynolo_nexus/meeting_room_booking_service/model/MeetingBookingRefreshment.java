package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "meeting_booking_refreshments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingRefreshment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private MeetingBooking booking;

    @Column(name = "refreshment_id", nullable = false)
    private Long refreshmentId;

    @Column(name = "refreshment_code", length = 50)
    private String refreshmentCode;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RefreshmentCategory category;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
}

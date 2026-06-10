package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
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
@Table(name = "meeting_booking_support_services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingSupportService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private MeetingBooking booking;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Column(name = "service_name", length = 255)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", length = 40)
    private SupportServiceCategory serviceCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_team", length = 40)
    private SupportAssignedTeam assignedTeam;

    @Column(nullable = false)
    private Boolean chargeable;

    @Column(name = "estimated_amount", precision = 18, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(length = 1000)
    private String remarks;
}

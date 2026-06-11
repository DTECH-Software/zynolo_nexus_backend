package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meeting_bookings", indexes = {
        @Index(name = "idx_meeting_bookings_company", columnList = "company_id"),
        @Index(name = "idx_meeting_bookings_request", columnList = "company_id,request_no", unique = true),
        @Index(name = "idx_meeting_bookings_room_date", columnList = "meeting_room_id,meeting_date"),
        @Index(name = "idx_meeting_bookings_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "request_no", nullable = false, length = 50)
    private String requestNo;

    @Column(name = "meeting_name", nullable = false, length = 255)
    private String meetingName;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 30)
    private MeetingBookingType meetingType;

    @Column(name = "meeting_room_id", nullable = false)
    private Long meetingRoomId;

    @Column(name = "meeting_room_code", length = 50)
    private String meetingRoomCode;

    @Column(name = "meeting_room_name", length = 255)
    private String meetingRoomName;

    @Column(name = "room_capacity", nullable = false)
    private Integer roomCapacity;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "number_of_attendees", nullable = false)
    private Integer numberOfAttendees;

    @Column(name = "purpose_remarks", length = 1000)
    private String purposeRemarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeetingBookingStatus status;

    @Column(name = "duration_hours", precision = 18, scale = 2)
    private BigDecimal durationHours;

    @Column(name = "estimated_refreshment_cost", precision = 18, scale = 2)
    private BigDecimal estimatedRefreshmentCost;

    @Column(name = "estimated_beverage_cost", precision = 18, scale = 2)
    private BigDecimal estimatedBeverageCost;

    @Column(name = "estimated_support_cost", precision = 18, scale = 2)
    private BigDecimal estimatedSupportCost;

    @Column(name = "room_charge", precision = 18, scale = 2)
    private BigDecimal roomCharge;

    @Column(name = "total_estimated_cost", precision = 18, scale = 2)
    private BigDecimal totalEstimatedCost;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "approval_remark", length = 1000)
    private String approvalRemark;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_remark", length = 1000)
    private String rejectionRemark;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "ongoing_update", length = 1000)
    private String ongoingUpdate;

    @Column(name = "ongoing_additional_attendees")
    private Integer ongoingAdditionalAttendees;

    @Column(name = "ongoing_updated_total_attendees")
    private Integer ongoingUpdatedTotalAttendees;

    @Column(name = "ongoing_updated_by", length = 100)
    private String ongoingUpdatedBy;

    @Column(name = "ongoing_updated_date")
    private LocalDateTime ongoingUpdatedDate;

    @Column(name = "invoice_no", length = 50)
    private String invoiceNo;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String lastModifiedBy;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MeetingBookingRefreshment> refreshments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MeetingBookingBeverage> beverages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MeetingBookingSupportService> supportServices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MeetingBookingParticipant> participants = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) {
            createdDate = now;
        }
        if (lastModifiedDate == null) {
            lastModifiedDate = now;
        }
        if (status == null) {
            status = MeetingBookingStatus.DRAFT;
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}

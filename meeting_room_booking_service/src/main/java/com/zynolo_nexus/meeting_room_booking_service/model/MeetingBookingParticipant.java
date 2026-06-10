package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingParticipantType;
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

@Entity
@Table(name = "meeting_booking_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private MeetingBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false, length = 20)
    private MeetingParticipantType participantType;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "employee_name", length = 150)
    private String employeeName;

    @Column(length = 150)
    private String name;

    @Column(length = 150)
    private String company;

    @Column(name = "contact_no", length = 30)
    private String contactNo;

    @Column(length = 150)
    private String email;
}

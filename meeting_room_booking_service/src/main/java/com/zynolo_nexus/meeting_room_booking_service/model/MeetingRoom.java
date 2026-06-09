package com.zynolo_nexus.meeting_room_booking_service.model;

import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
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
@Table(name = "meeting_rooms", indexes = {
        @Index(name = "idx_meeting_rooms_code", columnList = "room_code", unique = true),
        @Index(name = "idx_meeting_rooms_name", columnList = "room_name", unique = true),
        @Index(name = "idx_meeting_rooms_availability", columnList = "availability_status"),
        @Index(name = "idx_meeting_rooms_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 50)
    private String roomCode;

    @Column(name = "room_name", nullable = false, unique = true, length = 255)
    private String roomName;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 30)
    private RoomAvailabilityStatus availabilityStatus;

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
        if (availabilityStatus == null) {
            availabilityStatus = RoomAvailabilityStatus.AVAILABLE;
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

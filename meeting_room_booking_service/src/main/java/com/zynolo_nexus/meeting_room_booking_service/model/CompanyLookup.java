package com.zynolo_nexus.meeting_room_booking_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class CompanyLookup {

    @Id
    private Long id;

    @Column(length = 50)
    private String code;

    @Column(length = 255)
    private String description;
}

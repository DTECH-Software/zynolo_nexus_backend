package com.zynolo_nexus.po_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cheque_customers")
public class Vendor extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "street_1", length = 255)
    private String street1;

    @Column(name = "street_2", length = 255)
    private String street2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "contact_no", length = 30)
    private String contactNo;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}

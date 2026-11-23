package com.zynolo_nexus.po_service.model;

import com.zynolo_nexus.po_service.enums.PoRequestStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "po_requests")
public class PoRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", nullable = false, unique = true, length = 50)
    private String requestNo;

    @Column(name = "company_code", nullable = false, length = 50)
    private String companyCode;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "request_type", nullable = false, length = 50)
    private String requestType;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "cost_center", length = 50)
    private String costCenter;

    @Column(name = "currency_code", nullable = false, length = 20)
    private String currencyCode;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "justification", length = 1000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PoRequestStatus status;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_remark", length = 1000)
    private String reviewRemark;

    @OneToMany(mappedBy = "poRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PoRequestItem> items = new ArrayList<>();

    public void addItem(PoRequestItem item) {
        items.add(item);
        item.setPoRequest(this);
    }

    public void clearItems() {
        items.forEach(item -> item.setPoRequest(null));
        items.clear();
    }
}

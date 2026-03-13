package com.zynolo_nexus.po_service.model;

import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "purchase_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_order_request", columnNames = {"request_id"}),
        @UniqueConstraint(name = "uk_purchase_order_no", columnNames = {"po_no"})
})
public class PurchaseOrder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_no", nullable = false, unique = true, length = 50)
    private String poNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private PoRequest request;

    @Column(name = "request_no", nullable = false, length = 50)
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

    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", nullable = false, length = 150)
    private String vendorName;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "justification", length = 1000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PurchaseOrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(name = "sent_by", length = 100)
    private String sentBy;

    @Column(name = "send_remark", length = 1000)
    private String sendRemark;

    @Column(name = "vendor_confirmation_date")
    private LocalDateTime vendorConfirmationDate;

    @Column(name = "vendor_confirmation_by", length = 100)
    private String vendorConfirmationBy;

    @Column(name = "vendor_reference_no", length = 100)
    private String vendorReferenceNo;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "vendor_confirmation_remark", length = 1000)
    private String vendorConfirmationRemark;

    @Column(name = "match_status", length = 30)
    private String matchStatus;

    @Column(name = "matched_date")
    private LocalDateTime matchedDate;

    @Column(name = "matched_by", length = 100)
    private String matchedBy;

    @Column(name = "match_remark", length = 1000)
    private String matchRemark;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "close_remark", length = 1000)
    private String closeRemark;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void clearItems() {
        items.forEach(item -> item.setPurchaseOrder(null));
        items.clear();
    }
}

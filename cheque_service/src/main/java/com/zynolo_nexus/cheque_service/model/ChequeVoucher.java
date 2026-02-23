package com.zynolo_nexus.cheque_service.model;

import com.zynolo_nexus.cheque_service.enums.ChequeType;
import com.zynolo_nexus.cheque_service.enums.ChequeVoucherStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cheque_vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_no", nullable = false, unique = true, length = 50)
    private String voucherNo;

    @Column(name = "company_code", nullable = false, length = 50)
    private String companyCode;

    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    @Column(name = "cheque_no", nullable = false, length = 100)
    private String chequeNo;

    @Column(name = "cheque_bank_name", nullable = false, length = 150)
    private String chequeBankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "cheque_type", length = 20)
    private ChequeType chequeType;

    @Column(name = "cheque_date")
    private LocalDate chequeDate;

    @Column(length = 500)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChequeVoucherStatus status;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approval_remark", length = 500)
    private String approvalRemark;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChequeVoucherInvoice> invoices = new ArrayList<>();

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
        if (status == null) {
            status = ChequeVoucherStatus.DRAFT;
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}

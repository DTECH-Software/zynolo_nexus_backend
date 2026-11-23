package com.zynolo_nexus.cheque_service.model;

import com.zynolo_nexus.cheque_service.enums.ChequeReprintStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cheque_reprint_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeReprintRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "reprint_reason", nullable = false, length = 500)
    private String reprintReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChequeReprintStatus status;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "approval_remark", length = 500)
    private String approvalRemark;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "used_for_print")
    private Boolean usedForPrint;

    @Column(name = "used_date")
    private LocalDateTime usedDate;

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
            status = ChequeReprintStatus.REPRINT_PENDING;
        }
        if (usedForPrint == null) {
            usedForPrint = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}

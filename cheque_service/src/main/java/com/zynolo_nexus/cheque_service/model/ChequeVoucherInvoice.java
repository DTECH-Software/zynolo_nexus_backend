package com.zynolo_nexus.cheque_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "cheque_voucher_invoices",
        indexes = {
                @Index(name = "idx_cheque_voucher_invoices_company_id", columnList = "company_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeVoucherInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private ChequeVoucher voucher;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_no", nullable = false, length = 100)
    private String invoiceNo;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
}

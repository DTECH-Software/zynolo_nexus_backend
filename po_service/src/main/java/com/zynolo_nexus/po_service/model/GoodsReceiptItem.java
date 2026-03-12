package com.zynolo_nexus.po_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "goods_receipt_items")
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_description", nullable = false, length = 250)
    private String itemDescription;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "ordered_quantity", nullable = false, precision = 18, scale = 2)
    private BigDecimal orderedQuantity;

    @Column(name = "approved_quantity", precision = 18, scale = 2)
    private BigDecimal approvedQuantity;

    @Column(name = "received_quantity", nullable = false, precision = 18, scale = 2)
    private BigDecimal receivedQuantity;
}

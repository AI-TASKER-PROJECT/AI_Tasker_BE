package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id") private Long invoiceId;
    @Column(name = "transaction_id", nullable = false, unique = true) private Long transactionId;
    @Column(name = "bank_tx_code", length = 100) private String bankTxCode;
    @Column(name = "receipt_img_url", length = 255) private String receiptImgUrl;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}

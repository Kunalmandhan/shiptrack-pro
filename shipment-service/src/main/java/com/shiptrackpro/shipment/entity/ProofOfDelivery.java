package com.shiptrackpro.shipment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Proof of Delivery — evidence that a shipment was successfully delivered.
 *
 * Contains:
 * - Signature image URL (uploaded to Cloudinary)
 * - Photo of delivered package (uploaded to Cloudinary)
 * - Name of person who received the package
 * - Optional notes
 *
 * One-to-one relationship with Shipment. POD can only be created
 * when shipment is in OUT_FOR_DELIVERY status, and its creation
 * transitions the shipment to DELIVERED.
 */
@Entity
@Table(name = "proof_of_delivery", schema = "shiptrack_shipment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "received_by", nullable = false, length = 100)
    private String receivedBy;

    @Column(length = 500)
    private String notes;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

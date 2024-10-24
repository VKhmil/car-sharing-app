package com.carsharingapp.model;

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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.net.URL;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE payments SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column(name = "session_url", nullable = false)
    private URL sessionUrl;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "price", nullable = false)
    private BigDecimal amountToPay;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum PaymentStatus {
        PENDING,
        PAID,
        CANCELED
    }

    public enum PaymentType {
        PAYMENT,
        FINE
    }
}

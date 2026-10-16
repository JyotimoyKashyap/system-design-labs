package com.jyotimoykashyap.bookmyshowdesign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionReference;
    private Instant timestamp;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties("payment")
    private Booking booking;
}

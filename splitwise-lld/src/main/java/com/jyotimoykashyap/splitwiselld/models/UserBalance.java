package com.jyotimoykashyap.splitwiselld.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Column(name = "net_balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netBalance = BigDecimal.ZERO;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void adjust(BigDecimal delta) {
        this.netBalance = this.netBalance.add(delta);
        this.updatedAt = LocalDateTime.now();
    }

}

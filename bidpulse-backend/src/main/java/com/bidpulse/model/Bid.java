package com.bidpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bid", indexes = {
    @Index(name = "idx_bid_auction_amount", columnList = "auction_id, amount")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BidStatus status = BidStatus.PLACED;
}
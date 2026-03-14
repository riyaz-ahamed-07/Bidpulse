package com.bidpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction", indexes = {
    @Index(name = "idx_auction_status_endtime", columnList = "status, end_time")
})
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_data", columnDefinition = "TEXT")
    private String imageData;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "starting_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal startingPrice;

    @Builder.Default
    @Column(name = "min_increment", precision = 18, scale = 2, nullable = false)
    private BigDecimal minIncrement = BigDecimal.valueOf(1.00);

    @Column(name = "reserve_price", precision = 18, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AuctionStatus status = AuctionStatus.DRAFT;

    @Column(name = "highest_bid_amount", precision = 18, scale = 2)
    private BigDecimal highestBidAmount;

    @Column(name = "highest_bidder_id")
    private Long highestBidderId;

    // Optimistic lock version — useful as a fallback and for detecting concurrent updates
    @Version
    private Long version;

    // Bid list (denormalized write-side; lazy to avoid accidental loads)
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
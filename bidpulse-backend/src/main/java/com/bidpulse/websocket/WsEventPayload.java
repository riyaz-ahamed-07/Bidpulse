package com.bidpulse.websocket;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WsEventPayload {
    private String type;
    private Long auctionId;
    private Long bidId;
    private BigDecimal amount;
    private Long bidderId;
    private Instant placedAt;

    // convenience constructors
    public WsEventPayload(String type, Long auctionId) {
        this.type = type;
        this.auctionId = auctionId;
    }


    public WsEventPayload(String type, Long auctionId, Long bidId, BigDecimal amount) {
        this.type = type;
        this.auctionId = auctionId;
        this.bidId = bidId;
        this.amount = amount;
    }
}
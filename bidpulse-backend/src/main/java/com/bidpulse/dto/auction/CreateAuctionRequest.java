package com.bidpulse.dto.auction;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateAuctionRequest {
    private String title;
    private String description;
    private String imageData;
    private BigDecimal startingPrice;
    private BigDecimal minIncrement;
    private BigDecimal reservePrice;
    private Instant startTime;
    private Instant endTime;
}
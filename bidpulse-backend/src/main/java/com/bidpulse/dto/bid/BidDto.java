package com.bidpulse.dto.bid;

import com.bidpulse.model.Bid;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BidDto {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private Instant placedAt;

    public static BidDto from(Bid b) {
        return BidDto.builder()
            .id(b.getId())
            .auctionId(b.getAuction().getId())
            .bidderId(b.getBidder().getId())
            .amount(b.getAmount())
            .placedAt(b.getPlacedAt())
            .build();
    }
}
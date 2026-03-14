package com.bidpulse.dto.auction;

import com.bidpulse.model.Auction;
import com.bidpulse.model.AuctionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuctionDto {
    private Long id;
    private String title;
    private String description;
    private String imageData;
    private Long sellerId;
    private BigDecimal startingPrice;
    private BigDecimal minIncrement;
    private BigDecimal reservePrice;
    private Instant startTime;
    private Instant endTime;
    private AuctionStatus status;
    private BigDecimal highestBidAmount;
    private Long highestBidderId;

    public static AuctionDto from(Auction a) {
        return AuctionDto.builder()
            .id(a.getId())
            .title(a.getTitle())
            .description(a.getDescription())
            .imageData(a.getImageData())
            .sellerId(a.getSeller().getId())
            .startingPrice(a.getStartingPrice())
            .minIncrement(a.getMinIncrement())
            .reservePrice(a.getReservePrice())
            .startTime(a.getStartTime())
            .endTime(a.getEndTime())
            .status(a.getStatus())
            .highestBidAmount(a.getHighestBidAmount())
            .highestBidderId(a.getHighestBidderId())
            .build();
    }
}
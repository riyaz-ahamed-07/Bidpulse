package com.bidpulse.dto.bid;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaceBidRequest {
    private BigDecimal amount;
}
package com.bidpulse.util;

import com.bidpulse.dto.bid.BidDto;
import com.bidpulse.model.Bid;

public final class Mapper {
    private Mapper() {}
    public static BidDto toBidDto(Bid b) { return BidDto.from(b); }
}
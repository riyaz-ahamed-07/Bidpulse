package com.bidpulse.service;

import com.bidpulse.dto.bid.BidDto;
import com.bidpulse.dto.bid.PlaceBidRequest;

public interface BidService {
    BidDto placeBid(Long auctionId, Long bidderUserId, PlaceBidRequest req);
}
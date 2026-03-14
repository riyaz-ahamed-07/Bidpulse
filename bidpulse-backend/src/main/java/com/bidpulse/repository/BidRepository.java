package com.bidpulse.repository;

import com.bidpulse.model.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // top (highest) bid for an auction
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    // top N bids (descending by amount) — used for history previews
    List<Bid> findTop10ByAuctionIdOrderByAmountDesc(Long auctionId);

    // history paginated
    Page<Bid> findByAuctionIdOrderByPlacedAtDesc(Long auctionId, Pageable pageable);
}
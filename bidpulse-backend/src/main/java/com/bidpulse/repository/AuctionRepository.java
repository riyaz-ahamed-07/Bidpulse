package com.bidpulse.repository;

import com.bidpulse.model.Auction;
import com.bidpulse.model.AuctionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, Instant time);
    List<Auction> findByStatusAndStartTimeBefore(com.bidpulse.model.AuctionStatus status, java.time.Instant time);

    // Basic list pages will be done with findAll(Pageable) in service/controller
}
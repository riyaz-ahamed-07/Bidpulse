package com.bidpulse.repository;

import com.bidpulse.model.Wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Keep your existing method
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);

    // --- ADD THIS ONE LINE TO FIX THE ERROR ---
    // Spring Data JPA will automatically generate the implementation for this
    Optional<Wallet> findByUserId(Long userId);
}
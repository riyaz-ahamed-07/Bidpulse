package com.bidpulse.repository;

import com.bidpulse.model.SellerApplication;
import com.bidpulse.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {
    List<SellerApplication> findByStatus(ApplicationStatus status);
    Optional<SellerApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);
}
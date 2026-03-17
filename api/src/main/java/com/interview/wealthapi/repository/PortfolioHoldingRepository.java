package com.interview.wealthapi.repository;

import com.interview.wealthapi.domain.PortfolioHolding;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, UUID> {

    List<PortfolioHolding> findByCustomerId(UUID customerId);
}

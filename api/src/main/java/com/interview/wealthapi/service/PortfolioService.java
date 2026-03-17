package com.interview.wealthapi.service;

import com.interview.wealthapi.api.dto.CreateHoldingRequest;
import com.interview.wealthapi.api.dto.RebalanceRecommendationResponse;
import com.interview.wealthapi.domain.Customer;
import com.interview.wealthapi.domain.PortfolioHolding;
import com.interview.wealthapi.domain.RiskProfile;
import com.interview.wealthapi.exception.ResourceNotFoundException;
import com.interview.wealthapi.repository.CustomerRepository;
import com.interview.wealthapi.repository.PortfolioHoldingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioHoldingRepository holdingRepository;
    private final CustomerRepository customerRepository;

    public PortfolioService(PortfolioHoldingRepository holdingRepository, CustomerRepository customerRepository) {
        this.holdingRepository = holdingRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public PortfolioHolding addHolding(UUID customerId, CreateHoldingRequest request) {
        getCustomer(customerId);
        PortfolioHolding holding = new PortfolioHolding(
                UUID.randomUUID(),
                customerId,
                request.symbol().toUpperCase(),
                request.marketValue().setScale(4, RoundingMode.HALF_UP)
        );
        return holdingRepository.save(holding);
    }

    @Transactional(readOnly = true)
    public RebalanceRecommendationResponse rebalancePreview(UUID customerId) {
        Customer customer = getCustomer(customerId);
        List<PortfolioHolding> holdings = holdingRepository.findByCustomerId(customerId);

        BigDecimal total = holdings.stream()
                .map(PortfolioHolding::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> current = new LinkedHashMap<>();
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            holdings.forEach(holding -> current.put(
                    holding.getSymbol(),
                    holding.getMarketValue()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(total, 2, RoundingMode.HALF_UP)
            ));
        }

        Map<String, Integer> target = targetAllocation(customer.getRiskProfile());
        String guidance = "Shift portfolio gradually over 3-6 rebalancing windows to reduce execution risk.";

        return new RebalanceRecommendationResponse(total, current, target, guidance);
    }

    private Customer getCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public Map<String, Integer> targetAllocation(RiskProfile profile) {
        Map<String, Integer> target = new LinkedHashMap<>();
        switch (profile) {
            case CONSERVATIVE -> {
                target.put("BONDS", 70);
                target.put("EQUITY", 20);
                target.put("CASH", 10);
            }
            case BALANCED -> {
                target.put("BONDS", 40);
                target.put("EQUITY", 50);
                target.put("CASH", 10);
            }
            case AGGRESSIVE -> {
                target.put("BONDS", 15);
                target.put("EQUITY", 80);
                target.put("CASH", 5);
            }
        }
        return target;
    }
}

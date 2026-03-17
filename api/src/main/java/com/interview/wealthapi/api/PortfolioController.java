package com.interview.wealthapi.api;

import com.interview.wealthapi.api.dto.CreateHoldingRequest;
import com.interview.wealthapi.api.dto.RebalanceRecommendationResponse;
import com.interview.wealthapi.domain.PortfolioHolding;
import com.interview.wealthapi.service.PortfolioService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/{customerId}/holdings")
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioHolding addHolding(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateHoldingRequest request
    ) {
        return portfolioService.addHolding(customerId, request);
    }

    @PostMapping("/{customerId}/rebalance-preview")
    public RebalanceRecommendationResponse rebalancePreview(@PathVariable UUID customerId) {
        return portfolioService.rebalancePreview(customerId);
    }
}

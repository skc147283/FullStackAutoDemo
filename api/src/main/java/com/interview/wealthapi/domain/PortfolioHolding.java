package com.interview.wealthapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "portfolio_holdings")
public class PortfolioHolding {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 24)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal marketValue;

    protected PortfolioHolding() {
    }

    public PortfolioHolding(UUID id, UUID customerId, String symbol, BigDecimal marketValue) {
        this.id = id;
        this.customerId = customerId;
        this.symbol = symbol;
        this.marketValue = marketValue;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }
}

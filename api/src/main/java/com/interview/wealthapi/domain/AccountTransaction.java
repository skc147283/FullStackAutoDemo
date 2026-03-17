package com.interview.wealthapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_transactions")
public class AccountTransaction {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 64)
    private String direction;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(unique = true)
    private String clientRequestId;

    protected AccountTransaction() {
    }

    public AccountTransaction(UUID id, UUID accountId, TransactionType transactionType, BigDecimal amount,
                              String direction, OffsetDateTime createdAt, String clientRequestId) {
        this.id = id;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.direction = direction;
        this.createdAt = createdAt;
        this.clientRequestId = clientRequestId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDirection() {
        return direction;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }
}

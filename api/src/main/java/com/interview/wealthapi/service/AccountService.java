package com.interview.wealthapi.service;

import com.interview.wealthapi.api.dto.CreateAccountRequest;
import com.interview.wealthapi.api.dto.MoneyMovementRequest;
import com.interview.wealthapi.api.dto.TransferRequest;
import com.interview.wealthapi.domain.Account;
import com.interview.wealthapi.domain.AccountTransaction;
import com.interview.wealthapi.domain.TransactionType;
import com.interview.wealthapi.exception.BusinessException;
import com.interview.wealthapi.exception.ResourceNotFoundException;
import com.interview.wealthapi.repository.AccountRepository;
import com.interview.wealthapi.repository.AccountTransactionRepository;
import com.interview.wealthapi.repository.CustomerRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            AccountTransactionRepository transactionRepository,
            CustomerRepository customerRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        BigDecimal openingBalance = normalizeAmount(request.openingBalance());
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Opening balance cannot be negative");
        }

        Account account = new Account(
                UUID.randomUUID(),
                request.customerId(),
                request.currency().toUpperCase(Locale.ROOT),
                openingBalance
        );
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(UUID accountId, MoneyMovementRequest request) {
        Account account = getAccount(accountId);
        BigDecimal amount = normalizeAmount(request.amount());
        account.credit(amount);

        transactionRepository.save(new AccountTransaction(
                UUID.randomUUID(),
                account.getId(),
                TransactionType.DEPOSIT,
                amount,
                "CREDIT",
                OffsetDateTime.now(),
                null
        ));

        return accountRepository.save(account);
    }

    @Transactional
    public String transfer(TransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new BusinessException("Source and destination accounts must be different");
        }

        AccountTransaction existing = transactionRepository.findByClientRequestId(request.clientRequestId()).orElse(null);
        if (existing != null) {
            return "Duplicate request ignored. Existing transaction id: " + existing.getId();
        }

        Account source = getAccount(request.sourceAccountId());
        Account destination = getAccount(request.destinationAccountId());

        if (!source.getCurrency().equalsIgnoreCase(destination.getCurrency())) {
            throw new BusinessException("Currency mismatch between source and destination accounts");
        }

        BigDecimal amount = normalizeAmount(request.amount());
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        source.debit(amount);
        destination.credit(amount);

        accountRepository.save(source);
        accountRepository.save(destination);

        OffsetDateTime now = OffsetDateTime.now();
        transactionRepository.save(new AccountTransaction(
                UUID.randomUUID(),
                source.getId(),
                TransactionType.TRANSFER,
                amount,
                "DEBIT",
                now,
                request.clientRequestId()
        ));
        transactionRepository.save(new AccountTransaction(
                UUID.randomUUID(),
                destination.getId(),
                TransactionType.TRANSFER,
                amount,
                "CREDIT",
                now,
                null
        ));

        return "Transfer successful";
    }

    @Transactional(readOnly = true)
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public List<AccountTransaction> statement(UUID accountId, OffsetDateTime from, OffsetDateTime to) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(accountId, from, to);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}

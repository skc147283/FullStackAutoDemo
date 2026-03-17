package com.interview.wealthapi.api;

import com.interview.wealthapi.api.dto.CreateAccountRequest;
import com.interview.wealthapi.api.dto.MoneyMovementRequest;
import com.interview.wealthapi.api.dto.TransferRequest;
import com.interview.wealthapi.domain.Account;
import com.interview.wealthapi.domain.AccountTransaction;
import com.interview.wealthapi.service.AccountService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @PostMapping("/{accountId}/deposit")
    public Account deposit(@PathVariable UUID accountId, @Valid @RequestBody MoneyMovementRequest request) {
        return accountService.deposit(accountId, request);
    }

    @PostMapping("/transfer")
    public Map<String, String> transfer(@Valid @RequestBody TransferRequest request) {
        return Map.of("status", accountService.transfer(request));
    }

    @GetMapping("/{accountId}")
    public Account get(@PathVariable UUID accountId) {
        return accountService.getAccount(accountId);
    }

    @GetMapping("/{accountId}/statement")
    public List<AccountTransaction> statement(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return accountService.statement(accountId, from, to);
    }
}

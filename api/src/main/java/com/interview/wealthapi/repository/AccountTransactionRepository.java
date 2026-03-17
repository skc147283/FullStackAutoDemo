package com.interview.wealthapi.repository;

import com.interview.wealthapi.domain.AccountTransaction;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID> {

    Optional<AccountTransaction> findByClientRequestId(String clientRequestId);

    List<AccountTransaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID accountId,
            OffsetDateTime from,
            OffsetDateTime to
    );
}

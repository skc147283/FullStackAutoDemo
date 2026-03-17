package com.interview.wealthapi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateHoldingRequest(
        @NotBlank String symbol,
        @NotNull @Positive BigDecimal marketValue
) {
}

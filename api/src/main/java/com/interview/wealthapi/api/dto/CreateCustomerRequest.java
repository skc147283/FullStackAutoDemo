package com.interview.wealthapi.api.dto;

import com.interview.wealthapi.domain.RiskProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull RiskProfile riskProfile
) {
}

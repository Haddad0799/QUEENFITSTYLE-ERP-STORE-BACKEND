package br.com.erp.api.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceTokenRequest(
        @NotBlank String clientId,
        @NotBlank String clientSecret
) {}

package br.com.erp.api.order.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
        String reason
) {}

package br.com.erp.api.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockMovementDTO(
        @NotBlank
        String type,

        @Min(value = 0, message = "Quantidade não pode ser negativa")
        int quantity,

        String reason,

        @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
        Integer minQuantity
) {}
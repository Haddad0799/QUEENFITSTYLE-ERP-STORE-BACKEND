package br.com.erp.api.inventory.presentation.dto;

import br.com.erp.api.inventory.domain.enumerated.MovementType;

import java.time.LocalDateTime;

public record StockMovementDTO(
        Long id,
        MovementType type,
        int quantity,
        String reason,
        LocalDateTime createdAt
) {}

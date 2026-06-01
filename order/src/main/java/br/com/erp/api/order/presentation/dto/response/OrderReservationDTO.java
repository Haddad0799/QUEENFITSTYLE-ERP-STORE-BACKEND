package br.com.erp.api.order.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderReservationDTO(
        UUID reservationId,
        Long skuId,
        String skuCode,
        int quantity,
        String status,
        LocalDateTime expiresAt
) {}

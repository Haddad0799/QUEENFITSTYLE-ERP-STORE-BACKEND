package br.com.erp.api.order.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDetail(
        UUID reservationId,
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String sizeLabel,
        int quantity,
        String status,
        LocalDateTime expiresAt
) {}

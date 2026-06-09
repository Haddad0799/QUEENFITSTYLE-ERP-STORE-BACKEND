package br.com.erp.api.order.presentation.dto.response;

import br.com.erp.api.order.domain.enumerated.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long orderId,
        OrderStatus status,
        String customerName,
        String customerPhone,
        String customerEmail,
        BigDecimal totalAmount,
        int itemsCount,
        OrderDeliveryAddressDTO deliveryAddress,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        LocalDateTime expiresAt
) {}

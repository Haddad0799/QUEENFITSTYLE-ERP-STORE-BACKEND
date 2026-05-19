package br.com.erp.api.order.presentation.dto.response;

import br.com.erp.api.order.domain.enumerated.OrderStatus;

public record CreateOrderResponse(
        Long orderId,
        OrderStatus status,
        String whatsappUrl
) {}

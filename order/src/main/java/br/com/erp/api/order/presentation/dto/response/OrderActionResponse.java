package br.com.erp.api.order.presentation.dto.response;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.OrderStatus;

import java.time.LocalDateTime;

public record OrderActionResponse(
        Long orderId,
        OrderStatus status,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt
) {
    public static OrderActionResponse from(Order order) {
        return new OrderActionResponse(
                order.getId(),
                order.getStatus(),
                order.getConfirmedAt(),
                order.getCancelledAt()
        );
    }
}

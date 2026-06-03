package br.com.erp.api.order.presentation.dto.response;

import br.com.erp.api.order.domain.enumerated.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDTO(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        String notes,
        String whatsappMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        OrderCustomerDTO customer,
        OrderDeliveryAddressDTO deliveryAddress,
        List<OrderItemDTO> items,
        List<OrderReservationDTO> reservations,
        OrderReservationSummaryDTO reservationSummary,
        List<OrderTimelineDTO> timeline
) {}

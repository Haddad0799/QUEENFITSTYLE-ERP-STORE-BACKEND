package br.com.erp.api.order.presentation.dto.response;

import br.com.erp.api.order.domain.enumerated.OrderEventType;

import java.time.LocalDateTime;

public record OrderTimelineDTO(
        Long eventId,
        OrderEventType eventType,
        String description,
        String payload,
        String actor,
        LocalDateTime createdAt
) {}

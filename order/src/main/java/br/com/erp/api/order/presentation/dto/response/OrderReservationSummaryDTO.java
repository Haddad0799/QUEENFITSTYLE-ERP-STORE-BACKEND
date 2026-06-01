package br.com.erp.api.order.presentation.dto.response;

/**
 * Resumo operacional de reservas do pedido para alimentar countdown e destaques visuais
 * no front-end (separação/picking).
 */
public record OrderReservationSummaryDTO(
        int totalReservedItems,
        Long expiresInMinutes,
        boolean hasExpiredReservations
) {}

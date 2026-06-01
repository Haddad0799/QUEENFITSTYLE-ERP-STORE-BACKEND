package br.com.erp.api.order.application.query.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Linha bruta de reserva vinculada a um pedido. O expiresAt já vem resolvido com
 * fallback para o expiresAt do pedido quando a reserva não possui expiração própria.
 */
public record OrderReservationRow(
        UUID reservationId,
        Long skuId,
        String skuCode,
        int quantity,
        String status,
        LocalDateTime expiresAt
) {}

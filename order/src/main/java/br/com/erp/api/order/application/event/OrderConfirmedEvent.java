package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;

/**
 * Evento publicado quando um pedido tem o pagamento confirmado (PENDING_PAYMENT → PAID).
 * Consumido de forma assíncrona após o commit para disparar efeitos colaterais como o
 * e-mail de confirmação ao cliente, mantendo o use case desacoplado desses detalhes.
 */
public record OrderConfirmedEvent(Order order) {}

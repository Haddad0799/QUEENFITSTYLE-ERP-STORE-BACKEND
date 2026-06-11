package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;

/**
 * Evento publicado quando um pedido expira por falta de confirmação dentro do prazo
 * (PENDING_PAYMENT por mais de 24h). Consumido de forma assíncrona após o commit para
 * avisar a cliente, por e-mail, que o pedido foi cancelado automaticamente — mantendo o
 * use case desacoplado desse efeito colateral.
 */
public record OrderExpiredEvent(Order order) {}

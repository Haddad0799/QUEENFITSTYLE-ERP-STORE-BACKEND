package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;

/**
 * Evento publicado quando um pedido é cancelado pela vendedora (CancelOrderUseCase).
 * Consumido de forma assíncrona após o commit para avisar a cliente, por e-mail, que o
 * pedido foi cancelado — mantendo o use case desacoplado desse efeito colateral.
 */
public record OrderCancelledEvent(Order order) {}

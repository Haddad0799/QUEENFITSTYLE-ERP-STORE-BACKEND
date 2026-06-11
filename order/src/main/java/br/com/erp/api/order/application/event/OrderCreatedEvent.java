package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;

/**
 * Evento publicado quando um novo pedido é criado no e-commerce (checkout concluído).
 * Consumido de forma assíncrona após o commit para disparar a notificação por e-mail à
 * dona da loja, mantendo o use case desacoplado desse efeito colateral.
 *
 * Carrega o {@code whatsappUrl} já montado no checkout para que o e-mail aponte exatamente
 * para a conversa pré-preenchida — a mesma apresentada à cliente.
 */
public record OrderCreatedEvent(Order order, String whatsappUrl) {}

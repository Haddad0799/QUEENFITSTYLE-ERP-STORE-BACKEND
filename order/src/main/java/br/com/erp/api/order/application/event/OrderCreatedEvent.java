package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;

/**
 * Evento publicado quando um novo pedido é criado no e-commerce (checkout concluído).
 * Consumido de forma assíncrona após o commit para disparar a notificação por e-mail à
 * dona da loja, mantendo o use case desacoplado desse efeito colateral.
 *
 * Carrega o {@code whatsappUrl} já montado no checkout (conversa com a vendedora, do ponto
 * de vista da cliente) para usos futuros, e o {@code customerPhone} da cliente — usado no
 * e-mail da vendedora para montar o link {@code wa.me} que abre a conversa com a cliente.
 */
public record OrderCreatedEvent(Order order, String whatsappUrl, String customerPhone) {}

package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

import java.util.Map;

/**
 * Porta de saída para avisar a dona da loja de que um pedido foi cancelado pela própria
 * cliente no e-commerce — situação em que a vendedora, de outra forma, ficaria esperando uma
 * mensagem de WhatsApp que não virá, com o estoque já devolvido. A implementação concreta
 * (Resend) é selecionada por {@code notification.provider} via {@code @ConditionalOnProperty}.
 *
 * O e-mail vai para o destinatário configurado em {@code store_settings.notification_email}
 * (editável pela dona) e lista os dados básicos do pedido (número, cliente, itens, valor). O
 * {@code imageUrls} traz a URL pública da imagem de cada item indexada por
 * {@link br.com.erp.api.order.domain.entity.OrderItem#getId()}.
 */
public interface OrderCancelledStoreOwnerNotifier {

    void notify(Order order, Customer customer, Map<Long, String> imageUrls) throws Exception;
}

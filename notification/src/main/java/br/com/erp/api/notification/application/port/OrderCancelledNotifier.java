package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

import java.util.Map;

/**
 * Porta de saída para avisar a cliente de que seu pedido foi cancelado pela vendedora. A
 * implementação concreta (Resend) é selecionada por {@code notification.provider} via
 * {@code @ConditionalOnProperty}.
 *
 * O e-mail vai para {@code customer.getEmail()}, lista os itens cancelados (com imagem, para
 * contextualizar qual pedido foi cancelado) e traz um botão para falar com a vendedora pelo
 * WhatsApp, caso a cliente tenha dúvidas. O {@code imageUrls} traz a URL pública da imagem de
 * cada item indexada por {@link br.com.erp.api.order.domain.entity.OrderItem#getId()}.
 */
public interface OrderCancelledNotifier {

    void notify(Order order, Customer customer, Map<Long, String> imageUrls) throws Exception;
}

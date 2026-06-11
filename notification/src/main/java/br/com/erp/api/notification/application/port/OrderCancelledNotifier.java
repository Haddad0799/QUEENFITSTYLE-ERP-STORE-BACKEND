package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

/**
 * Porta de saída para avisar a cliente de que seu pedido foi cancelado pela vendedora. A
 * implementação concreta (Resend) é selecionada por {@code notification.provider} via
 * {@code @ConditionalOnProperty}.
 *
 * O e-mail vai para {@code customer.getEmail()} e traz um botão para falar com a vendedora
 * pelo WhatsApp, caso a cliente tenha dúvidas.
 */
public interface OrderCancelledNotifier {

    void notify(Order order, Customer customer) throws Exception;
}

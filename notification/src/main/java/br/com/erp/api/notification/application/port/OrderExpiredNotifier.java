package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

/**
 * Porta de saída para avisar a cliente de que seu pedido expirou (não confirmado dentro do
 * prazo) e foi cancelado automaticamente. A implementação concreta (Resend) é selecionada
 * por {@code notification.provider} via {@code @ConditionalOnProperty}.
 *
 * O e-mail vai para {@code customer.getEmail()} e convida a cliente a voltar à loja para
 * fazer um novo pedido.
 */
public interface OrderExpiredNotifier {

    void notify(Order order, Customer customer) throws Exception;
}

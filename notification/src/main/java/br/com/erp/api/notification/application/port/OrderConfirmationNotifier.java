package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

import java.util.Map;

/**
 * Porta de saída para notificar o cliente sobre a confirmação de um pedido.
 * A implementação concreta (Resend em produção, JavaMail localmente) é selecionada
 * por {@code notification.provider} via {@code @ConditionalOnProperty}.
 */
public interface OrderConfirmationNotifier {

    void notify(Order order, Customer customer, Map<Long, String> imageUrls) throws Exception;
}

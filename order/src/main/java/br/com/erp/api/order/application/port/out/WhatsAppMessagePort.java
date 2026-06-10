package br.com.erp.api.order.application.port.out;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

/**
 * Porta de saída para construção da URL/mensagem de WhatsApp do checkout (pré-pagamento).
 * A implementação vive no módulo notification, dono dessa responsabilidade.
 */
public interface WhatsAppMessagePort {

    String buildUrl(Order order, Customer customer);

    String buildMessageText(Order order, Customer customer);
}

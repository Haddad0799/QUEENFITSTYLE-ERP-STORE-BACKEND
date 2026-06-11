package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

/**
 * Porta de saída para notificar a dona da loja de que um novo pedido foi criado e
 * aguarda resposta pelo WhatsApp. A implementação concreta (Resend) é selecionada por
 * {@code notification.provider} via {@code @ConditionalOnProperty}.
 *
 * Os dados necessários ao e-mail vêm do {@code order} (número, itens com quantidade e
 * valor, total), do {@code customer} (nome, cidade) e do {@code whatsappUrl} (a conversa
 * já montada no checkout).
 */
public interface OrderCreatedNotifier {

    void notify(Order order, Customer customer, String whatsappUrl) throws Exception;
}

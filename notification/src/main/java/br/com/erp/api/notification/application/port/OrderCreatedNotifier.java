package br.com.erp.api.notification.application.port;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;

import java.util.Map;

/**
 * Porta de saída para notificar a dona da loja de que um novo pedido foi criado e
 * aguarda resposta pelo WhatsApp. A implementação concreta (Resend) é selecionada por
 * {@code notification.provider} via {@code @ConditionalOnProperty}.
 *
 * Os dados necessários ao e-mail vêm do {@code order} (número, itens com quantidade e
 * valor, total), do {@code customer} (nome, cidade), do {@code customerPhone} — usado para
 * montar o link {@code wa.me} do botão, que abre a conversa direta com a cliente — e do
 * {@code imageUrls}, com a URL pública da imagem de cada item indexada por
 * {@link br.com.erp.api.order.domain.entity.OrderItem#getId()}.
 */
public interface OrderCreatedNotifier {

    void notify(Order order, Customer customer, String customerPhone,
                Map<Long, String> imageUrls) throws Exception;
}

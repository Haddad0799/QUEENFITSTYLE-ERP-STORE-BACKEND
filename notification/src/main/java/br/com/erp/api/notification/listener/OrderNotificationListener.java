package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderConfirmationNotifier;
import br.com.erp.api.order.application.event.OrderConfirmedEvent;
import br.com.erp.api.order.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.order.application.query.OrderAdminQueryRepository;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * Dispara a notificação de confirmação de forma assíncrona após o commit do pagamento.
 *
 * Resolve as imagens dos produtos pela mesma via de leitura usada pela API admin
 * ({@link OrderAdminQueryRepository#findItemsByOrderId} + {@link ImageUrlResolverPort}),
 * já que o domínio {@code OrderItem} não carrega image_key.
 *
 * Falha no envio é logada como erro e NÃO propaga: a confirmação do pagamento
 * (estoque + status) já está consolidada no banco quando este listener executa.
 */
@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final OrderConfirmationNotifier notifier;
    private final CustomerRepositoryPort customerRepository;
    private final OrderAdminQueryRepository orderQueryRepository;
    private final ImageUrlResolverPort imageUrlResolver;

    public OrderNotificationListener(OrderConfirmationNotifier notifier,
                                     CustomerRepositoryPort customerRepository,
                                     OrderAdminQueryRepository orderQueryRepository,
                                     ImageUrlResolverPort imageUrlResolver) {
        this.notifier             = notifier;
        this.customerRepository   = customerRepository;
        this.orderQueryRepository = orderQueryRepository;
        this.imageUrlResolver     = imageUrlResolver;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        Order order = event.order();
        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            notifier.notify(order, customer, resolveImageUrls(order.getId()));
            log.info("Notificação de confirmação do pedido #{} enviada para {}", order.getId(), customer.getEmail());
        } catch (Exception e) {
            log.error("FALHA EMAIL pedido #{} — {}", order.getId(), e.getMessage());
            log.error("Stacktrace completo:", e);
        }
    }

    /**
     * Mapeia o id do item do pedido para a URL pública de sua imagem (cor da SKU,
     * com fallback para a imagem principal do produto — vide a query). Valores nulos
     * são preservados: o template decide o placeholder.
     */
    private Map<Long, String> resolveImageUrls(Long orderId) {
        List<OrderItemRow> rows = orderQueryRepository.findItemsByOrderId(orderId);
        Map<Long, String> urls = new HashMap<>();
        for (OrderItemRow row : rows) {
            urls.put(row.itemId(), imageUrlResolver.resolve(row.imageKey()));
        }
        return urls;
    }
}

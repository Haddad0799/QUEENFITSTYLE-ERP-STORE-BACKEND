package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderCancelledNotifier;
import br.com.erp.api.notification.application.service.OrderItemImageResolver;
import br.com.erp.api.order.application.event.OrderCancelledEvent;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * Avisa a cliente, de forma assíncrona após o commit, que seu pedido foi cancelado pela
 * vendedora.
 *
 * O notifier é injetado via {@link ObjectProvider} porque hoje só o provider {@code resend}
 * implementa a porta: sob outros providers (ex.: {@code javamail} no ambiente local) nenhum
 * bean existe e a notificação simplesmente não é enviada — sem quebrar o startup.
 *
 * Falha no envio é logada como erro e NÃO propaga: o cancelamento (estoque liberado + status)
 * já está consolidado no banco quando este listener executa.
 */
@Component
public class OrderCancelledNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledNotificationListener.class);

    private final ObjectProvider<OrderCancelledNotifier> notifierProvider;
    private final CustomerRepositoryPort customerRepository;
    private final OrderItemImageResolver imageResolver;

    public OrderCancelledNotificationListener(ObjectProvider<OrderCancelledNotifier> notifierProvider,
                                              CustomerRepositoryPort customerRepository,
                                              OrderItemImageResolver imageResolver) {
        this.notifierProvider   = notifierProvider;
        this.customerRepository = customerRepository;
        this.imageResolver      = imageResolver;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = event.order();

        OrderCancelledNotifier notifier = notifierProvider.getIfAvailable();
        if (notifier == null) {
            log.debug("Nenhum OrderCancelledNotifier configurado (provider != resend) — "
                    + "notificação de cancelamento do pedido #{} ignorada", order.getId());
            return;
        }

        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            notifier.notify(order, customer, imageResolver.resolveByItemId(order.getId()));
            log.info("Notificação de cancelamento do pedido #{} enviada para {}", order.getId(), customer.getEmail());
        } catch (Exception e) {
            log.error("FALHA EMAIL cancelamento pedido #{} — {}", order.getId(), e.getMessage());
            log.error("Stacktrace completo:", e);
        }
    }
}

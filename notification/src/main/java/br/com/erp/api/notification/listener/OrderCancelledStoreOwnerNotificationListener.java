package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderCancelledStoreOwnerNotifier;
import br.com.erp.api.notification.application.service.OrderItemImageResolver;
import br.com.erp.api.order.application.event.OrderCancelledEvent;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.CancellationOrigin;
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
 * Avisa a dona da loja, de forma assíncrona após o commit, que um pedido foi cancelado pela
 * própria cliente no e-commerce. Cancelamentos feitos pela vendedora no ERP NÃO disparam este
 * aviso — o filtro é a origem do {@link OrderCancelledEvent}
 * ({@link CancellationOrigin#CUSTOMER}).
 *
 * O notifier é injetado via {@link ObjectProvider} porque hoje só o provider {@code resend}
 * implementa a porta: sob outros providers (ex.: {@code javamail} no ambiente local) nenhum
 * bean existe e a notificação simplesmente não é enviada — sem quebrar o startup.
 *
 * Falha no envio é logada como erro e NÃO propaga: o cancelamento (estoque liberado + status)
 * já está consolidado no banco quando este listener executa.
 */
@Component
public class OrderCancelledStoreOwnerNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledStoreOwnerNotificationListener.class);

    private final ObjectProvider<OrderCancelledStoreOwnerNotifier> notifierProvider;
    private final CustomerRepositoryPort customerRepository;
    private final OrderItemImageResolver imageResolver;

    public OrderCancelledStoreOwnerNotificationListener(
            ObjectProvider<OrderCancelledStoreOwnerNotifier> notifierProvider,
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

        // Só avisa a dona quando o cancelamento parte da cliente; cancelamento da vendedora no
        // ERP é ação dela própria e não precisa de aviso.
        if (event.origin() != CancellationOrigin.CUSTOMER) {
            log.debug("Pedido #{} cancelado pela vendedora — aviso à dona não se aplica", order.getId());
            return;
        }

        OrderCancelledStoreOwnerNotifier notifier = notifierProvider.getIfAvailable();
        if (notifier == null) {
            log.debug("Nenhum OrderCancelledStoreOwnerNotifier configurado (provider != resend) — "
                    + "aviso de cancelamento pela cliente do pedido #{} ignorado", order.getId());
            return;
        }

        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            notifier.notify(order, customer, imageResolver.resolveByItemId(order.getId()));
            log.info("Aviso de cancelamento pela cliente do pedido #{} enviado para a dona da loja", order.getId());
        } catch (Exception e) {
            log.error("FALHA EMAIL aviso à dona — cancelamento pela cliente do pedido #{} — {}",
                    order.getId(), e.getMessage());
            log.error("Stacktrace completo:", e);
        }
    }
}

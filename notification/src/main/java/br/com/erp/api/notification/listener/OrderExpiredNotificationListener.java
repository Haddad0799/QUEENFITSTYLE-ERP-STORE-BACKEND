package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderExpiredNotifier;
import br.com.erp.api.order.application.event.OrderExpiredEvent;
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
 * Avisa a cliente, de forma assíncrona após o commit, que seu pedido expirou e foi
 * cancelado automaticamente.
 *
 * O notifier é injetado via {@link ObjectProvider} porque hoje só o provider {@code resend}
 * implementa a porta: sob outros providers (ex.: {@code javamail} no ambiente local) nenhum
 * bean existe e a notificação simplesmente não é enviada — sem quebrar o startup.
 *
 * Falha no envio é logada como erro e NÃO propaga: a expiração (estoque liberado + status)
 * já está consolidada no banco quando este listener executa.
 */
@Component
public class OrderExpiredNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiredNotificationListener.class);

    private final ObjectProvider<OrderExpiredNotifier> notifierProvider;
    private final CustomerRepositoryPort customerRepository;

    public OrderExpiredNotificationListener(ObjectProvider<OrderExpiredNotifier> notifierProvider,
                                            CustomerRepositoryPort customerRepository) {
        this.notifierProvider   = notifierProvider;
        this.customerRepository = customerRepository;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOrderExpired(OrderExpiredEvent event) {
        Order order = event.order();

        OrderExpiredNotifier notifier = notifierProvider.getIfAvailable();
        if (notifier == null) {
            log.debug("Nenhum OrderExpiredNotifier configurado (provider != resend) — "
                    + "notificação de expiração do pedido #{} ignorada", order.getId());
            return;
        }

        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            notifier.notify(order, customer);
            log.info("Notificação de expiração do pedido #{} enviada para {}", order.getId(), customer.getEmail());
        } catch (Exception e) {
            log.error("FALHA EMAIL expiração pedido #{} — {}", order.getId(), e.getMessage());
            log.error("Stacktrace completo:", e);
        }
    }
}

package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderCreatedNotifier;
import br.com.erp.api.order.application.event.OrderCreatedEvent;
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
 * Dispara a notificação de novo pedido para a dona da loja, de forma assíncrona após o
 * commit do checkout.
 *
 * O notifier é injetado via {@link ObjectProvider} porque hoje só o provider {@code resend}
 * implementa a porta: sob outros providers (ex.: {@code javamail} no ambiente local) nenhum
 * bean existe e a notificação simplesmente não é enviada — sem quebrar o startup.
 *
 * Falha no envio é logada como erro e NÃO propaga: o pedido já está consolidado no banco
 * quando este listener executa.
 */
@Component
public class OrderCreatedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedNotificationListener.class);

    private final ObjectProvider<OrderCreatedNotifier> notifierProvider;
    private final CustomerRepositoryPort customerRepository;

    public OrderCreatedNotificationListener(ObjectProvider<OrderCreatedNotifier> notifierProvider,
                                            CustomerRepositoryPort customerRepository) {
        this.notifierProvider   = notifierProvider;
        this.customerRepository = customerRepository;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOrderCreated(OrderCreatedEvent event) {
        Order order = event.order();

        OrderCreatedNotifier notifier = notifierProvider.getIfAvailable();
        if (notifier == null) {
            log.debug("Nenhum OrderCreatedNotifier configurado (provider != resend) — "
                    + "notificação do novo pedido #{} ignorada", order.getId());
            return;
        }

        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            notifier.notify(order, customer, event.customerPhone());
            log.info("Notificação de novo pedido #{} enviada para a dona da loja", order.getId());
        } catch (Exception e) {
            log.error("FALHA EMAIL novo pedido #{} — {}", order.getId(), e.getMessage());
            log.error("Stacktrace completo:", e);
        }
    }
}

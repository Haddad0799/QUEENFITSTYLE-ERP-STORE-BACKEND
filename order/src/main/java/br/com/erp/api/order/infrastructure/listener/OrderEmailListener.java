package br.com.erp.api.order.infrastructure.listener;

import br.com.erp.api.order.application.event.OrderConfirmedEvent;
import br.com.erp.api.order.application.service.OrderEmailService;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * Dispara o e-mail de confirmação de forma assíncrona após o commit do pagamento.
 *
 * Falha no envio é logada como aviso e NÃO propaga: a confirmação do pagamento
 * (estoque + status) já está consolidada no banco quando este listener executa.
 */
@Component
public class OrderEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEmailListener.class);

    private final OrderEmailService orderEmailService;
    private final CustomerRepositoryPort customerRepository;

    public OrderEmailListener(OrderEmailService orderEmailService,
                              CustomerRepositoryPort customerRepository) {
        this.orderEmailService  = orderEmailService;
        this.customerRepository = customerRepository;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        Order order = event.order();
        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            orderEmailService.sendConfirmation(order, customer);
            log.info("E-mail de confirmação do pedido #{} enviado para {}", order.getId(), customer.getEmail());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de confirmação do pedido #{}: {}", order.getId(), e.getMessage(), e);
        }
    }
}

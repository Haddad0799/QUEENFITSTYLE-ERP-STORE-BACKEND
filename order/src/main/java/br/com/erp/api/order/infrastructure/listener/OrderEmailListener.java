package br.com.erp.api.order.infrastructure.listener;

import br.com.erp.api.order.application.event.OrderConfirmedEvent;
import br.com.erp.api.order.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.order.application.query.OrderAdminQueryRepository;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.application.service.OrderEmailService;
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
 * Dispara o e-mail de confirmação de forma assíncrona após o commit do pagamento.
 *
 * Resolve as imagens dos produtos pela mesma via de leitura usada pela API admin
 * ({@link OrderAdminQueryRepository#findItemsByOrderId} + {@link ImageUrlResolverPort}),
 * já que o domínio {@code OrderItem} não carrega image_key.
 *
 * Falha no envio é logada como aviso e NÃO propaga: a confirmação do pagamento
 * (estoque + status) já está consolidada no banco quando este listener executa.
 */
@Component
public class OrderEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEmailListener.class);

    private final OrderEmailService orderEmailService;
    private final CustomerRepositoryPort customerRepository;
    private final OrderAdminQueryRepository orderQueryRepository;
    private final ImageUrlResolverPort imageUrlResolver;

    public OrderEmailListener(OrderEmailService orderEmailService,
                              CustomerRepositoryPort customerRepository,
                              OrderAdminQueryRepository orderQueryRepository,
                              ImageUrlResolverPort imageUrlResolver) {
        this.orderEmailService    = orderEmailService;
        this.customerRepository   = customerRepository;
        this.orderQueryRepository = orderQueryRepository;
        this.imageUrlResolver     = imageUrlResolver;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[DIAG] OrderEmailListener disparado para pedido #{}", event.order().getId());
        Order order = event.order();
        try {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cliente #" + order.getCustomerId() + " não encontrado para o pedido #" + order.getId()));
            orderEmailService.sendConfirmation(order, customer, resolveImageUrls(order.getId()));
            log.info("E-mail de confirmação do pedido #{} enviado para {}", order.getId(), customer.getEmail());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de confirmação do pedido #{}: {}", order.getId(), e.getMessage(), e);
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

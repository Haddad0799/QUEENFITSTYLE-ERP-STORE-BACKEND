package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job @Scheduled que varre pedidos vencidos em WAITING_SELLER_CONFIRMATION e os expira.
 * A expiração propriamente dita é delegada ao {@link ExpireOrderUseCase} para garantir
 * que cada pedido tenha suas reservas liberadas e timeline registrada — mesma garantia
 * usada pelo endpoint administrativo de expiração manual.
 */
@Service
public class ExpireOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpireOrdersUseCase.class);
    private static final String SYSTEM_ACTOR = "system";

    private final OrderRepositoryPort orderRepository;
    private final ExpireOrderUseCase expireOrderUseCase;

    public ExpireOrdersUseCase(OrderRepositoryPort orderRepository,
                               ExpireOrderUseCase expireOrderUseCase) {
        this.orderRepository    = orderRepository;
        this.expireOrderUseCase = expireOrderUseCase;
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void expireStaleOrders() {
        List<Order> expired = orderRepository.findExpiredPending(LocalDateTime.now());
        if (expired.isEmpty()) return;

        log.info("Expirando {} pedidos pendentes sem resposta", expired.size());
        for (Order order : expired) {
            try {
                expireOrderUseCase.expireOrder(order, SYSTEM_ACTOR);
            } catch (RuntimeException ex) {
                log.error("Falha ao expirar pedido #{}: {}", order.getId(), ex.getMessage(), ex);
            }
        }
    }
}

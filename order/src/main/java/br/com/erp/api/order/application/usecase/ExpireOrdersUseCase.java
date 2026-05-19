package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpireOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpireOrdersUseCase.class);

    private final OrderRepositoryPort orderRepository;

    public ExpireOrdersUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void expireStaleOrders() {
        List<Order> expired = orderRepository.findExpiredPending(LocalDateTime.now());
        if (expired.isEmpty()) return;

        log.info("Expirando {} pedidos pendentes sem resposta", expired.size());
        for (Order order : expired) {
            order.expire();
            orderRepository.updateStatus(order.getId(), order.getStatus());
            log.debug("Pedido #{} expirado", order.getId());
        }
    }
}

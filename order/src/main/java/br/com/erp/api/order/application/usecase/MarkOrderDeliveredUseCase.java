package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderTimelineEvent;
import br.com.erp.api.order.domain.enumerated.OrderEventType;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.domain.exception.InvalidOrderStateTransitionException;
import br.com.erp.api.order.domain.exception.OrderNotFoundException;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import br.com.erp.api.order.domain.port.OrderTimelineRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca um pedido pago como entregue (operação manual). Não altera estoque.
 *
 * Idempotente: pedido já DELIVERED é no-op. Só transita de PAID → DELIVERED.
 */
@Service
public class MarkOrderDeliveredUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkOrderDeliveredUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final OrderTimelineRepositoryPort timelineRepository;

    public MarkOrderDeliveredUseCase(OrderRepositoryPort orderRepository,
                                     OrderTimelineRepositoryPort timelineRepository) {
        this.orderRepository    = orderRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public Order execute(Long orderId, String actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.info("Pedido #{} já estava DELIVERED — operação ignorada (idempotência)", orderId);
            return order;
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "markDelivered");
        }

        order.markDelivered();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.DELIVERED,
                "Pedido entregue",
                null,
                actor
        ));

        log.info("Pedido #{} entregue por '{}'", orderId, actor);
        return order;
    }
}

package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.application.port.out.ReservationLifecyclePort;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
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

import java.util.UUID;

/**
 * Expira pedidos em WAITING_SELLER_CONFIRMATION e libera as reservas.
 *
 * Usado por:
 *   - {@code ExpireOrdersUseCase} (job @Scheduled), passando cada pedido encontrado
 *   - endpoint POST /erp/orders/{id}/expire (forçar expiração manual)
 *
 * Idempotência: pedido já EXPIRED é no-op.
 */
@Service
public class ExpireOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpireOrderUseCase.class);
    private static final String SYSTEM_ACTOR = "system";

    private final OrderRepositoryPort orderRepository;
    private final ReservationLifecyclePort reservationLifecycle;
    private final OrderTimelineRepositoryPort timelineRepository;

    public ExpireOrderUseCase(OrderRepositoryPort orderRepository,
                              ReservationLifecyclePort reservationLifecycle,
                              OrderTimelineRepositoryPort timelineRepository) {
        this.orderRepository      = orderRepository;
        this.reservationLifecycle = reservationLifecycle;
        this.timelineRepository   = timelineRepository;
    }

    @Transactional
    public Order execute(Long orderId, String actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return expireOrder(order, actor);
    }

    @Transactional
    public Order expireOrder(Order order, String actor) {
        if (order.getStatus() == OrderStatus.EXPIRED) {
            log.info("Pedido #{} já estava EXPIRED — operação ignorada (idempotência)", order.getId());
            return order;
        }

        if (order.getStatus() != OrderStatus.WAITING_SELLER_CONFIRMATION) {
            throw new InvalidOrderStateTransitionException(order.getId(), order.getStatus(), "expire");
        }

        releaseReservations(order, actor != null ? actor : SYSTEM_ACTOR);

        order.expire();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.EXPIRED,
                "Pedido expirou sem confirmação",
                null,
                actor != null ? actor : SYSTEM_ACTOR
        ));

        log.info("Pedido #{} expirado por '{}'", order.getId(), actor);
        return order;
    }

    private void releaseReservations(Order order, String actor) {
        for (OrderItem item : order.getItems()) {
            UUID reservationId = item.getReservationId();
            boolean released = reservationLifecycle.release(reservationId);
            log.debug("Reserva {} para pedido #{} → liberada={}", reservationId, order.getId(), released);
        }

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.RESERVATIONS_RELEASED,
                "Reservas liberadas — pedido expirado",
                buildReservationsPayload(order),
                actor
        ));
    }

    private String buildReservationsPayload(Order order) {
        StringBuilder sb = new StringBuilder("{\"reservationIds\":[");
        for (int i = 0; i < order.getItems().size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(order.getItems().get(i).getReservationId()).append('"');
        }
        sb.append("]}");
        return sb.toString();
    }
}

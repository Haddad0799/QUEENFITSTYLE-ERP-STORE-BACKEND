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
 * Marca um pedido pago/entregue como devolvido, repondo o estoque já consumido.
 *
 * Diferente do cancelamento (que libera reservas ainda RESERVED), aqui as reservas estão
 * CONFIRMED — o estoque físico já foi baixado — então a devolução usa {@code returnStock},
 * que reincrementa a quantidade.
 *
 * Idempotente: pedido já RETURNED é no-op. Só transita de PAID/DELIVERED → RETURNED.
 */
@Service
public class MarkOrderReturnedUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkOrderReturnedUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final ReservationLifecyclePort reservationLifecycle;
    private final OrderTimelineRepositoryPort timelineRepository;

    public MarkOrderReturnedUseCase(OrderRepositoryPort orderRepository,
                                    ReservationLifecyclePort reservationLifecycle,
                                    OrderTimelineRepositoryPort timelineRepository) {
        this.orderRepository      = orderRepository;
        this.reservationLifecycle = reservationLifecycle;
        this.timelineRepository   = timelineRepository;
    }

    @Transactional
    public Order execute(Long orderId, String reason, String actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            log.info("Pedido #{} já estava RETURNED — operação ignorada (idempotência)", orderId);
            return order;
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.DELIVERED) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "markReturned");
        }

        returnReservations(order, actor);

        order.markReturned();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.RETURNED,
                reason != null && !reason.isBlank()
                        ? "Pedido devolvido: " + reason
                        : "Pedido devolvido",
                null,
                actor
        ));

        log.info("Pedido #{} devolvido por '{}'", orderId, actor);
        return order;
    }

    private void returnReservations(Order order, String actor) {
        for (OrderItem item : order.getItems()) {
            UUID reservationId = item.getReservationId();
            boolean returned = reservationLifecycle.returnStock(reservationId);
            log.debug("Reserva {} para pedido #{} → devolvida={}", reservationId, order.getId(), returned);
        }

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.RESERVATIONS_RETURNED,
                "Reservas devolvidas — estoque reposto",
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

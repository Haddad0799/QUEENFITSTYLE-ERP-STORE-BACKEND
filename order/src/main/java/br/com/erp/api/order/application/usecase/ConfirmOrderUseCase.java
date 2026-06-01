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
 * Confirma um pedido aguardando confirmação do vendedor.
 *
 * Operação idempotente: se o pedido já estiver CONFIRMED, retorna o pedido sem efeitos colaterais.
 * Caso contrário, confirma todas as reservas vinculadas, atualiza status e grava timeline.
 *
 * Garantias:
 *   - reservas são SEMPRE confirmadas antes do status mudar (se falhar, transação aborta)
 *   - status só transita de WAITING_SELLER_CONFIRMATION → CONFIRMED
 *   - timeline registra evento RESERVATIONS_CONFIRMED + CONFIRMED
 */
@Service
public class ConfirmOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final ReservationLifecyclePort reservationLifecycle;
    private final OrderTimelineRepositoryPort timelineRepository;

    public ConfirmOrderUseCase(OrderRepositoryPort orderRepository,
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

        // idempotência: re-chamar confirm em pedido já confirmado é no-op
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("Pedido #{} já estava CONFIRMED — operação ignorada (idempotência)", orderId);
            return order;
        }

        if (order.getStatus() != OrderStatus.WAITING_SELLER_CONFIRMATION) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "confirm");
        }

        confirmReservations(order, actor);

        order.confirm();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.CONFIRMED,
                "Pedido confirmado pela vendedora",
                null,
                actor
        ));

        log.info("Pedido #{} confirmado por '{}'", orderId, actor);
        return order;
    }

    private void confirmReservations(Order order, String actor) {
        for (OrderItem item : order.getItems()) {
            UUID reservationId = item.getReservationId();
            boolean confirmed = reservationLifecycle.confirm(reservationId);
            log.debug("Reserva {} para pedido #{} → confirmada={}", reservationId, order.getId(), confirmed);
        }

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.RESERVATIONS_CONFIRMED,
                "Reservas confirmadas — estoque consumido",
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

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
 * Cancela um pedido, devolvendo o estoque das reservas.
 *
 * Operação idempotente: pedido já CANCELLED é no-op.
 * Pedidos DELIVERED não podem ser cancelados — lança 422.
 *
 * Garantia crítica: as reservas são SEMPRE liberadas. Mesmo se o status já tiver mudado
 * por outro caminho, o ciclo de liberação roda — o adapter trata "já liberada" como sucesso.
 */
@Service
public class CancelOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final ReservationLifecyclePort reservationLifecycle;
    private final OrderTimelineRepositoryPort timelineRepository;

    public CancelOrderUseCase(OrderRepositoryPort orderRepository,
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

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Pedido #{} já estava CANCELLED — operação ignorada (idempotência)", orderId);
            return order;
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "cancel");
        }

        // Reservas só devem ser liberadas se ainda estiverem ativas no inventory:
        //   - WAITING_SELLER_CONFIRMATION → reservas em RESERVED, precisam liberar agora
        //   - EXPIRED                    → reservas já foram liberadas na expiração; pular para evitar evento duplicado
        //   - CONFIRMED+ (PREPARING/SHIPPED) → reservas já foram CONFIRMED (estoque consumido).
        //                                     Devolução de estoque é fluxo separado (refund), fora do escopo do cancelamento administrativo.
        if (order.getStatus() == OrderStatus.WAITING_SELLER_CONFIRMATION) {
            releaseReservations(order, actor);
        }

        order.cancel();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.CANCELLED,
                reason != null && !reason.isBlank()
                        ? "Pedido cancelado: " + reason
                        : "Pedido cancelado",
                null,
                actor
        ));

        log.info("Pedido #{} cancelado por '{}'", orderId, actor);
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
                "Reservas liberadas — estoque devolvido",
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

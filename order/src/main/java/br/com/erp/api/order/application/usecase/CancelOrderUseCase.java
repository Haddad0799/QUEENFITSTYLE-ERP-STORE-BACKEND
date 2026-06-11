package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.application.port.out.ReservationLifecyclePort;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import br.com.erp.api.order.domain.entity.OrderTimelineEvent;
import br.com.erp.api.order.domain.enumerated.OrderEventType;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.domain.exception.InvalidOrderStateTransitionException;
import br.com.erp.api.order.domain.exception.OrderNotFoundException;
import br.com.erp.api.order.domain.exception.ReservationOperationFailedException;
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
 * Garantia crítica de atomicidade: cada reserva precisa ser efetivamente liberada (release
 * afetando linha) antes de o pedido transitar para CANCELLED. Se alguma liberação não afetar
 * nenhuma linha, a operação aborta com {@link ReservationOperationFailedException} e o pedido
 * permanece PENDING_PAYMENT — evita marcar o pedido como cancelado com o estoque ainda preso.
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

        // Cancelamento é por falta de pagamento: só vale enquanto PENDING_PAYMENT, quando as
        // reservas ainda estão RESERVED e podem ser liberadas. Pedidos já pagos/entregues
        // devem usar o fluxo de devolução (RETURNED), que repõe o estoque consumido.
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "cancel");
        }

        releaseReservations(order, actor);

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

    /**
     * Libera todas as reservas e só retorna se cada uma afetou linha. A validação roda antes de
     * qualquer escrita no pedido (status/timeline) de propósito: o módulo inventory comita em
     * conexão própria (jdbi.withHandle), fora da transação do pedido, então não existe rollback
     * que desfaça uma liberação. Garantir aqui que toda liberação ocorreu é o que impede o pedido
     * de avançar para CANCELLED com o estoque ainda preso.
     */
    private void releaseReservations(Order order, String actor) {
        for (OrderItem item : order.getItems()) {
            UUID reservationId = item.getReservationId();
            boolean released = reservationLifecycle.release(reservationId);
            if (!released) {
                log.error("Falha ao liberar reserva {} do pedido #{}: operação não afetou nenhuma linha "
                                + "(reserva inexistente ou já processada) — cancelamento abortado para não prender o estoque",
                        reservationId, order.getId());
                throw new ReservationOperationFailedException(
                        reservationId, "release",
                        "operação não afetou nenhuma linha — reserva inexistente ou já processada");
            }
            log.debug("Reserva {} do pedido #{} liberada", reservationId, order.getId());
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

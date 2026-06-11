package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.application.event.OrderConfirmedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Confirma o pagamento de um pedido aguardando pagamento.
 *
 * Operação idempotente: se o pedido já estiver PAID, retorna o pedido sem efeitos colaterais.
 * Caso contrário, confirma todas as reservas vinculadas (consome estoque), atualiza status e grava timeline.
 *
 * Garantias:
 *   - cada reserva precisa ser efetivamente confirmada (afetando linha) antes de o status mudar;
 *     se alguma não afetar linha, aborta com {@link ReservationOperationFailedException} e o
 *     pedido permanece PENDING_PAYMENT — não é marcado como pago com estoque não consumido
 *   - status só transita de PENDING_PAYMENT → PAID
 *   - timeline registra evento RESERVATIONS_CONFIRMED + PAID
 */
@Service
public class ConfirmOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final ReservationLifecyclePort reservationLifecycle;
    private final OrderTimelineRepositoryPort timelineRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmOrderUseCase(OrderRepositoryPort orderRepository,
                               ReservationLifecyclePort reservationLifecycle,
                               OrderTimelineRepositoryPort timelineRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.orderRepository      = orderRepository;
        this.reservationLifecycle = reservationLifecycle;
        this.timelineRepository   = timelineRepository;
        this.eventPublisher       = eventPublisher;
    }

    @Transactional
    public Order execute(Long orderId, String actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // idempotência: re-chamar em pedido já pago é no-op
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Pedido #{} já estava PAID — operação ignorada (idempotência)", orderId);
            return order;
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateTransitionException(orderId, order.getStatus(), "markPaid");
        }

        confirmReservations(order, actor);

        order.markPaid();
        orderRepository.updateStatus(order.getId(), order.getStatus());

        timelineRepository.append(OrderTimelineEvent.create(
                order.getId(),
                OrderEventType.PAID,
                "Pagamento confirmado pela vendedora",
                null,
                actor
        ));

        log.info("Pedido #{} marcado como pago por '{}'", orderId, actor);

        // efeitos colaterais (e-mail de confirmação) são tratados por listeners após o commit
        eventPublisher.publishEvent(new OrderConfirmedEvent(order));

        return order;
    }

    /**
     * Confirma todas as reservas e só retorna se cada uma afetou linha. A validação roda antes de
     * qualquer escrita no pedido (status/timeline) de propósito: o módulo inventory comita em
     * conexão própria (jdbi.withHandle), fora da transação do pedido, então não existe rollback
     * que desfaça um consumo de estoque. Garantir aqui que toda confirmação ocorreu é o que impede
     * o pedido de ser marcado como pago sem o estoque ter sido consumido.
     */
    private void confirmReservations(Order order, String actor) {
        for (OrderItem item : order.getItems()) {
            UUID reservationId = item.getReservationId();
            boolean confirmed = reservationLifecycle.confirm(reservationId);
            if (!confirmed) {
                log.error("Falha ao confirmar reserva {} do pedido #{}: operação não afetou nenhuma linha "
                                + "(reserva inexistente ou já processada) — confirmação abortada para não marcar o pedido pago sem consumir o estoque",
                        reservationId, order.getId());
                throw new ReservationOperationFailedException(
                        reservationId, "confirm",
                        "operação não afetou nenhuma linha — reserva inexistente ou já processada");
            }
            log.debug("Reserva {} do pedido #{} confirmada", reservationId, order.getId());
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

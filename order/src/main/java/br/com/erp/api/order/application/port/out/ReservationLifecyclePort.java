package br.com.erp.api.order.application.port.out;

import java.util.UUID;

/**
 * Porta de saída para operações de ciclo de vida de reservas pertencentes a um pedido.
 * Implementada por adapter que delega ao módulo inventory, mantendo a separação modular.
 *
 * Operações DEVEM ser idempotentes: chamar confirm/release sobre reserva já confirmada/liberada
 * não pode lançar exceção — apenas retornar o resultado (true se efetivou agora, false se já estava).
 */
public interface ReservationLifecyclePort {

    /**
     * Confirma a reserva: consome estoque e marca como CONFIRMED.
     * @return true se a reserva foi confirmada agora; false se já estava confirmada (idempotência).
     */
    boolean confirm(UUID reservationId);

    /**
     * Libera a reserva ainda RESERVED: devolve o reservado e marca como CANCELLED.
     * @return true se a reserva foi liberada agora; false se já estava liberada (idempotência).
     */
    boolean release(UUID reservationId);

    /**
     * Devolve ao estoque uma reserva já CONFIRMED (estoque físico já consumido): repõe a
     * quantidade e marca como RETURNED. Usado no fluxo de devolução de pedido pago/entregue.
     * @return true se devolvida agora; false se não estava confirmada (idempotência).
     */
    boolean returnStock(UUID reservationId);
}

package br.com.erp.api.order.infrastructure.adapter;

import br.com.erp.api.inventory.application.usecase.ConfirmReservationUseCase;
import br.com.erp.api.inventory.application.usecase.ReleaseReservationUseCase;
import br.com.erp.api.inventory.application.usecase.ReturnReservationUseCase;
import br.com.erp.api.inventory.domain.exception.ReservationNotFoundException;
import br.com.erp.api.order.application.port.out.ReservationLifecyclePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Delegação para os use cases do módulo inventory.
 *
 * Contrato do retorno booleano: {@code true} quando a operação afetou a reserva; {@code false}
 * quando o inventory dispara {@link ReservationNotFoundException} — reserva inexistente OU já
 * processada (nenhuma linha afetada). Cabe a cada use case de order decidir o que um {@code false}
 * significa: os fluxos de confirmação/expiração/cancelamento/devolução o tratam como falha de
 * atomicidade (abortam antes de mexer no status), pois a idempotência de chamadas repetidas já é
 * garantida pelos guard-rails de status do próprio pedido — quando o pedido já está em estado
 * terminal, o release/confirm sequer é chamado.
 */
@Component
public class ReservationLifecycleAdapter implements ReservationLifecyclePort {

    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final ReleaseReservationUseCase releaseReservationUseCase;
    private final ReturnReservationUseCase returnReservationUseCase;

    public ReservationLifecycleAdapter(ConfirmReservationUseCase confirmReservationUseCase,
                                       ReleaseReservationUseCase releaseReservationUseCase,
                                       ReturnReservationUseCase returnReservationUseCase) {
        this.confirmReservationUseCase = confirmReservationUseCase;
        this.releaseReservationUseCase = releaseReservationUseCase;
        this.returnReservationUseCase  = returnReservationUseCase;
    }

    @Override
    public boolean confirm(UUID reservationId) {
        try {
            confirmReservationUseCase.confirm(reservationId);
            return true;
        } catch (ReservationNotFoundException ex) {
            return false;
        }
    }

    @Override
    public boolean release(UUID reservationId) {
        try {
            releaseReservationUseCase.release(reservationId);
            return true;
        } catch (ReservationNotFoundException ex) {
            return false;
        }
    }

    @Override
    public boolean returnStock(UUID reservationId) {
        try {
            returnReservationUseCase.returnStock(reservationId);
            return true;
        } catch (ReservationNotFoundException ex) {
            return false;
        }
    }
}

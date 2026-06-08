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
 * Idempotência: o módulo inventory dispara {@link ReservationNotFoundException} quando a reserva
 * não existe OU já foi processada. Aqui interpretamos "já processada" como sucesso silencioso
 * (retorno false) — assim chamadas repetidas pela vendedora não geram 404.
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

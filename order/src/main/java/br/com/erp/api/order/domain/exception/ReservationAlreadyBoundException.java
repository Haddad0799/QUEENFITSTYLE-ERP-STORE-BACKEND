package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

import java.util.UUID;

public class ReservationAlreadyBoundException extends DomainException {

    public ReservationAlreadyBoundException(UUID reservationId) {
        super("Reserva [%s] já está vinculada a um pedido ou não está ativa".formatted(reservationId));
    }
}

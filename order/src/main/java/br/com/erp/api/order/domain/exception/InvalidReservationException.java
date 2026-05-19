package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

import java.util.UUID;

public class InvalidReservationException extends DomainException {

    public InvalidReservationException(UUID reservationId, String reason) {
        super("Reserva inválida [%s]: %s".formatted(reservationId, reason));
    }
}

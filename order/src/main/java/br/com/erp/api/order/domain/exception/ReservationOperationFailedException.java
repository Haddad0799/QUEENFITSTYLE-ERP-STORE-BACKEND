package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

import java.util.UUID;

public class ReservationOperationFailedException extends DomainException {

    private final UUID reservationId;
    private final String operation;

    public ReservationOperationFailedException(UUID reservationId, String operation, String reason) {
        super("Falha ao executar '%s' na reserva [%s]: %s".formatted(operation, reservationId, reason));
        this.reservationId = reservationId;
        this.operation     = operation;
    }

    public UUID getReservationId() { return reservationId; }
    public String getOperation()   { return operation; }
}

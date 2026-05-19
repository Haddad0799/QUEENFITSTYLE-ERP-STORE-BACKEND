package br.com.erp.api.inventory.domain.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException() {
        super("Reserva não encontrada");
    }
}


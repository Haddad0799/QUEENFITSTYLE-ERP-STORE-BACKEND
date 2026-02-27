package br.com.erp.api.inventory.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InsufficientReservationException extends DomainException {
    public InsufficientReservationException(Long skuId, int requested, int reserved) {
        super("Reserva insuficiente para SKU " + skuId +
                ": solicitado=" + requested + ", reservado=" + reserved);
    }
}
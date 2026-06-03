package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidDeliveryAddressException extends DomainException {

    public InvalidDeliveryAddressException(String message) {
        super(message);
    }
}

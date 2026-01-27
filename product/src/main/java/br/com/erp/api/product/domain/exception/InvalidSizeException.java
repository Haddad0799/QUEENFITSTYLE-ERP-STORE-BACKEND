package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidSizeException extends DomainException {
    public InvalidSizeException(String message) {
        super(message);
    }
}

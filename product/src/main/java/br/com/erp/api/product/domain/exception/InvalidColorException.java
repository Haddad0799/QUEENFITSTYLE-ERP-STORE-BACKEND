package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidColorException extends DomainException {
    public InvalidColorException(String message) {
        super(message);
    }
}

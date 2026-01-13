package br.com.erp.api.catalog.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidNameException extends DomainException {
    public InvalidNameException(String message) {
        super(message);
    }
}

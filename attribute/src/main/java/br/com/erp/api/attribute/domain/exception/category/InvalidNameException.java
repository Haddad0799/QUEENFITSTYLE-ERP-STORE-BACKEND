package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidNameException extends DomainException {
    public InvalidNameException(String message) {
        super(message);
    }
}


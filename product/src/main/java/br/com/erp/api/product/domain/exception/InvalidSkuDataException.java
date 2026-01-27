package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidSkuDataException extends DomainException {
    public InvalidSkuDataException(String message) {
        super(message);
    }
}

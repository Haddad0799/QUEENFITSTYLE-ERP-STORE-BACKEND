package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class DuplicateSkuCombinationException extends DomainException {
    public DuplicateSkuCombinationException(String message) {
        super(message);
    }
}

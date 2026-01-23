package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidCategoryException extends DomainException {

    public InvalidCategoryException() {
        super("A categoria à qual o produto pertence não existe ou está inativa.");
    }
}

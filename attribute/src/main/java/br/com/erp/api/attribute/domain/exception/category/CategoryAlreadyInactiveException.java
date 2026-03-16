package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryAlreadyInactiveException extends DomainException {
    public CategoryAlreadyInactiveException() {
        super("Categoria já esta inativa.");
    }
}


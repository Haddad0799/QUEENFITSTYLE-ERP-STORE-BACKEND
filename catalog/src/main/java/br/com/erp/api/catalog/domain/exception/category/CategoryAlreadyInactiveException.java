package br.com.erp.api.catalog.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryAlreadyInactiveException extends DomainException {
    public CategoryAlreadyInactiveException() {
        super("Categoria já esta inativa.");
    }
}

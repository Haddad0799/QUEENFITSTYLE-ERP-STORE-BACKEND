package br.com.erp.api.catalog.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryAlreadyExistsException extends DomainException {
    public CategoryAlreadyExistsException() {
        super("Categoria já existente e ativa.");
    }
}

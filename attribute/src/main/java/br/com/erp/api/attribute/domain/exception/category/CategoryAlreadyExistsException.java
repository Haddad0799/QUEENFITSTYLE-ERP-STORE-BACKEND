package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryAlreadyExistsException extends DomainException {
    public CategoryAlreadyExistsException() {
        super("Categoria já existente e ativa.");
    }
}


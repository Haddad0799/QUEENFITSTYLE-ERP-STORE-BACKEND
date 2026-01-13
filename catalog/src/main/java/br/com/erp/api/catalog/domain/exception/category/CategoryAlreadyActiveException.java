package br.com.erp.api.catalog.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryAlreadyActiveException extends DomainException {
    public CategoryAlreadyActiveException() {
        super("Categoria já esta ativa");
    }
}

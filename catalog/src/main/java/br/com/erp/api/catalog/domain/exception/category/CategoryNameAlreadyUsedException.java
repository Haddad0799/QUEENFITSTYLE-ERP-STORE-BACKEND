package br.com.erp.api.catalog.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryNameAlreadyUsedException extends DomainException {
    public CategoryNameAlreadyUsedException() {
        super("O nome da categoria já está em uso.");
    }
}

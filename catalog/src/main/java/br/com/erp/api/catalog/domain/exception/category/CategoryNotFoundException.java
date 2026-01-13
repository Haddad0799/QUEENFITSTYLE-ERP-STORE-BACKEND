package br.com.erp.api.catalog.domain.exception.category;

import br.com.erp.api.shared.domain.exception.EntityNotFoundException;

public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(String entity, Object id) {
        super(entity, id);
    }
}

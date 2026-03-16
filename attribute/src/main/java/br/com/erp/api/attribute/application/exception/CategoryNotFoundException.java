package br.com.erp.api.attribute.application.exception;

import br.com.erp.api.shared.application.exception.EntityNotFoundException;

public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(String entity, Object id) {
        super(entity, id);
    }
}


package br.com.erp.api.product.application.exception;

import br.com.erp.api.shared.application.exception.EntityNotFoundException;

public class InvalidCategoryException extends EntityNotFoundException {
    public InvalidCategoryException(String entity, Object id) {
        super(entity, id);
    }
}

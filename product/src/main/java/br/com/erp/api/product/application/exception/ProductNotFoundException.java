package br.com.erp.api.product.application.exception;

import br.com.erp.api.shared.application.exception.EntityNotFoundException;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException(String entity, Object id) {
        super(entity, id);
    }
}

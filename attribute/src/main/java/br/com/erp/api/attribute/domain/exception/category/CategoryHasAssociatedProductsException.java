package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryHasAssociatedProductsException extends DomainException {
    public CategoryHasAssociatedProductsException(String categoryName) {
        super("Não é possível excluir a categoria '%s' pois existem produtos associados a ela.".formatted(categoryName));
    }
}


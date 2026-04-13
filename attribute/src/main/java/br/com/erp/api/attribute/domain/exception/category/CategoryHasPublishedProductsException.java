package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryHasPublishedProductsException extends DomainException {
    public CategoryHasPublishedProductsException(String categoryName) {
        super("Não é possível desativar a categoria '%s' pois existem produtos publicados associados a ela.".formatted(categoryName));
    }
}


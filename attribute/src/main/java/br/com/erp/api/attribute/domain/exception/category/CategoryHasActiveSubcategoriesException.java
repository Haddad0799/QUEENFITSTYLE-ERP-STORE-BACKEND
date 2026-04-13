package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryHasActiveSubcategoriesException extends DomainException {
    public CategoryHasActiveSubcategoriesException(String categoryName) {
        super("Não é possível desativar a categoria '%s' pois existem subcategorias ativas associadas a ela.".formatted(categoryName));
    }
}


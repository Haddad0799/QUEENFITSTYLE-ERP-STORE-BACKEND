package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class CategoryHasSubcategoriesException extends DomainException {
    public CategoryHasSubcategoriesException(String categoryName) {
        super("Não é possível excluir a categoria '%s' pois existem subcategorias associadas a ela.".formatted(categoryName));
    }
}


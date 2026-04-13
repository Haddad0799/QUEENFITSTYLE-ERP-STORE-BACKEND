package br.com.erp.api.attribute.domain.exception.category;

import br.com.erp.api.shared.domain.exception.DomainException;

public class SubcategoryDepthExceededException extends DomainException {
    public SubcategoryDepthExceededException() {
        super("Subcategoria não pode ser pai de outra categoria. Hierarquia máxima de 2 níveis.");
    }
}


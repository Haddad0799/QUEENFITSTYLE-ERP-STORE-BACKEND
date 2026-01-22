package br.com.erp.api.product.domain.valueobject;

public record CategoryId(Long value) {
    public CategoryId {
        if (value == null || value <= 0) {
            throw new InvalidCategoryIdException("categoria inválida.");
        }
    }
}

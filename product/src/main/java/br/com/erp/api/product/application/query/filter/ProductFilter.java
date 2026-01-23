package br.com.erp.api.product.application.query.filter;

public record ProductFilter(
        Boolean active,
        Long categoryId
) {
}

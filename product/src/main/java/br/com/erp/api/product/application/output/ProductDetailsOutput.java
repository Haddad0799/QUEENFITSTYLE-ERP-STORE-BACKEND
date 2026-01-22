package br.com.erp.api.product.application.output;

public record ProductDetailsOutput(
        Long id,
        String name,
        String slug,
        Long categoryId,
        boolean active
) {
}

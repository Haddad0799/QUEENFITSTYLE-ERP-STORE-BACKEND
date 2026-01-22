package br.com.erp.api.product.application.command;

public record CreateProductCommand(
        String name,
        String description,
        Long categoryId
) {
}

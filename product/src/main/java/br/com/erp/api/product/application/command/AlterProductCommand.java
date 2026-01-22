package br.com.erp.api.product.application.command;

public record AlterProductCommand(
        Long productId,
        String name,
        String description,
        Long categoryId
) {
}

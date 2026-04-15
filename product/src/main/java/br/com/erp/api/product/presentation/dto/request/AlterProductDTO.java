package br.com.erp.api.product.presentation.dto.request;

public record AlterProductDTO(
        String name,
        String description,
        Long categoryId,
        Boolean isLaunch
) {
}

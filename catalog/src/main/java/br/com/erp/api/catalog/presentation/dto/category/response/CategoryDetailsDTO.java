package br.com.erp.api.catalog.presentation.dto.category.response;

public record CategoryDetailsDTO(
        Long id,
        String name,
        Boolean active
) {
}

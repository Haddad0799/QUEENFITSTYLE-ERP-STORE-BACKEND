package br.com.erp.api.attribute.presentation.dto.category.response;

public record CategoryDetailsDTO(
        Long id,
        String name,
        Boolean active
) {
}


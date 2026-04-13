package br.com.erp.api.attribute.presentation.dto.category.response;

import java.util.List;

public record CategoryTreeDTO(
        Long id,
        String name,
        String normalizedName,
        List<CategoryTreeDTO> subcategories
) {}


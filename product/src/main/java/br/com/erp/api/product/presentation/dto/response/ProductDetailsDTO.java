package br.com.erp.api.product.presentation.dto.response;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record ProductDetailsDTO(
        Long id,
        String name,
        String description,
        String slug,
        @ColumnName("category_id") Long categoryId,
        boolean active
) {
}

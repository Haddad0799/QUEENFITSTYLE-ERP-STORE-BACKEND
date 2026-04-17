package br.com.erp.api.catalog.presentation.dto;

import java.util.List;

public record CatalogAvailableFiltersDTO(
        List<CatalogAvailableColorDTO> colors,
        List<String> sizes
) {}

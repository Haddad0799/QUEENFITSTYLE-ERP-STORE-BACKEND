package br.com.erp.api.catalog.presentation.dto;

import java.util.List;

public record CatalogAvailableFiltersDTO(
        List<CatalogColorDTO> colors,
        List<String> sizes
) {}

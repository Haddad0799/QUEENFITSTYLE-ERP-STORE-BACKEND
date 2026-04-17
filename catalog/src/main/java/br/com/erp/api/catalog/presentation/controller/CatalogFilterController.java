package br.com.erp.api.catalog.presentation.controller;

import br.com.erp.api.catalog.application.query.CatalogFilterQueryService;
import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogAvailableFiltersDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/store/catalog/filters")
public class CatalogFilterController {

    private final CatalogFilterQueryService catalogFilterQueryService;

    public CatalogFilterController(CatalogFilterQueryService catalogFilterQueryService) {
        this.catalogFilterQueryService = catalogFilterQueryService;
    }

    @GetMapping
    public ResponseEntity<CatalogAvailableFiltersDTO> getAvailableFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search
    ) {
        CatalogFilter filter = new CatalogFilter(
                normalize(resolveEffectiveCategory(category, subcategory)),
                color,
                label,
                minPrice,
                maxPrice,
                search
        );

        return ResponseEntity.ok(catalogFilterQueryService.getAvailableFilters(filter));
    }

    private String resolveEffectiveCategory(String category, String subcategory) {
        if (subcategory != null && !subcategory.isBlank()) {
            return subcategory;
        }
        return category;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}

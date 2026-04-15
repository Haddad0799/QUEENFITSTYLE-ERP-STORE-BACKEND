package br.com.erp.api.catalog.presentation.controller;

import br.com.erp.api.catalog.application.query.CatalogCategoryQueryService;
import br.com.erp.api.catalog.presentation.dto.CatalogNavigationCategoryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/store/catalog/categories")
public class CatalogCategoryController {

    private final CatalogCategoryQueryService catalogCategoryQueryService;

    public CatalogCategoryController(CatalogCategoryQueryService catalogCategoryQueryService) {
        this.catalogCategoryQueryService = catalogCategoryQueryService;
    }

    @GetMapping
    public ResponseEntity<List<CatalogNavigationCategoryDTO>> listNavigableCategories() {
        return ResponseEntity.ok(catalogCategoryQueryService.listNavigableCategories());
    }
}

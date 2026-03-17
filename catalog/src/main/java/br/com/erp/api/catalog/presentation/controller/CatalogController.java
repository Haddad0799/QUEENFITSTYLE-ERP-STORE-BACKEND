package br.com.erp.api.catalog.presentation.controller;

import br.com.erp.api.catalog.application.query.CatalogQueryService;
import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductDetailDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogSkuDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/store/products")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping
    public Page<CatalogProductSummaryDTO> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        CatalogFilter filter = new CatalogFilter(category, color, size, minPrice, maxPrice, search);
        return catalogQueryService.listProducts(filter, pageable);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CatalogProductDetailDTO> getProduct(@PathVariable String slug) {
        return ResponseEntity.ok(catalogQueryService.getProductBySlug(slug));
    }

    @GetMapping("/{slug}/skus/{skuCode}")
    public ResponseEntity<CatalogSkuDetailDTO> getSku(
            @PathVariable String slug,
            @PathVariable String skuCode
    ) {
        return ResponseEntity.ok(catalogQueryService.getSkuBySlugAndCode(slug, skuCode));
    }
}


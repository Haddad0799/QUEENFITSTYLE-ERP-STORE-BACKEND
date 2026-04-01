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
            // legacy: clients may send 'size' for SKU filter or pagination; prefer explicit 'sizeName'
            @RequestParam(name = "size", required = false) String rawSize,
            @RequestParam(name = "sizeName", required = false) String sizeName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        // Determine effective SKU size filter:
        // 1) if explicit 'sizeName' provided, use it
        // 2) else if 'size' (rawSize) provided and it's not an integer, treat it as the SKU size
        // 3) otherwise no SKU size filter (assume 'size' numeric is pagination and will be handled by Pageable)
        String effectiveSizeName = null;
        if (sizeName != null && !sizeName.isBlank()) {
            effectiveSizeName = sizeName;
        } else if (rawSize != null && !rawSize.isBlank()) {
            // check if rawSize is integer (pagination 'size')
            try {
                Integer.parseInt(rawSize);
                // it's numeric -> treat as pagination, do not use as filter
            } catch (NumberFormatException e) {
                effectiveSizeName = rawSize;
            }
        }

        CatalogFilter filter = new CatalogFilter(category, color, effectiveSizeName, minPrice, maxPrice, search);
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


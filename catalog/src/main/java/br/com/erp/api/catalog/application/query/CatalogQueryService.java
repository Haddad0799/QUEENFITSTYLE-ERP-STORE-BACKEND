package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductDetailDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogSkuDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogQueryService {

    private final CatalogQueryRepository queryRepository;
    private final CatalogCategoryScopeResolver categoryScopeResolver;

    public CatalogQueryService(
            CatalogQueryRepository queryRepository,
            CatalogCategoryScopeResolver categoryScopeResolver
    ) {
        this.queryRepository = queryRepository;
        this.categoryScopeResolver = categoryScopeResolver;
    }

    public Page<CatalogProductSummaryDTO> listProducts(CatalogFilter filter, Pageable pageable) {
        List<String> categorySlugs = categoryScopeResolver.resolve(filter);
        if (filter.hasCategory() && categorySlugs.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return queryRepository.findAll(ResolvedCatalogFilter.from(filter, categorySlugs), pageable);
    }

    public CatalogProductDetailDTO getProductBySlug(String slug) {
        return queryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + slug));
    }

    public CatalogSkuDetailDTO getSkuBySlugAndCode(String slug, String skuCode) {
        return queryRepository.findSkuBySlugAndCode(slug, skuCode)
                .orElseThrow(() -> new RuntimeException("SKU nao encontrado: " + skuCode));
    }
}

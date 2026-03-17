package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductDetailDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogSkuDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CatalogQueryService {

    private final CatalogQueryRepository queryRepository;

    public CatalogQueryService(CatalogQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public Page<CatalogProductSummaryDTO> listProducts(CatalogFilter filter, Pageable pageable) {
        return queryRepository.findAll(filter, pageable);
    }

    public CatalogProductDetailDTO getProductBySlug(String slug) {
        return queryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + slug));
    }

    public CatalogSkuDetailDTO getSkuBySlugAndCode(String slug, String skuCode) {
        return queryRepository.findSkuBySlugAndCode(slug, skuCode)
                .orElseThrow(() -> new RuntimeException("SKU não encontrado: " + skuCode));
    }
}


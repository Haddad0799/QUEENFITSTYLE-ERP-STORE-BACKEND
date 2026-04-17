package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductDetailDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogSkuDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CatalogQueryRepository {
    Page<CatalogProductSummaryDTO> findAll(ResolvedCatalogFilter filter, Pageable pageable);
    Optional<CatalogProductDetailDTO> findBySlug(String slug);
    Optional<CatalogSkuDetailDTO> findSkuBySlugAndCode(String slug, String skuCode);
}


package br.com.erp.api.catalog.domain.port;

import br.com.erp.api.product.application.dto.ProductSnapshot;

public interface CatalogRepositoryPort {
    void publishProduct(ProductSnapshot snapshot);
    void unpublishByProductId(Long productId);
    boolean existsByProductId(Long productId);
}


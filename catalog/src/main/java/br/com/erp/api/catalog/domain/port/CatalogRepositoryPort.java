package br.com.erp.api.catalog.domain.port;

import br.com.erp.api.product.application.dto.ProductSnapshot;

public interface CatalogRepositoryPort {

    // Publicação
    void unpublishByProductId(Long productId);
    void replaceProduct(ProductSnapshot snapshot);
}
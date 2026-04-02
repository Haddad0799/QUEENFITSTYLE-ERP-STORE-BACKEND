package br.com.erp.api.catalog.domain.port;

import br.com.erp.api.product.application.dto.ProductSnapshot;

import java.util.Optional;

public interface CatalogRepositoryPort {

    // Publicação
    void unpublishByProductId(Long productId);
    void replaceProduct(ProductSnapshot snapshot);

    // Consultas auxiliares
    Optional<String> findSlugByProductId(Long productId);
}
package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.ProductSnapshot;

public interface CatalogGateway {
    void publish(ProductSnapshot snapshot);
    void unpublish(Long productId);
}


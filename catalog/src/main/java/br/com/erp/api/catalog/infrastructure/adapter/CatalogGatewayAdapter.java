package br.com.erp.api.catalog.infrastructure.adapter;

import br.com.erp.api.catalog.application.usecase.PublishProductToCatalogUseCase;
import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.gateway.CatalogGateway;
import org.springframework.stereotype.Component;

@Component
public class CatalogGatewayAdapter implements CatalogGateway {

    private final PublishProductToCatalogUseCase publishUseCase;
    private final CatalogRepositoryPort catalogRepository;

    public CatalogGatewayAdapter(PublishProductToCatalogUseCase publishUseCase,
                                 CatalogRepositoryPort catalogRepository) {
        this.publishUseCase = publishUseCase;
        this.catalogRepository = catalogRepository;
    }

    @Override
    public void publish(ProductSnapshot snapshot) {
        publishUseCase.execute(snapshot);
    }

    @Override
    public void unpublish(Long productId) {
        catalogRepository.unpublishByProductId(productId);
    }
}


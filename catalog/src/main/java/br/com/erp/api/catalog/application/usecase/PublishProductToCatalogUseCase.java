package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishProductToCatalogUseCase {

    private final CatalogRepositoryPort catalogRepository;

    public PublishProductToCatalogUseCase(CatalogRepositoryPort catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Transactional
    public void execute(ProductSnapshot snapshot) {
        // Se já foi publicado antes, remove o antigo e republica (upsert)
        if (catalogRepository.existsByProductId(snapshot.productId())) {
            catalogRepository.unpublishByProductId(snapshot.productId());
        }
        catalogRepository.publishProduct(snapshot);
    }
}


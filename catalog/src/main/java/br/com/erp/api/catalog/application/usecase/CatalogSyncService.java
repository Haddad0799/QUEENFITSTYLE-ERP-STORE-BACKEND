package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogSyncService {

    private final CatalogRepositoryPort catalogRepository;

    public CatalogSyncService(
            CatalogRepositoryPort catalogRepository
    ) {
        this.catalogRepository = catalogRepository;

    }

    // idempotente
    @Transactional
    public void publishProduct(ProductSnapshot snapshot) {
        catalogRepository.replaceProduct(snapshot);
    }

    @Transactional
    public void unpublishProduct(Long productId) {
        catalogRepository.unpublishByProductId(productId);
    }
}
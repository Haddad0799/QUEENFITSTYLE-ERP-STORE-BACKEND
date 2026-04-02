package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.port.CatalogRevalidationPort;
import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogSyncService {

    private static final Logger log = LoggerFactory.getLogger(CatalogSyncService.class);

    private final CatalogRepositoryPort catalogRepository;
    private final CatalogRevalidationPort revalidation;

    public CatalogSyncService(
            CatalogRepositoryPort catalogRepository,
            CatalogRevalidationPort revalidation
    ) {
        this.catalogRepository = catalogRepository;
        this.revalidation = revalidation;
    }

    // idempotente
    @Transactional
    public void publishProduct(ProductSnapshot snapshot) {
        catalogRepository.replaceProduct(snapshot);

        revalidation.revalidate(List.of(
                "catalog-products",
                "catalog-product-" + snapshot.slug()
        ));
    }

    @Transactional
    public void unpublishProduct(Long productId) {
        // Busca o slug antes de deletar, para poder invalidar o cache
        String slug = catalogRepository.findSlugByProductId(productId).orElse(null);

        catalogRepository.unpublishByProductId(productId);

        if (slug != null) {
            revalidation.revalidate(List.of(
                    "catalog-products",
                    "catalog-product-" + slug
            ));
        } else {
            // Produto já não existia no catálogo; invalida apenas a listagem
            revalidation.revalidate(List.of("catalog-products"));
            log.warn("Slug não encontrado para productId {} ao despublicar — apenas 'catalog-products' invalidado", productId);
        }
    }
}
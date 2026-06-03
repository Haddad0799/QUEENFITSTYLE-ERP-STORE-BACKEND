package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.port.CatalogRevalidationPort;
import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogSyncService {

    private static final Logger log = LoggerFactory.getLogger(CatalogSyncService.class);

    private static final String TAG_PRODUCTS = "catalog-products";
    private static final String TAG_FILTERS = "catalog-filters";
    private static final String TAG_CATEGORIES = "catalog-categories";

    private final CatalogRepositoryPort catalogRepository;
    private final CatalogRevalidationPort revalidation;

    public CatalogSyncService(
            CatalogRepositoryPort catalogRepository,
            CatalogRevalidationPort revalidation
    ) {
        this.catalogRepository = catalogRepository;
        this.revalidation = revalidation;
    }

    @Transactional
    public void publishProduct(ProductSnapshot snapshot) {
        catalogRepository.replaceProduct(snapshot);

        revalidation.revalidate(buildTags(snapshot.slug()));
    }

    @Transactional
    public void unpublishProduct(Long productId) {
        String slug = catalogRepository.findSlugByProductId(productId).orElse(null);

        catalogRepository.unpublishByProductId(productId);

        revalidation.revalidate(buildTags(slug));

        if (slug == null) {
            log.warn(
                    "Slug não encontrado para productId {} ao despublicar — cache de detalhe não pôde ser invalidado, mas listagem, filtros e categorias foram invalidados",
                    productId
            );
        }
    }

    private List<String> buildTags(String slug) {
        List<String> tags = new ArrayList<>();
        tags.add(TAG_PRODUCTS);
        tags.add(TAG_FILTERS);
        tags.add(TAG_CATEGORIES);

        if (slug != null && !slug.isBlank()) {
            tags.add("catalog-product-" + slug);
        }

        return tags;
    }
}
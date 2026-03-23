package br.com.erp.api.catalog.domain.port;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;

import java.math.BigDecimal;
import java.util.List;

public interface CatalogRepositoryPort {

    // Publicação
    void publishProduct(ProductSnapshot snapshot);
    void unpublishByProductId(Long productId);
    boolean existsByProductId(Long productId);

    // Dados gerais do produto
    void updateProductInfo(ProductSnapshot snapshot);

    // Preço
    void updateSkuPrice(Long skuId, BigDecimal sellingPrice);
    void recalculateMinPrice(Long productId);

    // Estoque
    void updateSkuStock(Long skuId, int available);

    // Imagens
    void updateColorGroupImages(Long productId, Long colorId, List<String> imageUrls);

    // Novo SKU em produto já publicado
    void addSku(Long productId, SkuSnapshot skuSnapshot);
}
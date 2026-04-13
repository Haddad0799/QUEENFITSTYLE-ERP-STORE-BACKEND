package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeleteProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductUseCase.class);

    private final ProductRepositoryPort productRepository;
    private final ProductColorImageRepositoryPort imageRepository;
    private final StorageGateway storageGateway;
    private final ProductCatalogPort productCatalogPort;

    public DeleteProductUseCase(
            ProductRepositoryPort productRepository,
            ProductColorImageRepositoryPort imageRepository,
            StorageGateway storageGateway,
            ProductCatalogPort productCatalogPort
    ) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.storageGateway = storageGateway;
        this.productCatalogPort = productCatalogPort;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Regra de domínio: não permite excluir produto publicado
        product.assertDeletable();

        // Coleta image keys ANTES de deletar (CASCADE vai limpar a tabela)
        List<String> imageKeys = imageRepository.findAllKeysByProductId(productId);

        // DELETE em cascata: products → skus → sku_price, sku_stock, stock_movement, product_color_images
        productRepository.deleteById(productId);

        // Remove snapshot do catálogo (caso tenha sido publicado alguma vez)
        try {
            productCatalogPort.unpublish(productId);
        } catch (Exception ex) {
            log.warn("Falha ao remover produto do catálogo (pode não existir): {}", ex.getMessage());
        }

        // Remove imagens do storage (MinIO) — por último, pois é side-effect externo
        if (!imageKeys.isEmpty()) {
            try {
                storageGateway.deleteImages(imageKeys);
            } catch (Exception ex) {
                log.error("Falha ao excluir {} imagem(ns) do storage para o produto {}. Keys: {}",
                        imageKeys.size(), productId, imageKeys, ex);
                // Não relança: o produto já foi excluído no banco.
                // Imagens órfãs podem ser limpas por um job futuro.
            }
        }

        log.info("Produto {} excluído com sucesso ({} imagens removidas do storage)", productId, imageKeys.size());
    }
}


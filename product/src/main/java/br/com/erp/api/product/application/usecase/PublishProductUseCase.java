package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.ProductNotReadyForSaleException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;

    public PublishProductUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = skuRepository.findByProductId(productId);

        List<Sku> readySkus = skus.stream()
                .filter(Sku::isReady)
                .toList();

        if (readySkus.isEmpty()) {
            throw new ProductNotReadyForSaleException();
        }

        readySkus.forEach(Sku::activate);
        skus.stream()
                .filter(sku -> !sku.isReady())
                .forEach(Sku::markAsIncomplete);

        product.publish();

        productRepository.updateStatus(product);
        skuRepository.updateStatusBatch(readySkus);
    }
}
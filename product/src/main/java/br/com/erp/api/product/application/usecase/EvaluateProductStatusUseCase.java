package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluateProductStatusUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;

    public EvaluateProductStatusUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow();

        List<Sku> skus = skuRepository.findByProductId(productId);

        boolean hasReadySku = skus.stream().anyMatch(Sku::isReady);

        if (hasReadySku) {
            product.markAsReadyForSale();
        } else {
            product.markAsDraft();
        }

        productRepository.updateStatus(product);
    }
}


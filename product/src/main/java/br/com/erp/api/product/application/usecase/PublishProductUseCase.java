package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PublishProductUseCase {

    private final ProductRepositoryPort productRepository;

    public PublishProductUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    public void execute(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.publish();

        productRepository.update(product);
    }
}

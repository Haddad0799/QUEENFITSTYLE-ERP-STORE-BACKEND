package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivateProductUseCase {

    private final ProductRepository productRepository;

    public ActivateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void execute(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.activate();

        productRepository.update(product);
    }
}

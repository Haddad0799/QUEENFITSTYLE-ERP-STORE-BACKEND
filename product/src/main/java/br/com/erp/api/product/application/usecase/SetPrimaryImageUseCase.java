package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetPrimaryImageUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductColorImageRepositoryPort imageRepository;

    public SetPrimaryImageUseCase(ProductRepositoryPort productRepository,
                                  ProductColorImageRepositoryPort imageRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void execute(Long productId, Long imageId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Valida que a imagem existe e pertence ao produto
        List<ProductColorImage> found = imageRepository.findAllByIds(List.of(imageId));
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Imagem não encontrada: " + imageId);
        }

        ProductColorImage image = found.get(0);
        if (!productId.equals(image.getProductId())) {
            throw new IllegalArgumentException("A imagem " + imageId + " não pertence ao produto " + productId);
        }

        product.definePrimaryImage(imageId);
        productRepository.updatePrimaryImage(product);
    }
}


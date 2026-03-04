package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.presentation.dto.request.ImageConfirmationItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfirmImageUploadUseCase {

    private final ProductColorImageRepositoryPort imageRepository;
    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;

    public ConfirmImageUploadUseCase(ProductColorImageRepositoryPort imageRepository, ProductRepositoryPort productRepository, SkuRepositoryPort skuRepository) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public void execute(Long productId, Long colorId, List<ImageConfirmationItem> items) {

        // valida se o produto existe
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        // valida se existe pelo menos um SKU com essa cor nesse produto
        if (!skuRepository.existsByProductIdAndColorId(productId, colorId)) {
            throw new InvalidColorException(
                    "Nenhum SKU encontrado com essa cor para o produto informado."
            );
        }

        List<ProductColorImage> images = items.stream()
                .map(item -> new ProductColorImage(
                        productId,
                        colorId,
                        item.imageKey(),
                        item.order()
                ))
                .toList();

        imageRepository.saveAll(images);
    }
}
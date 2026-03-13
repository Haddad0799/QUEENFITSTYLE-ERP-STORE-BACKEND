package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeleteProductImagesUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductImagesUseCase.class);

    private final ProductColorImageRepositoryPort imageRepository;
    private final StorageGateway storageGateway;
    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;

    public DeleteProductImagesUseCase(
            ProductColorImageRepositoryPort imageRepository,
            StorageGateway storageGateway,
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository
    ) {
        this.imageRepository = imageRepository;
        this.storageGateway = storageGateway;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public void execute(Long productId, Long colorId, List<Long> imageIds) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (imageIds == null || imageIds.isEmpty()) return;

        List<ProductColorImage> images = imageRepository.findAllByIds(imageIds);

        if (images.size() != imageIds.size()) {
            throw new IllegalArgumentException("Algumas imagens informadas não foram encontradas");
        }

        boolean invalid = images.stream().anyMatch(img ->
                !productId.equals(img.getProductId()) || !colorId.equals(img.getColorId())
        );

        if (invalid) {
            throw new IllegalArgumentException("As imagens informadas não pertencem ao produto/cor informados");
        }

        List<String> imageKeys = images.stream()
                .map(ProductColorImage::getImageKey)
                .collect(Collectors.toList());

        revalidatePrimaryImage(product, imageIds);


        imageRepository.deleteAllByIds(imageIds);

        boolean stillHasImages = imageRepository.existsByProductIdAndColorId(productId, colorId);
        if (!stillHasImages) {
            skuRepository.updateStatusByProductIdAndColorId(productId, colorId, SkuStatus.INCOMPLETE);
        }

        try {
            storageGateway.deleteImages(imageKeys);
        } catch (Exception e) {
            log.error("Falha ao excluir imagens do storage. Keys: {}. Forçando rollback.", imageKeys, e);
            throw new RuntimeException("Falha ao excluir imagens do storage", e);
        }
    }

    private void revalidatePrimaryImage(Product product, List<Long> deletedImageIds) {
        if (product.isPrimaryImageAmong(deletedImageIds)) {
            imageRepository
                    .findFirstByProductIdExcluding(product.getId(), deletedImageIds)
                    .ifPresentOrElse(
                            image -> product.definePrimaryImage(image.getId()),
                            () -> product.definePrimaryImage(null)
                    );
            productRepository.updatePrimaryImage(product);
        }
    }
}


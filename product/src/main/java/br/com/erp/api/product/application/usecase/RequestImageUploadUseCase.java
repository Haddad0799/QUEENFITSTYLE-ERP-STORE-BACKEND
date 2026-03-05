package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestImageUploadUseCase {

    private final ImageProvider imageProvider;
    private final StorageGateway storageGateway;
    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;

    public RequestImageUploadUseCase(
            ImageProvider imageProvider,
            StorageGateway storageGateway, ProductRepositoryPort productRepository, SkuRepositoryPort skuRepository
    ) {
        this.imageProvider = imageProvider;
        this.storageGateway = storageGateway;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    public List<PresignedUrlResult> execute(Long productId, Long colorId, List<String> filenames) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        if (!skuRepository.existsByProductIdAndColorId(productId, colorId)) {
            throw new InvalidColorException("Cor não disponível para este produto");
        }

        if (filenames.isEmpty() || filenames.size() > 5) {
            throw new IllegalArgumentException("Informe entre 1 e 5 arquivos");
        }

        int existing = imageProvider.countByProductIdAndColorId(productId, colorId);

        if (existing + filenames.size() > 5) {
            throw new IllegalArgumentException(
                    "Limite de 5 imagens por cor. Já existem " + existing + " imagens cadastradas."
            );
        }

        return storageGateway.generatePresignedUrls(productId, colorId, filenames);
    }
}
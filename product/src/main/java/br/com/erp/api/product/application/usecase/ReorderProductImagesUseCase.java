package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReorderProductImagesUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductColorImageRepositoryPort imageRepository;
    private final ProductCatalogPort productCatalogPublisher;

    public ReorderProductImagesUseCase(
            ProductRepositoryPort productRepository,
            ProductColorImageRepositoryPort imageRepository, ProductCatalogPort productCatalogPublisher
    ) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(Long productId, Long colorId, List<Long> orderedImageIds) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        if (orderedImageIds == null || orderedImageIds.isEmpty()) {
            throw new IllegalArgumentException("A lista de imagens não pode ser vazia.");
        }

        // Valida duplicatas internas no request
        Set<Long> uniqueIds = new HashSet<>(orderedImageIds);
        if (uniqueIds.size() != orderedImageIds.size()) {
            throw new IllegalArgumentException("IDs de imagens duplicados no request.");
        }

        // Busca todas as imagens atuais dessa cor
        List<ProductColorImage> currentImages =
                imageRepository.findByProductIdAndColorId(productId, colorId);

        if (currentImages.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhuma imagem encontrada para o produto " + productId + " e cor " + colorId
            );
        }

        // Valida que a lista enviada contém exatamente as mesmas imagens que existem
        Set<Long> currentIds = currentImages.stream()
                .map(ProductColorImage::getId)
                .collect(Collectors.toSet());

        if (!currentIds.equals(uniqueIds)) {
            throw new IllegalArgumentException(
                    "A lista deve conter exatamente todas as imagens da cor. " +
                    "Esperado: " + currentIds + ", recebido: " + uniqueIds
            );
        }

        // Monta o mapa imageId → nova ordem (1-based)
        Map<Long, Integer> newOrders = new LinkedHashMap<>();
        for (int i = 0; i < orderedImageIds.size(); i++) {
            newOrders.put(orderedImageIds.get(i), i + 1);
        }

        imageRepository.updateOrders(newOrders);

        productCatalogPublisher.publishIfPublished(productId);
    }
}


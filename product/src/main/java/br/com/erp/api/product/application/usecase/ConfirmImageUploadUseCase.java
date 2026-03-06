package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.presentation.dto.request.ImageConfirmationItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConfirmImageUploadUseCase {

    private final ProductColorImageRepositoryPort imageRepository;
    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final EvaluateSkuCompletenessUseCase evaluateSkuCompleteness;
    private final ImageProvider imageProvider;

    public ConfirmImageUploadUseCase(ProductColorImageRepositoryPort imageRepository, ProductRepositoryPort productRepository, SkuRepositoryPort skuRepository, EvaluateSkuCompletenessUseCase evaluateSkuCompleteness, ImageProvider imageProvider) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.evaluateSkuCompleteness = evaluateSkuCompleteness;
        this.imageProvider = imageProvider;
    }

    @Transactional
    public void execute(Long productId, Long colorId, List<ImageConfirmationItem> items) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

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

        List<Integer> orders = items.stream()
                .map(ImageConfirmationItem::order)
                .toList();

        List<String> existingKeys = imageRepository.findKeysByProductIdAndColorId(productId, colorId);

        for (ImageConfirmationItem item : items) {
            if (existingKeys.contains(item.imageKey())) {
                throw new IllegalArgumentException("Imagem já confirmada: " + item.imageKey());
            }
        }

        // duplicata interna no próprio request
        Set<Integer> uniqueOrders = new HashSet<>(orders);
        if (uniqueOrders.size() != orders.size()) {
            throw new IllegalArgumentException("Ordem de exibição duplicada no request.");
        }

        // conflito com imagens já existentes no banco
        List<Integer> existingOrders = imageRepository.findOrdersByProductIdAndColorId(productId, colorId);
        for (Integer order : orders) {
            if (existingOrders.contains(order)) {
                throw new IllegalArgumentException("Ordem " + order + " já está em uso para esta cor.");
            }
        }

        int existing = imageProvider.countByProductIdAndColorId(productId, colorId);

        if (existing + items.size() > ProductColorImage.MAX_IMAGES_PER_COLOR) {
            throw new IllegalArgumentException(
                    "Limite de " + ProductColorImage.MAX_IMAGES_PER_COLOR + " imagens por cor. Já existem " + existing + " cadastradas."
            );
        }

        imageRepository.saveAll(images);

        List<Long> skuIds = skuRepository.findIdsByProductIdAndColorId(productId, colorId);
        skuIds.forEach(evaluateSkuCompleteness::execute);
    }
}
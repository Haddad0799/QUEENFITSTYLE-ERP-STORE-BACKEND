package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.port.ColorLookupPort;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.presentation.dto.response.ColorImagesDTO;
import br.com.erp.api.product.presentation.dto.response.ImageItemDTO;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListProductImagesUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductColorImageRepositoryPort imageRepository;
    private final ColorLookupPort colorLookup;
    private final StorageGateway storageGateway;

    public ListProductImagesUseCase(ProductRepositoryPort productRepository,
                                    ProductColorImageRepositoryPort imageRepository,
                                    ColorLookupPort colorLookup,
                                    StorageGateway storageGateway) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.colorLookup = colorLookup;
        this.storageGateway = storageGateway;
    }

    public List<ColorImagesDTO> execute(Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        List<ProductColorImage> allImages = imageRepository.findAllByProductIdGroupedByColor(productId);

        if (allImages.isEmpty()) return List.of();

        // Agrupa por colorId
        Map<Long, List<ProductColorImage>> byColor = allImages.stream()
                .collect(Collectors.groupingBy(ProductColorImage::getColorId));

        // Busca nomes das cores
        Set<Long> colorIds = byColor.keySet();
        Map<Long, String> colorNames = colorLookup.findByIds(colorIds).stream()
                .collect(Collectors.toMap(IdNameProjection::id, IdNameProjection::name));

        return byColor.entrySet().stream()
                .map(entry -> {
                    Long colorId = entry.getKey();
                    String colorName = colorNames.getOrDefault(colorId, "Cor #" + colorId);
                    List<ImageItemDTO> items = entry.getValue().stream()
                            .map(img -> new ImageItemDTO(
                                    img.getId(),
                                    storageGateway.getPublicUrl(img.getImageKey()),
                                    img.getOrder()
                            ))
                            .toList();
                    return new ColorImagesDTO(colorName, items);
                })
                .toList();
    }
}


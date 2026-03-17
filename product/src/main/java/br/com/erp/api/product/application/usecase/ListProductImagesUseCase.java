package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.application.provider.ColorProvider;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.presentation.dto.response.ColorImagesDTO;
import br.com.erp.api.product.presentation.dto.response.ImageItemDTO;
import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListProductImagesUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductColorImageRepositoryPort imageRepository;
    private final ColorProvider colorLookup;
    private final StorageGateway storageGateway;

    public ListProductImagesUseCase(ProductRepositoryPort productRepository,
                                    ProductColorImageRepositoryPort imageRepository,
                                    ColorProvider colorLookup,
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

        // Busca detalhes das cores (nome + hexCode)
        Set<Long> colorIds = byColor.keySet();
        Map<Long, ColorDetailProjection> colorDetails = colorLookup.findWithHexByIds(colorIds).stream()
                .collect(Collectors.toMap(ColorDetailProjection::id, c -> c));

        return byColor.entrySet().stream()
                .map(entry -> {
                    Long colorId = entry.getKey();
                    ColorDetailProjection color = colorDetails.get(colorId);
                    String colorName = color != null ? color.name() : "Cor #" + colorId;
                    String hexCode = color != null ? color.hexCode() : null;
                    List<ImageItemDTO> items = entry.getValue().stream()
                            .map(img -> new ImageItemDTO(
                                    img.getId(),
                                    storageGateway.getPublicUrl(img.getImageKey()),
                                    img.getOrder()
                            ))
                            .toList();
                    return new ColorImagesDTO(colorName, hexCode, items);
                })
                .sorted(java.util.Comparator.comparing(ColorImagesDTO::colorName))
                .toList();
    }
}


package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.provider.InventoryProvider;
import br.com.erp.api.product.application.provider.PriceProvider;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.application.provider.ColorProvider;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.application.provider.SizeProvider;
import br.com.erp.api.product.presentation.dto.response.SkuPriceDTO;
import br.com.erp.api.product.presentation.dto.response.SkuStock;
import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SnapshotAssembler {

    private final StorageGateway storageGateway;
    private final InventoryProvider inventoryProvider;
    private final PriceProvider priceProvider;
    private final ColorProvider colorLookup;
    private final SizeProvider sizeLookup;
    private final ProductColorImageRepositoryPort imageRepository;

    public SnapshotAssembler(StorageGateway storageGateway,
                             InventoryProvider inventoryProvider,
                             PriceProvider priceProvider,
                             ColorProvider colorLookup,
                             SizeProvider sizeLookup,
                             ProductColorImageRepositoryPort imageRepository) {
        this.storageGateway = storageGateway;
        this.inventoryProvider = inventoryProvider;
        this.priceProvider = priceProvider;
        this.colorLookup = colorLookup;
        this.sizeLookup = sizeLookup;
        this.imageRepository = imageRepository;
    }

    public ProductSnapshot assemble(Product product, List<Sku> activeSkus, String categoryName) {

        // Batch lookup de cores e tamanhos
        Set<Long> colorIds = activeSkus.stream().map(Sku::getColorId).collect(Collectors.toSet());
        Set<Long> sizeIds = activeSkus.stream().map(Sku::getSizeId).collect(Collectors.toSet());

        Map<Long, ColorDetailProjection> colors = colorLookup.findWithHexByIds(colorIds).stream()
                .collect(Collectors.toMap(ColorDetailProjection::id, c -> c));

        Map<Long, String> sizes = sizeLookup.findByIds(sizeIds).stream()
                .collect(Collectors.toMap(IdNameProjection::id, IdNameProjection::name));

        List<SkuSnapshot> skuSnapshots = activeSkus.stream()
                .map(sku -> {
                    SkuPriceDTO price = priceProvider.getBySkuId(sku.getId());
                    SkuStock stock = inventoryProvider.getBySkuId(sku.getId());

                    List<String> imageUrls = imageRepository
                            .findByProductIdAndColorId(product.getId(), sku.getColorId())
                            .stream()
                            .map(img -> storageGateway.getPublicUrl(img.getImageKey()))
                            .toList();

                    ColorDetailProjection color = colors.get(sku.getColorId());
                    String sizeName = sizes.getOrDefault(sku.getSizeId(), "");

                    return new SkuSnapshot(
                            sku.getId(),
                            sku.getCode().value(),
                            color != null ? color.name() : "",
                            color != null ? color.hexCode() : "",
                            sizeName,
                            price.sellingPrice(),
                            stock.available(),
                            sku.getDimensions().width(),
                            sku.getDimensions().height(),
                            sku.getDimensions().length(),
                            sku.getDimensions().weight(),
                            imageUrls
                    );
                }).toList();

        // Resolve URL da imagem principal
        String mainImageUrl = resolveMainImageUrl(product);

        return new ProductSnapshot(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlugValue(),
                categoryName,
                mainImageUrl,
                skuSnapshots
        );
    }

    private String resolveMainImageUrl(Product product) {
        if (product.getPrimaryImageId() == null) {
            return null;
        }
        List<ProductColorImage> primaryImages = imageRepository.findAllByIds(List.of(product.getPrimaryImageId()));
        if (primaryImages.isEmpty()) {
            return null;
        }
        return storageGateway.getPublicUrl(primaryImages.getFirst().getImageKey());
    }
}


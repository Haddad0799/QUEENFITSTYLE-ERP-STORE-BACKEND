package br.com.erp.api.product.application.assembler;

import br.com.erp.api.product.application.dto.CategorySnapshot;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.provider.CategoryProvider;
import br.com.erp.api.product.application.provider.ColorProvider;
import br.com.erp.api.product.application.provider.InventoryProvider;
import br.com.erp.api.product.application.provider.PriceProvider;
import br.com.erp.api.product.application.provider.SizeProvider;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
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
    private final ColorProvider colorProvider;
    private final SizeProvider sizeProvider;
    private final CategoryProvider categoryProvider;
    private final ProductColorImageRepositoryPort imageRepository;
    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final SnapshotShowcaseResolver showcaseResolver;

    public SnapshotAssembler(StorageGateway storageGateway,
                             InventoryProvider inventoryProvider,
                             PriceProvider priceProvider,
                             ColorProvider colorProvider,
                             SizeProvider sizeProvider,
                             CategoryProvider categoryProvider,
                             ProductColorImageRepositoryPort imageRepository,
                             ProductRepositoryPort productRepository,
                             SkuRepositoryPort skuRepository,
                             SnapshotShowcaseResolver showcaseResolver) {
        this.storageGateway = storageGateway;
        this.inventoryProvider = inventoryProvider;
        this.priceProvider = priceProvider;
        this.colorProvider = colorProvider;
        this.sizeProvider = sizeProvider;
        this.categoryProvider = categoryProvider;
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.showcaseResolver = showcaseResolver;
    }

    // Carga completa — usado pelo ProductPublishedEvent
    public ProductSnapshot assemble(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> activeSkus = skuRepository.findByProductIdAndStatusIn(
                productId, List.of(SkuStatus.READY, SkuStatus.PUBLISHED)
        );

        // Busca subcategoria (categoria direta do produto) e categoria pai
        CategorySnapshot subcategory = categoryProvider.findById(product.getCategoryId());
        CategorySnapshot parentCategory = subcategory.parentId() != null
                ? categoryProvider.findById(subcategory.parentId())
                : null;

        return assemble(product, activeSkus, subcategory, parentCategory);
    }

    private ProductSnapshot assemble(Product product, List<Sku> activeSkus,
                                     CategorySnapshot subcategory, CategorySnapshot parentCategory) {

        Set<Long> colorIds = activeSkus.stream()
                .map(Sku::getColorId)
                .collect(Collectors.toSet());

        Set<Long> sizeIds = activeSkus.stream()
                .map(Sku::getSizeId)
                .collect(Collectors.toSet());

        Map<Long, ColorDetailProjection> colors = colorProvider.findWithHexByIds(colorIds).stream()
                .collect(Collectors.toMap(ColorDetailProjection::id, c -> c));

        Map<Long, String> sizes = sizeProvider.findByIds(sizeIds).stream()
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
                            sku.getColorId(),
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

        ProductColorImage primaryImage = resolvePrimaryImage(product);
        String mainImageUrl = primaryImage != null
                ? storageGateway.getPublicUrl(primaryImage.getImageKey())
                : null;
        SnapshotShowcaseResolver.ShowcaseSnapshot showcase = showcaseResolver.resolve(
                primaryImage,
                colors,
                skuSnapshots
        );

        return new ProductSnapshot(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlugValue(),
                product.isLaunch(),
                subcategory.id(),
                subcategory.name(),
                subcategory.normalizedName(),
                parentCategory != null ? parentCategory.id() : null,
                parentCategory != null ? parentCategory.name() : null,
                parentCategory != null ? parentCategory.normalizedName() : null,
                mainImageUrl,
                showcase.mainColor(),
                showcase.defaultSelection(),
                showcase.displayPrice(),
                skuSnapshots
        );
    }


    private ProductColorImage resolvePrimaryImage(Product product) {
        if (product.getPrimaryImageId() == null) return null;

        List<ProductColorImage> primaryImages = imageRepository
                .findAllByIds(List.of(product.getPrimaryImageId()));

        if (primaryImages.isEmpty()) return null;

        return primaryImages.getFirst();
    }
}

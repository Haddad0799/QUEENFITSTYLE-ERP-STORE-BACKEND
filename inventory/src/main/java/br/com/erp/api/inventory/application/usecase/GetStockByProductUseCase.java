package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.application.query.projection.ProductSkuStockRow;
import br.com.erp.api.inventory.application.query.projection.ProductStockRow;
import br.com.erp.api.inventory.presentation.dto.ProductStockColorDTO;
import br.com.erp.api.inventory.presentation.dto.ProductStockDTO;
import br.com.erp.api.inventory.presentation.dto.ProductStockSkuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetStockByProductUseCase {

    private final StockQueryRepository queryRepository;
    private final ImageUrlResolverPort imageUrlResolver;

    public GetStockByProductUseCase(StockQueryRepository queryRepository,
                                    ImageUrlResolverPort imageUrlResolver) {
        this.queryRepository = queryRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    public Page<ProductStockDTO> execute(String search, Pageable pageable) {
        Page<ProductStockRow> products = queryRepository.findProductsWithStock(search, pageable);

        List<Long> productIds = products.getContent().stream()
                .map(ProductStockRow::productId)
                .toList();

        Map<Long, List<ProductSkuStockRow>> skusByProduct =
                queryRepository.findSkuStockByProductIds(productIds).stream()
                        .collect(Collectors.groupingBy(ProductSkuStockRow::productId));

        List<ProductStockDTO> content = products.getContent().stream()
                .map(product -> toDTO(product, skusByProduct.getOrDefault(product.productId(), List.of())))
                .toList();

        return new PageImpl<>(content, pageable, products.getTotalElements());
    }

    private ProductStockDTO toDTO(ProductStockRow product, List<ProductSkuStockRow> skuRows) {
        // LinkedHashMap preserva a ordenação por nome da cor vinda da query
        Map<Long, List<ProductSkuStockRow>> rowsByColor = skuRows.stream()
                .collect(Collectors.groupingBy(ProductSkuStockRow::colorId,
                        LinkedHashMap::new, Collectors.toList()));

        List<ProductStockColorDTO> colors = rowsByColor.values().stream()
                .map(this::toColorDTO)
                .toList();

        boolean hasLowStock = colors.stream().anyMatch(ProductStockColorDTO::hasLowStock);

        return new ProductStockDTO(
                product.productId(),
                product.productName(),
                imageUrlResolver.resolve(product.primaryImageKey()),
                hasLowStock,
                colors
        );
    }

    private ProductStockColorDTO toColorDTO(List<ProductSkuStockRow> colorRows) {
        ProductSkuStockRow first = colorRows.get(0);

        List<ProductStockSkuDTO> skus = colorRows.stream()
                .map(row -> new ProductStockSkuDTO(
                        row.skuId(),
                        row.skuCode(),
                        row.sizeName(),
                        row.quantity(),
                        row.reserved(),
                        row.available(),
                        row.minQuantity(),
                        row.lowStock()
                ))
                .toList();

        boolean hasLowStock = colorRows.stream().anyMatch(ProductSkuStockRow::lowStock);
        int totalAvailable = colorRows.stream().mapToInt(ProductSkuStockRow::available).sum();

        return new ProductStockColorDTO(
                first.colorId(),
                first.colorName(),
                first.colorHex(),
                hasLowStock,
                totalAvailable,
                skus
        );
    }
}

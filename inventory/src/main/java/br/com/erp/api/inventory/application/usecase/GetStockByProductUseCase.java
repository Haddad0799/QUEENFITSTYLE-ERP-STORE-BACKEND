package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.application.query.projection.ProductSkuStockRow;
import br.com.erp.api.inventory.application.query.projection.ProductStockRow;
import br.com.erp.api.inventory.presentation.dto.ProductStockDTO;
import br.com.erp.api.inventory.presentation.dto.ProductStockSkuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        List<ProductStockSkuDTO> skus = skuRows.stream()
                .map(row -> new ProductStockSkuDTO(
                        row.skuId(),
                        row.skuCode(),
                        row.colorName(),
                        row.sizeName(),
                        row.quantity(),
                        row.reserved(),
                        row.available(),
                        row.minQuantity(),
                        row.lowStock()
                ))
                .toList();

        boolean hasLowStock = skuRows.stream().anyMatch(ProductSkuStockRow::lowStock);

        return new ProductStockDTO(
                product.productId(),
                product.productName(),
                imageUrlResolver.resolve(product.primaryImageKey()),
                hasLowStock,
                skus
        );
    }
}

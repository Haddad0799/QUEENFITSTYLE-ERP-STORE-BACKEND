package br.com.erp.api.product.application.service;

import br.com.erp.api.product.application.dto.PriceInitialization;
import br.com.erp.api.product.application.dto.ProductImportData;
import br.com.erp.api.product.application.dto.StockInitialization;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.application.gateway.PriceGateway;
import br.com.erp.api.product.application.provider.CategoryProvider;
import br.com.erp.api.product.application.provider.ColorProvider;
import br.com.erp.api.product.application.provider.SizeProvider;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.domain.valueobject.Slug;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductGroupProcessor {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final CategoryProvider categoryProvider;
    private final ColorProvider colorProvider;
    private final SizeProvider sizeProvider;
    private final InventoryGateway inventoryGateway;
    private final PriceGateway priceGateway;

    public ProductGroupProcessor(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            CategoryProvider categoryProvider,
            ColorProvider colorProvider,
            SizeProvider sizeProvider,
            InventoryGateway inventoryGateway,
            PriceGateway priceGateway
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.categoryProvider = categoryProvider;
        this.colorProvider = colorProvider;
        this.sizeProvider = sizeProvider;
        this.inventoryGateway = inventoryGateway;
        this.priceGateway = priceGateway;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(List<ProductImportData> rows) {

        validateGroup(rows);

        ProductImportData first = rows.getFirst();

        Long categoryId = resolveCategory(first.category());

        Long productId = createOrGetProduct(first, categoryId);

        //cria todos os skus (sem se preocupar com duplicado)
        List<Sku> skus = buildSkus(rows);

        //banco resolve duplicidade
        Map<String, Long> skuCodeToId = skuRepository.saveAll(productId, skus);

        initializeStock(rows, skuCodeToId);
        initializePrices(rows, skuCodeToId);
    }

    private void initializeStock(List<ProductImportData> rows, Map<String, Long> skuCodeToId) {
        List<StockInitialization> stocks = rows.stream()
                .map(r -> new StockInitialization(
                        skuCodeToId.get(r.skuCode()),
                        r.stockQuantity()
                ))
                .toList();

        inventoryGateway.initializeStocks(stocks);
    }

    private void initializePrices(List<ProductImportData> rows, Map<String, Long> skuCodeToId) {
        List<PriceInitialization> prices = rows.stream()
                .map(r -> new PriceInitialization(
                        skuCodeToId.get(r.skuCode()),
                        r.costPrice(),
                        r.sellingPrice()
                ))
                .toList();

        priceGateway.initializePrices(prices);
    }

    private Long createOrGetProduct(ProductImportData data, Long categoryId) {
        String slug = Slug.fromName(data.name()).value();

        return productRepository.findBySlug(slug)
                .map(Product::getId)
                .orElseGet(() -> {
                    Product product = new Product(
                            data.name(),
                            data.description(),
                            categoryId
                    );
                    return productRepository.save(product);
                });
    }

    private List<Sku> buildSkus(List<ProductImportData> rows) {
        return rows.stream()
                .map(row -> new Sku(
                        br.com.erp.api.product.domain.valueobject.SkuCode.of(row.skuCode()),
                        resolveColor(row.color()),
                        resolveSize(row.size()),
                        br.com.erp.api.product.domain.valueobject.Dimensions.of(
                                row.width(),
                                row.height(),
                                row.length(),
                                row.weight()
                        )
                ))
                .toList();
    }

    private Long resolveCategory(String name) {
        return categoryProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new RuntimeException("Categoria inválida: " + name));
    }

    private Long resolveColor(String name) {
        return colorProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new RuntimeException("Cor inválida: " + name));
    }

    private Long resolveSize(String name) {
        return sizeProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new RuntimeException("Tamanho inválido: " + name));
    }

    private void validateGroup(List<ProductImportData> rows) {
        String category = rows.getFirst().category();

        boolean mismatch = rows.stream()
                .anyMatch(r -> !r.category().equalsIgnoreCase(category));

        if (mismatch) {
            throw new RuntimeException("Produto com múltiplas categorias");
        }
    }
}
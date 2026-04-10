package br.com.erp.api.product.application.service;

import br.com.erp.api.product.application.dto.*;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.application.gateway.PriceGateway;
import br.com.erp.api.product.application.provider.CategoryProvider;
import br.com.erp.api.product.application.provider.ColorProvider;
import br.com.erp.api.product.application.provider.SizeProvider;
import br.com.erp.api.product.application.usecase.EvaluateSkuCompletenessUseCase;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.InvalidCategoryException;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.exception.InvalidSizeException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;
import br.com.erp.api.product.domain.valueobject.Slug;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductGroupProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProductGroupProcessor.class);

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final CategoryProvider categoryProvider;
    private final ColorProvider colorProvider;
    private final SizeProvider sizeProvider;
    private final InventoryGateway inventoryGateway;
    private final PriceGateway priceGateway;
    private final EvaluateSkuCompletenessUseCase evaluateSkuCompletenessUseCase;

    public ProductGroupProcessor(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            CategoryProvider categoryProvider,
            ColorProvider colorProvider,
            SizeProvider sizeProvider,
            InventoryGateway inventoryGateway,
            PriceGateway priceGateway,
            EvaluateSkuCompletenessUseCase evaluateSkuCompletenessUseCase
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.categoryProvider = categoryProvider;
        this.colorProvider = colorProvider;
        this.sizeProvider = sizeProvider;
        this.inventoryGateway = inventoryGateway;
        this.priceGateway = priceGateway;
        this.evaluateSkuCompletenessUseCase = evaluateSkuCompletenessUseCase;
    }

    /**
     * Processa um grupo de linhas pertencentes ao mesmo produto.
     * <p>
     * Regras de tolerância a falhas:
     * - Se o PRODUTO falhar → toda a exceção propaga e nenhum SKU é criado
     * - Se um SKU falhar → apenas ele é ignorado, os outros continuam
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GroupProcessingResult process(List<ProductImportData> rows) {

        List<ProductImportError> errors = new ArrayList<>();
        ProductImportData first = rows.getFirst();

        // --- 1. Validar consistência do grupo ---
        validateGroup(rows);

        // --- 2. Resolver categoria (falha = aborta o grupo inteiro) ---
        Long categoryId = resolveCategory(first.category());

        // --- 3. Criar ou reaproveitar o produto ---
        boolean[] productCreated = {false};
        Long productId = createOrGetProduct(first, categoryId, productCreated);

        // --- 4. Construir SKUs individualmente (tolerante a falhas) ---
        List<Sku> validSkus = new ArrayList<>();
        List<ProductImportData> validSkuRows = new ArrayList<>();
        int skusFailed = 0;

        for (ProductImportData row : rows) {
            try {
                Sku sku = buildSku(row);
                validSkus.add(sku);
                validSkuRows.add(row);
            } catch (Exception e) {
                skusFailed++;
                errors.add(ProductImportError.sku(
                        row.rowNumber(), first.name(), first.category(),
                        row.skuCode(), e.getMessage()
                ));
                log.warn("SKU ignorado na importação — linha {}, sku [{}]: {}",
                        row.rowNumber(), row.skuCode(), e.getMessage());
            }
        }

        if (validSkus.isEmpty()) {
            log.warn("Nenhum SKU válido para o produto [{}]", first.name());
            return new GroupProcessingResult(productCreated[0], 0, 0, skusFailed, errors);
        }

        // --- 5. Batch insert com idempotência + diff (novos vs existentes) ---
        SkuBatchResult batchResult = skuRepository.saveAllWithDiff(productId, validSkus);

        int skusCreated = batchResult.totalCreated();
        int skusIgnored = batchResult.totalIgnored();

        // --- 6. Inicializar estoque e preço APENAS para SKUs NOVOS ---
        Map<String, Long> allSkuCodeToId = batchResult.allSkuCodeToId();

        initializeStock(validSkuRows, allSkuCodeToId, batchResult);
        initializePrices(validSkuRows, allSkuCodeToId, batchResult);

        // --- 7. Avaliar completude dos SKUs criados (status INCOMPLETE → READY) ---
        allSkuCodeToId.values().forEach(evaluateSkuCompletenessUseCase::execute);

        log.info("Produto [{}] processado: criado={}, skusCriados={}, skusIgnorados={}, skusFalhos={}",
                first.name(), productCreated[0], skusCreated, skusIgnored, skusFailed);

        return new GroupProcessingResult(
                productCreated[0],
                skusCreated,
                Math.max(skusIgnored, 0),
                skusFailed,
                errors
        );
    }

    private Long createOrGetProduct(ProductImportData data, Long categoryId, boolean[] created) {
        String slug = Slug.fromName(data.name()).value();

        return productRepository.findBySlug(slug)
                .map(existing -> {
                    created[0] = false;
                    return existing.getId();
                })
                .orElseGet(() -> {
                    Product product = new Product(data.name(), data.description(), categoryId);
                    created[0] = true;
                    return productRepository.save(product);
                });
    }

    private Sku buildSku(ProductImportData row) {
        Long colorId = resolveColor(row.color());
        Long sizeId = resolveSize(row.size());

        return new Sku(
                SkuCode.of(row.skuCode()),
                colorId,
                sizeId,
                Dimensions.of(row.width(), row.height(), row.length(), row.weight())
        );
    }

    /**
     * Inicializa estoque APENAS para SKUs novos (evita duplicate key no sku_stock).
     */
    private void initializeStock(List<ProductImportData> rows, Map<String, Long> skuCodeToId,
                                 SkuBatchResult batchResult) {
        List<StockInitialization> stocks = rows.stream()
                .filter(r -> {
                    String code = normalizeSkuCode(r.skuCode());
                    return skuCodeToId.containsKey(code) && batchResult.isNew(code);
                })
                .map(r -> new StockInitialization(
                        skuCodeToId.get(normalizeSkuCode(r.skuCode())),
                        r.stockQuantity()
                ))
                .toList();

        if (!stocks.isEmpty()) {
            inventoryGateway.initializeStocks(stocks);
        }
    }

    /**
     * Inicializa preços APENAS para SKUs novos (evita duplicate key no sku_price).
     */
    private void initializePrices(List<ProductImportData> rows, Map<String, Long> skuCodeToId,
                                  SkuBatchResult batchResult) {
        List<PriceInitialization> prices = rows.stream()
                .filter(r -> {
                    String code = normalizeSkuCode(r.skuCode());
                    return skuCodeToId.containsKey(code) && batchResult.isNew(code);
                })
                .map(r -> new PriceInitialization(
                        skuCodeToId.get(normalizeSkuCode(r.skuCode())),
                        r.costPrice(),
                        r.sellingPrice()
                ))
                .toList();

        if (!prices.isEmpty()) {
            priceGateway.initializePrices(prices);
        }
    }

    /**
     * Normaliza o código SKU para corresponder ao formato armazenado no banco
     * (SkuCode value object faz trim + toUpperCase).
     */
    private String normalizeSkuCode(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    private Long resolveCategory(String name) {
        return categoryProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new InvalidCategoryException("Categoria não encontrada: " + name));
    }

    private Long resolveColor(String name) {
        return colorProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new InvalidColorException("Cor não encontrada: " + name));
    }

    private Long resolveSize(String name) {
        return sizeProvider.findByName(name)
                .map(IdNameProjection::id)
                .orElseThrow(() -> new InvalidSizeException("Tamanho não encontrado: " + name));
    }

    private void validateGroup(List<ProductImportData> rows) {
        String category = rows.getFirst().category();

        boolean mismatch = rows.stream()
                .anyMatch(r -> !r.category().equalsIgnoreCase(category));

        if (mismatch) {
            throw new IllegalStateException("Produto com múltiplas categorias na mesma planilha");
        }
    }
}
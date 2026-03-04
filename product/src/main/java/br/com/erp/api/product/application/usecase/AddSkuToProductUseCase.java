package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.CreateSkuCommand;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.InventoryProvider;
import br.com.erp.api.product.application.gateway.StockInitialization;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.DuplicateSkuCombinationException;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.exception.InvalidSizeException;
import br.com.erp.api.product.domain.exception.SkuConflictDetail;
import br.com.erp.api.product.domain.port.*;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;
import br.com.erp.api.product.domain.valueobject.SkuCombination;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AddSkuToProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final SkuUniquenessChecker skuUniquenessChecker;
    private final ColorLookupPort colorLookupPort;
    private final SizeLookupPort sizeLookupPort;
    private final InventoryProvider inventoryGateway;

    public AddSkuToProductUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            SkuUniquenessChecker skuUniquenessChecker,
            ColorLookupPort colorLookupPort,
            SizeLookupPort sizeLookupPort, InventoryProvider inventoryGateway
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.skuUniquenessChecker = skuUniquenessChecker;
        this.colorLookupPort = colorLookupPort;
        this.sizeLookupPort = sizeLookupPort;
        this.inventoryGateway = inventoryGateway;
    }

    @Transactional
    public void execute(CreateSkuCommand command) {

        //Valida produto
        if (!productRepository.existsById(command.productId())) {
            throw new ProductNotFoundException(command.productId());
        }

        //Extrai combinações do request
        List<SkuCombination> combinations = command.skus().stream()
                .map(s -> new SkuCombination(s.colorId(), s.sizeId()))
                .toList();

        //Valida duplicidade interna no request
        validateInternalDuplicates(combinations);

        //Extrai IDs únicos
        Set<Long> colorIds = combinations.stream()
                .map(SkuCombination::colorId)
                .collect(Collectors.toSet());

        Set<Long> sizeIds = combinations.stream()
                .map(SkuCombination::sizeId)
                .collect(Collectors.toSet());

        //Lookup
        Map<Long, String> colors = colorLookupPort.findByIds(colorIds)
                .stream()
                .collect(Collectors.toMap(
                        IdNameProjection::id,
                        IdNameProjection::name
                ));

        Map<Long, String> sizes = sizeLookupPort.findByIds(sizeIds)
                .stream()
                .collect(Collectors.toMap(
                        IdNameProjection::id,
                        IdNameProjection::name
                ));

        //Valida existência
        validateExistence(combinations, colors, sizes);

        //Valida duplicidade no banco
        validateDatabaseDuplicates(
                command.productId(),
                combinations,
                colors,
                sizes
        );

        //Cria entidades
        List<Sku> skusToSave = command.skus().stream()
                .map(data -> {

                    Dimensions dimensions = Dimensions.of(
                            data.width(),
                            data.height(),
                            data.length(),
                            data.weight()
                    );

                    return new Sku(
                            SkuCode.of(data.code()),
                            data.colorId(),
                            data.sizeId(),
                            dimensions
                    );
                })
                .toList();

        //Persiste skus em bath e retorna seus ids.
        List<Long> skuIds =
                skuRepository.saveAll(command.productId(), skusToSave);

        //utiliza os ids dos skus persistidos para inicializar o estoque zerado desses skus no módulo (inventory)
        List<StockInitialization> stocks = skuIds.stream()
                .map(id -> new StockInitialization(id, 0))
                .toList();
        //chama o método via gateway(iterface) iplementado na infra do módulo de estoque(inventory)
        inventoryGateway.initializeStocks(stocks);
    }


    private void validateInternalDuplicates(List<SkuCombination> combinations) {

        Set<SkuCombination> unique = new HashSet<>();

        for (SkuCombination combination : combinations) {
            if (!unique.add(combination)) {
                throw new DuplicateSkuCombinationException(
                        List.of(new SkuConflictDetail(
                                combination.colorId(),
                                null,
                                combination.sizeId(),
                                null
                        ))
                );
            }
        }
    }

    private void validateExistence(
            List<SkuCombination> combinations,
            Map<Long, String> colors,
            Map<Long, String> sizes
    ) {

        for (SkuCombination combination : combinations) {

            if (!colors.containsKey(combination.colorId())) {
                throw new InvalidColorException(
                        "Cor inválida: id=" + combination.colorId()
                );
            }

            if (!sizes.containsKey(combination.sizeId())) {
                throw new InvalidSizeException(
                        "Tamanho inválido: id=" + combination.sizeId()
                );
            }
        }
    }

    private void validateDatabaseDuplicates(
            Long productId,
            List<SkuCombination> combinations,
            Map<Long, String> colors,
            Map<Long, String> sizes
    ) {

        List<SkuCombination> existing =
                skuUniquenessChecker.existsBatch(productId, combinations);

        if (!existing.isEmpty()) {

            List<SkuConflictDetail> conflicts = existing.stream()
                    .map(c -> new SkuConflictDetail(
                            c.colorId(),
                            colors.get(c.colorId()),
                            c.sizeId(),
                            sizes.get(c.sizeId())
                    ))
                    .toList();

            throw new DuplicateSkuCombinationException(conflicts);
        }
    }
}
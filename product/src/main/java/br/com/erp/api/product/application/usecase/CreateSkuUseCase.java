package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.CreateSkuCommand;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.DuplicateSkuCombinationException;
import br.com.erp.api.product.domain.exception.InvalidColorException;
import br.com.erp.api.product.domain.exception.InvalidSizeException;
import br.com.erp.api.product.domain.port.*;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreateSkuUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final SkuUniquenessChecker skuUniquenessChecker;
    private final ColorLookupPort colorLookupPort;
    private final SizeLookupPort sizeLookupPort;

    public CreateSkuUseCase(ProductRepositoryPort productRepository,
                            SkuRepositoryPort skuRepository,
                            SkuUniquenessChecker skuUniquenessChecker,
                            ColorLookupPort colorLookupPort,
                            SizeLookupPort sizeLookupPort) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.skuUniquenessChecker = skuUniquenessChecker;
        this.colorLookupPort = colorLookupPort;
        this.sizeLookupPort = sizeLookupPort;
    }

    @Transactional
    public void execute(CreateSkuCommand command) {

        //Recupera o produto
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        //Busca cores e tamanhos válidos de uma vez (lookup ports)
        Set<Long> validColorIds = new HashSet<>(colorLookupPort.findAllIds());
        Set<Long> validSizeIds = new HashSet<>(sizeLookupPort.findAllIds());

        // 3️⃣ Valida cores e tamanhos, coleta todos os erros

        for (var skuData : command.skus()) {
            if (!validColorIds.contains(skuData.colorId())) {
                throw new InvalidColorException("Cor inválida: " + skuData.colorId());
            }
            if (!validSizeIds.contains(skuData.sizeId())) {
               throw new InvalidSizeException("Tamanho inválido: " + skuData.sizeId());
            }
        }

        //Valida duplicidade em batch (colorId + sizeId)
        List<Map.Entry<Long, Long>> skuPairs = command.skus().stream()
                .map(s -> Map.entry(s.colorId(), s.sizeId()))
                .toList();

        Set<String> existingKeys = skuUniquenessChecker.existsBatch(product.getId(), skuPairs)
                .stream()
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.toSet());

        Set<String> newKeys = skuPairs.stream()
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.toSet());

        // Verifica se algum SKU do lote já existe
        newKeys.retainAll(existingKeys);
        if (!newKeys.isEmpty()) {
            throw new DuplicateSkuCombinationException(
                    "As seguintes combinações de cor+tamanho já existem: " + newKeys
            );
        }

        //Cria entidades de domínio e adiciona ao aggregate
        List<Sku> skusToSave = new ArrayList<>();
        for (var data : command.skus()) {
            Dimensions dimensions = Dimensions.of(data.width(), data.height(), data.length(), data.weight());
            var sku = new Sku(
                    SkuCode.of(data.code()),
                    data.colorId(),
                    data.sizeId(),
                    dimensions
            );
            product.addSku(sku);
            skusToSave.add(sku);
        }

        //Persiste os SKUs
        skuRepository.saveAll(skusToSave);

    }

}

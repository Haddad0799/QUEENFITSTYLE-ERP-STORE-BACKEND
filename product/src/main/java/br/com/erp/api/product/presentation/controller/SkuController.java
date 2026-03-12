package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.*;
import br.com.erp.api.product.application.query.SkuQueryService;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.application.usecase.AddSkuToProductUseCase;
import br.com.erp.api.product.application.usecase.RegisterSkuStockMovementUseCase;
import br.com.erp.api.product.application.usecase.UpdateSkuDimensionsUseCase;
import br.com.erp.api.product.application.usecase.UpdateSkuPriceUseCase;
import br.com.erp.api.product.presentation.dto.request.CreateSkuDTO;
import br.com.erp.api.product.presentation.dto.request.StockMovementDTO;
import br.com.erp.api.product.presentation.dto.request.UpdateSkuDimensionsDTO;
import br.com.erp.api.product.presentation.dto.request.UpdateSkuPriceDTO;
import br.com.erp.api.product.presentation.dto.response.PageResponse;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.SkuSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/erp/products/{productId}/skus")
public class SkuController {

    private final AddSkuToProductUseCase addSkuToProductUseCase;
    private final SkuQueryService skuQueryService;
    private final UpdateSkuDimensionsUseCase updateSkuDimensionsUseCase;
    private final RegisterSkuStockMovementUseCase registerSkuStockMovementUseCase;
    private final UpdateSkuPriceUseCase updateSkuPriceUseCase;

    public SkuController(AddSkuToProductUseCase addSkuToProductUseCase, SkuQueryService skuQueryService, UpdateSkuDimensionsUseCase updateSkuDimensionsUseCase, RegisterSkuStockMovementUseCase registerSkuStockMovementUseCase, UpdateSkuPriceUseCase updateSkuPriceUseCase) {
        this.addSkuToProductUseCase = addSkuToProductUseCase;
        this.skuQueryService = skuQueryService;
        this.updateSkuDimensionsUseCase = updateSkuDimensionsUseCase;
        this.registerSkuStockMovementUseCase = registerSkuStockMovementUseCase;
        this.updateSkuPriceUseCase = updateSkuPriceUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> createProductSku(
            @PathVariable Long productId,
            @RequestBody CreateSkuDTO dto
    ) {

        CreateSkuCommand command = new CreateSkuCommand(
                productId,
                dto.skus().stream()
                        .map(s -> new SkuData(
                                s.code(),
                                s.colorId(),
                                s.sizeId(),
                                s.width(),
                                s.height(),
                                s.length(),
                                s.weight(),
                                s.stockQuantity(),
                                s.costPrice(),
                                s.sellingPrice()
                        ))
                        .toList()
        );

        addSkuToProductUseCase.execute(command);

        URI location = URI.create("/erp/products/" + productId + "/skus");
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public PageResponse<SkuSummaryDTO> findSkus(
            @PathVariable Long productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId,
            Pageable pageable
    ) {

        SkuFilter filter = new SkuFilter(status, colorId, sizeId);

        return PageResponse.from(skuQueryService.findByProductId(productId, filter, pageable));
    }

    @GetMapping("/{skuId}")
    public ResponseEntity<SkuDetailsDTO> findSkuById(
            @PathVariable Long productId,
            @PathVariable Long skuId
    ) {
        return ResponseEntity.ok(skuQueryService.findByProductIdAndSkuCode(productId, skuId));
    }

    @PatchMapping("/{skuId}/dimensions")
    public ResponseEntity<Void> updateDimensions(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @RequestBody UpdateSkuDimensionsDTO dto
    ) {
        updateSkuDimensionsUseCase.execute(new UpdateSkuDimensionsCommand(
                productId,
                skuId,
                dto.width(),
                dto.height(),
                dto.length(),
                dto.weight()
        ));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{skuId}/stock/movements")
    public ResponseEntity<Void> registerStockMovement(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @RequestBody @Valid StockMovementDTO dto
    ) {
        registerSkuStockMovementUseCase.execute(new RegisterSkuStockMovementCommand(
                productId,
                skuId,
                dto.type(),
                dto.quantity(),
                dto.reason()
        ));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{skuId}/price")
    public ResponseEntity<Void> updatePrice(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @RequestBody @Valid UpdateSkuPriceDTO dto
    ) {
        if (dto.costPrice() == null && dto.sellingPrice() == null) {
            throw new IllegalArgumentException("Informe ao menos um preço para atualizar");
        }

        updateSkuPriceUseCase.execute(new UpdateSkuPriceCommand(
                productId,
                skuId,
                dto.costPrice(),
                dto.sellingPrice()
        ));

        return ResponseEntity.noContent().build();
    }

}

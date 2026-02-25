package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.CreateSkuCommand;
import br.com.erp.api.product.application.command.SkuData;
import br.com.erp.api.product.application.query.SkuQueryService;
import br.com.erp.api.product.application.usecase.AddSkuToProductUseCase;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.presentation.dto.request.CreateSkuDTO;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/erp/products/{productId}/skus")
public class SkuController {

    private final AddSkuToProductUseCase addSkuToProductUseCase;
    private final SkuQueryService skuQueryService;

    public SkuController(AddSkuToProductUseCase addSkuToProductUseCase, SkuQueryService skuQueryService) {
        this.addSkuToProductUseCase = addSkuToProductUseCase;
        this.skuQueryService = skuQueryService;
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
                                s.weight()
                        ))
                        .toList()
        );

        addSkuToProductUseCase.execute(command);

        URI location = URI.create("/erp/products/" + productId + "/skus");
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public Page<SkuDetailsDTO> findSkus(
            @PathVariable Long productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId,
            Pageable pageable
    ) {

        SkuFilter filter = new SkuFilter(status, colorId, sizeId);

        return skuQueryService.findByProductId(productId, filter, pageable);
    }


}

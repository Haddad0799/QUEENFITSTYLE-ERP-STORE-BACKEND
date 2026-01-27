package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.CreateSkuCommand;
import br.com.erp.api.product.application.command.SkuData;
import br.com.erp.api.product.application.usecase.CreateSkuUseCase;
import br.com.erp.api.product.presentation.dto.request.CreateSkuDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/erp/products/{productId}/skus")
public class SkuController {

    private final CreateSkuUseCase createSkuUseCase;

    public SkuController(CreateSkuUseCase createSkuUseCase) {
        this.createSkuUseCase = createSkuUseCase;
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

        createSkuUseCase.execute(command);

        URI location = URI.create("/erp/products/" + productId + "/skus");
        return ResponseEntity.created(location).build();
    }

}

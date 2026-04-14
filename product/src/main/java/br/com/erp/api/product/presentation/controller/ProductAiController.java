package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.usecase.GenerateProductDescriptionUseCase;
import br.com.erp.api.product.presentation.dto.request.GenerateProductDescriptionRequest;
import br.com.erp.api.product.presentation.dto.response.GenerateProductDescriptionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/erp/products/ai")
public class ProductAiController {

    private final GenerateProductDescriptionUseCase generateProductDescriptionUseCase;

    public ProductAiController(GenerateProductDescriptionUseCase generateProductDescriptionUseCase) {
        this.generateProductDescriptionUseCase = generateProductDescriptionUseCase;
    }

    @PostMapping("/generate-description")
    public ResponseEntity<GenerateProductDescriptionResponse> generateDescription(
            @Valid @RequestBody GenerateProductDescriptionRequest request
    ) {
        return ResponseEntity.ok(generateProductDescriptionUseCase.execute(request));
    }
}

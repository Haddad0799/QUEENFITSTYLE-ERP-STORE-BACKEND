package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.application.usecase.AlterProductUseCase;
import br.com.erp.api.product.application.usecase.CreateProductUseCase;
import br.com.erp.api.product.presentation.dto.request.AlterProductDTO;
import br.com.erp.api.product.presentation.dto.request.CreateProductDTO;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/admin/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final AlterProductUseCase alterProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, AlterProductUseCase alterProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.alterProductUseCase = alterProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDTO> createProduct(
            @RequestBody CreateProductDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        var command = new CreateProductCommand(
                dto.name(),
                dto.description(),
                dto.categoryId()
        );

        var output = createProductUseCase.execute(command);

        URI location = uriBuilder
                .path("/admin/products/{id}")
                .buildAndExpand(output.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(new ProductDetailsDTO(
                        output.id(),
                        output.name(),
                        output.slug(),
                        output.categoryId(),
                        output.active()
                ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> alterProduct(@RequestBody AlterProductDTO dto
            , @PathVariable Long id) {

        var command = new AlterProductCommand(id,
                dto.name(),
                dto.description(),
                dto.categoryId());

        var output = alterProductUseCase.execute(command);

    }


}

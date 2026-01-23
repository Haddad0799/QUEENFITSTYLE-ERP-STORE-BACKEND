package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.application.query.ProductAdminQueryService;
import br.com.erp.api.product.application.usecase.ActivateProductUseCase;
import br.com.erp.api.product.application.usecase.AlterProductUseCase;
import br.com.erp.api.product.application.usecase.CreateProductUseCase;
import br.com.erp.api.product.application.usecase.DeactivateProductUseCase;
import br.com.erp.api.product.presentation.dto.request.AlterProductDTO;
import br.com.erp.api.product.presentation.dto.request.CreateProductDTO;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/erp/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final AlterProductUseCase alterProductUseCase;
    private final ProductAdminQueryService productAdminQueryService;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, AlterProductUseCase alterProductUseCase, ProductAdminQueryService productAdminQueryService, ActivateProductUseCase activateProductUseCase, DeactivateProductUseCase deactivateProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.alterProductUseCase = alterProductUseCase;
        this.productAdminQueryService = productAdminQueryService;
        this.activateProductUseCase = activateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDetailsDTO>> getAllProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        Page<ProductDetailsDTO> page =
                productAdminQueryService.getAll(active, categoryId, pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productAdminQueryService.getProductById(id));
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
                .path("/erp/products/{id}")
                .buildAndExpand(output.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(new ProductDetailsDTO(
                        output.id(),
                        output.description(),
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

        return ResponseEntity.ok(new ProductDetailsDTO(
                output.id(),
                output.description(),
                output.name(),
                output.slug(),
                output.categoryId(),
                output.active()));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        activateProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

}

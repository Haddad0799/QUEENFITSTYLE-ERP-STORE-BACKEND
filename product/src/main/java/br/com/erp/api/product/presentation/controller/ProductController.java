package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.application.query.ProductAdminQueryService;
import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.application.usecase.AlterProductUseCase;
import br.com.erp.api.product.application.usecase.CreateProductUseCase;
import br.com.erp.api.product.application.usecase.DeleteProductUseCase;
import br.com.erp.api.product.application.usecase.ImportProductsFromFileUseCase;
import br.com.erp.api.product.application.dto.ImportResult;
import br.com.erp.api.product.application.usecase.PublishProductUseCase;
import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.presentation.dto.request.AlterProductDTO;
import br.com.erp.api.product.presentation.dto.request.CreateProductDTO;
import br.com.erp.api.product.presentation.dto.response.ImportResultResponse;
import br.com.erp.api.product.presentation.dto.response.PageResponse;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.ProductSummaryDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/erp/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final AlterProductUseCase alterProductUseCase;
    private final ProductAdminQueryService productAdminQueryService;
    private final PublishProductUseCase publishProductUseCase;
    private final ImportProductsFromFileUseCase importProductsFromExcelUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                             AlterProductUseCase alterProductUseCase,
                             ProductAdminQueryService productAdminQueryService,
                             PublishProductUseCase publishProductUseCase,
                             ImportProductsFromFileUseCase importProductsFromExcelUseCase,
                             DeleteProductUseCase deleteProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.alterProductUseCase = alterProductUseCase;
        this.productAdminQueryService = productAdminQueryService;
        this.publishProductUseCase = publishProductUseCase;
        this.importProductsFromExcelUseCase = importProductsFromExcelUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @GetMapping
    public PageResponse<ProductSummaryDTO> getAll(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        ProductFilter filter = new ProductFilter(status, categoryId);
        return PageResponse.from(productAdminQueryService.getAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productAdminQueryService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createProduct(
            @RequestBody CreateProductDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        var command = new CreateProductCommand(
                dto.name(),
                dto.description(),
                dto.categoryId()
        );

        Long newProductId = createProductUseCase.execute(command);

        URI location = uriBuilder
                .path("/erp/products/{id}")
                .buildAndExpand(newProductId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> alterProduct(
            @PathVariable Long id,
            @RequestBody AlterProductDTO dto) {

        var command = new AlterProductCommand(id,
                dto.name(),
                dto.description(),
                dto.categoryId());

        alterProductUseCase.execute(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishProduct(@PathVariable Long id) {
        publishProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultResponse> importProducts(
            @RequestParam("file") MultipartFile file
    ) {
        ImportResult result = importProductsFromExcelUseCase.execute(file);
        return ResponseEntity.ok(ImportResultResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

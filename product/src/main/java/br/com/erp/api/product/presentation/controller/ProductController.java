package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.query.ProductAdminQueryService;
import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.application.usecase.AlterProductUseCase;
import br.com.erp.api.product.application.usecase.ConfirmImageUploadUseCase;
import br.com.erp.api.product.application.usecase.CreateProductUseCase;
import br.com.erp.api.product.application.usecase.RequestImageUploadUseCase;
import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.presentation.dto.request.AlterProductDTO;
import br.com.erp.api.product.presentation.dto.request.ConfirmImageUploadDTO;
import br.com.erp.api.product.presentation.dto.request.CreateProductDTO;
import br.com.erp.api.product.presentation.dto.request.RequestUploadUrlsDTO;
import br.com.erp.api.product.presentation.dto.response.PageResponse;
import br.com.erp.api.product.presentation.dto.response.PresignedUrlDTO;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.ProductSummaryDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/erp/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final AlterProductUseCase alterProductUseCase;
    private final ProductAdminQueryService productAdminQueryService;
    private final RequestImageUploadUseCase requestImageUploadUseCase;
    private final ConfirmImageUploadUseCase confirmImageUploadUseCase;


    public ProductController(CreateProductUseCase createProductUseCase, AlterProductUseCase alterProductUseCase, ProductAdminQueryService productAdminQueryService, RequestImageUploadUseCase requestImageUploadUseCase, ConfirmImageUploadUseCase confirmImageUploadUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.alterProductUseCase = alterProductUseCase;
        this.productAdminQueryService = productAdminQueryService;
        this.requestImageUploadUseCase = requestImageUploadUseCase;
        this.confirmImageUploadUseCase = confirmImageUploadUseCase;
    }

    @GetMapping
    public PageResponse<ProductSummaryDTO> getAll(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId,
            Pageable pageable
    ) {

        ProductFilter filter = new ProductFilter(
                status,
                categoryId,
                colorId,
                sizeId
        );

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

        return ResponseEntity
                .created(location).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> alterProduct(@RequestBody AlterProductDTO dto
            , @PathVariable Long id) {

        var command = new AlterProductCommand(id,
                dto.name(),
                dto.description(),
                dto.categoryId());

        alterProductUseCase.execute(command);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{productId}/colors/{colorId}/images/upload-urls")
    public ResponseEntity<List<PresignedUrlDTO>> generateUploadUrls(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestBody RequestUploadUrlsDTO dto
    ) {
        List<PresignedUrlResult> results = requestImageUploadUseCase.execute(productId, colorId, dto.files());

        List<PresignedUrlDTO> response = results.stream()
                .map(r -> new PresignedUrlDTO(r.uploadUrl(), r.imageKey()))
                .toList();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{productId}/colors/{colorId}/images")
    public ResponseEntity<Void> confirmUpload(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestBody ConfirmImageUploadDTO dto
    ) {
        confirmImageUploadUseCase.execute(productId, colorId, dto.images());
        return ResponseEntity.noContent().build();
    }

}

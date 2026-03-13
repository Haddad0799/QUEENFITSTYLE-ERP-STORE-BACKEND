package br.com.erp.api.product.presentation.controller;

import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.usecase.*;
import br.com.erp.api.product.presentation.dto.request.ConfirmImageUploadDTO;
import br.com.erp.api.product.presentation.dto.request.ReorderImagesDTO;
import br.com.erp.api.product.presentation.dto.request.RequestUploadUrlsDTO;
import br.com.erp.api.product.presentation.dto.request.SetPrimaryImageDTO;
import br.com.erp.api.product.presentation.dto.response.ColorImagesDTO;
import br.com.erp.api.product.presentation.dto.response.PresignedUrlDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/erp/products/{productId}")
public class ProductImageController {

    private final RequestImageUploadUseCase requestImageUploadUseCase;
    private final ConfirmImageUploadUseCase confirmImageUploadUseCase;
    private final DeleteProductImagesUseCase deleteProductImagesUseCase;
    private final SetPrimaryImageUseCase setPrimaryImageUseCase;
    private final ListProductImagesUseCase listProductImagesUseCase;
    private final ReorderProductImagesUseCase reorderProductImagesUseCase;

    public ProductImageController(RequestImageUploadUseCase requestImageUploadUseCase,
                                  ConfirmImageUploadUseCase confirmImageUploadUseCase,
                                  DeleteProductImagesUseCase deleteProductImagesUseCase,
                                  SetPrimaryImageUseCase setPrimaryImageUseCase,
                                  ListProductImagesUseCase listProductImagesUseCase,
                                  ReorderProductImagesUseCase reorderProductImagesUseCase) {
        this.requestImageUploadUseCase = requestImageUploadUseCase;
        this.confirmImageUploadUseCase = confirmImageUploadUseCase;
        this.deleteProductImagesUseCase = deleteProductImagesUseCase;
        this.setPrimaryImageUseCase = setPrimaryImageUseCase;
        this.listProductImagesUseCase = listProductImagesUseCase;
        this.reorderProductImagesUseCase = reorderProductImagesUseCase;
    }

    @PostMapping("/colors/{colorId}/images/upload-urls")
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

    @PostMapping("/colors/{colorId}/images")
    public ResponseEntity<Void> confirmUpload(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestBody ConfirmImageUploadDTO dto
    ) {
        confirmImageUploadUseCase.execute(productId, colorId, dto.images());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/colors/{colorId}/images")
    public ResponseEntity<Void> deleteImages(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestParam(name = "imageIds") List<Long> imageIds) {

        deleteProductImagesUseCase.execute(productId, colorId, imageIds);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/primary-image")
    public ResponseEntity<Void> setPrimaryImage(
            @PathVariable Long productId,
            @RequestBody SetPrimaryImageDTO dto) {
        setPrimaryImageUseCase.execute(productId, dto.imageId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/images")
    public ResponseEntity<List<ColorImagesDTO>> listProductImages(@PathVariable Long productId) {
        return ResponseEntity.ok(listProductImagesUseCase.execute(productId));
    }

    @PutMapping("/colors/{colorId}/images/reorder")
    public ResponseEntity<Void> reorderImages(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestBody ReorderImagesDTO dto) {

        reorderProductImagesUseCase.execute(productId, colorId, dto.orderedImageIds());

        return ResponseEntity.noContent().build();
    }
}


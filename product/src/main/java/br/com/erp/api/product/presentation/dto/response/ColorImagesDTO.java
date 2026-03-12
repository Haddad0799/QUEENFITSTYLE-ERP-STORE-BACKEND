package br.com.erp.api.product.presentation.dto.response;

import java.util.List;

public record ColorImagesDTO(
        String colorName,
        List<ImageItemDTO> images
) {}


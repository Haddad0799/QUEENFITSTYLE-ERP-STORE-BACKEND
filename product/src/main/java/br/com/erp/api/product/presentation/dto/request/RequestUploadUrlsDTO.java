package br.com.erp.api.product.presentation.dto.request;

import java.util.List;

public record RequestUploadUrlsDTO(
        List<String> files
) {}
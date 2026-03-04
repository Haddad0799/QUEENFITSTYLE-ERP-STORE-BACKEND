package br.com.erp.api.product.presentation.dto.response;

public record PresignedUrlDTO(
        String uploadUrl,
        String imageKey
) {}
package br.com.erp.api.product.application.dto;

public record PresignedUrlResult(
        String uploadUrl,
        String imageKey
) {}
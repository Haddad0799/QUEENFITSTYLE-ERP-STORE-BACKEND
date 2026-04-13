package br.com.erp.api.product.application.dto;

public record CategorySnapshot(
        Long id,
        String name,
        String normalizedName,
        Long parentId
) {}


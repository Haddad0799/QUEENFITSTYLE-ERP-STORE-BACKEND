package br.com.erp.api.product.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchDeleteSkusDTO(
        @NotEmpty(message = "A lista de SKUs não pode estar vazia")
        List<Long> skuIds
) {}


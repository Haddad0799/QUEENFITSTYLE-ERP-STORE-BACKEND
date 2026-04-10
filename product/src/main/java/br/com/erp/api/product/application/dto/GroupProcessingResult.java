package br.com.erp.api.product.application.dto;

import java.util.List;

/**
 * Resultado do processamento de um grupo de linhas (um produto + seus SKUs).
 * Retornado pelo ProductGroupProcessor para o UseCase acumular no ImportResult.
 */
public record GroupProcessingResult(
        boolean productCreated,
        int skusCreated,
        int skusIgnored,
        int skusFailed,
        List<ProductImportError> errors
) {}


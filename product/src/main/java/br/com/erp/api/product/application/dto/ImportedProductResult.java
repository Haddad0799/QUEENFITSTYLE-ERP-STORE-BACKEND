package br.com.erp.api.product.application.dto;

import java.util.Collections;
import java.util.List;

public record ImportedProductResult(
        String productName,
        String slug,
        String category,
        int totalRows,
        ProductImportStatus productStatus,
        int skusCreated,
        int skusIgnored,
        int skusFailed,
        List<ProductImportError> errors
) {
    public ImportedProductResult {
        errors = errors == null ? List.of() : Collections.unmodifiableList(errors);
    }
}

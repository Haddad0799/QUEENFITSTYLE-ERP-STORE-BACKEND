package br.com.erp.api.product.presentation.dto.response;

import br.com.erp.api.product.application.dto.ImportResult;
import br.com.erp.api.product.application.dto.ProductImportError;

import java.util.List;

public record ImportResultResponse(
        int totalRows,
        int validRows,
        int productsCreated,
        int productsReused,
        int skusCreated,
        int skusIgnored,
        int skusFailed,
        List<ProductImportError> errors
) {

    public static ImportResultResponse from(ImportResult result) {
        return new ImportResultResponse(
                result.totalRows(),
                result.validRows(),
                result.productsCreated(),
                result.productsReused(),
                result.skusCreated(),
                result.skusIgnored(),
                result.skusFailed(),
                result.errors()
        );
    }
}


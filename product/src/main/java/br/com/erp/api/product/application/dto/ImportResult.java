package br.com.erp.api.product.application.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ImportResult(
        int totalRows,
        int validRows,
        int productsCreated,
        int productsReused,
        int skusCreated,
        int skusIgnored,
        int skusFailed,
        List<ProductImportError> errors,
        List<ImportedProductResult> products
) {

    public static class Builder {

        private int totalRows;
        private int validRows;
        private int productsCreated;
        private int productsReused;
        private int skusCreated;
        private int skusIgnored;
        private int skusFailed;

        private final List<ProductImportError> errors = new ArrayList<>();
        private final List<ImportedProductResult> products = new ArrayList<>();

        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }

        public void setValidRows(int validRows) {
            this.validRows = validRows;
        }

        public void incrementProductsCreated() {
            this.productsCreated++;
        }

        public void incrementProductsReused() {
            this.productsReused++;
        }

        public void addSkusCreated(int count) {
            this.skusCreated += count;
        }

        public void addSkusIgnored(int count) {
            this.skusIgnored += count;
        }

        public void addSkusFailed(int count) {
            this.skusFailed += count;
        }

        public void addError(ProductImportError error) {
            this.errors.add(error);
        }

        public void addErrors(List<ProductImportError> errors) {
            this.errors.addAll(errors);
        }

        public void addProductResult(ImportedProductResult productResult) {
            this.products.add(productResult);
        }

        public ImportResult build() {
            return new ImportResult(
                    totalRows,
                    validRows,
                    productsCreated,
                    productsReused,
                    skusCreated,
                    skusIgnored,
                    skusFailed,
                    Collections.unmodifiableList(errors),
                    Collections.unmodifiableList(products)
            );
        }
    }
}

package br.com.erp.api.product.application.query.filter;

public record SkuFilter(
        String status,
        Long colorId,
        Long sizeId
) {
    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }

    public boolean hasColor() {
        return colorId != null;
    }

    public boolean hasSize() {
        return sizeId != null;
    }
}
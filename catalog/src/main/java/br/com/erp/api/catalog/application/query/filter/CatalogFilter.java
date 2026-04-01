package br.com.erp.api.catalog.application.query.filter;

import java.math.BigDecimal;

public record CatalogFilter(
        String category,
        String color,
        String sizeName,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String search
) {
    public boolean hasCategory() { return category != null && !category.isBlank(); }
    public boolean hasColor()    { return color != null && !color.isBlank(); }
    public boolean hasSizeName()     { return sizeName != null && !sizeName.isBlank(); }
    public boolean hasMinPrice() { return minPrice != null; }
    public boolean hasMaxPrice() { return maxPrice != null; }
    public boolean hasSearch()   { return search != null && !search.isBlank(); }
}


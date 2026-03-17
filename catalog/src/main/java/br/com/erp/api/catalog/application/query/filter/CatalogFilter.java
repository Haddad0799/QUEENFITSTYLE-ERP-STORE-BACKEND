package br.com.erp.api.catalog.application.query.filter;

import java.math.BigDecimal;

public record CatalogFilter(
        String category,
        String color,
        String size,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String search
) {
    public boolean hasCategory() { return category != null && !category.isBlank(); }
    public boolean hasColor()    { return color != null && !color.isBlank(); }
    public boolean hasSize()     { return size != null && !size.isBlank(); }
    public boolean hasMinPrice() { return minPrice != null; }
    public boolean hasMaxPrice() { return maxPrice != null; }
    public boolean hasSearch()   { return search != null && !search.isBlank(); }
}


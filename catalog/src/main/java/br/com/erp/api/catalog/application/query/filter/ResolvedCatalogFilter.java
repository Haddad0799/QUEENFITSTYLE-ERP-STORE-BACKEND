package br.com.erp.api.catalog.application.query.filter;

import java.math.BigDecimal;
import java.util.List;

public record ResolvedCatalogFilter(
        String category,
        List<String> categorySlugs,
        String color,
        String label,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String search
) {
    public static ResolvedCatalogFilter from(CatalogFilter filter, List<String> categorySlugs) {
        return new ResolvedCatalogFilter(
                filter.category(),
                List.copyOf(categorySlugs),
                filter.color(),
                filter.label(),
                filter.minPrice(),
                filter.maxPrice(),
                filter.search()
        );
    }

    public boolean hasCategoryScope() { return categorySlugs != null && !categorySlugs.isEmpty(); }
    public boolean hasColor() { return color != null && !color.isBlank(); }
    public boolean hasLabel() { return label != null && !label.isBlank(); }
    public boolean hasMinPrice() { return minPrice != null; }
    public boolean hasMaxPrice() { return maxPrice != null; }
    public boolean hasSearch() { return search != null && !search.isBlank(); }
}

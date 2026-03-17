package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CatalogFilterSqlResolver {

    public CatalogPageQuery build(CatalogFilter filter, Pageable pageable) {

        StringBuilder baseSql = new StringBuilder("""
            FROM catalog_products cp
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        List<String> skuConditions = new ArrayList<>();

        // ── Filtros no produto ──
        if (filter.hasCategory()) {
            baseSql.append(" AND cp.category_name = :category ");
            params.put("category", filter.category());
        }

        if (filter.hasSearch()) {
            baseSql.append(" AND cp.name ILIKE :search ");
            params.put("search", "%" + filter.search() + "%");
        }

        // ── Filtros nos SKUs (EXISTS subquery) ──
        if (filter.hasColor()) {
            skuConditions.add("cs.color_name = :color");
            params.put("color", filter.color());
        }

        if (filter.hasSize()) {
            skuConditions.add("cs.size_name = :size");
            params.put("size", filter.size());
        }

        if (filter.hasMinPrice()) {
            skuConditions.add("cs.selling_price >= :minPrice");
            params.put("minPrice", filter.minPrice());
        }

        if (filter.hasMaxPrice()) {
            skuConditions.add("cs.selling_price <= :maxPrice");
            params.put("maxPrice", filter.maxPrice());
        }

        // Sempre exigir estoque > 0 no catálogo (só mostra SKUs disponíveis)
        skuConditions.add("cs.available_stock > 0");

        // Monta EXISTS
        String skuWhere = String.join(" AND ", skuConditions);
        baseSql.append("""
            AND EXISTS (
                SELECT 1 FROM catalog_skus cs
                WHERE cs.catalog_product_id = cp.id
                AND %s
            )
        """.formatted(skuWhere));

        // ── SELECT ──
        String selectSql = """
            SELECT cp.name, cp.slug, cp.category_name, cp.main_image_url, cp.min_price
        """ + baseSql + resolveOrderBy(pageable) + """
            LIMIT :limit OFFSET :offset
        """;

        String countSql = "SELECT COUNT(*) " + baseSql;

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new CatalogPageQuery(selectSql, countSql, params);
    }

    private String resolveOrderBy(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return " ORDER BY cp.published_at DESC ";
        }

        return pageable.getSort().stream()
                .map(o -> toColumn(o.getProperty()) + " " + o.getDirection().name())
                .collect(Collectors.joining(", ", " ORDER BY ", " "));
    }

    private String toColumn(String property) {
        return switch (property) {
            case "price"  -> "cp.min_price";
            case "name"   -> "cp.name";
            case "newest" -> "cp.published_at";
            default       -> "cp.published_at";
        };
    }
}


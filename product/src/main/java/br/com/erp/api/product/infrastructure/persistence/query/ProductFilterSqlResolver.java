package br.com.erp.api.product.infrastructure.persistence.query;

import br.com.erp.api.product.application.query.filter.ProductFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductFilterSqlResolver {

    public PageQuery build(ProductFilter filter, Pageable pageable) {

        StringBuilder baseSql = new StringBuilder("""
        FROM products p
        JOIN categories c ON c.id = p.category_id
        WHERE 1=1
    """);

        Map<String, Object> filterParams = new HashMap<>();

        if (filter.status() != null) {
            baseSql.append(" AND p.status = :status ");
            filterParams.put("status", filter.status().name());
        }

        if (filter.categoryId() != null) {
            baseSql.append(" AND p.category_id = :categoryId ");
            filterParams.put("categoryId", filter.categoryId());
        }

        if (filter.colorId() != null || filter.sizeId() != null) {
            baseSql.append("""
            AND EXISTS (
                SELECT 1
                FROM skus s
                WHERE s.product_id = p.id
        """);

            if (filter.colorId() != null) {
                baseSql.append(" AND s.color_id = :colorId ");
                filterParams.put("colorId", filter.colorId());
            }

            if (filter.sizeId() != null) {
                baseSql.append(" AND s.size_id = :sizeId ");
                filterParams.put("sizeId", filter.sizeId());
            }

            baseSql.append(" ) ");
        }

        String selectSql = """
        SELECT
            p.id,
            p.name,
            p.slug,
            c.display_name AS category_name,
            p.status
    """ + baseSql + resolveOrderBy(pageable) + """
        LIMIT :limit OFFSET :offset
    """;

        String countSql = "SELECT COUNT(*) " + baseSql;

        Map<String, Object> pageParams = Map.of(
                "limit", pageable.getPageSize(),
                "offset", pageable.getOffset()
        );

        return new PageQuery(selectSql, countSql, filterParams, pageParams);
    }

    private String resolveOrderBy(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return " ORDER BY p.name ASC ";
        }

        return pageable.getSort().stream()
                .map(o -> toColumn(o.getProperty()) + " " + o.getDirection().name())
                .collect(Collectors.joining(", ", " ORDER BY ", " "));
    }

    private String toColumn(String property) {
        return switch (property) {
            case "categoryName" -> "c.display_name";
            case "status"       -> "p.status";
            default             -> "p.name";
        };
    }
}
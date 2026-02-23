package br.com.erp.api.product.infrastructure.persistence.query;

import br.com.erp.api.product.application.query.filter.ProductFilter;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

public class ProductFilterSqlResolver {

    public PageQuery build(ProductFilter filter, Pageable pageable) {

        StringBuilder baseSql = new StringBuilder("""
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();

        // STATUS
        if (filter.status() != null) {
            baseSql.append(" AND p.status = :status ");
            params.put("status", filter.status().name());
        }

        // CATEGORY
        if (filter.categoryId() != null) {
            baseSql.append(" AND p.category_id = :categoryId ");
            params.put("categoryId", filter.categoryId());
        }

        // COLOR / SIZE
        if (filter.colorId() != null || filter.sizeId() != null) {
            baseSql.append("""
                AND EXISTS (
                    SELECT 1
                    FROM skus s
                    WHERE s.product_id = p.id
            """);

            if (filter.colorId() != null) {
                baseSql.append(" AND s.color_id = :colorId ");
                params.put("colorId", filter.colorId());
            }

            if (filter.sizeId() != null) {
                baseSql.append(" AND s.size_id = :sizeId ");
                params.put("sizeId", filter.sizeId());
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
        """ + baseSql + """
            LIMIT :limit OFFSET :offset
        """;

        String countSql = "SELECT COUNT(*) " + baseSql;

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new PageQuery(selectSql, countSql, params);
    }
}

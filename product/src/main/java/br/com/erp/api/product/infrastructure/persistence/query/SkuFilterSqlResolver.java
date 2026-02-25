package br.com.erp.api.product.infrastructure.persistence.query;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SkuFilterSqlResolver {

    public PageQuery build(Long productId, SkuFilter filter, Pageable pageable) {

        StringBuilder baseSql = new StringBuilder("""
            FROM skus s
            LEFT JOIN colors c ON c.id = s.color_id
            LEFT JOIN sizes sz ON sz.id = s.size_id
            WHERE s.product_id = :productId
        """);

        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("productId", productId);

        if (filter.status() != null) {
            baseSql.append(" AND s.status = :status ");
            filterParams.put("status", filter.status());
        }

        if (filter.colorId() != null) {
            baseSql.append(" AND s.color_id = :colorId ");
            filterParams.put("colorId", filter.colorId());
        }

        if (filter.sizeId() != null) {
            baseSql.append(" AND s.size_id = :sizeId ");
            filterParams.put("sizeId", filter.sizeId());
        }

        String selectSql = """
            SELECT
                s.id,
                s.sku_code AS code,
                s.color_id AS colorId,
                c.name AS colorName,
                s.size_id AS sizeId,
                sz.label AS sizeName,
                s.width,
                s.height,
                s.length,
                s.weight,
                s.status
        """ + baseSql + """
            ORDER BY s.id
            LIMIT :limit OFFSET :offset
        """;

        String countSql = "SELECT COUNT(*) " + baseSql;

        Map<String, Object> pageParams = Map.of(
                "limit", pageable.getPageSize(),
                "offset", pageable.getOffset()
        );

        return new PageQuery(selectSql, countSql, filterParams, pageParams);
    }
}
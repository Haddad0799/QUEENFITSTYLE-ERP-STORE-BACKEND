package br.com.erp.api.product.infrastructure.persistence.query;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkuFilterSqlResolver {

    public PageQuery build(Long productId, SkuFilter filter, Pageable pageable) {

        String baseSql = """
            FROM skus s
            LEFT JOIN colors c ON c.id = s.color_id
            LEFT JOIN sizes sz ON sz.id = s.size_id
            WHERE s.product_id = :productId
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);

        List<String> conditions = new ArrayList<>();

        if (filter.hasStatus()) {
            conditions.add("s.status = :status");
            params.put("status", filter.status());
        }

        if (filter.hasColor()) {
            conditions.add("s.color_id = :colorId");
            params.put("colorId", filter.colorId());
        }

        if (filter.hasSize()) {
            conditions.add("s.size_id = :sizeId");
            params.put("sizeId", filter.sizeId());
        }

        String whereClause = conditions.isEmpty()
                ? ""
                : " AND " + String.join(" AND ", conditions);

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
        """ + baseSql + whereClause + """
            ORDER BY s.id
            LIMIT :limit OFFSET :offset
        """;

        String countSql = "SELECT COUNT(*) " + baseSql + whereClause;

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new PageQuery(selectSql, countSql, params, Map.of());
    }

    public PageQuery buildSummary(Long productId,
                                  SkuFilter filter,
                                  Pageable pageable) {

        String baseSql = """
        FROM skus s
        LEFT JOIN colors c ON c.id = s.color_id
        LEFT JOIN sizes sz ON sz.id = s.size_id
        WHERE s.product_id = :productId
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);

        List<String> conditions = new ArrayList<>();

        if (filter.hasStatus()) {
            conditions.add("s.status = :status");
            params.put("status", filter.status());
        }

        if (filter.hasColor()) {
            conditions.add("s.color_id = :colorId");
            params.put("colorId", filter.colorId());
        }

        if (filter.hasSize()) {
            conditions.add("s.size_id = :sizeId");
            params.put("sizeId", filter.sizeId());
        }

        String whereClause = conditions.isEmpty()
                ? ""
                : " AND " + String.join(" AND ", conditions);

        String selectSql = """
        SELECT
            s.id,
            s.sku_code AS code,
            c.name AS color_name,
            sz.label AS size_name,
            s.status
    """ + baseSql + whereClause + """
        ORDER BY s.id
        LIMIT :limit OFFSET :offset
    """;

        String countSql = "SELECT COUNT(*) " + baseSql + whereClause;

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new PageQuery(selectSql, countSql, params, Map.of());
    }
}
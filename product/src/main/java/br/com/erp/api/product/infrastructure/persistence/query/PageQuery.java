package br.com.erp.api.product.infrastructure.persistence.query;

import java.util.Map;

public record PageQuery(
        String selectSql,
        String countSql,
        Map<String, Object> filterParams,
        Map<String, Object> pageParams
) {}
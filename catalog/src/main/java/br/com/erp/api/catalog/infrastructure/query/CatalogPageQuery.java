package br.com.erp.api.catalog.infrastructure.query;

import java.util.Map;

public record CatalogPageQuery(
        String selectSql,
        String countSql,
        Map<String, Object> params
) {}


package br.com.erp.api.catalog.infrastructure.query;

import java.util.List;
import java.util.Map;

public record CatalogPageQuery(
        String selectSql,
        String countSql,
        Map<String, Object> params,
        Map<String, List<?>> listParams
) {}


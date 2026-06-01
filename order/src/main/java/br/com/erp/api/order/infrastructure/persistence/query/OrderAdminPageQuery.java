package br.com.erp.api.order.infrastructure.persistence.query;

import java.util.Map;

public record OrderAdminPageQuery(
        String selectSql,
        String countSql,
        Map<String, Object> filterParams,
        int limit,
        long offset
) {}

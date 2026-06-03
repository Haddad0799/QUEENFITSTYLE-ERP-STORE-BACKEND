package br.com.erp.api.order.infrastructure.persistence.query;

import br.com.erp.api.order.application.query.filter.OrderAdminFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolve dinamicamente os filtros administrativos em SQL parametrizado.
 * Mantém builder + parâmetros desacoplados, evitando concatenação insegura.
 */
@Component
public class OrderAdminFilterSqlResolver {

    public OrderAdminPageQuery build(OrderAdminFilter filter, Pageable pageable) {
        StringBuilder fromClause = new StringBuilder("""
                FROM orders o
                JOIN customers cu ON cu.id = o.customer_id
                """);

        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        boolean joinItems = filter.hasSkuCode();

        if (joinItems) {
            fromClause.append("JOIN order_items oi ON oi.order_id = o.id\n");
            conditions.add("oi.sku_code = :skuCode");
            params.put("skuCode", filter.skuCode());
        }

        if (filter.hasStatus()) {
            conditions.add("o.status = :status");
            params.put("status", filter.status().name());
        }

        if (filter.hasCustomerName()) {
            conditions.add("lower(cu.name) LIKE :customerName");
            params.put("customerName", "%" + filter.customerName().toLowerCase() + "%");
        }

        if (filter.hasPhone()) {
            conditions.add("cu.phone = :phone");
            params.put("phone", filter.phone());
        }

        if (filter.hasCreatedAtFrom()) {
            conditions.add("o.created_at >= :createdAtFrom");
            params.put("createdAtFrom", Timestamp.valueOf(filter.createdAtFrom().atStartOfDay()));
        }

        if (filter.hasCreatedAtTo()) {
            conditions.add("o.created_at <= :createdAtTo");
            params.put("createdAtTo", Timestamp.valueOf(filter.createdAtTo().atTime(LocalTime.MAX)));
        }

        // Quebras de linha explícitas entre fragmentos para impedir que o último parâmetro
        // (ex: ":status") cole no próximo token ("ORDER BY ...") — JDBI captura o nome
        // do parâmetro até o primeiro delimitador, e a colagem ":statusORDER" o tornaria inválido.
        String whereClause = conditions.isEmpty()
                ? ""
                : "WHERE " + String.join(" AND ", conditions) + "\n";

        String distinct = joinItems ? "DISTINCT " : "";

        String selectColumns = """
                o.id              AS order_id,
                o.status          AS status,
                o.total_amount    AS total_amount,
                o.created_at      AS created_at,
                o.confirmed_at    AS confirmed_at,
                o.cancelled_at    AS cancelled_at,
                o.expires_at      AS expires_at,
                cu.name           AS customer_name,
                cu.phone          AS customer_phone,
                o.delivery_cep          AS delivery_cep,
                o.delivery_street       AS delivery_street,
                o.delivery_number       AS delivery_number,
                o.delivery_complement   AS delivery_complement,
                o.delivery_neighborhood AS delivery_neighborhood,
                o.delivery_city         AS delivery_city,
                o.delivery_state        AS delivery_state,
                (SELECT COUNT(*) FROM order_items oi2 WHERE oi2.order_id = o.id) AS items_count
                """;

        String tail = """
                ORDER BY o.created_at DESC, o.id DESC
                LIMIT :limit OFFSET :offset
                """;

        String selectSql = "SELECT " + distinct + selectColumns + fromClause + whereClause + tail;

        String countSql = (joinItems
                ? "SELECT COUNT(DISTINCT o.id)\n"
                : "SELECT COUNT(*)\n") + fromClause + whereClause;

        return new OrderAdminPageQuery(selectSql, countSql, params,
                pageable.getPageSize(), pageable.getOffset());
    }
}

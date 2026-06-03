package br.com.erp.api.order.infrastructure.persistence;

import br.com.erp.api.order.application.query.OrderAdminQueryRepository;
import br.com.erp.api.order.application.query.filter.OrderAdminFilter;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.application.query.projection.OrderReservationRow;
import br.com.erp.api.order.domain.enumerated.OrderEventType;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.infrastructure.persistence.query.OrderAdminFilterSqlResolver;
import br.com.erp.api.order.infrastructure.persistence.query.OrderAdminPageQuery;
import br.com.erp.api.order.presentation.dto.response.OrderCustomerDTO;
import br.com.erp.api.order.presentation.dto.response.OrderDeliveryAddressDTO;
import br.com.erp.api.order.presentation.dto.response.OrderSummaryDTO;
import br.com.erp.api.order.presentation.dto.response.OrderTimelineDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderAdminJdbiQueryRepository implements OrderAdminQueryRepository {

    private final Jdbi jdbi;
    private final OrderAdminFilterSqlResolver resolver;

    public OrderAdminJdbiQueryRepository(Jdbi jdbi, OrderAdminFilterSqlResolver resolver) {
        this.jdbi     = jdbi;
        this.resolver = resolver;
    }

    @Override
    public Page<OrderSummaryDTO> findAll(OrderAdminFilter filter, Pageable pageable) {
        OrderAdminPageQuery query = resolver.build(filter, pageable);

        List<OrderSummaryDTO> items = jdbi.withHandle(handle ->
                handle.createQuery(query.selectSql())
                        .bindMap(query.filterParams())
                        .bind("limit",  query.limit())
                        .bind("offset", query.offset())
                        .map((rs, ctx) -> new OrderSummaryDTO(
                                rs.getLong("order_id"),
                                OrderStatus.valueOf(rs.getString("status")),
                                rs.getString("customer_name"),
                                rs.getString("customer_phone"),
                                rs.getBigDecimal("total_amount"),
                                rs.getInt("items_count"),
                                toDeliveryAddressDTO(rs),
                                toLocalDateTime(rs.getTimestamp("created_at")),
                                toLocalDateTime(rs.getTimestamp("confirmed_at")),
                                toLocalDateTime(rs.getTimestamp("cancelled_at")),
                                toLocalDateTime(rs.getTimestamp("expires_at"))
                        ))
                        .list()
        );

        Long total = jdbi.withHandle(handle ->
                handle.createQuery(query.countSql())
                        .bindMap(query.filterParams())
                        .mapTo(Long.class)
                        .one()
        );

        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public Optional<OrderCustomerDTO> findCustomerByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT cu.id, cu.name, cu.phone, cu.city
                        FROM orders o
                        JOIN customers cu ON cu.id = o.customer_id
                        WHERE o.id = :orderId
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> new OrderCustomerDTO(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("phone"),
                                rs.getString("city")
                        ))
                        .findOne()
        );
    }

    /**
     * Carrega todos os itens do pedido em UMA única query, evitando N+1:
     * - JOIN com skus/products/colors para resolver hex da cor e produto/imagem.
     * - LATERAL para escolher a imagem da cor da SKU (menor "order"), com fallback
     *   para a imagem principal do produto (products.primary_image_id).
     */
    @Override
    public List<OrderItemRow> findItemsByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT oi.id            AS item_id,
                               oi.sku_id        AS sku_id,
                               oi.sku_code      AS sku_code,
                               oi.product_name  AS product_name,
                               oi.color_name    AS color_name,
                               oi.size_label    AS size_label,
                               oi.reservation_id,
                               oi.quantity,
                               oi.unit_price,
                               oi.subtotal,
                               c.hex_code       AS color_hex,
                               COALESCE(color_img.image_key, primary_img.image_key) AS image_key
                        FROM order_items oi
                        LEFT JOIN skus s     ON s.id = oi.sku_id
                        LEFT JOIN products p ON p.id = s.product_id
                        LEFT JOIN colors c   ON c.id = s.color_id
                        LEFT JOIN LATERAL (
                            SELECT pci.image_key
                            FROM product_color_images pci
                            WHERE pci.product_id = s.product_id
                              AND pci.color_id   = s.color_id
                            ORDER BY pci."order" ASC, pci.id ASC
                            LIMIT 1
                        ) AS color_img ON TRUE
                        LEFT JOIN product_color_images primary_img
                               ON primary_img.id = p.primary_image_id
                        WHERE oi.order_id = :orderId
                        ORDER BY oi.id ASC
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> new OrderItemRow(
                                rs.getLong("item_id"),
                                rs.getLong("sku_id"),
                                rs.getString("sku_code"),
                                rs.getString("product_name"),
                                rs.getString("color_name"),
                                rs.getString("color_hex"),
                                rs.getString("size_label"),
                                rs.getString("image_key"),
                                (UUID) rs.getObject("reservation_id"),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("unit_price"),
                                rs.getBigDecimal("subtotal")
                        ))
                        .list()
        );
    }

    /**
     * Resolve expires_at com fallback para o expiresAt do pedido: a tabela
     * stock_reservations pode persistir reservas sem expiração própria — nesse caso,
     * o pedido é a fonte de verdade do ciclo de vida operacional da reserva.
     */
    @Override
    public List<OrderReservationRow> findReservationsByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT sr.reservation_id,
                               sr.sku_id,
                               s.sku_code,
                               sr.quantity,
                               sr.status,
                               COALESCE(sr.expires_at, o.expires_at) AS expires_at
                        FROM order_items oi
                        JOIN orders o              ON o.id              = oi.order_id
                        JOIN stock_reservations sr ON sr.reservation_id = oi.reservation_id
                        JOIN skus s                ON s.id              = sr.sku_id
                        WHERE oi.order_id = :orderId
                        ORDER BY sr.created_at ASC
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> new OrderReservationRow(
                                (UUID) rs.getObject("reservation_id"),
                                rs.getLong("sku_id"),
                                rs.getString("sku_code"),
                                rs.getInt("quantity"),
                                rs.getString("status"),
                                toLocalDateTime(rs.getTimestamp("expires_at"))
                        ))
                        .list()
        );
    }

    @Override
    public List<OrderTimelineDTO> findTimelineByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, event_type, description, payload, actor, created_at
                        FROM order_timeline
                        WHERE order_id = :orderId
                        ORDER BY created_at ASC, id ASC
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> {
                            Object rawPayload = rs.getObject("payload");
                            return new OrderTimelineDTO(
                                    rs.getLong("id"),
                                    OrderEventType.valueOf(rs.getString("event_type")),
                                    rs.getString("description"),
                                    rawPayload != null ? rawPayload.toString() : null,
                                    rs.getString("actor"),
                                    toLocalDateTime(rs.getTimestamp("created_at"))
                            );
                        })
                        .list()
        );
    }

    private OrderDeliveryAddressDTO toDeliveryAddressDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        String cep = rs.getString("delivery_cep");
        if (cep == null) {
            return null;
        }
        return new OrderDeliveryAddressDTO(
                cep,
                rs.getString("delivery_street"),
                rs.getString("delivery_number"),
                rs.getString("delivery_complement"),
                rs.getString("delivery_neighborhood"),
                rs.getString("delivery_city"),
                rs.getString("delivery_state")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}

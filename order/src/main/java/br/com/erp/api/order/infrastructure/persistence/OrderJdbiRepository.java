package br.com.erp.api.order.infrastructure.persistence;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderJdbiRepository implements OrderRepositoryPort {

    private final Jdbi jdbi;

    public OrderJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Order saveWithItems(Order order) {
        // inTransaction garante atomicidade: order + items inseridos ou nada
        return jdbi.inTransaction(handle -> {

            Long orderId = handle.createQuery("""
                    INSERT INTO orders (
                        customer_id, status, total_amount, notes,
                        expires_at, created_at, updated_at
                    ) VALUES (
                        :customerId, :status, :totalAmount, :notes,
                        :expiresAt, now(), now()
                    ) RETURNING id
                    """)
                    .bind("customerId",  order.getCustomerId())
                    .bind("status",      order.getStatus().name())
                    .bind("totalAmount", order.getTotalAmount())
                    .bind("notes",       order.getNotes())
                    .bind("expiresAt",   order.getExpiresAt())
                    .mapTo(Long.class)
                    .one();

            var batch = handle.prepareBatch("""
                    INSERT INTO order_items (
                        order_id, sku_id, reservation_id, product_name, sku_code,
                        color_name, size_label, quantity, unit_price, subtotal, created_at
                    ) VALUES (
                        :orderId, :skuId, :reservationId, :productName, :skuCode,
                        :colorName, :sizeLabel, :quantity, :unitPrice, :subtotal, now()
                    )
                    """);

            for (OrderItem item : order.getItems()) {
                batch.bind("orderId",       orderId)
                     .bind("skuId",         item.getSkuId())
                     .bind("reservationId", item.getReservationId())
                     .bind("productName",   item.getProductName())
                     .bind("skuCode",       item.getSkuCode())
                     .bind("colorName",     item.getColorName())
                     .bind("sizeLabel",     item.getSizeLabel())
                     .bind("quantity",      item.getQuantity())
                     .bind("unitPrice",     item.getUnitPrice())
                     .bind("subtotal",      item.getSubtotal())
                     .add();
            }
            batch.execute();

            return Order.restore(
                    orderId,
                    order.getCustomerId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getNotes(),
                    null,
                    order.getExpiresAt(),
                    null, null,
                    order.getCreatedAt(),
                    order.getUpdatedAt(),
                    order.getItems()
            );
        });
    }

    @Override
    public void updateWhatsappMessage(Long orderId, String message) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE orders
                        SET whatsapp_message = :message, updated_at = now()
                        WHERE id = :id
                        """)
                        .bind("message", message)
                        .bind("id",      orderId)
                        .execute()
        );
    }

    @Override
    public void updateStatus(Long orderId, OrderStatus status) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE orders
                        SET status = :status, updated_at = now()
                        WHERE id = :id
                        """)
                        .bind("status", status.name())
                        .bind("id",     orderId)
                        .execute()
        );
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, customer_id, status, total_amount, notes,
                               whatsapp_message, expires_at, confirmed_at,
                               cancelled_at, created_at, updated_at
                        FROM orders
                        WHERE id = :id
                        """)
                        .bind("id", id)
                        .map((rs, ctx) -> Order.restore(
                                rs.getLong("id"),
                                rs.getLong("customer_id"),
                                OrderStatus.valueOf(rs.getString("status")),
                                rs.getBigDecimal("total_amount"),
                                rs.getString("notes"),
                                rs.getString("whatsapp_message"),
                                toLocalDateTime(rs.getTimestamp("expires_at")),
                                toLocalDateTime(rs.getTimestamp("confirmed_at")),
                                toLocalDateTime(rs.getTimestamp("cancelled_at")),
                                toLocalDateTime(rs.getTimestamp("created_at")),
                                toLocalDateTime(rs.getTimestamp("updated_at")),
                                findItemsByOrderId(id)
                        ))
                        .findOne()
        );
    }

    @Override
    public List<Order> findExpiredPending(LocalDateTime expiredBefore) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, customer_id, status, total_amount, notes,
                               whatsapp_message, expires_at, confirmed_at,
                               cancelled_at, created_at, updated_at
                        FROM orders
                        WHERE status = 'WAITING_SELLER_CONFIRMATION'
                          AND expires_at < :before
                        """)
                        .bind("before", expiredBefore)
                        .map((rs, ctx) -> Order.restore(
                                rs.getLong("id"),
                                rs.getLong("customer_id"),
                                OrderStatus.valueOf(rs.getString("status")),
                                rs.getBigDecimal("total_amount"),
                                rs.getString("notes"),
                                rs.getString("whatsapp_message"),
                                toLocalDateTime(rs.getTimestamp("expires_at")),
                                toLocalDateTime(rs.getTimestamp("confirmed_at")),
                                toLocalDateTime(rs.getTimestamp("cancelled_at")),
                                toLocalDateTime(rs.getTimestamp("created_at")),
                                toLocalDateTime(rs.getTimestamp("updated_at")),
                                List.of()
                        ))
                        .list()
        );
    }

    private List<OrderItem> findItemsByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, order_id, sku_id, reservation_id, product_name, sku_code,
                               color_name, size_label, quantity, unit_price, subtotal, created_at
                        FROM order_items
                        WHERE order_id = :orderId
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> OrderItem.restore(
                                rs.getLong("id"),
                                rs.getLong("order_id"),
                                rs.getLong("sku_id"),
                                (UUID) rs.getObject("reservation_id"),
                                rs.getString("product_name"),
                                rs.getString("sku_code"),
                                rs.getString("color_name"),
                                rs.getString("size_label"),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("unit_price"),
                                rs.getBigDecimal("subtotal"),
                                toLocalDateTime(rs.getTimestamp("created_at"))
                        ))
                        .list()
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}

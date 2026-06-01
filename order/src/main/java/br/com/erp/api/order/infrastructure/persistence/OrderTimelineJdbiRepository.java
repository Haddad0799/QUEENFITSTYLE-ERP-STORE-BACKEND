package br.com.erp.api.order.infrastructure.persistence;

import br.com.erp.api.order.domain.entity.OrderTimelineEvent;
import br.com.erp.api.order.domain.enumerated.OrderEventType;
import br.com.erp.api.order.domain.port.OrderTimelineRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class OrderTimelineJdbiRepository implements OrderTimelineRepositoryPort {

    private final Jdbi jdbi;

    public OrderTimelineJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public OrderTimelineEvent append(OrderTimelineEvent event) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        INSERT INTO order_timeline (
                            order_id, event_type, description, payload, actor, created_at
                        ) VALUES (
                            :orderId, :eventType, :description, CAST(:payload AS JSONB), :actor, now()
                        )
                        RETURNING id, order_id, event_type, description, payload, actor, created_at
                        """)
                        .bind("orderId",     event.getOrderId())
                        .bind("eventType",   event.getEventType().name())
                        .bind("description", event.getDescription())
                        .bind("payload",     event.getPayload())
                        .bind("actor",       event.getActor())
                        .map((rs, ctx) -> mapRow(rs))
                        .one()
        );
    }

    @Override
    public List<OrderTimelineEvent> findByOrderId(Long orderId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, order_id, event_type, description, payload, actor, created_at
                        FROM order_timeline
                        WHERE order_id = :orderId
                        ORDER BY created_at ASC, id ASC
                        """)
                        .bind("orderId", orderId)
                        .map((rs, ctx) -> mapRow(rs))
                        .list()
        );
    }

    private OrderTimelineEvent mapRow(ResultSet rs) throws SQLException {
        Object rawPayload = rs.getObject("payload");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return OrderTimelineEvent.restore(
                rs.getLong("id"),
                rs.getLong("order_id"),
                OrderEventType.valueOf(rs.getString("event_type")),
                rs.getString("description"),
                rawPayload != null ? rawPayload.toString() : null,
                rs.getString("actor"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}

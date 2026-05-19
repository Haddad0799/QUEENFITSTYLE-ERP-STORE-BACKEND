package br.com.erp.api.order.infrastructure.adapter;

import br.com.erp.api.order.application.port.out.ReservationDetail;
import br.com.erp.api.order.application.port.out.ReservationValidationPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ReservationValidationAdapter implements ReservationValidationPort {

    private final Jdbi jdbi;

    public ReservationValidationAdapter(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<ReservationDetail> fetchDetails(List<UUID> reservationIds) {
        if (reservationIds.isEmpty()) return List.of();

        // JOIN único: busca dados de reserva + sku + produto + cor + tamanho em uma query
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT
                            sr.reservation_id,
                            sr.sku_id,
                            s.sku_code,
                            p.name       AS product_name,
                            c.name       AS color_name,
                            sz.label     AS size_label,
                            sr.quantity,
                            sr.status,
                            sr.expires_at
                        FROM stock_reservations sr
                        JOIN skus     s  ON s.id  = sr.sku_id
                        JOIN products p  ON p.id  = s.product_id
                        LEFT JOIN colors c  ON c.id  = s.color_id
                        LEFT JOIN sizes  sz ON sz.id = s.size_id
                        WHERE sr.reservation_id IN (<reservationIds>)
                        """)
                        .bindList("reservationIds", reservationIds)
                        .map((rs, ctx) -> new ReservationDetail(
                                (UUID) rs.getObject("reservation_id"),
                                rs.getLong("sku_id"),
                                rs.getString("sku_code"),
                                rs.getString("product_name"),
                                rs.getString("color_name"),
                                rs.getString("size_label"),
                                rs.getInt("quantity"),
                                rs.getString("status"),
                                toLocalDateTime(rs.getTimestamp("expires_at"))
                        ))
                        .list()
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}

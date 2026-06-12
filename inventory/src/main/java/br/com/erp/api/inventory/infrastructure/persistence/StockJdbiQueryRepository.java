package br.com.erp.api.inventory.infrastructure.persistence;

import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import br.com.erp.api.inventory.presentation.dto.StockMovementDTO;
import br.com.erp.api.inventory.presentation.dto.StockOverviewItemDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StockJdbiQueryRepository implements StockQueryRepository {

    private final Jdbi jdbi;

    public StockJdbiQueryRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<StockOverviewItemDTO> findStockOverview() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT s.sku_id                       AS sku_id,
                               sk.sku_code                    AS sku_code,
                               p.name                         AS product_name,
                               c."name"                       AS color_name,
                               sz.label                       AS size_name,
                               s.quantity                     AS quantity,
                               s.reserved                     AS reserved,
                               (s.quantity - s.reserved)      AS available,
                               s.min_quantity                 AS min_quantity,
                               (s.quantity - s.reserved) <= s.min_quantity AS low_stock
                        FROM sku_stock s
                        JOIN skus sk     ON sk.id = s.sku_id
                        JOIN products p  ON p.id = sk.product_id
                        JOIN colors c    ON c.id = sk.color_id
                        JOIN sizes sz    ON sz.id = sk.size_id
                        ORDER BY p.name, c."name", sz.label
                        """)
                        .map((rs, ctx) -> new StockOverviewItemDTO(
                                rs.getLong("sku_id"),
                                rs.getString("sku_code"),
                                rs.getString("product_name"),
                                rs.getString("color_name"),
                                rs.getString("size_name"),
                                rs.getInt("quantity"),
                                rs.getInt("reserved"),
                                rs.getInt("available"),
                                rs.getInt("min_quantity"),
                                rs.getBoolean("low_stock")
                        ))
                        .list()
        );
    }

    @Override
    public List<StockMovementDTO> findMovementsBySkuId(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, type, quantity, reason, created_at
                        FROM stock_movement
                        WHERE sku_id = :skuId
                        ORDER BY created_at DESC
                        """)
                        .bind("skuId", skuId)
                        .map((rs, ctx) -> new StockMovementDTO(
                                rs.getLong("id"),
                                MovementType.valueOf(rs.getString("type")),
                                rs.getInt("quantity"),
                                rs.getString("reason"),
                                toLocalDateTime(rs.getTimestamp("created_at"))
                        ))
                        .list()
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

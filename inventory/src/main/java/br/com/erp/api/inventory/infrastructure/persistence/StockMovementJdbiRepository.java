package br.com.erp.api.inventory.infrastructure.persistence;

import br.com.erp.api.inventory.domain.entity.StockMovement;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import br.com.erp.api.inventory.domain.port.StockMovementRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockMovementJdbiRepository implements StockMovementRepositoryPort {

    private final Jdbi jdbi;

    public StockMovementJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void save(StockMovement movement) {
        jdbi.useHandle(handle -> {
            if (movement.getReferenceUuid() == null) {
                // insert explicit NULL for reference_uuid to avoid binding issues
                handle.createUpdate("""
                    INSERT INTO stock_movement (sku_id, type, quantity, reason, reference_uuid)
                    VALUES (:skuId, :type, :quantity, :reason, null)
                    """)
                        .bind("skuId",    movement.getSkuId())
                        .bind("type",      movement.getType().name())
                        .bind("quantity",  movement.getQuantity())
                        .bind("reason",    movement.getReason())
                        .execute();
            } else {
                // bind UUID with explicit cast to uuid to ensure correct SQL type
                handle.createUpdate("""
                    INSERT INTO stock_movement (sku_id, type, quantity, reason, reference_uuid)
                    VALUES (:skuId, :type, :quantity, :reason, :referenceUuid::uuid)
                    """)
                        .bind("skuId",    movement.getSkuId())
                        .bind("type",      movement.getType().name())
                        .bind("quantity",  movement.getQuantity())
                        .bind("reason",    movement.getReason())
                        .bindByType("referenceUuid", movement.getReferenceUuid(), java.util.UUID.class)
                        .execute();
            }
        });
    }

    @Override
    public List<StockMovement> findBySkuId(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT id, sku_id, type, quantity, reason, reference_uuid, created_at
                    FROM stock_movement
                    WHERE sku_id = :skuId
                    ORDER BY created_at DESC
                    """)
                        .bind("skuId", skuId)
                        .map((rs, ctx) -> new StockMovement(
                                rs.getLong("sku_id"),
                                MovementType.valueOf(rs.getString("type")),
                                rs.getInt("quantity"),
                                rs.getString("reason"),
                                rs.getObject("reference_uuid", java.util.UUID.class)
                        ))
                        .list()
        );
    }
}
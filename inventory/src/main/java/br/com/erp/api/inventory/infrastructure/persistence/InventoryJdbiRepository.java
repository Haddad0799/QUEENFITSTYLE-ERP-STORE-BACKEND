package br.com.erp.api.inventory.infrastructure.persistence;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.entity.SkuStock;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InventoryJdbiRepository implements InventoryRepositoryPort {

    private final Jdbi jdbi;

    public InventoryJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Optional<SkuStock> findBySkuId(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT id, sku_id, quantity, reserved, min_quantity
                    FROM sku_stock
                    WHERE sku_id = :skuId
                    """)
                        .bind("skuId", skuId)
                        .map((rs, ctx) -> SkuStock.restore(
                                rs.getLong("id"),
                                rs.getLong("sku_id"),
                                rs.getInt("quantity"),
                                rs.getInt("reserved"),
                                rs.getInt("min_quantity")
                        ))
                        .findOne()
        );
    }

    @Override
    public boolean tryReserve(Long skuId, int qty, UUID reservationId) {
        return jdbi.withHandle(handle -> {
            Boolean exists = handle.createQuery("""
                SELECT EXISTS (
                    SELECT 1
                    FROM stock_reservations
                    WHERE reservation_id = :reservationId
                )
                """)
                    .bind("reservationId", reservationId)
                    .mapTo(Boolean.class)
                    .one();

            if (Boolean.TRUE.equals(exists)) {
                return true;
            }

            Integer rows = handle.createQuery("""
                WITH updated AS (
                    UPDATE sku_stock
                    SET reserved = reserved + :qty,
                        updated_at = now()
                    WHERE sku_id = :skuId
                      AND (quantity - reserved - min_quantity) >= :qty
                    RETURNING sku_id
                ),
                reservation AS (
                    INSERT INTO stock_reservations (
                        reservation_id,
                        sku_id,
                        quantity,
                        status,
                        created_at
                    )
                    SELECT
                        :reservationId,
                        sku_id,
                        :qty,
                        'RESERVED',
                        now()
                    FROM updated
                    RETURNING sku_id
                ),
                movement AS (
                    INSERT INTO stock_movement (
                        sku_id,
                        type,
                        quantity,
                        reason,
                        reference_uuid,
                        created_at
                    )
                    SELECT
                        sku_id,
                        'RESERVATION',
                        :qty,
                        CONCAT(:reason, ' reservationId=', :reservationId),
                        :reservationId::uuid,
                        now()
                    FROM reservation
                    RETURNING sku_id
                )
                SELECT COUNT(*) FROM movement
                """)
                    .bind("skuId", skuId)
                    .bind("qty", qty)
                    .bind("reservationId", reservationId)
                    .bind("reason", "reserve")
                    .mapTo(Integer.class)
                    .one();

            return rows != null && rows > 0;
        });
    }

    @Override
    public boolean confirmReservation(UUID reservationId) {
        return jdbi.withHandle(handle -> {
            Integer rows = handle.createQuery("""
                WITH res AS (
                    UPDATE stock_reservations
                    SET status = 'CONFIRMED'
                    WHERE reservation_id = :reservationId
                      AND status = 'RESERVED'
                    RETURNING sku_id, quantity
                ),
                upd AS (
                    UPDATE sku_stock s
                    SET reserved = s.reserved - res.quantity,
                        quantity = s.quantity - res.quantity,
                        updated_at = now()
                    FROM res
                    WHERE s.sku_id = res.sku_id
                    RETURNING s.sku_id, res.quantity
                ),
                movement AS (
                    INSERT INTO stock_movement (
                        sku_id,
                        type,
                        quantity,
                        reason,
                        reference_uuid,
                        created_at
                    )
                    SELECT
                        sku_id,
                        'OUTBOUND',
                        quantity,
                        CONCAT('confirm reservation reservationId=', :reservationId),
                        :reservationId::uuid,
                        now()
                    FROM upd
                    RETURNING sku_id
                )
                SELECT COUNT(*) FROM movement
                """)
                    .bind("reservationId", reservationId)
                    .mapTo(Integer.class)
                    .one();

            return rows != null && rows > 0;
        });
    }

    @Override
    public boolean releaseReservation(UUID reservationId) {
        return jdbi.withHandle(handle -> {
            Integer rows = handle.createQuery("""
                WITH res AS (
                    UPDATE stock_reservations
                    SET status = 'CANCELLED'
                    WHERE reservation_id = :reservationId
                      AND status = 'RESERVED'
                    RETURNING sku_id, quantity
                ),
                upd AS (
                    UPDATE sku_stock s
                    SET reserved = s.reserved - res.quantity,
                        updated_at = now()
                    FROM res
                    WHERE s.sku_id = res.sku_id
                    RETURNING s.sku_id, res.quantity
                ),
                movement AS (
                    INSERT INTO stock_movement (
                        sku_id,
                        type,
                        quantity,
                        reason,
                        reference_uuid,
                        created_at
                    )
                    SELECT
                        sku_id,
                        'RELEASE',
                        quantity,
                        CONCAT('release reservation reservationId=', :reservationId),
                        :reservationId::uuid,
                        now()
                    FROM upd
                    RETURNING sku_id
                )
                SELECT COUNT(*) FROM movement
                """)
                    .bind("reservationId", reservationId)
                    .mapTo(Integer.class)
                    .one();

            return rows != null && rows > 0;
        });
    }

    @Override
    public java.util.Optional<Long> findSkuIdByReservationId(UUID reservationId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT sku_id
                    FROM stock_reservations
                    WHERE reservation_id = :reservationId
                    LIMIT 1
                    """)
                        .bind("reservationId", reservationId)
                        .mapTo(Long.class)
                        .findOne()
        );
    }

    @Override
    public java.util.Optional<Long> findSkuIdByCode(String skuCode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT id
                    FROM skus
                    WHERE sku_code = :skuCode
                    LIMIT 1
                    """)
                        .bind("skuCode", skuCode)
                        .mapTo(Long.class)
                        .findOne()
        );
    }

    @Override
    public void adjustStock(Long skuId, int newQuantity, String reason) {
        jdbi.useHandle(handle -> {
            Optional<Integer> currentQuantity = handle.createQuery("""
                SELECT quantity
                FROM sku_stock
                WHERE sku_id = :skuId
                """)
                    .bind("skuId", skuId)
                    .mapTo(Integer.class)
                    .findOne();

            if (currentQuantity.isEmpty()) {
                handle.createUpdate("""
                    INSERT INTO sku_stock (
                        sku_id,
                        quantity,
                        reserved,
                        min_quantity,
                        updated_at
                    )
                    VALUES (
                        :skuId,
                        :quantity,
                        0,
                        0,
                        now()
                    )
                    """)
                        .bind("skuId", skuId)
                        .bind("quantity", newQuantity)
                        .execute();

                // For initial creation, record INITIALIZED when quantity == 0, otherwise ADJUSTMENT
                String initType = newQuantity == 0 ? "INITIALIZED" : "ADJUSTMENT";
                int initQty = Math.abs(newQuantity);

                handle.createUpdate("""
                    INSERT INTO stock_movement (
                        sku_id,
                        type,
                        quantity,
                        reason,
                        reference_uuid,
                        created_at
                    )
                    VALUES (
                        :skuId,
                        :type,
                        :quantity,
                        :reason,
                        null,
                        now()
                    )
                    """)
                        .bind("skuId", skuId)
                        .bind("type", initType)
                        .bind("quantity", initQty)
                        .bind("reason", reason)
                        .execute();

                return;
            }

            int change = newQuantity - currentQuantity.get();

            handle.createUpdate("""
                UPDATE sku_stock
                SET quantity = :newQuantity,
                    updated_at = now()
                WHERE sku_id = :skuId
                """)
                    .bind("newQuantity", newQuantity)
                    .bind("skuId", skuId)
                    .execute();

            // If there's no change, don't insert a movement
            if (change == 0) return;

            int movementQty = Math.abs(change);

            handle.createUpdate("""
                INSERT INTO stock_movement (
                    sku_id,
                    type,
                    quantity,
                    reason,
                    reference_uuid,
                    created_at
                )
                VALUES (
                    :skuId,
                    'ADJUSTMENT',
                    :quantity,
                    :reason,
                    null,
                    now()
                )
                """)
                    .bind("skuId", skuId)
                    .bind("quantity", movementQty)
                    .bind("reason", reason)
                    .execute();
        });
    }
}
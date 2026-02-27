package br.com.erp.api.inventory.infrastructure.persistence;

import br.com.erp.api.inventory.domain.entity.SkuStock;
import br.com.erp.api.inventory.domain.port.SkuStockRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkuStockJdbiRepository implements SkuStockRepositoryPort {

    private final Jdbi jdbi;

    public SkuStockJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void saveAll(List<SkuStock> stocks) {
        jdbi.useHandle(handle -> {
            var batch = handle.prepareBatch("""
                    INSERT INTO sku_stock (sku_id, quantity, reserved, min_quantity)
                    VALUES (:skuId, :quantity, :reserved, :minQuantity)
                    """);

            for (SkuStock stock : stocks) {
                batch
                        .bind("skuId",       stock.getSkuId())
                        .bind("quantity",    stock.getQuantity())
                        .bind("reserved",    stock.getReserved())
                        .bind("minQuantity", stock.getMinQuantity())
                        .add();
            }

            batch.execute();
        });
    }

    @Override
    public void update(SkuStock stock) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                    UPDATE sku_stock
                    SET quantity     = :quantity,
                        reserved     = :reserved,
                        min_quantity = :minQuantity,
                        updated_at   = NOW()
                    WHERE sku_id = :skuId
                    """)
                        .bind("skuId",       stock.getSkuId())
                        .bind("quantity",    stock.getQuantity())
                        .bind("reserved",    stock.getReserved())
                        .bind("minQuantity", stock.getMinQuantity())
                        .execute()
        );
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
}
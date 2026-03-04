package br.com.erp.api.pricing.infrastructure.repository;

import br.com.erp.api.pricing.domain.entity.SkuPrice;
import br.com.erp.api.pricing.domain.port.SkuPriceRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkuPriceJdbiRepository implements SkuPriceRepositoryPort {

    private final Jdbi jdbi;

    public SkuPriceJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void saveAll(List<SkuPrice> prices) {

        if (prices == null || prices.isEmpty()) return;

        jdbi.withHandle(handle -> {

            var batch = handle.prepareBatch("""
                INSERT INTO sku_price (
                    sku_id,
                    cost_price,
                    selling_price
                ) VALUES (
                    :skuId,
                    :costPrice,
                    :sellingPrice
                )
            """);

            for (SkuPrice price : prices) {
                batch.bind("skuId", price.getSkuId())
                        .bind("costPrice", price.getCostPrice())
                        .bind("sellingPrice", price.getSellingPrice())
                        .add();
            }

            return batch.execute();
        });
    }

    @Override
    public Optional<SkuPrice> findBySkuId(Long skuId) {

        String sql = """
        SELECT
            id,
            sku_id,
            cost_price,
            selling_price,
            created_at
        FROM sku_price
        WHERE sku_id = :skuId
    """;

        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("skuId", skuId)
                        .map((rs, ctx) -> new SkuPrice(
                                rs.getLong("id"),
                                rs.getLong("sku_id"),
                                rs.getBigDecimal("cost_price"),
                                rs.getBigDecimal("selling_price"),
                                rs.getTimestamp("created_at").toLocalDateTime()
                        ))
                        .findOne()
        );
    }

}
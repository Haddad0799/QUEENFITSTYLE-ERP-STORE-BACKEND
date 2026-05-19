package br.com.erp.api.order.infrastructure.adapter;

import br.com.erp.api.order.application.port.out.SkuPricingPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SkuPricingAdapter implements SkuPricingPort {

    private final Jdbi jdbi;

    public SkuPricingAdapter(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Map<Long, BigDecimal> findSellingPrices(List<Long> skuIds) {
        if (skuIds.isEmpty()) return Map.of();

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT sku_id, selling_price
                        FROM sku_price
                        WHERE sku_id IN (<skuIds>)
                        """)
                        .bindList("skuIds", skuIds)
                        .map((rs, ctx) -> Map.entry(
                                rs.getLong("sku_id"),
                                rs.getBigDecimal("selling_price")
                        ))
                        .list()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}

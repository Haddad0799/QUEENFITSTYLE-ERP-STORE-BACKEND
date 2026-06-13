package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.product.application.port.OrderHistoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class OrderHistoryJdbiAdapter implements OrderHistoryPort {

    private final Jdbi jdbi;

    public OrderHistoryJdbiAdapter(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT EXISTS(
                        SELECT 1
                        FROM order_items oi
                        JOIN skus s ON s.id = oi.sku_id
                        WHERE s.product_id = :productId
                    )
                """)
                        .bind("productId", productId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public Set<Long> findSkuIdsWithOrders(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Set.of();
        }

        return jdbi.withHandle(handle ->
                new HashSet<>(handle.createQuery("""
                    SELECT DISTINCT sku_id
                    FROM order_items
                    WHERE sku_id IN (<ids>)
                """)
                        .bindList("ids", skuIds)
                        .mapTo(Long.class)
                        .list())
        );
    }
}

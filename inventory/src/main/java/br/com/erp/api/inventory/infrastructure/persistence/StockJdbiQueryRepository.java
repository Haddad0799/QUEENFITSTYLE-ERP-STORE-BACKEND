package br.com.erp.api.inventory.infrastructure.persistence;

import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.application.query.projection.ProductSkuStockRow;
import br.com.erp.api.inventory.application.query.projection.ProductStockRow;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockJdbiQueryRepository implements StockQueryRepository {

    /**
     * Apenas produtos que possuem ao menos uma SKU com registro de estoque
     * aparecem na visão de estoque.
     */
    private static final String PRODUCTS_WITH_STOCK_WHERE = """
            WHERE EXISTS (
                SELECT 1
                FROM skus sk
                JOIN sku_stock s ON s.sku_id = sk.id
                WHERE sk.product_id = p.id
            )
            """;

    private static final String SEARCH_FILTER = " AND p.name ILIKE '%' || :search || '%'\n";

    private final Jdbi jdbi;

    public StockJdbiQueryRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Page<ProductStockRow> findProductsWithStock(String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        String filter = PRODUCTS_WITH_STOCK_WHERE + (hasSearch ? SEARCH_FILTER : "");

        String selectSql = """
                SELECT p.id                  AS product_id,
                       p.name                AS product_name,
                       primary_img.image_key AS primary_image_key
                FROM products p
                LEFT JOIN product_color_images primary_img ON primary_img.id = p.primary_image_id
                """ + filter + """
                ORDER BY p.name, p.id
                LIMIT :limit OFFSET :offset
                """;

        String countSql = "SELECT COUNT(*) FROM products p\n" + filter;

        List<ProductStockRow> items = jdbi.withHandle(handle -> {
            var query = handle.createQuery(selectSql)
                    .bind("limit", pageable.getPageSize())
                    .bind("offset", pageable.getOffset());
            if (hasSearch) {
                query.bind("search", search.trim());
            }
            return query
                    .map((rs, ctx) -> new ProductStockRow(
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("primary_image_key")
                    ))
                    .list();
        });

        Long total = jdbi.withHandle(handle -> {
            var query = handle.createQuery(countSql);
            if (hasSearch) {
                query.bind("search", search.trim());
            }
            return query.mapTo(Long.class).one();
        });

        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public List<ProductSkuStockRow> findSkuStockByProductIds(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT sk.product_id                  AS product_id,
                               c.id                           AS color_id,
                               c."name"                       AS color_name,
                               c.hex_code                     AS color_hex,
                               s.sku_id                       AS sku_id,
                               sk.sku_code                    AS sku_code,
                               sz.label                       AS size_name,
                               s.quantity                     AS quantity,
                               s.reserved                     AS reserved,
                               (s.quantity - s.reserved)      AS available,
                               s.min_quantity                 AS min_quantity,
                               (s.quantity - s.reserved) <= s.min_quantity AS low_stock
                        FROM sku_stock s
                        JOIN skus sk     ON sk.id = s.sku_id
                        JOIN colors c    ON c.id = sk.color_id
                        JOIN sizes sz    ON sz.id = sk.size_id
                        WHERE sk.product_id IN (<productIds>)
                        ORDER BY c."name", sz.label
                        """)
                        .bindList("productIds", productIds)
                        .map((rs, ctx) -> new ProductSkuStockRow(
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("color_name"),
                                rs.getString("color_hex"),
                                rs.getLong("sku_id"),
                                rs.getString("sku_code"),
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
}

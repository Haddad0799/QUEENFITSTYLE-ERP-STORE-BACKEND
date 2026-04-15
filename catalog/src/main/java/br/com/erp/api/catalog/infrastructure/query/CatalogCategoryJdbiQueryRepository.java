package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.CatalogCategoryNodeView;
import br.com.erp.api.catalog.application.query.CatalogCategoryQueryRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CatalogCategoryJdbiQueryRepository implements CatalogCategoryQueryRepository {

    private static final String NAVIGABLE_CATEGORIES_SQL = """
        WITH sellable_products AS (
            SELECT DISTINCT
                cp.product_id,
                cp.subcategory_id,
                cp.subcategory_name,
                cp.subcategory_normalized_name,
                cp.parent_category_id,
                cp.parent_category_name,
                cp.parent_category_normalized_name
            FROM catalog_products cp
            WHERE EXISTS (
                SELECT 1
                FROM catalog_skus cs
                WHERE cs.catalog_product_id = cp.id
                  AND cs.available_stock > 0
            )
        ),
        leaf_category_counts AS (
            SELECT
                sp.subcategory_id AS id,
                sp.subcategory_name AS name,
                sp.subcategory_normalized_name AS slug,
                sp.parent_category_id AS parent_id,
                COUNT(DISTINCT sp.product_id) AS direct_product_count
            FROM sellable_products sp
            GROUP BY
                sp.subcategory_id,
                sp.subcategory_name,
                sp.subcategory_normalized_name,
                sp.parent_category_id
        ),
        parent_category_nodes AS (
            SELECT DISTINCT
                sp.parent_category_id AS id,
                sp.parent_category_name AS name,
                sp.parent_category_normalized_name AS slug,
                CAST(NULL AS BIGINT) AS parent_id,
                CAST(0 AS BIGINT) AS direct_product_count
            FROM sellable_products sp
            WHERE sp.parent_category_id IS NOT NULL
        )
        SELECT
            node.id,
            node.name,
            node.slug,
            node.parent_id,
            SUM(node.direct_product_count) AS direct_product_count
        FROM (
            SELECT *
            FROM parent_category_nodes
            UNION ALL
            SELECT *
            FROM leaf_category_counts
        ) node
        GROUP BY
            node.id,
            node.name,
            node.slug,
            node.parent_id
        ORDER BY
            node.parent_id NULLS FIRST,
            node.name
        """;

    private final Jdbi jdbi;

    public CatalogCategoryJdbiQueryRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<CatalogCategoryNodeView> findNavigableCategories() {
        return jdbi.withHandle(handle ->
                handle.createQuery(NAVIGABLE_CATEGORIES_SQL)
                        .map((rs, ctx) -> {
                            long parentIdRaw = rs.getLong("parent_id");
                            Long parentId = rs.wasNull() ? null : parentIdRaw;

                            return new CatalogCategoryNodeView(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("slug"),
                                    parentId,
                                    rs.getLong("direct_product_count")
                            );
                        })
                        .list()
        );
    }
}

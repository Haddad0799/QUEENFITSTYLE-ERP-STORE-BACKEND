package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CatalogFilterSqlResolver {

    public CatalogPageQuery build(ResolvedCatalogFilter filter, Pageable pageable) {

        Map<String, Object> params = new HashMap<>();
        Map<String, List<?>> listParams = new HashMap<>();

        StringBuilder productFilters = new StringBuilder();

        if (filter.hasCategoryScope()) {
            productFilters.append("""
                    AND cp2.subcategory_normalized_name IN (<categorySlugs>)
                    """);
            listParams.put("categorySlugs", filter.categorySlugs());
        }

        if (filter.hasSearch()) {
            productFilters.append(" AND cp2.name ILIKE :search ");
            params.put("search", "%" + filter.search() + "%");
        }

        List<String> skuMatchConditions = new ArrayList<>();

        if (filter.hasColor()) {
            skuMatchConditions.add("ccg.color_name = :color");
            params.put("color", filter.color());
        }

        if (filter.hasLabel()) {
            skuMatchConditions.add("cs.size_name = :label");
            params.put("label", filter.label());
        }

        if (filter.hasMinPrice()) {
            skuMatchConditions.add("cs.selling_price >= :minPrice");
            params.put("minPrice", filter.minPrice());
        }

        if (filter.hasMaxPrice()) {
            skuMatchConditions.add("cs.selling_price <= :maxPrice");
            params.put("maxPrice", filter.maxPrice());
        }

        skuMatchConditions.add("cs.available_stock > 0");

        String skuMatchWhere = String.join(" AND ", skuMatchConditions);
        SelectionQuerySpec selectionQuerySpec = filter.hasSelectionFilters()
                ? buildEffectiveSelectionSpec(skuMatchWhere)
                : buildShowcaseSelectionSpec();

        String selectSql = """
            SELECT cp.name, cp.slug, cp.is_launch,
                   cp.parent_category_id, cp.parent_category_name, cp.parent_category_normalized_name,
                   cp.subcategory_id, cp.subcategory_name, cp.subcategory_normalized_name,
                   cp.category_name, cp.category_normalized_name,
                   %s
            FROM (
            SELECT cp2.id
            FROM catalog_products cp2
            WHERE 1=1
            %s
            AND EXISTS (
                SELECT 1 FROM catalog_skus cs
                JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
                WHERE cs.catalog_product_id = cp2.id
                AND %s
        )
            ORDER BY cp2.published_at DESC
            LIMIT :limit OFFSET :offset
            ) page
            JOIN catalog_products cp ON cp.id = page.id
            %s
            ORDER BY cp.published_at DESC
        """.formatted(selectionQuerySpec.selectClause(), productFilters, skuMatchWhere, selectionQuerySpec.joinClause());

        String countSql = """
            SELECT COUNT(*)
            FROM catalog_products cp2
            WHERE 1=1
            %s
            AND EXISTS (
                SELECT 1 FROM catalog_skus cs
                JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
                WHERE cs.catalog_product_id = cp2.id
                AND %s
            )
        """.formatted(productFilters, skuMatchWhere);

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new CatalogPageQuery(selectSql, countSql, params, listParams);
    }

    private SelectionQuerySpec buildShowcaseSelectionSpec() {
        return new SelectionQuerySpec(
                """
                cp.default_sku_code AS selection_sku_code,
                cp.default_selection_label AS selection_label,
                cp.display_price,
                cp.main_image_url AS display_image_url
                """,
                ""
        );
    }

    private SelectionQuerySpec buildEffectiveSelectionSpec(String skuMatchWhere) {
        return new SelectionQuerySpec(
                """
                effective_selection.sku_code AS selection_sku_code,
                effective_selection.selection_label AS selection_label,
                effective_selection.selling_price AS display_price,
                COALESCE(effective_selection_image.image_url, cp.main_image_url) AS display_image_url
                """,
                """
                JOIN (
                    SELECT ranked_selection.catalog_product_id,
                           ranked_selection.catalog_color_group_id,
                           ranked_selection.sku_code,
                           ranked_selection.selection_label,
                           ranked_selection.selling_price
                    FROM (
                        SELECT cs.catalog_product_id,
                               cs.catalog_color_group_id,
                               cs.code AS sku_code,
                               cs.size_name AS selection_label,
                               cs.selling_price,
                               ROW_NUMBER() OVER (
                                   PARTITION BY cs.catalog_product_id
                                   ORDER BY cs.selling_price, cs.size_name, cs.code
                               ) AS row_num
                        FROM catalog_skus cs
                        JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
                        WHERE %s
                    ) ranked_selection
                    WHERE ranked_selection.row_num = 1
                ) effective_selection ON effective_selection.catalog_product_id = cp.id
                LEFT JOIN (
                    SELECT ranked_image.catalog_color_group_id,
                           ranked_image.image_url
                    FROM (
                        SELECT cci.catalog_color_group_id,
                               cci.image_url,
                               ROW_NUMBER() OVER (
                                   PARTITION BY cci.catalog_color_group_id
                                   ORDER BY cci."order", cci.id
                               ) AS row_num
                        FROM catalog_color_images cci
                    ) ranked_image
                    WHERE ranked_image.row_num = 1
                ) effective_selection_image
                    ON effective_selection_image.catalog_color_group_id = effective_selection.catalog_color_group_id
                """.formatted(skuMatchWhere)
        );
    }

    private record SelectionQuerySpec(
            String selectClause,
            String joinClause
    ) {}
}

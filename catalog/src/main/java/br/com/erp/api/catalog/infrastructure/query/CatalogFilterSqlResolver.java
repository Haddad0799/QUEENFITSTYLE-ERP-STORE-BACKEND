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

        List<String> skuConditions = new ArrayList<>();

        if (filter.hasColor()) {
            skuConditions.add("ccg.color_name = :color");
            params.put("color", filter.color());
        }

        if (filter.hasLabel()) {
            skuConditions.add("cs.size_name = :label");
            params.put("label", filter.label());
        }

        if (filter.hasMinPrice()) {
            skuConditions.add("cs.selling_price >= :minPrice");
            params.put("minPrice", filter.minPrice());
        }

        if (filter.hasMaxPrice()) {
            skuConditions.add("cs.selling_price <= :maxPrice");
            params.put("maxPrice", filter.maxPrice());
        }

        skuConditions.add("cs.available_stock > 0");

        String skuWhere = String.join(" AND ", skuConditions);
        String displayImageSelect = "cp.main_image_url AS display_image_url";
        String displayImageJoin = "";

        if (filter.hasColor()) {
            displayImageSelect = "COALESCE(filtered_color_image.image_url, cp.main_image_url) AS display_image_url";
            displayImageJoin = """
                LEFT JOIN LATERAL (
                    SELECT cci.image_url
                    FROM catalog_color_groups ccg
                    LEFT JOIN catalog_color_images cci ON cci.catalog_color_group_id = ccg.id
                    WHERE ccg.catalog_product_id = cp.id
                      AND ccg.color_name = :color
                    ORDER BY cci."order" NULLS LAST
                    LIMIT 1
                ) filtered_color_image ON TRUE
                """;
        }

        String selectSql = """
            SELECT cp.name, cp.slug, cp.is_launch,
                   cp.parent_category_id, cp.parent_category_name, cp.parent_category_normalized_name,
                   cp.subcategory_id, cp.subcategory_name, cp.subcategory_normalized_name,
                   cp.category_name, cp.category_normalized_name,
                   cp.main_image_url, %s, cp.min_price
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
        """.formatted(displayImageSelect, productFilters, skuWhere, displayImageJoin);

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
        """.formatted(productFilters, skuWhere);

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new CatalogPageQuery(selectSql, countSql, params, listParams);
    }
}

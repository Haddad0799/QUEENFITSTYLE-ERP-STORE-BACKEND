package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CatalogFilterSqlResolver {

    public CatalogPageQuery build(CatalogFilter filter, Pageable pageable) {

        Map<String, Object> params = new HashMap<>();

        StringBuilder productFilters = new StringBuilder();

        if (filter.hasCategory()) {
            productFilters.append("""
                     AND (
                        cp2.parent_category_normalized_name = :category
                        OR cp2.subcategory_normalized_name = :category
                     )
                    """);
            params.put("category", filter.category());
        }

        if (filter.hasSubcategory()) {
            productFilters.append(" AND cp2.subcategory_normalized_name = :subcategory ");
            params.put("subcategory", filter.subcategory());
        }

        if (filter.hasSearch()) {
            productFilters.append(" AND cp2.name ILIKE :search ");
            params.put("search", "%" + filter.search() + "%");
        }

        List<String> skuConditions = new ArrayList<>();

        if (filter.hasColor()) {
            skuConditions.add("cs.color_name = :color");
            params.put("color", filter.color());
        }

        if (filter.hasSizeName()) {
            skuConditions.add("cs.size_name = :skuSize");
            params.put("skuSize", filter.sizeName());
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

        String selectSql = """
            SELECT cp.name, cp.slug, cp.is_launch,
                   cp.parent_category_id, cp.parent_category_name, cp.parent_category_normalized_name,
                   cp.subcategory_id, cp.subcategory_name, cp.subcategory_normalized_name,
                   cp.category_name, cp.category_normalized_name,
                   cp.main_image_url, cp.min_price
            FROM (
            SELECT cp2.id
            FROM catalog_products cp2
            WHERE 1=1
            %s
            AND EXISTS (
                SELECT 1 FROM catalog_skus cs
                WHERE cs.catalog_product_id = cp2.id
                AND %s
        )
            ORDER BY cp2.published_at DESC
            LIMIT :limit OFFSET :offset
            ) page
            JOIN catalog_products cp ON cp.id = page.id
            ORDER BY cp.published_at DESC
        """.formatted(productFilters, skuWhere);

        String countSql = """
            SELECT COUNT(*)
            FROM catalog_products cp2
            WHERE 1=1
            %s
            AND EXISTS (
                SELECT 1 FROM catalog_skus cs
                WHERE cs.catalog_product_id = cp2.id
                AND %s
            )
        """.formatted(productFilters, skuWhere);

        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        return new CatalogPageQuery(selectSql, countSql, params);
    }
}

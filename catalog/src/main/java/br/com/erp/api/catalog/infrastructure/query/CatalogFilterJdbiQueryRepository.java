package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.CatalogFilterQueryRepository;
import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogAvailableFiltersDTO;
import br.com.erp.api.catalog.presentation.dto.CatalogColorDTO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CatalogFilterJdbiQueryRepository implements CatalogFilterQueryRepository {

    private final Jdbi jdbi;

    public CatalogFilterJdbiQueryRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public CatalogAvailableFiltersDTO findAvailableFilters(ResolvedCatalogFilter filter) {
        List<CatalogColorDTO> colors = findAvailableColors(filter);
        List<String> sizes = findDistinctValues("cs.size_name", "cs.size_name", filter, true, false);
        return new CatalogAvailableFiltersDTO(colors, sizes);
    }

    private List<CatalogColorDTO> findAvailableColors(ResolvedCatalogFilter filter) {
        QuerySpec querySpec = buildQuerySpec(filter, false, true);
        String sql = """
            SELECT DISTINCT ccg.color_name AS name, ccg.color_hex AS hex
            FROM catalog_products cp
            JOIN catalog_skus cs ON cs.catalog_product_id = cp.id
            JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
            WHERE cs.available_stock > 0
            %s
              AND ccg.color_name IS NOT NULL
              AND ccg.color_name <> ''
              AND ccg.color_hex IS NOT NULL
              AND ccg.color_hex <> ''
            ORDER BY ccg.color_name
            """.formatted(querySpec.whereClause());

        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(sql);
            bindQuery(query, querySpec);
            return query.map((rs, ctx) -> new CatalogColorDTO(
                    rs.getString("name"),
                    rs.getString("hex")
            )).list();
        });
    }

    private List<String> findDistinctValues(
            String selectColumn,
            String orderColumn,
            ResolvedCatalogFilter filter,
            boolean includeColorFilter,
            boolean includeSizeFilter
    ) {
        QuerySpec querySpec = buildQuerySpec(filter, includeColorFilter, includeSizeFilter);
        String sql = """
            SELECT DISTINCT %s AS value
            FROM catalog_products cp
            JOIN catalog_skus cs ON cs.catalog_product_id = cp.id
            JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
            WHERE cs.available_stock > 0
            %s
              AND %s IS NOT NULL
              AND %s <> ''
            ORDER BY %s
            """.formatted(selectColumn, querySpec.whereClause(), selectColumn, selectColumn, orderColumn);

        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(sql);
            bindQuery(query, querySpec);
            return query.mapTo(String.class).list();
        });
    }

    private QuerySpec buildQuerySpec(
            ResolvedCatalogFilter filter,
            boolean includeColorFilter,
            boolean includeSizeFilter
    ) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        Map<String, List<?>> listParams = new HashMap<>();

        if (filter.hasCategoryScope()) {
            where.append(" AND cp.subcategory_normalized_name IN (<categorySlugs>)");
            listParams.put("categorySlugs", filter.categorySlugs());
        }

        if (filter.hasSearch()) {
            where.append(" AND cp.name ILIKE :search");
            params.put("search", "%" + filter.search() + "%");
        }

        if (includeColorFilter && filter.hasColor()) {
            where.append(" AND ccg.color_name = :color");
            params.put("color", filter.color());
        }

        if (includeSizeFilter && filter.hasLabel()) {
            where.append(" AND cs.size_name = :label");
            params.put("label", filter.label());
        }

        if (filter.hasMinPrice()) {
            where.append(" AND cs.selling_price >= :minPrice");
            params.put("minPrice", filter.minPrice());
        }

        if (filter.hasMaxPrice()) {
            where.append(" AND cs.selling_price <= :maxPrice");
            params.put("maxPrice", filter.maxPrice());
        }

        return new QuerySpec(where.toString(), params, listParams);
    }

    private void bindQuery(Query query, QuerySpec querySpec) {
        querySpec.params().forEach(query::bind);
        querySpec.listParams().forEach(query::bindList);
    }

    private record QuerySpec(
            String whereClause,
            Map<String, Object> params,
            Map<String, List<?>> listParams
    ) {}
}
